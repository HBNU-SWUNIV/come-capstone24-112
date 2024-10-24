package com.example.capstone4;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class LoginRequest extends StringRequest {

    // 서버 URL 설정 ( PHP 파일 연동 )
    //final static private String URL = "http:/10.0.2.2/login.php";
    //final static private String URL = "http:/192.168.0.103/login.php"; //집 와이파이랑 핸드폰 와이파이랑 동일하게 맞추기
    final static private String URL = "http://54.157.226.97/login.php";
    private Map<String, String> map;


    public LoginRequest(String userID, String userPassword, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);

        map = new HashMap<>();
        map.put("userID",userID);
        map.put("userPassword", userPassword);

    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }
}
