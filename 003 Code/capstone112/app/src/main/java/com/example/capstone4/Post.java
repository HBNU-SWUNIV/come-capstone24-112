package com.example.capstone4;

public class Post {

    private int id;  // id 필드 추가
    private String title;
    private String content;

    // 생성자
    public Post(int id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }

    // id getter
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}
