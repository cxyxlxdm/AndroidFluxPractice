package cn.easydone.androidfluxpractice.request;

import cn.easydone.androidfluxpractice.bean.GitHubUser;
import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by Android Studio
 * User: Ailurus(ailurus@foxmail.com)
 * Date: 2016-01-07
 * Time: 08:32
 */
public interface GitHubApi {

    @GET("users/{user}")
    Observable<GitHubUser> user(
            @Path("user") String user);

}
