package com.example.capstone4;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
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

public class ChangeEmailActivity extends AppCompatActivity {

    private EditText newEmail, password;
    private Button btnChangeEmail;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String CHANGE_EMAIL_URL = "http://54.157.226.97/change_email.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_email);

        newEmail = findViewById(R.id.new_email);
        password = findViewById(R.id.password);
        btnChangeEmail = findViewById(R.id.btn_change_email);

        // SharedPreferences에서 저장된 기존 이메일을 불러와 EditText에 설정
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String currentEmail = preferences.getString("email", "");
        String userID = preferences.getString("userID", ""); // 유저 ID 가져오기
        newEmail.setText(currentEmail);

        btnChangeEmail.setOnClickListener(v -> {
            String email = newEmail.getText().toString();
            String pass = password.getText().toString();

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(ChangeEmailActivity.this, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 로그 추가
            Log.d("ChangeEmailActivity", "이메일 변경 요청: " + email);
            changeEmail(userID, email, pass);
        });
    }

    private void changeEmail(String userID, String email, String password) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, CHANGE_EMAIL_URL,
                response -> {
                    // 응답 로그 추가
                    Log.d("ChangeEmailActivity", "서버 응답: " + response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = jsonObject.getBoolean("success");

                        if (success) {
                            // 이메일 변경 성공
                            SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                            editor.putString("email", email);
                            editor.apply();

                            Toast.makeText(ChangeEmailActivity.this, "이메일이 성공적으로 변경되었습니다.", Toast.LENGTH_SHORT).show();

                            // User Activity로 이메일 값을 전달하며 돌아가기
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("newEmail", email);
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        } else {
                            // 이메일 변경 실패
                            Toast.makeText(ChangeEmailActivity.this, "이메일 변경에 실패했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    // 실패 로그 추가
                    Log.e("ChangeEmailActivity", "서버 요청 실패", error);
                    Toast.makeText(ChangeEmailActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userID", userID);
                params.put("newEmail", email);
                params.put("password", password);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}
