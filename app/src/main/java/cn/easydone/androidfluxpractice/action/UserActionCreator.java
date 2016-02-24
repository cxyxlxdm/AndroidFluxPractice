package cn.easydone.androidfluxpractice.action;

import java.util.List;

import cn.easydone.androidfluxpractice.bean.User;
import cn.easydone.androidfluxpractice.dispatcher.Dispatcher;

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

    public void refreshUserList(List<User> users) {
        dispatcher.dispatch(new UserAction(UserAction.INIT_RECYCLER_VIEW, users));
    }

    public void loadError(Throwable e) {
        dispatcher.dispatch(new UserAction(UserAction.FETCH_DATA_ERROR, e));
    }
}
