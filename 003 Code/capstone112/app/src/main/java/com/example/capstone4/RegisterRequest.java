package com.example.capstone4;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class RegisterRequest extends StringRequest {

    // 서버 URL 설정 ( PHP 파일 연동 )
    //final static private String URL = "http://10.0.2.2/Register.php";
    //final static private String URL = "http://192.168.0.103/Register.php"; //집 와이파이랑 핸드폰 와이파이랑 동일하게 맞추기
    final static private String URL = "http://54.157.226.97/Register.php";
    private Map<String, String> map;


    public RegisterRequest(String userID, String userPassword, String userEmail, String userNickname,  Response.Listener<String> listener) {
        super(Request.Method.POST, URL, listener, null);

        map = new HashMap<>();
        map.put("userID",userID);
        map.put("userPassword", userPassword);
        map.put("userEmail", userEmail);
        map.put("userNickname", userNickname);

    }

    @Override
    protected Map<String, String> getParams() {
        return map;
    }
}