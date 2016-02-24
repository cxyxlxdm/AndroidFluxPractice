package cn.easydone.androidfluxpractice.request;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Android Studio
 * User: Ailurus(ailurus@foxmail.com)
 * Date: 2016-01-07
 * Time: 08:59
 */
public class GitHubApiUtils {

    private static GitHubApiUtils mInstance;
    private GitHubApi gitHubApi;

    private GitHubApiUtils() {
        /* JSON 解析 */
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient())
                .build();

        gitHubApi = retrofit.create(GitHubApi.class);
    }

    /* 单例 */
    public static GitHubApiUtils getInstance() {
        if (mInstance == null) {
            synchronized (GitHubApiUtils.class) {
                if (mInstance == null) {
                    mInstance = new GitHubApiUtils();
                }
            }
        }
        return mInstance;
    }

    public GitHubApi getGitHubApi() {
        return gitHubApi;
    }

    private OkHttpClient okHttpClient() {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder builder = new OkHttpClient.Builder().addInterceptor(logging).addInterceptor(headerInterceptor);

        return builder.build();
    }

    /* header */
    Interceptor headerInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request original = chain.request();

            Request request = original.newBuilder()
                    .addHeader("User-Agent", "Test")
                    .addHeader("Accept", "application/vnd.github.v3+json")
//                .addHeader("Authorization", "")
                    .method(original.method(), original.body())
                    .build();

            return chain.proceed(request);
        }
    };
}
