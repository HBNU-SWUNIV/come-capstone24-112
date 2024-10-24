package com.example.capstone4;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourseListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CourseAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.course_relative);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //apiService = RetrofitClient.getClient("http://192.168.0.103/").create(ApiService.class);10.0.2.2
        //apiService = RetrofitClient.getClient("http://10.0.2.2/").create(ApiService.class);
        apiService = RetrofitClient.getClient("http://54.157.226.97/").create(ApiService.class);
        loadCourses();
    }

    private void loadCourses() {
        Call<List<Course>> call = apiService.getCourses();
        call.enqueue(new Callback<List<Course>>() {
            @Override
            public void onResponse(Call<List<Course>> call, Response<List<Course>> response) {
                List<Course> courseList = response.body();
                adapter = new CourseAdapter(courseList);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onFailure(Call<List<Course>> call, Throwable t) {
                Log.e("CourseListActivity", "Failed to load courses", t);
            }
        });
    }
}
