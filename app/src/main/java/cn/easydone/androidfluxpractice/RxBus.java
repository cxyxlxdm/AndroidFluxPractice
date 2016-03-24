package cn.easydone.androidfluxpractice;


import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import rx.Observable;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * Created by Android Studio
 * User: liangzhitao
 * Date: 16/3/24
 * Time: 上午10:36
 * Description:
 */

public class RxBus {

    private static volatile RxBus instance;

    private RxBus() {}

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

    private ConcurrentHashMap<Object, List<Subject>> methodCache = new ConcurrentHashMap<>();

    public void register(Object tag) {
        List<Subject> subjectList = methodCache.get(tag);
        if (null == subjectList) {
            subjectList = new ArrayList<>();
            methodCache.put(tag, subjectList);
        }
        subjectList.add(new SerializedSubject<>(PublishSubject.create()));
    }

    public void unegister(Object tag, Observable observable) {
        List<Subject> subjectList = methodCache.get(tag);
        if (null != subjectList && subjectList.size() > 0) {
            subjectList.remove(observable);
            if (subjectList.size() == 0) {
                methodCache.remove(tag);
            }
        }
    }

    public void post(Object tag, Object event) {
        List<Subject> subjectList = methodCache.get(tag);

        if (subjectList != null && subjectList.size() > 0) {
            for (Subject subject : subjectList) {
                subject.onNext(event);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> List<Observable<T>> toObserverableList(Object tag, final Class<T> eventType) {
        List<Subject> subjectList = methodCache.get(tag);
        List<Observable<T>> observables = new ArrayList<>();
        for (Subject subject : subjectList) {
            Observable<T> observable = ((Observable<T>)subject).filter(new Func1<T, Boolean>() {
                @Override
                public Boolean call(T t) {
                    return eventType.isInstance(t);
                }
            }).cast(eventType);
            observables.add(observable);
        }
        return observables;
    }
}

