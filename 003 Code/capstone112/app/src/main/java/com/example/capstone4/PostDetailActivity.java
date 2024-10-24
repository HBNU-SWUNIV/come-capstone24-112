package com.example.capstone4;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PostDetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvContent;
    private int postId; // 글 ID를 저장
    private DatabaseHelper dbHelper;
    private EditText etComment;
    private ImageButton btnSendComment;
    private RecyclerView rvComments;
    private CommentAdapter commentAdapter;
    private CheckBox checkBoxAnonymous;  // CheckBox 멤버 변수 선언

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        dbHelper = new DatabaseHelper(this);

        tvTitle = findViewById(R.id.tvTitleDetail);
        tvContent = findViewById(R.id.tvContentDetail);
        etComment = findViewById(R.id.etComment);
        btnSendComment = findViewById(R.id.btnSendComment);
        rvComments = findViewById(R.id.recyclerViewComments);
        checkBoxAnonymous = findViewById(R.id.checkBoxAnonymous);  // CheckBox 초기화

        // 댓글 RecyclerView 초기화
        rvComments.setLayoutManager(new LinearLayoutManager(this));

        // Intent로 전달된 데이터 받아서 표시
        String title = getIntent().getStringExtra("title");
        String content = getIntent().getStringExtra("content");
        postId = getIntent().getIntExtra("postId", -1);

        tvTitle.setText(title);
        tvContent.setText(content);

        // 댓글 불러오기
        loadComments();
        // 댓글 보내기 버튼 클릭 시
        btnSendComment.setOnClickListener(v -> {
            String commentText = etComment.getText().toString().trim();

            if (!commentText.isEmpty()) {
                // 익명 댓글 체크박스 확인 후 사용자 이름 설정
                String username = checkBoxAnonymous.isChecked() ? "익명" : "사용자 이름";

                // 댓글을 DB에 추가
                dbHelper.addComment(postId, username, commentText);
                etComment.setText("");  // 댓글 입력란 초기화

                // 댓글 목록 다시 불러오기
                loadComments();
            } else {
                Toast.makeText(PostDetailActivity.this, "댓글을 입력하세요.", Toast.LENGTH_SHORT).show();
            }
        });

        // 옵션 버튼 (점 3개) 정의
        ImageButton btnOptions = findViewById(R.id.btnOptions);

        // postId를 받아오기 (이 부분 추가)
        postId = getIntent().getIntExtra("postId", -1);  // postId를 받음
        if (postId == -1) {
            Toast.makeText(this, "Post ID를 받지 못했습니다.", Toast.LENGTH_SHORT).show();
            finish();  // postId가 없으면 액티비티를 종료
        }

        // 클릭 시 팝업 메뉴 표시
        btnOptions.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(PostDetailActivity.this, v);
            popupMenu.getMenuInflater().inflate(R.menu.menu_post_options, popupMenu.getMenu());

            // 메뉴 항목 클릭 이벤트 처리
            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.action_edit) {
                    // 수정 버튼 클릭 시 해당 액티비티로 이동
                    Toast.makeText(PostDetailActivity.this, "수정 클릭됨", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(PostDetailActivity.this, WritePostActivity.class);
                    intent.putExtra("postId", postId);  // 수정할 글 ID 전달
                    intent.putExtra("title", tvTitle.getText().toString());  // 제목 전달
                    intent.putExtra("content", tvContent.getText().toString());  // 내용 전달
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.action_delete) {
                    // 삭제 버튼 클릭 시 Toast로 알림 표시
                    Toast.makeText(PostDetailActivity.this, "삭제 클릭됨", Toast.LENGTH_SHORT).show();
                    showDeleteConfirmationDialog();
                    // 삭제 처리 코드 추가
                    return true;
                }
                return false;
            });

            popupMenu.show();
        });

    }

    // 삭제 확인 대화상자를 띄우는 메서드
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(PostDetailActivity.this);
        builder.setMessage("삭제하시겠습니까?");
        builder.setPositiveButton("확인", (dialog, which) -> {
            // 데이터베이스에서 글 삭제
            dbHelper.deletePost(postId);

            // 글 삭제 후 BoardListActivity로 돌아가기
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);  // 삭제되었음을 BoardListActivity에 알림
            finish(); // 현재 Activity 종료
        });
        builder.setNegativeButton("취소", (dialog, which) -> {
            dialog.dismiss(); // 취소하면 대화상자 닫기
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // 게시글 삭제 메서드
    private void deletePost() {
        // DB에서 삭제
        dbHelper.deletePost(postId);

        // 삭제된 게시글 반영을 위해 결과값 반환
        Intent resultIntent = new Intent();
        resultIntent.putExtra("deletedPostId", postId);
        setResult(RESULT_OK, resultIntent);
        finish();  // 현재 액티비티 종료
    }

    // 댓글 불러오는 메서드
    private void loadComments() {
        List<Comment> comments = dbHelper.getCommentsForPost(postId);  // List<Comment> 타입으로 수정
        commentAdapter = new CommentAdapter(comments, this);  // 어댑터에 전달
        rvComments.setAdapter(commentAdapter);
    }



}
