package cn.easydone.androidfluxpractice.action;

import java.util.ArrayList;
import java.util.List;

import cn.easydone.androidfluxpractice.bean.User;

/**
 * Created by Android Studio
 * User: liangzhitao
 * Date: 2016-02-24
 * Time: 12:34
 */
public class UserAction extends Action {

    public static final String INIT_RECYCLER_VIEW = "initRecyclerView";
    public static final String FETCH_DATA_ERROR = "fetchDataError";

    public UserAction(String type, Object data) {
        super(type, data);
    }

    public List<User> userList = new ArrayList<>();
}
