package com.example.capstone4;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.HashMap;
import java.util.Map;

public class TimetableActivity extends AppCompatActivity implements CourseBottomSheetDialog.OnItemSelectedListener {

    private ImageButton btn_add, btn_reset;
    private Map<String, TextView> timetableCells;
    private RelativeLayout selectedCourseLayout;
    private TextView selectedCourseText;
    private Button addCourseButton;
    private Course selectedCourse;
    private Map<String, Course> highlightedCourses; // Highlighted courses

    private BottomSheetDialog courseDetailDialog;

    // 추가된 부분: 색상 리스트와 인덱스 변수
    private int[] courseColors;
    private int currentColorIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        //색상 리스트 초기화
        courseColors = new int[]{
                getResources().getColor(R.color.red),
                getResources().getColor(R.color.orange),
                getResources().getColor(R.color.yellow),
                getResources().getColor(R.color.green1),
                getResources().getColor(R.color.green2),
                getResources().getColor(R.color.green3),
                getResources().getColor(R.color.blue),
                getResources().getColor(R.color.purple1),
                getResources().getColor(R.color.purple2),

        };


        btn_reset = findViewById(R.id.btn_reset);
        btn_add = findViewById(R.id.btn_add);
        selectedCourseLayout = findViewById(R.id.selected_course_layout);
        selectedCourseText = findViewById(R.id.selected_course_text);
        addCourseButton = findViewById(R.id.add_course_button);

        highlightedCourses = new HashMap<>();

        //btn_reset.setOnClickListener(v -> resetTimetable());
        btn_reset.setOnClickListener(v -> {
            clearTimetableHighlights();
            highlightedCourses.clear();
            saveTimetableData(); // 데이터 저장
        });

        btn_add.setOnClickListener(v -> {
            CourseBottomSheetDialog bottomSheetDialog = new CourseBottomSheetDialog();
            bottomSheetDialog.setOnItemSelectedListener(TimetableActivity.this);
            bottomSheetDialog.show(getSupportFragmentManager(), "CourseBottomSheet");
        });

        addCourseButton.setOnClickListener(v -> {
            addSelectedCourseToTimetable();
            selectedCourseLayout.setVisibility(View.GONE); // 추가 후 숨김
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setSelectedItemId(R.id.navigation_schedule);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                Intent intent = new Intent(TimetableActivity.this, MainActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.navigation_graduate) {
                startActivity(new Intent(TimetableActivity.this, GraduationClick.class));
                return true;
            } else if (itemId == R.id.navigation_schedule) {
                // 현재 페이지, 아무 작업도 하지 않음
                return true;
            } else if (itemId == R.id.navigation_user) {
                // 내 정보 메뉴 클릭 시
                startActivity(new Intent(TimetableActivity.this, User.class));
                return true;
            }
            return false;
        });

        initializeTimetableCells();
        loadTimetableData(); // 데이터 불러오기

        //clearTimetableHighlights();
    }

    //색상 순환적으로 지정하는 메서드
    private int getNextColor(){
        int color = courseColors[currentColorIndex];
        currentColorIndex = (currentColorIndex + 1) % courseColors.length;
        return color;
    }

    private void resetTimetable() {
        // 모든 셀의 배경색과 텍스트를 초기화
        for (TextView cell : timetableCells.values()) {
            cell.setBackgroundColor(Color.TRANSPARENT);
            cell.setText("");
            cell.setOnClickListener(null); // 클릭 리스너 제거
        }

        // 하이라이트된 과목 초기화
        highlightedCourses.clear();

        // SharedPreferences 초기화
        SharedPreferences sharedPreferences = getSharedPreferences("TimetablePrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Toast.makeText(this, "시간표가 초기화되었습니다.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemSelected(Course course) {
        //기존 선택 과목의 하이라이트 제거
        clearTemporaryHighlights(); //색 칠해진 셀 초기화, 과목 클릭하고 추가 안누르면 회색 안사라짐
        //추가 버튼 클릭 안하고 다른 과목 클릭하면 셀 색(회색) 초기화

        selectedCourse = course;
        selectedCourseText.setText(course.getCourseName());
        selectedCourseLayout.setVisibility(View.VISIBLE);

        highlightCurrentCourse(course); // 임시로 선택한 과목을 강조

        // 과목의 시간표 정보를 파싱하여 시간표에 색칠
        String schedule = course.getSchedule(); // 예: "수1,2,3,4"
        String day = schedule.substring(0, 1); // 요일 추출
        String[] timeSlots = schedule.substring(1).split(","); // 시간대들을 쉼표로 구분하여 분리

        // 각 시간대를 제대로 분리하여 처리
        for (String timeStr : timeSlots) {
            timeStr = timeStr.trim();

            try {
                int time = Integer.parseInt(timeStr);
                Log.d("OnItemSelected", "Parsed time: " + time);
                String cellKey = getDayName(day) + timeToCellSuffix(time);
                TextView cell = timetableCells.get(cellKey);
                if (cell != null) {
                    cell.setBackgroundColor(Color.GRAY); // 셀 회색으로 임시 색칠
                }
            } catch (NumberFormatException e) {
                Log.e("OnItemSelected", "Failed to parse time: " + timeStr, e);
            }
        }
    }

    // 기존 임시 하이라이트 제거 메서드
    private void clearTemporaryHighlights() {
        for (TextView cell : timetableCells.values()) {
            if (cell.getBackground() instanceof ColorDrawable) {
                ColorDrawable colorDrawable = (ColorDrawable) cell.getBackground();
                if (colorDrawable.getColor() == Color.GRAY) { // 회색으로 칠해진 셀만 초기화
                    cell.setBackgroundColor(Color.TRANSPARENT);
                }
            }
        }
    }

    @Override
    public void onAddCourseClicked(Course course) {
        selectedCourse = course;
        addSelectedCourseToTimetable();
    }



    private void addSelectedCourseToTimetable() {
        if (selectedCourse == null) return;

        int color = getNextColor(); //순환 색상 가져오기

        String schedule = selectedCourse.getSchedule();
        String day = schedule.substring(0, 1);
        String[] timeSlots = schedule.substring(1).split(",");

        boolean firstSlot = true;

        for (String timeStr : timeSlots) {
            timeStr = timeStr.trim();
            try {
                int time = Integer.parseInt(timeStr);
                //필요한 시간대가 6시 이후인 경우 동적으로 추가
                String cellKey = getDayName(day) + timeToCellSuffix(time);
                TextView cell = timetableCells.get(cellKey);
                if (cell != null) {
                    cell.setBackgroundColor(color);
                    if (firstSlot) {
                        cell.setText(selectedCourse.getCourseName());
                        firstSlot = false;
                    } else {
                        cell.setText("");
                    }
                    cell.setOnClickListener(v -> showCourseDetailDialog(cellKey, selectedCourse)); // 클릭 리스너 추가
                }
            } catch (NumberFormatException e) {
                Log.e("AddCourse", "Failed to parse time: " + timeStr, e);
            }
        }

        highlightedCourses.put(selectedCourse.getCourseName(), selectedCourse);
        saveTimetableData();
    }

    private void initializeTimetableCells() {
        timetableCells = new HashMap<>();
        // 월요일
        timetableCells.put("mon9", findViewById(R.id.mon9));
        timetableCells.put("mon10", findViewById(R.id.mon10));
        timetableCells.put("mon11", findViewById(R.id.mon11));
        timetableCells.put("mon12", findViewById(R.id.mon12));
        timetableCells.put("mon13", findViewById(R.id.mon13));
        timetableCells.put("mon14", findViewById(R.id.mon14));
        timetableCells.put("mon15", findViewById(R.id.mon15));
        timetableCells.put("mon16", findViewById(R.id.mon16));
        timetableCells.put("mon17", findViewById(R.id.mon17));

        // 화요일
        timetableCells.put("tue9", findViewById(R.id.tue9));
        timetableCells.put("tue10", findViewById(R.id.tue10));
        timetableCells.put("tue11", findViewById(R.id.tue11));
        timetableCells.put("tue12", findViewById(R.id.tue12));
        timetableCells.put("tue13", findViewById(R.id.tue13));
        timetableCells.put("tue14", findViewById(R.id.tue14));
        timetableCells.put("tue15", findViewById(R.id.tue15));
        timetableCells.put("tue16", findViewById(R.id.tue16));
        timetableCells.put("tue17", findViewById(R.id.tue17));

        // 수요일
        timetableCells.put("wed9", findViewById(R.id.wed9));
        timetableCells.put("wed10", findViewById(R.id.wed10));
        timetableCells.put("wed11", findViewById(R.id.wed11));
        timetableCells.put("wed12", findViewById(R.id.wed12));
        timetableCells.put("wed13", findViewById(R.id.wed13));
        timetableCells.put("wed14", findViewById(R.id.wed14));
        timetableCells.put("wed15", findViewById(R.id.wed15));
        timetableCells.put("wed16", findViewById(R.id.wed16));
        timetableCells.put("wed17", findViewById(R.id.wed17));

        // 목요일
        timetableCells.put("thu9", findViewById(R.id.thu9));
        timetableCells.put("thu10", findViewById(R.id.thu10));
        timetableCells.put("thu11", findViewById(R.id.thu11));
        timetableCells.put("thu12", findViewById(R.id.thu12));
        timetableCells.put("thu13", findViewById(R.id.thu13));
        timetableCells.put("thu14", findViewById(R.id.thu14));
        timetableCells.put("thu15", findViewById(R.id.thu15));
        timetableCells.put("thu16", findViewById(R.id.thu16));
        timetableCells.put("thu17", findViewById(R.id.thu17));

        // 금요일
        timetableCells.put("fri9", findViewById(R.id.fri9));
        timetableCells.put("fri10", findViewById(R.id.fri10));
        timetableCells.put("fri11", findViewById(R.id.fri11));
        timetableCells.put("fri12", findViewById(R.id.fri12));
        timetableCells.put("fri13", findViewById(R.id.fri13));
        timetableCells.put("fri14", findViewById(R.id.fri14));
        timetableCells.put("fri15", findViewById(R.id.fri15));
        timetableCells.put("fri16", findViewById(R.id.fri16));
        timetableCells.put("fri17", findViewById(R.id.fri17));
    }

    //시간표를 초기 상태로 되돌리기 위해 사용
    // ex) 과목 선택했다가 취소하거나, 새로 고침할 때 시간표 초기화 할 때 유용하게 사용
    private void clearTimetableHighlights() {
        for (TextView cell : timetableCells.values()) {
            cell.setBackgroundColor(Color.TRANSPARENT); //셀 배경 색상을 투명하게 설정
            cell.setText("");//셀 텍스트를 빈 문자열로 설정
            cell.setOnClickListener(null); // 클릭 리스너 제거
        }
    }

    private void highlightCurrentCourse(Course course) {
        if (course == null) return;

        String schedule = course.getSchedule();
        String day = schedule.substring(0, 1);
        String[] timeSlots = schedule.substring(1).split(",");

        for (String timeStr : timeSlots) {
            timeStr = timeStr.trim();
            try {
                int time = Integer.parseInt(timeStr);
                String cellKey = getDayName(day) + timeToCellSuffix(time);
                TextView cell = timetableCells.get(cellKey);
                if (cell != null) {
                    cell.setBackgroundColor(Color.GRAY);
                }
            } catch (NumberFormatException e) {
                Log.e("HighlightCourse", "Failed to parse time: " + timeStr, e);
            }
        }
    }

    private void saveTimetableData() {
        SharedPreferences sharedPreferences = getSharedPreferences("TimetablePrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        for (Map.Entry<String, TextView> entry : timetableCells.entrySet()) {
            String key = entry.getKey();
            TextView cell = entry.getValue();
            editor.putString(key + "_text", cell.getText().toString());
            editor.putInt(key + "_color", ((ColorDrawable) cell.getBackground()).getColor());
        }

        // 과목 데이터를 저장
        for (Map.Entry<String, Course> entry : highlightedCourses.entrySet()) {
            String courseName = entry.getKey();
            Course course = entry.getValue();
            editor.putString(courseName + "_schedule", course.getSchedule());
            editor.putString(courseName + "_professor", course.getProfessorName());
            editor.putInt(courseName + "_courseNum", course.getCourseNum());
            editor.putString(courseName + "_classroom", course.getClassroom());
        }

        editor.apply();
    }



    private void loadTimetableData() {
        SharedPreferences sharedPreferences = getSharedPreferences("TimetablePrefs", MODE_PRIVATE);

        for (Map.Entry<String, TextView> entry : timetableCells.entrySet()) {
            String key = entry.getKey();
            TextView cell = entry.getValue();
            cell.setText(sharedPreferences.getString(key + "_text", ""));
            int color = sharedPreferences.getInt(key + "_color", Color.TRANSPARENT);
            cell.setBackgroundColor(color);
            if (!cell.getText().toString().isEmpty()) {
                String courseName = cell.getText().toString();
                String courseSchedule = sharedPreferences.getString(cell.getText().toString() + "_schedule", null);
                String professorName = sharedPreferences.getString(cell.getText().toString() + "_professor", "");
                int courseNum = sharedPreferences.getInt(cell.getText().toString() + "_courseNum", 0);
                String classroom = sharedPreferences.getString(cell.getText().toString() + "_classroom", "");
                if (courseSchedule != null) {
                    //Course course = new Course(cell.getText().toString(), courseNum, professorName, courseSchedule, classroom);
                    //highlightedCourses.put(cell.getText().toString(), course);
                    Course course = new Course(courseName, 0, professorName, courseSchedule, ""); //23:55추가
                    highlightedCourses.put(courseName, course);//23:55추가
                    cell.setOnClickListener(v -> showCourseDetailDialog(key, course)); // 저장된 Course 객체 전달
                }
            }
        }
    }



    private void showCourseDetailDialog(String cellKey, Course course) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.course_detail_dialog, null);
        builder.setView(dialogView);

        TextView courseNameTextView = dialogView.findViewById(R.id.course_name);
        TextView professorNameTextView = dialogView.findViewById(R.id.professor_name);
        TextView courseTimeTextView = dialogView.findViewById(R.id.course_time);
        Button deleteButton = dialogView.findViewById(R.id.delete_button);

        // 과목 정보 설정
        courseNameTextView.setText(course.getCourseName());
        professorNameTextView.setText(course.getProfessorName()); // 실제 교수명으로 설정
        courseTimeTextView.setText(convertScheduleToReadableFormat(course.getSchedule())); // 실제 시간대로 설정

        AlertDialog alertDialog = builder.create();

        deleteButton.setOnClickListener(v -> {
            // 과목 삭제 처리
            //timetableCells.get(cellKey).setText("");
            //timetableCells.get(cellKey).setBackgroundColor(Color.TRANSPARENT);
            //highlightedCourses.remove(course.getCourseName());
            //alertDialog.dismiss();
            //saveTimetableData(); // 데이터 저장
            removeCourseFromTimetable(course);
            alertDialog.dismiss();
            saveTimetableData(); // 데이터 저장
        });

        alertDialog.show();
    }

    private void removeCourseFromTimetable(Course course) {
        String schedule = course.getSchedule(); // 예: "수1,2,3,4"
        String day = schedule.substring(0, 1); // 요일 추출
        String[] timeSlots = schedule.substring(1).split(","); // 시간대들을 쉼표로 구분하여 분리

        for (String timeStr : timeSlots) {
            timeStr = timeStr.trim();
            try {
                int time = Integer.parseInt(timeStr);
                String cellKey = getDayName(day) + timeToCellSuffix(time);
                TextView cell = timetableCells.get(cellKey);
                if (cell != null) {
                    cell.setText("");
                    cell.setBackgroundColor(Color.TRANSPARENT);
                    cell.setOnClickListener(null); // 클릭 리스너 제거
                }
            } catch (NumberFormatException e) {
                Log.e("RemoveCourse", "Failed to parse time: " + timeStr, e);
            }
        }

        highlightedCourses.remove(course.getCourseName());
    }

    private String getDayName(String day) {
        switch (day) {
            case "월": return "mon";
            case "화": return "tue";
            case "수": return "wed";
            case "목": return "thu";
            case "금": return "fri";
            default: return "";
        }
    }

    private String getKoreanDayName(String day) { //삭제창에 뜨는 요일 한글로
        switch (day) {
            case "월": return "월";
            case "화": return "화";
            case "수": return "수";
            case "목": return "목";
            case "금": return "금";
            default: return "";
        }
    }

    // 시간대를 "월 9:00 ~ 12:00" 형식으로 변환하는 메서드
    private String convertScheduleToReadableFormat(String schedule) {
        String day = schedule.substring(0, 1); // 요일 추출
        String[] timeSlots = schedule.substring(1).split(",");
        int startHour = Integer.parseInt(timeSlots[0].trim());
        int endHour = Integer.parseInt(timeSlots[timeSlots.length - 1].trim()) + 1;

        return String.format("%s %d:00 ~ %d:00", getKoreanDayName(day), getHourFromSlot(startHour), getHourFromSlot(endHour));
    }

    private int getHourFromSlot(int slot) {
        switch (slot) {
            case 1: return 9;
            case 2: return 10;
            case 3: return 11;
            case 4: return 12;
            case 5: return 13;
            case 6: return 14;
            case 7: return 15;
            case 8: return 16;
            case 9: return 17;
            case 10: return 18;
            case 11: return 19;
            case 12: return 20;
            case 13: return 21;
            case 14: return 22;
            case 15: return 23;
            default: return 0;
        }
    }

    private String timeToCellSuffix(int time) {
        //return String.valueOf(time);
        switch (time) {
            case 1: return "9";
            case 2: return "10";
            case 3: return "11";
            case 4: return "12";
            case 5: return "13";
            case 6: return "14";
            case 7: return "15";
            case 8: return "16";
            case 9: return "17";
            case 10: return "18";
            case 11: return "19";
            case 12: return "20";
            case 13: return "21";
            case 14: return "22";
            case 15: return "23";
            default:
                Log.e("TimeToCellSuffix", "Unexpected time value: " + time);
                return "";
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveTimetableData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTimetableData();
    }
}