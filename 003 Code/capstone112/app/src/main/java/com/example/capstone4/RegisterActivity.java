package com.example.capstone4;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private EditText et_id, et_pw, et_confirm_pw, et_nickname, et_email;
    private Button btn_signup, btn_check_id;
    private boolean isIDChecked = false; // 아이디 중복체크 여부 플래그

    @Override
    protected void onCreate(Bundle savedInstanceState) { // 액티비티 시작시 처음으로 실행되는 생명주기!
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // 아이디 값 찾아주기
        et_id = findViewById(R.id.et_id);
        et_pw = findViewById(R.id.et_pw);
        et_confirm_pw = findViewById(R.id.et_confirm_pw);
        et_nickname = findViewById(R.id.et_nickname);
        et_email = findViewById(R.id.et_email);

        // 중복체크 버튼 클릭 시 수행
        btn_check_id = findViewById(R.id.btn_check_id); //6월11일 9:20 추가한 내용들
        btn_check_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userID = et_id.getText().toString();

                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");
                            if (success) { // 아이디 중복 없음
                                isIDChecked = true;
                                Toast.makeText(getApplicationContext(), "사용 가능한 아이디입니다.", Toast.LENGTH_SHORT).show();
                            } else { // 아이디 중복 있음
                                Toast.makeText(getApplicationContext(), "이미 사용 중인 아이디입니다.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };
                // 서버로 Volley를 이용해서 요청을 함.
                CheckIDRequest checkIDRequest = new CheckIDRequest(userID, responseListener);
                RequestQueue queue = Volley.newRequestQueue(RegisterActivity.this);
                queue.add(checkIDRequest);
            }
        });


        // 회원가입 버튼 클릭 시 수행
        btn_signup = findViewById(R.id.btn_signup);
        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // EditText에 현재 입력되어있는 값을 get(가져온다)해온다.
                String userID = et_id.getText().toString();
                String userPassword = et_pw.getText().toString();
                String userConfirmPassword = et_confirm_pw.getText().toString();
                String userEmail = et_email.getText().toString();
                String userNickname = et_nickname.getText().toString();

                if (!isIDChecked) {
                    Toast.makeText(getApplicationContext(), "아이디 중복체크를 해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!userPassword.equals(userConfirmPassword)) {
                    Toast.makeText(getApplicationContext(), "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!isPasswordValid(userPassword)) {
                    Toast.makeText(getApplicationContext(), "비밀번호는 영문, 숫자, 특수기호를 포함하여 8~16자리여야 합니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!isEmailValid(userEmail)) {
                    Toast.makeText(getApplicationContext(), "유효한 이메일 형식을 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");
                            if (success) { // 회원등록에 성공한 경우
                                Toast.makeText(getApplicationContext(),"회원 등록에 성공하였습니다.",Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                startActivity(intent);
                            } else { // 회원등록에 실패한 경우
                                Toast.makeText(getApplicationContext(),"회원 등록에 실패하였습니다.",Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                };
                // 서버로 Volley를 이용해서 요청을 함.
                RegisterRequest registerRequest = new RegisterRequest(userID,userPassword,userEmail,userNickname, responseListener);
                RequestQueue queue = Volley.newRequestQueue(RegisterActivity.this);
                queue.add(registerRequest);

            }
        });
    }

    private boolean isPasswordValid(String password) {
        // 비밀번호 유효성 검사 (영문, 숫자, 특수기호 포함 8~16자리)
        String passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,16}$";
        Pattern pattern = Pattern.compile(passwordPattern);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

    private boolean isEmailValid(String email) {
        // 이메일 형식 검사
        String emailPattern = "^[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+$";
        Pattern pattern = Pattern.compile(emailPattern);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

}
