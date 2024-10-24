package com.example.capstone4;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageAnnotatorSettings;
import com.google.protobuf.ByteString;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GalleryActivity extends AppCompatActivity {

    private static final int PICK_IMAGES = 100;
    private LinearLayout imageContainer;
    private TextView tvResult, tvSubjects;
    private List<Uri> imageUris = new ArrayList<>();
    private Map<String, Double> totalCredits = new HashMap<>();
    private List<String> allSubjectNames = new ArrayList<>(); // 모든 이미지의 과목명을 누적할 리스트
    private static final String TAG = "GalleryActivity";

    // unmatchedCourses를 전역 변수로 선언하여 여러 이미지 처리 시에도 상태 유지
    private List<String> unmatchedCourses = new ArrayList<>();

    // 분야별 과목 관리
    private Map<String, List<String>> fieldCourses = new HashMap<>();

    private void initializeFieldCourses() {
        fieldCourses.put("사고와 표현", new ArrayList<>(Arrays.asList("문화콘텐츠스토리텔링", "인권과현대사회", "종교와 문화", "인간과 환경")));
        fieldCourses.put("분석과 판단", new ArrayList<>(Arrays.asList("합리적문제해결과논리", "과학기술사", "인간과윤리", "심리분석", "통계로 보는 세상")));
        fieldCourses.put("도전과 미래", new ArrayList<>(Arrays.asList("한국사의 이해", "세계시민과 국제화", "인간삶과 교육", "경제와사회", "역사와 문화")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        Button btnSelectImage = findViewById(R.id.btnSelectImage);
        imageContainer = findViewById(R.id.imageContainer);
        tvResult = findViewById(R.id.tvResult);
        tvSubjects = findViewById(R.id.tvSubjects); // 과목명 출력 TextView

        btnSelectImage.setOnClickListener(v -> openGallery());

        initializeCreditMap();
        initializeSpecialCourses(); // 특필 목록을 초기화
        initializeFieldCourses(); // 분야별 과목 초기화
    }

    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        gallery.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(gallery, PICK_IMAGES);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGES) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    imageUris.add(imageUri);
                    addImageToScrollView(imageUri);
                }
            } else if (data.getData() != null) {
                Uri imageUri = data.getData();
                imageUris.add(imageUri);
                addImageToScrollView(imageUri);
            }

            try {
                processAllImages();
            } catch (IOException e) {
                Log.e(TAG, "Error processing images: ", e);
            }
        }
    }

    private void addImageToScrollView(Uri imageUri) {
        ImageView imageView = new ImageView(this);
        imageView.setImageURI(imageUri);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                400 // 이미지 뷰의 높이를 설정
        );
        layoutParams.setMargins(0, 16, 0, 16);
        imageView.setLayoutParams(layoutParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageContainer.addView(imageView);
    }

    private void processAllImages() throws IOException {
        if (totalCredits.isEmpty()) {
            initializeCreditMap(); // 학점 누적 맵도 초기화 (한 번만 실행)
        }

        for (Uri imageUri : imageUris) {
            performOCR(imageUri);
        }

        // 학점 정보와 분야 매칭 정보 모두 출력
        displayTotalCredits(); // 학점 정보 출력
        displayMatches(allSubjectNames);
    }

    private void performOCR(Uri imageUri) throws IOException {
        Log.d(TAG, "Starting OCR process");
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] imageBytes = stream.toByteArray();

        InputStream credentialsStream = getAssets().open("apitest_424020_df691b31fe0e.json");
        GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);
        ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder().setCredentialsProvider(() -> credentials).build();

        try (ImageAnnotatorClient vision = ImageAnnotatorClient.create(settings)) {
            ByteString imgBytes = ByteString.copyFrom(imageBytes);
            Image img = Image.newBuilder().setContent(imgBytes).build();
            Feature feat = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
            AnnotateImageRequest request = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
            List<AnnotateImageRequest> requests = new ArrayList<>();
            requests.add(request);

            Log.d(TAG, "Sending request to Vision API");
            BatchAnnotateImagesResponse response = vision.batchAnnotateImages(requests);
            List<AnnotateImageResponse> imageResponses = response.getResponsesList();
            if (!imageResponses.isEmpty()) {
                List<EntityAnnotation> textAnnotations = imageResponses.get(0).getTextAnnotationsList();
                if (textAnnotations != null && !textAnnotations.isEmpty()) {
                    String resultText = textAnnotations.get(0).getDescription();
                    Log.d(TAG, "Text found: " + resultText);
                    processText(resultText);
                } else {
                    Log.d(TAG, "No text found");
                }
            } else {
                Log.d(TAG, "Image response is empty");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during OCR process: ", e);
        }
    }

    private void processText(String text) {
        String[] lines = text.split("\n");
        Map<String, Double> creditMap = new HashMap<>();
        initializeCreditMap(creditMap);

        List<String> ocrSubjects = new ArrayList<>(); // OCR로 인식된 과목명 리스트

        String[] 이수구분_패턴 = {"교필", "교선", "일선", "전선", "심선", "특선", "특필"};

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            line = line.replace("고필", "교필").replace("밀선", "일선")
                    .replace("고선", "교선").replace("삼선", "심선").replace("트릴", "특필");

            if (line.matches("([A-Z]{4}[0-9]{2,4}|[0-9]{8}).*")) {
                String subjectName = line.replaceAll("([A-Z]{4}[0-9]{2,4}|[0-9]{8})", "").trim();

                // 괄호 안의 과목명 추출 (닫히지 않은 괄호를 처리)
                if (subjectName.contains("(")) {
                    if (!subjectName.contains(")")) {
                        // 괄호가 닫히지 않은 경우 닫힘 괄호 추가
                        subjectName = subjectName + ")";
                    }
                    subjectName = subjectName.substring(subjectName.indexOf("(") + 1, subjectName.indexOf(")")).trim();
                }

                if (subjectName.isEmpty() && i + 1 < lines.length) {
                    subjectName = lines[i + 1].trim();
                }

                // 불필요한 기호 및 공백 제거 (후처리)
                subjectName = cleanSubjectName(subjectName);

                Log.d(TAG, "Extracted subject name: " + subjectName);
                ocrSubjects.add(subjectName.toLowerCase().trim());
            }

            for (String pattern : 이수구분_패턴) {
                if (line.contains(pattern)) {
                    if (i + 1 < lines.length) {
                        String nextLine = lines[i + 1].trim();
                        String[] numbers = nextLine.split("\\s+");
                        for (String num : numbers) {
                            num = num.replace(",", ".");
                            if (num.matches("[0-9.]+")) {
                                try {
                                    double credits = Double.parseDouble(num);
                                    creditMap.put(pattern, creditMap.get(pattern) + credits);
                                    break;
                                } catch (NumberFormatException e) {
                                    Log.e(TAG, "Error parsing credits: " + num, e);
                                }
                            }
                        }
                    }
                }
            }
        }

        for (Map.Entry<String, Double> entry : creditMap.entrySet()) {
            totalCredits.put(entry.getKey(), totalCredits.get(entry.getKey()) + entry.getValue());
        }

        // OCR로 인식된 모든 과목명을 누적 리스트에 추가
        allSubjectNames.addAll(ocrSubjects);
    }

    // 과목명 후처리: 불필요한 기호 제거, 숫자가 빠진 과목명 보정
    private String cleanSubjectName(String subjectName) {
        // 불필요한 기호 제거 (예: |, -, _, 등)
        subjectName = subjectName.replaceAll("[|_\\-]", "").trim();

        // 띄어쓰기 및 대소문자 무시
        //subjectName = subjectName.replaceAll("\\s+", "").toLowerCase();

        // 특정 오타 수정
        subjectName = normalizeSubjectName(subjectName);  // 오타 교정 함수 호출

        // '캡스톤디자인'을 '캡스톤디자인1'으로 자동 보정
        if (subjectName.equalsIgnoreCase("캡스톤디자인")) {
            subjectName = "캡스톤디자인1";
        }

        // "글쓰기" 관련 과목 매칭 (OCR 인식에 따라 자동 보정)
        if (subjectName.equalsIgnoreCase("공학글쓰기") ||
                subjectName.equalsIgnoreCase("실용글쓰기") ||
                subjectName.equalsIgnoreCase("창의글쓰기")) {
            subjectName = "글쓰기"; // 세 과목을 "글쓰기"로 통합
        }

        // 기타 교필 과목 매칭
        if (subjectName.equalsIgnoreCase("발표와 토론")) {
            subjectName = "발표와 토론";
        } else if (subjectName.equalsIgnoreCase("대학영어")) {
            subjectName = "대학영어";
        } else if (subjectName.equalsIgnoreCase("C 프로그래밍")) {
            subjectName = "C 프로그래밍";
        }

        return subjectName;
    }

    // 오타 교정 함수
    private String normalizeSubjectName(String subjectName) {
        // 기본 오타 교정 맵
        Map<String, String> corrections = new HashMap<>();
        corrections.put("민간과 환경", "인간과 환경");  // 오타 사례 추가

        // 추가적으로 정규화할 오타들을 여기에 계속 추가할 수 있습니다.

        // 오타가 있다면 수정된 이름을 반환, 없다면 원래 이름 반환
        return corrections.getOrDefault(subjectName, subjectName);
    }

    private void initializeSpecialCourses() {
        unmatchedCourses.add("진로설계1");
        unmatchedCourses.add("진로설계2(진로탐색과 자기계발)");
        unmatchedCourses.add("진로설계3(진로설정과 경력개발)");
        unmatchedCourses.add("진로설계4");
        unmatchedCourses.add("기업가정신과 창업");
        unmatchedCourses.add("공학설계입문");
        unmatchedCourses.add("캡스톤디자인1");

        // 교필 과목 추가
        unmatchedCourses.add("글쓰기");
        unmatchedCourses.add("발표와 토론");
        unmatchedCourses.add("대학영어");
        unmatchedCourses.add("C 프로그래밍");
    }

    private void displayMatches(List<String> ocrSubjects) {
        tvSubjects.setText(""); // TextView 초기화

        StringBuilder result = new StringBuilder(); // 결과를 담을 StringBuilder

        // 분야별 매칭 여부를 저장할 맵 (사고와 표현, 분석과 판단, 도전과 미래 등)
        Map<String, List<String>> fieldMatchedCourses = new HashMap<>();
        for (String field : fieldCourses.keySet()) {
            fieldMatchedCourses.put(field, new ArrayList<>()); // 각 분야에 매칭된 과목들을 담을 리스트 초기화
        }

        // OCR로 인식된 과목을 담을 리스트
        List<String> matchedCourses = new ArrayList<>();
        List<String> remainingCourses = new ArrayList<>(unmatchedCourses); // 복사본 사용

        // 1. 분야별 매칭 처리
        for (String ocrSubject : ocrSubjects) {
            String cleanedOcrSubject = cleanSubjectName(ocrSubject); // 과목명 후처리

            // 분야별 매칭 여부 확인
            for (Map.Entry<String, List<String>> entry : fieldCourses.entrySet()) {
                String field = entry.getKey();
                List<String> courses = entry.getValue();

                // OCR 과목이 해당 분야에 속하는지 확인
                for (String course : courses) {
                    if (cleanedOcrSubject.equalsIgnoreCase(course)) {
                        fieldMatchedCourses.get(field).add(course); // 매칭된 과목 리스트에 추가
                        break;
                    }
                }
            }

            // 2. 특수 과목 매칭 처리
            for (String specialCourse : new ArrayList<>(remainingCourses)) {
                String[] splitCourse = specialCourse.split("\\("); // 괄호로 분리
                String mainCourseName = splitCourse[0].trim().toLowerCase();
                String additionalInfo = splitCourse.length > 1 ? splitCourse[1].replace(")", "").trim().toLowerCase() : "";

                // 과목명이나 부가 정보 중 하나라도 매칭되면 해당 과목으로 처리
                if (cleanedOcrSubject.equalsIgnoreCase(mainCourseName) || (!additionalInfo.isEmpty() && cleanedOcrSubject.equalsIgnoreCase(additionalInfo))) {
                    matchedCourses.add(specialCourse);
                    remainingCourses.remove(specialCourse);
                    break;
                }
            }
        }

        // 3. 매칭된 분야 출력 및 추천 과목 추가
        for (Map.Entry<String, List<String>> entry : fieldMatchedCourses.entrySet()) {
            String field = entry.getKey();
            List<String> matchedFieldCourses = entry.getValue();
            List<String> remainingFieldCourses = new ArrayList<>(fieldCourses.get(field)); // 해당 분야의 모든 과목 복사

            if (!matchedFieldCourses.isEmpty()) { // 해당 분야에 매칭된 과목이 있을 때
                result.append("<b>").append(field).append(" 분야에 매칭된 과목:</b><br>");
                for (String course : matchedFieldCourses) {
                    result.append("<font color='blue'>").append(course).append("</font><br>");
                    remainingFieldCourses.remove(course); // 매칭된 과목을 추천 목록에서 제외
                }
            } else { // 매칭된 과목이 없을 때
                result.append("<font color='red'>").append(field).append("</font> 분야에 매칭된 과목이 없습니다.<br>");

                // 해당 분야에서 매칭되지 않은 과목들을 추천
                if (!remainingFieldCourses.isEmpty()) {
                    result.append("<b>추천 과목:</b><br>");
                    for (String recCourse : remainingFieldCourses) {
                        result.append("<font color='green'>").append(recCourse).append("</font><br>");
                    }
                }
            }
        }

        // 4. 매칭되지 않은 과목 출력
        if (!remainingCourses.isEmpty()) {
            result.append("<b>매칭되지 않은 과목명:</b><br>");
            for (String unmatchedCourse : remainingCourses) {
                result.append("<font color='red'>" + unmatchedCourse + "</font><br>");
            }
        } else {
            result.append("<b>모든 과목이 매칭되었습니다.</b><br>");
        }

        // 최종 결과를 TextView에 출력
        tvSubjects.setText(HtmlCompat.fromHtml(result.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY));
    }


    private void displayTotalCredits() {
        StringBuilder result = new StringBuilder();

        double totalGeneralEducation = totalCredits.get("교필") + totalCredits.get("교선");
        double totalMajor = totalCredits.get("전선") + totalCredits.get("심선");
        double totalSpecialCourses = totalCredits.get("특선") + totalCredits.get("특필");
        double totalCreditsEarned = totalGeneralEducation + totalMajor + totalSpecialCourses + totalCredits.get("일선");

        boolean meetsTotalCreditRequirement = totalCreditsEarned >= 130;
        boolean meetsIndividualRequirements =
                totalCredits.get("교필") >= 9 &&
                        totalCredits.get("교선") >= 24 &&
                        totalCredits.get("전선") >= 51 &&
                        totalCredits.get("심선") >= 21 &&
                        totalCredits.get("특필") >= 9;

        if (meetsTotalCreditRequirement) {
            result.append("<font color='blue'>총 이수 학점: ").append(totalCreditsEarned).append(" / 130</font><br>");
        } else {
            result.append("<font color='red'>총 이수 학점: ").append(totalCreditsEarned).append(" / 130</font><br>");
        }

        if (totalCredits.get("교필") < 9) {
            result.append("   <font color='red'>교필: ").append(totalCredits.get("교필")).append(" / 9</font><br>");
        } else {
            result.append("   <font color='blue'>교필: ").append(totalCredits.get("교필")).append(" / 9</font><br>");
        }

        if (totalCredits.get("교선") < 24) {
            result.append("   <font color='red'>교선: ").append(totalCredits.get("교선")).append(" / 24</font><br>");
        } else {
            result.append("   <font color='blue'>교선: ").append(totalCredits.get("교선")).append(" / 24</font><br>");
        }

        if (totalCredits.get("전선") < 51) {
            result.append("   <font color='red'>전선: ").append(totalCredits.get("전선")).append(" / 51</font><br>");
        } else {
            result.append("   <font color='blue'>전선: ").append(totalCredits.get("전선")).append(" / 51</font><br>");
        }

        if (totalCredits.get("심선") < 21) {
            result.append("   <font color='red'>심선: ").append(totalCredits.get("심선")).append(" / 21</font><br>");
        } else {
            result.append("   <font color='blue'>심선: ").append(totalCredits.get("심선")).append(" / 21</font><br>");
        }

        if (totalCredits.get("특필") < 9) {
            result.append("   <font color='red'>특필: ").append(totalCredits.get("특필")).append(" / 9</font><br>");
        } else {
            result.append("   <font color='blue'>특필: ").append(totalCredits.get("특필")).append(" / 9</font><br>");
        }

        result.append("   특선: ").append(totalCredits.get("특선")).append("<br>")
                .append("일선: ").append(totalCredits.get("일선")).append("<br>");

        if (meetsTotalCreditRequirement && meetsIndividualRequirements) {
            result.append("<font color='blue'>졸업 요건을 충족합니다.</font><br>");
        } else {
            result.append("<font color='red'>졸업 요건을 충족하지 않습니다.</font><br>");
        }

        // 학점 정보 출력
        tvResult.setText(HtmlCompat.fromHtml(result.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY));
    }

    private void initializeCreditMap() {
        totalCredits.put("교필", 0.0);
        totalCredits.put("교선", 0.0);
        totalCredits.put("일선", 0.0);
        totalCredits.put("전선", 0.0);
        totalCredits.put("심선", 0.0);
        totalCredits.put("특선", 0.0);
        totalCredits.put("특필", 0.0);
    }

    private void initializeCreditMap(Map<String, Double> map) {
        map.put("교필", 0.0);
        map.put("교선", 0.0);
        map.put("일선", 0.0);
        map.put("전선", 0.0);
        map.put("심선", 0.0);
        map.put("특선", 0.0);
        map.put("특필", 0.0);
    }
}
