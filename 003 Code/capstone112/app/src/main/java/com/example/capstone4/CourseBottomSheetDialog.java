package com.example.capstone4;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourseBottomSheetDialog extends BottomSheetDialogFragment {

    private RecyclerView recyclerView;
    private CourseAdapter courseAdapter;
    private List<Course> courseList;
    private OnItemSelectedListener onItemSelectedListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.course_relative, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        fetchCourses();
        return view;
    }



    public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
        this.onItemSelectedListener = onItemSelectedListener;
    }

    private void fetchCourses() {
        //ApiService apiService = RetrofitClient.getClient("http://192.168.0.103/").create(ApiService.class);
        //ApiService apiService = RetrofitClient.getClient("http://10.0.2.2/").create(ApiService.class);
        ApiService apiService = RetrofitClient.getClient("http://54.157.226.97/").create(ApiService.class);
        Call<List<Course>> call = apiService.getCourses();
        call.enqueue(new Callback<List<Course>>() {
            @Override
            public void onResponse(Call<List<Course>> call, Response<List<Course>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    courseList = response.body();
                    courseAdapter = new CourseAdapter(courseList);
                    courseAdapter.setOnItemSelectedListener(new CourseAdapter.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(Course course) {
                            if (onItemSelectedListener != null) {
                                onItemSelectedListener.onItemSelected(course);
                            }
                        }

                        @Override
                        public void onAddCourseClicked(Course course) {
                            if (onItemSelectedListener != null) {
                                onItemSelectedListener.onAddCourseClicked(course);
                            }
                        }
                    });
                    recyclerView.setAdapter(courseAdapter);
                }
            }

            @Override
            public void onFailure(Call<List<Course>> call, Throwable t) {
                // Handle failure
            }
        });
    }

    public interface OnItemSelectedListener {
        void onItemSelected(Course course);
        void onAddCourseClicked(Course course); // 추가된 부분
    }
}
