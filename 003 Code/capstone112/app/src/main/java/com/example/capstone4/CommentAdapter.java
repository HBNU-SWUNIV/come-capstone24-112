package com.example.capstone4;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.capstone4.Comment;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> commentList;
    private Context context;

    // 생성자
    public CommentAdapter(List<Comment> commentList, Context context) {
        this.commentList = commentList;
        this.context = context;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        holder.tvUsername.setText(comment.getUsername());
        holder.tvComment.setText(comment.getCommentText());
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    // ViewHolder 클래스
    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvComment;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvComment = itemView.findViewById(R.id.tvComment);
        }
    }

    // 댓글 리스트를 갱신하는 메서드
    public void updateComments(List<Comment> comments) {
        this.commentList = comments;
        notifyDataSetChanged();
    }
}
