package com.example.capstone4;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "postDB";
    private static final int DATABASE_VERSION = 2;

    // 테이블 이름과 열 정의
    public static final String TABLE_POSTS = "posts";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CONTENT = "content";

    // 댓글 테이블 정의
    public static final String TABLE_COMMENTS = "comments";
    public static final String COLUMN_COMMENT_ID = "comment_id";
    public static final String COLUMN_POST_ID = "post_id";  // 댓글이 연결된 글 ID
    public static final String COLUMN_COMMENT = "comment";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 게시글 테이블 생성
        String createPostTable = "CREATE TABLE " + TABLE_POSTS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_CONTENT + " TEXT)";
        db.execSQL(createPostTable);

        // 댓글 테이블 생성
        String createCommentTable = "CREATE TABLE " + TABLE_COMMENTS + " (" +
                COLUMN_COMMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_POST_ID + " INTEGER, " +
                "username TEXT, " +  // username 필드 추가
                COLUMN_COMMENT + " TEXT, " +
                "FOREIGN KEY (" + COLUMN_POST_ID + ") REFERENCES " + TABLE_POSTS + "(" + COLUMN_ID + "))";
        db.execSQL(createCommentTable);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_POSTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMMENTS);  // 댓글 테이블 삭제
        onCreate(db);
    }

    // 글 삽입 메서드
    public void addPost(String title, String content) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_CONTENT, content);

        db.insert(TABLE_POSTS, null, values);
        db.close();
    }

    // 모든 글 가져오기 메서드
    public List<Post> getAllPosts() {
        List<Post> postList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // SELECT 문을 통해 모든 게시글을 가져옴
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_POSTS, null);

        // 커서에서 데이터를 읽음
        if (cursor.moveToFirst()) {
            do {
                // getColumnIndexOrThrow()로 열이 존재하는지 확인
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                String content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT));
                Post post = new Post(id, title, content);
                postList.add(post);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return postList;
    }
    // 글 삭제 메서드 추가
    // 글 삭제 메서드
    public void deletePost(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_POSTS, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        Log.d("DatabaseHelper", "Rows Deleted: " + rowsDeleted + ", Post ID: " + id); // 삭제된 행 개수 및 ID 로그
        db.close();

    }


    // 글 수정 메서드
    public void updatePost(int id, String title, String content) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_CONTENT, content);

        // 해당 ID의 글을 업데이트
        db.update(TABLE_POSTS, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    // 댓글 추가 메서드 (username 포함)
    public void addComment(int postId, String username, String comment) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_POST_ID, postId);
        values.put("username", username);  // username 추가
        values.put(COLUMN_COMMENT, comment);

        db.insert(TABLE_COMMENTS, null, values);
        db.close();
    }

    // 특정 게시글의 모든 댓글 가져오기
    // DatabaseHelper.java에 있는 메서드 수정
    public List<Comment> getCommentsForPost(int postId) {
        List<Comment> commentList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // 쿼리를 통해 댓글을 가져옴
        Cursor cursor = db.rawQuery("SELECT username, comment FROM comments WHERE post_id=?", new String[]{String.valueOf(postId)});
        Log.d("DatabaseHelper", "Loaded comments for post ID: " + postId);

        if (cursor.moveToFirst()) {
            do {
                String username = cursor.getString(cursor.getColumnIndexOrThrow("username"));
                String commentText = cursor.getString(cursor.getColumnIndexOrThrow("comment"));
                Comment comment = new Comment(username, commentText);
                commentList.add(comment);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return commentList;
    }



}
