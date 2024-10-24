package com.example.capstone4;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    private List<Course> courseList;
    private OnItemSelectedListener onItemSelectedListener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public static class CourseViewHolder extends RecyclerView.ViewHolder {
        public TextView courseName;
        public TextView professorName;
        public TextView schedule;
        public TextView classroom;
        public Button addCourseButton;

        public CourseViewHolder(View itemView, OnItemSelectedListener onItemSelectedListener) {
            super(itemView);
            courseName = itemView.findViewById(R.id.course_name);
            professorName = itemView.findViewById(R.id.professor_name);
            schedule = itemView.findViewById(R.id.schedule);
            classroom = itemView.findViewById(R.id.classroom);
            addCourseButton = itemView.findViewById(R.id.btn_add_course);

            itemView.setOnClickListener(v -> {
                if (onItemSelectedListener != null) {
                    onItemSelectedListener.onItemSelected((Course) itemView.getTag());
                }
            });

            addCourseButton.setOnClickListener(v -> {
                if (onItemSelectedListener != null) {
                    onItemSelectedListener.onAddCourseClicked((Course) itemView.getTag());
                }
            });
        }
    }

    public CourseAdapter(List<Course> courseList) {
        this.courseList = courseList;
    }

    public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
        this.onItemSelectedListener = onItemSelectedListener;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.course_item, parent, false);
        return new CourseViewHolder(v, onItemSelectedListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course currentCourse = courseList.get(position);
        holder.courseName.setText(currentCourse.getCourseName());
        holder.professorName.setText(currentCourse.getProfessorName());
        holder.schedule.setText(currentCourse.getSchedule());
        holder.classroom.setText(currentCourse.getClassroom());
        holder.itemView.setTag(currentCourse);

        // 선택된 항목의 배경색을 변경하고 추가 버튼 표시
        if (selectedPosition == position) {
            holder.itemView.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.selected_item));
            holder.addCourseButton.setVisibility(View.VISIBLE);
        } else {
            holder.itemView.setBackgroundColor(holder.itemView.getContext().getResources().getColor(android.R.color.transparent));
            holder.addCourseButton.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onItemSelectedListener != null) {
                onItemSelectedListener.onItemSelected(currentCourse);
                notifyItemChanged(selectedPosition);
                selectedPosition = holder.getAdapterPosition();
                notifyItemChanged(selectedPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    public interface OnItemSelectedListener {
        void onItemSelected(Course course);
        void onAddCourseClicked(Course course); // 추가된 부분
    }
}
