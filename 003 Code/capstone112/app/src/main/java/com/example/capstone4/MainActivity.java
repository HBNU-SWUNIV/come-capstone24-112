package com.example.capstone4;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    private ImageButton btnSchool, btnBell, btnCom, btnCalender;
    private ApiService apiService;
    private TextView tvTodayClass1, tvClassDate1, tvClassTime1, tvClassTitle1;
    private TextView tvFreeBoard; // 자유게시판 TextView 선언


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSchool = findViewById(R.id.btn_school_home);
        btnBell = findViewById(R.id.btn_academic_notice);
        btnCom = findViewById(R.id.btn_com_notice);
        btnCalender = findViewById(R.id.btn_academic_schedule);

        tvTodayClass1 = findViewById(R.id.tv_today_class_1);
        tvClassDate1 = findViewById(R.id.tv_class_date_1);
        tvClassTime1 = findViewById(R.id.tv_class_time_1);
        tvClassTitle1 = findViewById(R.id.tv_class_title_1);

        tvFreeBoard = findViewById(R.id.tv_free_board);


        // 오늘의 날짜 설정
        String todayDate = new SimpleDateFormat("MM월 dd일 (E) ", Locale.getDefault()).format(new Date());
        tvClassDate1.setText(todayDate);

        // 자유게시판 클릭 시 BoardListActivity로 이동
        tvFreeBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BoardListActivity.class);
                startActivity(intent);
            }
        });


        btnSchool.setOnClickListener(new View.OnClickListener() { //학교 홈페이지
            @Override
            public void onClick(View v) {
                openWebPage("https://www.hanbat.ac.kr/kor/index.do");
            }
        });

        btnBell.setOnClickListener(new View.OnClickListener() { //학사 공지
            @Override
            public void onClick(View v) {
                openWebPage("https://www.hanbat.ac.kr/bbs/BBSMSTR_000000000042/list.do?mno=sub05_01");
            }
        });

        btnCom.setOnClickListener(new View.OnClickListener() { //학과 공지
            @Override
            public void onClick(View v) {
                openWebPage("https://www.hanbat.ac.kr/prog/bbsArticle/BBSMSTR_000000000333/list.do");
            }
        });

        btnCalender.setOnClickListener(new View.OnClickListener() { //학사 일정
            @Override
            public void onClick(View v) {
                openWebPage("https://www.hanbat.ac.kr/prog/schafsSchdul/kor/sub05_0201/A/scheduleList.do");
            }
        });



        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                //Intent intent = new Intent(MainActivity.this, MainActivity.class);
                //startActivity(intent);
                return true;
            } else if (itemId == R.id.navigation_graduate) {
                startActivity(new Intent(MainActivity.this, GraduationClick.class));
                return true;
            } else if (itemId == R.id.navigation_schedule) {
                startActivity(new Intent(MainActivity.this, TimetableActivity.class));
                return true;
            } else if (itemId == R.id.navigation_user) {
                //selectedFragment = new UserFragment();
                startActivity(new Intent(MainActivity.this, User.class));
                return true;
            }
            return false;
        });


    }

    private void openWebPage(String url) {
        try {
            Uri webpage = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "웹 페이지를 열 수 없습니다. 브라우저가 설치되어 있는지 확인하세요.", Toast.LENGTH_LONG).show();
        }
    }
}
