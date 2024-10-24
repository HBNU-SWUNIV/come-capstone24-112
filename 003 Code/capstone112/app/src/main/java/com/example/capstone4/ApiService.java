package com.example.capstone4;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.List;

public interface ApiService {
    @GET("courses.php")
    Call<List<Course>> getCourses();

}
