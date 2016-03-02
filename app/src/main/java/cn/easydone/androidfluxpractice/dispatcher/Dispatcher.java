package cn.easydone.androidfluxpractice.dispatcher;

import java.util.ArrayList;
import java.util.List;

import cn.easydone.androidfluxpractice.action.Action;
import cn.easydone.androidfluxpractice.store.Store;

/**
 * Created by Android Studio
 * User: liangzhitao
 * Date: 2016-02-24
 * Time: 12:14
 */
public class Dispatcher {

    private static Dispatcher instance;
    private List<Store> stores;

    private Dispatcher() {
        stores = new ArrayList<>();
    }

    public static Dispatcher getInstance() {
        if (instance == null) {
            synchronized (Dispatcher.class) {
                instance = new Dispatcher();
            }
        }
        return instance;
    }

    public void register(Store store) {
        stores.add(store);
    }

    public void unRegister(Store store) {
        stores.remove(store);
    }

    public void dispatch(Action action) {
        post(action);
    }

    private void post(final Action action) {
        for (Store store : stores) {
            store.onAction(action);
        }
    }
}
