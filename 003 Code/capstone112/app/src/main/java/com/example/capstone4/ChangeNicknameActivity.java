package com.example.capstone4;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
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

public class ChangeNicknameActivity extends AppCompatActivity {

    private EditText nowNickname, newNickname;
    private Button btnChangeNickname;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String CHECK_NICKNAME_URL = "http://54.157.226.97/check_nickname.php";
    private static final String CHANGE_NICKNAME_URL = "http://54.157.226.97/change_nickname.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_nickname);

        nowNickname = findViewById(R.id.now_nickname);
        newNickname = findViewById(R.id.new_nickname);
        btnChangeNickname = findViewById(R.id.btn_change_nickname);

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String currentNickname = preferences.getString("nickname", ""); // 유저 닉네임 가져오기
        String userID = preferences.getString("userID", ""); // 유저 ID 가져오기

        nowNickname.setText(currentNickname); // 현재 닉네임 표시

        btnChangeNickname.setOnClickListener(v -> {
            String newNick = newNickname.getText().toString().trim();

            if (newNick.isEmpty()) {
                Toast.makeText(ChangeNicknameActivity.this, "새 닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 닉네임 중복 확인
            checkNicknameAvailability(userID, newNick);
        });
    }

    private void checkNicknameAvailability(String userID, String newNick) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, CHECK_NICKNAME_URL,
                response -> {
                    Log.d("ChangeNicknameActivity", "닉네임 중복 확인 서버 응답: " + response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean isAvailable = jsonObject.getBoolean("isAvailable");

                        if (isAvailable) {
                            // 닉네임 변경 요청
                            changeNickname(userID, newNick);
                        } else {
                            Toast.makeText(ChangeNicknameActivity.this, "이미 사용 중인 닉네임입니다.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Log.e("ChangeNicknameActivity", "닉네임 중복 확인 실패", error);
                    Toast.makeText(ChangeNicknameActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userID", userID);
                params.put("newNickname", newNick);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void changeNickname(String userID, String newNick) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, CHANGE_NICKNAME_URL,
                response -> {
                    Log.d("ChangeNicknameActivity", "닉네임 변경 서버 응답: " + response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = jsonObject.getBoolean("success");

                        if (success) {
                            // SharedPreferences에 변경된 닉네임 저장
                            SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                            editor.putString("nickname", newNick);
                            editor.apply();

                            Toast.makeText(ChangeNicknameActivity.this, "닉네임이 성공적으로 변경되었습니다.", Toast.LENGTH_SHORT).show();

                            // User Activity로 닉네임 값을 전달하며 돌아가기
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("newNickname", newNick);
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        } else {
                            Toast.makeText(ChangeNicknameActivity.this, "닉네임 변경에 실패했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Log.e("ChangeNicknameActivity", "닉네임 변경 요청 실패", error);
                    Toast.makeText(ChangeNicknameActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userID", userID);
                params.put("newNickname", newNick);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}
