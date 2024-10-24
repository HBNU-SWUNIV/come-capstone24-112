package com.example.capstone4;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class WritePostActivity extends AppCompatActivity {

    private EditText etTitle, etContent;
    private Button btnComplete;
    private ImageButton btnClose;
    private TextView tvTitle;
    private int postId = -1;  // 수정할 글의 ID를 저장할 변수, 기본값은 -1 (새로운 글)
    private DatabaseHelper dbHelper; // dbHelper를 클래스 멤버로 선언

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);

        // View 요소들 초기화
        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        btnComplete = findViewById(R.id.btnComplete);
        btnClose = findViewById(R.id.btnClose);
        tvTitle = findViewById(R.id.tvTitle);

        // 닫기 버튼 클릭 시 동작 (현재 Activity 종료)
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();  // 창 닫기 (뒤로가기 효과와 동일)
            }
        });

        // Intent로 수정할 게시글 정보가 전달되었는지 확인
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("postId")) {
            // 전달된 기존 게시글 정보를 가져와서 EditText에 표시
            postId = intent.getIntExtra("postId", -1);
            String title = intent.getStringExtra("title");
            String content = intent.getStringExtra("content");

            // EditText에 기존 글의 제목과 내용 표시
            etTitle.setText(title);
            etContent.setText(content);
            tvTitle.setText("글 수정하기");  // 상단 타이틀을 "글 수정하기"로 변경
        } else {
            tvTitle.setText("새 글 작성");  // 새 글 작성일 경우 타이틀 설정
        }

        // 완료 버튼 클릭 시 동작
        btnComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = etTitle.getText().toString().trim();
                String content = etContent.getText().toString().trim();

                // 데이터베이스에 글 저장
                //DatabaseHelper dbHelper = new DatabaseHelper(WritePostActivity.this);
                //dbHelper.addPost(title, content);

                // 제목과 내용이 모두 입력되었는지 확인
                if (title.isEmpty()) {
                    Toast.makeText(WritePostActivity.this, "제목을 입력하세요.", Toast.LENGTH_SHORT).show();
                } else if (content.isEmpty()) {
                    Toast.makeText(WritePostActivity.this, "내용을 입력하세요.", Toast.LENGTH_SHORT).show();
                } else {
                    DatabaseHelper dbHelper = new DatabaseHelper(WritePostActivity.this);
                    if (postId == -1) {
                        // 새로운 글 작성
                        dbHelper.addPost(title, content);
                        Toast.makeText(WritePostActivity.this, "글이 작성되었습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        // 기존 글 수정
                        dbHelper.updatePost(postId, title, content);
                        Toast.makeText(WritePostActivity.this, "글이 수정되었습니다.", Toast.LENGTH_SHORT).show();
                    }

                    // 완료 후 목록으로 돌아가기
                    Intent resultIntent = new Intent(WritePostActivity.this, BoardListActivity.class);
                    startActivity(resultIntent);
                    finish();  // 현재 창 닫기
                }
            }
        });
    }
}
