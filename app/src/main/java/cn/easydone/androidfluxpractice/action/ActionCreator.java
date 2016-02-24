package cn.easydone.androidfluxpractice.action;

import cn.easydone.androidfluxpractice.dispatcher.Dispatcher;

/**
 * Created by Android Studio
 * User: liangzhitao
 * Date: 2016-02-24
 * Time: 12:14
 */
public class ActionCreator {
    protected Dispatcher dispatcher;

    protected ActionCreator(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }
}
