package com.example.capstone4;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
import java.util.List;

public class UnitCalculationActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 100;
    private ImageView imageView1;
    private TextView tvResult1;
    private Uri imageUri;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unit_calculation);

        Button btnSelectImage1 = findViewById(R.id.btnSelectImage1);
        imageView1 = findViewById(R.id.imageView1);
        tvResult1 = findViewById(R.id.tvResult1);

        btnSelectImage1.setOnClickListener(v -> openGallery());
    }

    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            imageUri = data.getData();
            imageView1.setImageURI(imageUri);
            try {
                performOCR();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void performOCR() throws IOException {
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
                    Log.d(TAG, "Text found: " + textAnnotations.get(0).getDescription());
                    //tvResult.setText(textAnnotations.get(0).getDescription());
                    String resultText = textAnnotations.get(0).getDescription();
                    double totalScore = calculateTotalScore(resultText);
                    if (totalScore >= 70) {
                        tvResult1.setText("총점 합계: " + totalScore);
                    } else {
                        double missingScore = 70 - totalScore;
                        String resultMessage = "총점 합계: "+totalScore + "\n"+ "UNIT이 " + missingScore + "점 부족합니다";
                        SpannableString spannable = new SpannableString(resultMessage);
                        int start = resultMessage.indexOf(String.valueOf(missingScore));
                        int end = start + String.valueOf(missingScore).length();
                        spannable.setSpan(new ForegroundColorSpan(Color.RED), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        tvResult1.setText(spannable);
                    }
                } else {
                    Log.d(TAG, "No text found");
                    tvResult1.setText("No text found");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during OCR process: ", e);
        }
    }
    private double calculateTotalScore(String text) {
        String[] lines = text.split("\n");
        double totalScore = 0.0;
        for (String line : lines) {
            if (line.matches(".*\\d+\\.\\d+")) {
                String[] parts = line.split("\\s+");
                try {
                    double score = Double.parseDouble(parts[parts.length - 1]);
                    totalScore += score;
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error parsing score: ", e);
                }
            }
        }
        return totalScore;
    }
}




