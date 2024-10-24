package com.example.capstone4;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.List;

public class BoardListActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_DETAIL = 1; // 요청 코드 정의
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private DatabaseHelper dbHelper;
    private ExtendedFloatingActionButton fabWritePost; // FAB 버튼 선언
    private static final int REQUEST_CODE_DELETE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_list);

        dbHelper = new DatabaseHelper(this);

        recyclerView = findViewById(R.id.recyclerViewPosts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fabWritePost = findViewById(R.id.fabWritePost); // FAB 버튼 초기화
        fabWritePost.setOnClickListener(v -> {
            // 글쓰기 화면으로 이동
            Intent intent = new Intent(BoardListActivity.this, WritePostActivity.class);
            startActivity(intent);
        });

        // DB에서 모든 게시글 불러오기
        loadPosts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Activity가 재개될 때마다 글 목록을 갱신
        loadPosts();
    }

    private void loadPosts() {
        postList = dbHelper.getAllPosts();
        postAdapter = new PostAdapter(postList, this);
        recyclerView.setAdapter(postAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_DELETE && resultCode == RESULT_OK && data != null) {
            int deletedPostId = data.getIntExtra("deletedPostId", -1);
            Log.d("BoardListActivity", "Deleted Post ID: " + deletedPostId);  // 로그 추가
            if (deletedPostId == -1) {
                Log.e("BoardListActivity", "Deleted Post ID is missing.");
            } else {
                // 삭제된 게시글의 위치를 찾아서 어댑터에서 제거
                for (int i = 0; i < postList.size(); i++) {
                    if (postList.get(i).getId() == deletedPostId) {
                        postAdapter.removeItem(i);
                        break;
                    }
                }
            }
        }
    }

}
