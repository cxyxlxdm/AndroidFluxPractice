package cn.easydone.androidfluxpractice.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import cn.easydone.androidfluxpractice.R;
import cn.easydone.androidfluxpractice.bean.User;

/**
 * Created by Android Studio
 * User: Ailurus(ailurus@foxmail.com)
 * Date: 2016-01-09
 * Time: 10:14
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private List<User> users;
    private Context context;

    public UserAdapter(List<User> users, Context context) {
        this.users = users;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_github_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final User user = users.get(position);
        Glide.with(context)
                .load(user.getAvatarUrl())
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(holder.ivAvatar);
        holder.ivAvatar.setTag(R.id.iv_user_avatar, position);
        holder.tvBlog.setText(user.getBlog());
        holder.tvEmail.setText(user.getEmail());
        holder.tvUserName.setText(user.getName());
        holder.tvFollowerNum.setText(context.getString(R.string.follower_num, user.getFollowers()));
        holder.tvFollowingNum.setText(context.getString(R.string.following_num, user.getFollowing()));
        holder.tvPubReposNum.setText(context.getString(R.string.pub_repos_num, user.getPublicRepos()));
        holder.tvPubGistsNum.setText(context.getString(R.string.pub_gists_num, user.getPublicGists()));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void refreshUi(List<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivAvatar;
        private final TextView tvUserName;
        private final TextView tvBlog;
        private final TextView tvEmail;
        private final TextView tvPubReposNum;
        private final TextView tvFollowerNum;
        private final TextView tvPubGistsNum;
        private final TextView tvFollowingNum;

        public ViewHolder(View itemView) {
            super(itemView);
            ivAvatar = (ImageView) itemView.findViewById(R.id.iv_user_avatar);
            tvUserName = (TextView) itemView.findViewById(R.id.tv_user_name);
            tvBlog = (TextView) itemView.findViewById(R.id.tv_blog);
            tvEmail = (TextView) itemView.findViewById(R.id.tv_email);
            tvPubReposNum = (TextView) itemView.findViewById(R.id.tv_pub_repos_num);
            tvFollowerNum = (TextView) itemView.findViewById(R.id.tv_follower_num);
            tvPubGistsNum = (TextView) itemView.findViewById(R.id.tv_pub_gists_num);
            tvFollowingNum = (TextView) itemView.findViewById(R.id.tv_following_num);
        }
    }
}
