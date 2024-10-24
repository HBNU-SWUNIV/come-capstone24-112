package com.example.capstone4;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class CheckIDRequest extends StringRequest {

    // 서버 URL 설정 (PHP 파일 연동)
    //final static private String URL = "http://192.168.0.103/CheckID.php";
    //final static private String URL = "http://10.0.2.2/CheckID.php"; //학교
    final static private String URL = "http://54.157.226.97/CheckID.php";
    private Map<String, String> map;

    public CheckIDRequest(String userID, Response.Listener<String> listener) {
        super(Request.Method.POST, URL, listener, null);

        map = new HashMap<>();
        map.put("userID", userID);
    }

    @Override
    protected Map<String, String> getParams() {
        return map;
    }
}
