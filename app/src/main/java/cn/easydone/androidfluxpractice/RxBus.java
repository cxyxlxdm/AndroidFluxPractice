package cn.easydone.androidfluxpractice;


import android.support.annotation.NonNull;

import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.functions.Action1;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;

/**
 * Created by Android Studio
 * User: liangzhitao
 * Date: 16/3/24
 * Time: 上午10:36
 */

public class RxBus {

    private static volatile RxBus instance;
    private final SerializedSubject<Object, Object> subject;

    private RxBus() {
        subject = new SerializedSubject<>(PublishSubject.create());
    }

    public static RxBus getInstance() {
        if (instance == null) {
            synchronized (RxBus.class) {
                if (instance == null) {
                    instance = new RxBus();
                }
            }
        }
        return instance;
    }

    public void post(Object object) {
        subject.onNext(object);
    }

    @NonNull
    public <T> Observable<T> toObservable(final Class<T> type) {
        return subject.ofType(type);
    }

    public boolean hasObservers() {
        return subject.hasObservers();
    }

    public <T> Subscription toSubscription(Class<T> type, Action1<T> action1, Scheduler scheduler) {
        return RxBus.getInstance()
                .toObservable(type)
                .observeOn(scheduler)
                .subscribe(action1);
    }
}

