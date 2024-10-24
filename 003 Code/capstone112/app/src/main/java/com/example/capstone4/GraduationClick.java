package com.example.capstone4;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class GraduationClick extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graduation_requirements);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setSelectedItemId(R.id.navigation_graduate);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                Intent intent = new Intent(GraduationClick.this, MainActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.navigation_graduate) {
                // 현재 페이지, 아무 작업도 하지 않음
                return true;
            } else if (itemId == R.id.navigation_schedule) {
                startActivity(new Intent(GraduationClick.this, TimetableActivity.class));
                return true;
            } else if (itemId == R.id.navigation_user) {
                // 내 정보 메뉴 클릭 시
                startActivity(new Intent(GraduationClick.this, User.class));
                return true;
            }
            return false;
        });


        Button btnManualInput = findViewById(R.id.btn_upload_photo);
        Button btnCameraInput = findViewById(R.id.btn_take_photo);
        Button btnUnit = findViewById(R.id.btn_unit);

        btnManualInput.setOnClickListener(v -> {
            Intent intent = new Intent(GraduationClick.this, GalleryActivity.class);
            startActivity(intent);
        });

        btnCameraInput.setOnClickListener(v -> {
            Intent intent = new Intent(GraduationClick.this, CameraActivity.class);
            startActivity(intent);
        });

        btnUnit.setOnClickListener(v -> {
            Intent intent = new Intent(GraduationClick.this, UnitCalculationActivity.class);
            startActivity(intent);
        });
    }
}
