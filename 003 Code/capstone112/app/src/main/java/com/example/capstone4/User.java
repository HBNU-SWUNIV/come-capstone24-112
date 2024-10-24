package com.example.capstone4;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class User extends AppCompatActivity {

    private static final int PICK_IMAGE = 100;
    private static final int REQUEST_CHANGE_EMAIL = 200; // 이메일 변경 요청 코드
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_IMAGE_URI = "image_uri";
    private ImageView profilePicture;
    private TextView tvNickname, tvEmail, tvChangePassword, tvChangeEmail, tvChangeNickname;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setSelectedItemId(R.id.navigation_user);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                startActivity(new Intent(User.this, MainActivity.class));
                return true;
            } else if (itemId == R.id.navigation_graduate) {
                startActivity(new Intent(User.this, GraduationClick.class));
                return true;
            } else if (itemId == R.id.navigation_schedule) {
                startActivity(new Intent(User.this, TimetableActivity.class));
                return true;
            } else if (itemId == R.id.navigation_user) {
                // 현재 화면이므로 추가 액션 없음
                return true;
            }
            return false;
        });

        // UI 요소 초기화
        profilePicture = findViewById(R.id.profile_picture);
        tvNickname = findViewById(R.id.nickname);
        tvEmail = findViewById(R.id.email);
        tvChangePassword = findViewById(R.id.tv_change_password);
        tvChangeEmail = findViewById(R.id.tv_change_email);
        tvChangeNickname = findViewById(R.id.tv_change_nickname);


        // SharedPreferences에서 이메일 불러오기
        loadEmailFromPreferences();

        // SharedPreferences에서 저장된 이미지 URI와 사용자 정보를 복원
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String uriString = preferences.getString(KEY_IMAGE_URI, null);
        String nickname = preferences.getString("nickname", "닉네임 없음");
        String email = preferences.getString("email", "이메일 없음");

        // 데이터가 null이 아닌지 확인 후 설정
        if (tvNickname != null) {
            tvNickname.setText(nickname);
        } else {
            Toast.makeText(this, "닉네임을 불러오는 중 오류 발생", Toast.LENGTH_SHORT).show();
        }

        if (tvEmail != null) {
            tvEmail.setText(email);
        } else {
            Toast.makeText(this, "이메일을 불러오는 중 오류 발생", Toast.LENGTH_SHORT).show();
        }

        // 프로필 사진 로드
        if (uriString != null) {
            selectedImageUri = Uri.parse(uriString);
            loadProfilePicture(selectedImageUri);
        }

        // profile_picture 클릭 시 갤러리 열기
        profilePicture.setOnClickListener(v -> openGallery());

        //닉네임 변경 TextView 클릭 시 액티비티 이동
        tvChangeNickname.setOnClickListener(v -> {
            Intent intent = new Intent(User.this, ChangeNicknameActivity.class);
            startActivity(intent);
        });

        // 비밀번호 변경 TextView 클릭 시 액티비티 이동
        tvChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(User.this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        // 이메일 변경 TextView 클릭 시 액티비티 이동
        tvChangeEmail.setOnClickListener(v -> {
            Intent intent = new Intent(User.this, ChangeEmailActivity.class);
            startActivityForResult(intent, REQUEST_CHANGE_EMAIL);
        });
    }

    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    private void loadEmailFromPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String userEmail = sharedPreferences.getString("userEmail", "이메일 없음");
        tvEmail.setText(userEmail);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE && data != null) {
                Uri imageUri = data.getData();
                if (imageUri != null) {
                    selectedImageUri = imageUri;
                    loadProfilePicture(selectedImageUri);

                    // 이미지 URI를 SharedPreferences에 저장
                    SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                    editor.putString(KEY_IMAGE_URI, selectedImageUri.toString());
                    editor.apply();
                }
            } else if (requestCode == REQUEST_CHANGE_EMAIL && data != null) {
                // 이메일이 변경되었을 경우 새 이메일 반영
                String newEmail = data.getStringExtra("newEmail");
                if (newEmail != null) {
                    tvEmail.setText(newEmail);

                    // 변경된 이메일을 SharedPreferences에 저장
                    SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                    editor.putString("email", newEmail);
                    editor.apply();
                }
            }
        }
    }

    private void loadProfilePicture(Uri imageUri) {
        // Glide를 사용하여 원형 이미지로 설정
        Glide.with(this)
                .load(imageUri)
                .apply(RequestOptions.circleCropTransform()) // 원형 크롭 적용
                .into(profilePicture); // profilePicture에 이미지 적용
    }
}

