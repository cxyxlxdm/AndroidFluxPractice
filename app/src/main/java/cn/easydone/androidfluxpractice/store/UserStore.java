package cn.easydone.androidfluxpractice.store;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import cn.easydone.androidfluxpractice.action.UserAction;
import cn.easydone.androidfluxpractice.bean.User;

/**
 * Created by Android Studio
 * User: liangzhitao
 * Date: 2016-02-24
 * Time: 13:18
 */
public class UserStore extends Store<UserAction> {

    private static UserStore userStore;
    private List<User> userList;
    private Throwable throwable;
    private StoreChangeEvent changeEvent;

    public static UserStore getInstance() {
        if (userStore == null) {
            synchronized (UserStore.class) {
            }
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

    public Throwable getThrowable() {
        return throwable;
    }

    @Subscribe
    @Override
    public void onAction(final UserAction action) {
        switch (action.getType()) {
            case UserAction.LOADING_START:
                changeEvent = new LoadingStartChangeEvent();
                post();
                break;
            case UserAction.INIT_RECYCLER_VIEW:
                changeEvent = new InitRecyclerViewChangeEvent();
                userList = (List<User>) action.getData();
                post();
                break;
            case UserAction.FETCH_DATA_ERROR:
                changeEvent = new ErrorChangeEvent();
                throwable = (Throwable) action.getData();
                post();
                break;
        }
    }

    @Override
    protected StoreChangeEvent changeEvent() {
        return changeEvent;
    }

    public class LoadingStartChangeEvent extends StoreChangeEvent {}

    public class InitRecyclerViewChangeEvent extends StoreChangeEvent {}

    public class ErrorChangeEvent extends StoreChangeEvent {}
}
