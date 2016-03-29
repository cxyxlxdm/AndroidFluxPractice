package cn.easydone.androidfluxpractice.store;

import cn.easydone.androidfluxpractice.RxBus;

/**
 * Created by Android Studio
 * User: liangzhitao
 * Date: 2016-02-24
 * Time: 12:26
 */
public abstract class Store<T> {
    private RxBus rxBus;

    protected Store() {
        rxBus = RxBus.getInstance();
    }

    //处理接收到不同的事件
    public abstract void onAction(T action);

    protected abstract StoreChangeEvent changeEvent();

    //发送更新UI的事件给View
    protected void post() {
        StoreChangeEvent storeChangeEvent = changeEvent();
        if (storeChangeEvent != null && rxBus.hasObservers()) {
            rxBus.post(storeChangeEvent);
        }
    }

    public class StoreChangeEvent {}
}
