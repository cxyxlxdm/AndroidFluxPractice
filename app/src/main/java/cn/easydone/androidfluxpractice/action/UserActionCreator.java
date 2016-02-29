package cn.easydone.androidfluxpractice.action;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

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
 * Time: 12:45
 */
public class UserActionCreator extends ActionCreator {

    private static UserActionCreator userActionCreator;

    public static UserActionCreator getInstance(Dispatcher dispatcher) {
        if (userActionCreator == null) {
            synchronized (UserActionCreator.class) {
                if (userActionCreator == null) {
                    userActionCreator = new UserActionCreator(dispatcher);
                }
            }
        }
        return userActionCreator;
    }

    protected UserActionCreator(Dispatcher dispatcher) {
        super(dispatcher);
    }


    public void fetechData(List<String> users) {
        dispatcher.dispatch(new UserAction(UserAction.LOADING_START, null));
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
                        dispatcher.dispatch(new UserAction(UserAction.INIT_RECYCLER_VIEW, users));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        dispatcher.dispatch(new UserAction(UserAction.FETCH_DATA_ERROR, throwable));
                    }
                });
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
