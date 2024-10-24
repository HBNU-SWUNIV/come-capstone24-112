package com.example.capstone4;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> postList;
    private Context context;
    // 상수 정의
    private static final int REQUEST_CODE_DELETE = 1001;  // 요청 코드 추가

    public PostAdapter(List<Post> postList, Context context) {
        this.postList = postList;
        this.context = context;
    }

    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PostViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.tvTitle.setText(post.getTitle());
        holder.tvContent.setText(post.getContent());

        // 항목 클릭 시 세부 화면으로 이동
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId", post.getId());  // postId 추가
                intent.putExtra("title", post.getTitle());
                intent.putExtra("content", post.getContent());
                //context.startActivity(intent);

                // PostDetailActivity로 이동하면서 결과를 받을 수 있도록 요청 코드 전달
                ((Activity) context).startActivityForResult(intent, REQUEST_CODE_DELETE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    // 항목을 삭제하는 메서드 추가
    public void removeItem(int position) {
        postList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, postList.size());
    }
    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent;

        public PostViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvContent = itemView.findViewById(R.id.tvContent);
        }
    }
}
