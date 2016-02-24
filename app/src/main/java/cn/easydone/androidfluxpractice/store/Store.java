package cn.easydone.androidfluxpractice.store;

import org.greenrobot.eventbus.EventBus;

import cn.easydone.androidfluxpractice.dispatcher.Dispatcher;

/**
 * Created by Android Studio
 * User: liangzhitao
 * Date: 2016-02-24
 * Time: 12:26
 */
public abstract class Store<T> {
    private EventBus eventBus;

    protected Store() {
        eventBus = EventBus.getDefault();
    }

    public void register(Object subscriber) {
        eventBus.register(subscriber);
    }

    public void unRegister(Object subscriber) {
        eventBus.unregister(subscriber);
    }

    //处理接收到不同的事件
    public abstract void onAction(T action);

    protected abstract StoreChangeEvent changeEvent();

    //发送更新UI的事件给View
    protected void post() {
        StoreChangeEvent storeChangeEvent = changeEvent();
        if (storeChangeEvent != null) {
            eventBus.post(storeChangeEvent);
        }
    }

    public class StoreChangeEvent {
    }
}
