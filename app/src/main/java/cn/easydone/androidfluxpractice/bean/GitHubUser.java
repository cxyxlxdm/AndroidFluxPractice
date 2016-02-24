package cn.easydone.androidfluxpractice.bean;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Android Studio
 * User: Ailurus(ailurus@foxmail.com)
 * Date: 2016-01-07
 * Time: 08:58
 */
public class GitHubUser {
    public String name;
    public String email;
    @SerializedName("public_repos")
    public int publicRepos;
    @SerializedName("public_gists")
    public int publicGists;
    public String login;
    public int id;
    @SerializedName("avatar_url")
    public String avatarUrl;
    @SerializedName("gravatar_id")
    public String gravatarId;
    public String url;
    @SerializedName("html_url")
    public String htmlUrl;
    @SerializedName("followers_url")
    public String followersUrl;
    @SerializedName("following_url")
    public String followingUrl;
    @SerializedName("gists_url")
    public String gistsUrl;
    @SerializedName("starred_url")
    public String starredUrl;
    @SerializedName("subscriptions_url")
    public String subscriptionsUrl;
    @SerializedName("organizations_url")
    public String organizationsUrl;
    @SerializedName("repos_url")
    public String reposUrl;
    @SerializedName("events_url")
    public String eventsUrl;
    @SerializedName("received_events_url")
    public String receivedEventsUrl;
    public String type;
    @SerializedName("site_admin")
    public boolean siteAdmin;
    public String company;
    public String blog;
    public String location;
    public boolean hireable;
    public String bio;
    public int followers;
    public int following;
    @SerializedName("created_at")
    public String createdAt;
    @SerializedName("updated_at")
    public String updatedAt;
}
