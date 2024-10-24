package com.example.capstone4;

import com.google.gson.annotations.SerializedName;

public class Course {
    @SerializedName("course_name")
    private String courseName;
    @SerializedName("course_num")
    private int courseNum;
    @SerializedName("professor_name")
    private String professorName;
    @SerializedName("schedule")
    private String schedule;
    @SerializedName("classroom")
    private String classroom;

    public Course(String courseName, int courseNum, String professorName, String schedule, String classroom) {
        this.courseName = courseName;
        this.courseNum = courseNum;
        this.professorName = professorName;
        this.schedule = schedule;
        this.classroom = classroom;
    }

    public String getCourseName() {
        return courseName;
    }

    public int getCourseNum() {
        return courseNum;
    }

    public String getProfessorName() {
        return professorName;
    }

    public String getSchedule() {
        return schedule;
    }

    public String getClassroom() {
        return classroom;
    }
}
