package com.example.capstone4;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText newPassword, confirmPassword, currentPassword;
    private Button btnChangePassword;
    private ImageView ivToggleNewPassword, ivToggleConfirmPassword, ivToggleCurrentPassword;
    private boolean isNewPasswordVisible = false, isConfirmPasswordVisible = false, isCurrentPasswordVisible = false;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String CHANGE_PASSWORD_URL = "http://54.157.226.97/change_password.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        newPassword = findViewById(R.id.new_password);
        confirmPassword = findViewById(R.id.confirm_password);
        currentPassword = findViewById(R.id.current_password);
        btnChangePassword = findViewById(R.id.btn_change_password);
        ivToggleNewPassword = findViewById(R.id.iv_toggle_new_password);
        ivToggleConfirmPassword = findViewById(R.id.iv_toggle_confirm_password);
        ivToggleCurrentPassword = findViewById(R.id.iv_toggle_current_password);

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String userID = preferences.getString("userID", ""); // 유저 ID 가져오기

        // 새 비밀번호 필드에 대한 토글 기능
        ivToggleNewPassword.setOnClickListener(v -> {
            togglePasswordVisibility(newPassword, ivToggleNewPassword);
            isNewPasswordVisible = !isNewPasswordVisible;
        });

        // 새 비밀번호 확인 필드에 대한 토글 기능
        ivToggleConfirmPassword.setOnClickListener(v -> {
            togglePasswordVisibility(confirmPassword, ivToggleConfirmPassword);
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
        });

        // 현재 비밀번호 필드에 대한 토글 기능
        ivToggleCurrentPassword.setOnClickListener(v -> {
            togglePasswordVisibility(currentPassword, ivToggleCurrentPassword);
            isCurrentPasswordVisible = !isCurrentPasswordVisible;
        });

        btnChangePassword.setOnClickListener(v -> {
            String newPass = newPassword.getText().toString();
            String confirmPass = confirmPassword.getText().toString();
            String currentPass = currentPassword.getText().toString();

            if (newPass.isEmpty() || confirmPass.isEmpty() || currentPass.isEmpty()) {
                Toast.makeText(ChangePasswordActivity.this, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPass.equals(confirmPass)) {
                Toast.makeText(ChangePasswordActivity.this, "새 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isPasswordValid(newPass)) {
                Toast.makeText(ChangePasswordActivity.this, "비밀번호는 영문, 숫자, 특수기호를 포함한 8~16자리여야 합니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            changePassword(userID, currentPass, newPass);
        });
    }

    private void togglePasswordVisibility(EditText passwordField, ImageView toggleIcon) {
        if (passwordField.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            passwordField.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            toggleIcon.setImageResource(R.drawable.ic_eye_on);
        } else {
            passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            toggleIcon.setImageResource(R.drawable.ic_eye_off);
        }
        passwordField.setSelection(passwordField.length());
    }

    // 비밀번호 유효성 검사 (영문, 숫자, 특수기호 포함 8~16자리)
    private boolean isPasswordValid(String password) {
        String passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,16}$";
        Pattern pattern = Pattern.compile(passwordPattern);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

    private void changePassword(String userID, String currentPassword, String newPassword) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, CHANGE_PASSWORD_URL,
                response -> {
                    Log.d("ChangePasswordActivity", "서버 응답: " + response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = jsonObject.getBoolean("success");

                        if (success) {
                            Toast.makeText(ChangePasswordActivity.this, "비밀번호가 성공적으로 변경되었습니다.", Toast.LENGTH_SHORT).show();
                            finish(); // 액티비티 종료
                        } else {
                            String message = jsonObject.getString("message");
                            Toast.makeText(ChangePasswordActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Log.e("ChangePasswordActivity", "서버 요청 실패", error);
                    Toast.makeText(ChangePasswordActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userID", userID);
                params.put("currentPassword", currentPassword);
                params.put("newPassword", newPassword);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}
