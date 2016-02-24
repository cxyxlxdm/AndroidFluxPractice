package cn.easydone.androidfluxpractice.store;

import android.support.annotation.NonNull;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import cn.easydone.androidfluxpractice.action.UserAction;
import cn.easydone.androidfluxpractice.bean.GitHubUser;
import cn.easydone.androidfluxpractice.bean.User;
import cn.easydone.androidfluxpractice.dispatcher.Dispatcher;
import cn.easydone.androidfluxpractice.request.GitHubApiUtils;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Android Studio
 * User: liangzhitao
 * Date: 2016-02-24
 * Time: 13:18
 */
public class UserStore extends Store<UserAction> {

    private static UserStore userStore;
    private List<User> userList;

    public static UserStore getInstance() {
        if (userStore == null) {
            synchronized (UserStore.class) {}
            userStore = new UserStore();
        }
        return userStore;
    }

    protected UserStore() {
        userList = new ArrayList<>();
    }

    public List<User> getUserList() {
        return userList;
    }

    @Subscribe
    @Override
    public void onAction(final UserAction action) {
        List<String> users = new ArrayList<>();
        users.add("liangzhitao");
        users.add("AlanCheen");
        users.add("yongjhih");
        users.add("zzz40500");
        users.add("greenrobot");
        users.add("nimengbo");
        Observable.merge(getObservables(users))
                .buffer(users.size())
                .map(new Func1<List<GitHubUser>, List<User>>() {
                    @Override
                    public List<User> call(List<GitHubUser> gitHubUsers) {
                        return getUserList(gitHubUsers);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<User>>() {
                    @Override
                    public void call(List<User> users) {
                        if (action.getType().equals(UserAction.INIT_RECYCLER_VIEW)) {
                            userList.addAll(users);
                            action.setData(users);
                            post();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        if (action.getType().equals(UserAction.FETCH_DATA_ERROR)) {
                            action.setData(throwable);
                            post();
                        }
                    }
                });
    }

    @Override
    protected StoreChangeEvent changeEvent() {
        return new StoreChangeEvent();
    }

    @NonNull
    private List<User> getUserList(List<GitHubUser> gitHubUsers) {
        List<User> userList = new ArrayList<>();
        for (GitHubUser gitHubUser : gitHubUsers) {
            userList.add(getUser(gitHubUser));
        }
        return userList;
    }

    @NonNull
    private List<Observable<GitHubUser>> getObservables(List<String> users) {
        List<Observable<GitHubUser>> observableList = new ArrayList<>();
        for (String user : users) {
            Observable<GitHubUser> observable = GitHubApiUtils.getInstance().getGitHubApi().user(user);
            observableList.add(observable);
        }
        return observableList;
    }

    @NonNull
    private User getUser(GitHubUser gitHubUser) {
        User user = new User();
        user.setUrl(gitHubUser.url);
        user.setAvatarUrl(gitHubUser.avatarUrl);
        user.setName(gitHubUser.name);
        user.setBlog(gitHubUser.blog);
        user.setEmail(gitHubUser.email);
        user.setFollowers(gitHubUser.followers);
        user.setFollowing(gitHubUser.following);
        user.setPublicGists(gitHubUser.publicGists);
        user.setPublicRepos(gitHubUser.publicRepos);
        return user;
    }
}
