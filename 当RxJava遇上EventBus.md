##引言
接触过 `EventBus` 和 `RxJava` 的都知道，可以用 `RxJava` 来实现 `EventBus`，网上随便一搜，就可以拿得到代码。（本文假定读者都已经了解 `EventBus` 和 `RxJava` 是什么，可以做什么。）

```Java
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
```
可以看到，代码非常简练，当 `RxJava` 遇上 `EventBus` ，居然会如此神奇~那么问题来了，为什么这段代码就可以实现 `EventBus` 的功能？

要搞明白这几个问题，我们得弄清楚这些东西。

1. `EventBus` 是如何实现的？
2. `RxJava` 满足了实现 `EventBus` 的哪些条件？
3. 如何用 `RxJava` 去封装一个 `EventBus` ？

## `EventBus`工作流程
简单讲，事件总线，顾名思义，分为两个概念，一个事件，即 Event ，一个总线，即 Bus ，这是在整个 APP 里一种规范地传递事件的方式。作为独立于项目里各个模块的 Application 级别的存在，可以很好地用来程序的解耦。使用大致有四个步骤：注册→发送→接收→取消注册。具体的源码分析，可以参看 codeKK 上 [Trinea](http://www.trinea.cn/) 的 [EventBus源码解析](http://a.codekk.com/detail/Android/Trinea/EventBus%20%E6%BA%90%E7%A0%81%E8%A7%A3%E6%9E%90) 和 [kymjs张涛](http://www.kymjs.com/) 的 [EventBus源码研读](http://kymjs.com/column/resourcecode.html)。

我们来看看要实现一个 `EventBus` 需要满足什么条件。

1. 获取一个 `EventBus` 实例，可以用单例，也可以用 `Builder`；
2. 注册 `EventBus` 和取消注册 `EventBus`；
3. 发送和接收事件的方法。

##`RxJava` VS `EventBus`
要实现 `EventBus` 需要满足的条件，在 `RxJava` 里是如何体现的呢？

首先我们需要明确的是，`EventBus` 里都有哪些角色：`Event`、`Subscriber`、`Publisher`，也就是说需要`Event`、`Observer`、`Observable`，Event 自不必说，在 `RxJava` 里既能充当`Observer`，又能充当`Observable`的对象就是 `Subject`，而 `Subject` 是线程非安全的，我们要构造一个线程安全的 `Subject` ，需要用到它的子类 `SerializedSubject`，而实际使用的时候，我们的观察者只需要订阅发生之后的，来自 `Observable` 的数据，因此还需要给 `SerializedSubject` 传入的对象应该是 `PublishSubject`。

1. 获取 `Bus` 实例，一个单例即可，当然，`EventBus` 还提供了使用 `Builder` 创建实例的方法，可根据具体情况自行实现；
 
	```Java
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
	```
2. 注册和取消注册 `Bus`;

	`EventBus` 的注册过程，就是对发送某个事件的所有方法进行 `subscribe()` ，在 `subscribe()` 方法里拿到这些的方法，把这些方法存进 `subscriberMethods`（一个 List 集合）中，然后把事件的类名作为 key ，把 `subscriberMethods` 作为 value ，存进 `methodCache`（一个 HashMap 缓存）里。
	
	明确了上面的流程，对 RxJava 的封装就好办了。

	```Java
    private ConcurrentHashMap<Object, List<Subject>> methodCache = new ConcurrentHashMap<>();

    public void register(Object tag) {
        List<Subject> subjectList = methodCache.get(tag);
        if (null == subjectList) {
            subjectList = new ArrayList<>();
            methodCache.put(tag, subjectList);
        }
        subjectList.add(new SerializedSubject<>(PublishSubject.create()));
    }
	```
	对应的取消注册的过程就简单多了，清空 List 和 Map 就可以了。
	
	```Java
	public void unregister(Object tag, Subject subject) {
	    List<Subject> subjectList = methodCache.get(tag);
	    if (null != subjectList && subjectList.size() > 0) {
	        subjectList.remove(subject);
	        if (subjectList.size() == 0) {
	            methodCache.remove(tag);
	        }
	    }
	}
	```
	
3. 发送和接收事件。

	`EventBus` 发送事件，就是 post 方法，只要 `eventQueue` 不为空，就不断地从 `eventQueue` 里取出事件，然后调用 `postSingleEvent` 方法，最后调 `postToSubscription` 把事件发出去。对应的 `RxJava` 处理事件就是调方法 `onNext`（当然，许多细节可能不如 `EventBus` 处理的那么完善）。这样代码就有了。
	
	```Java
	public void post(Object tag, Object event) {
	    List<Subject> subjectList = methodCache.get(tag);
	
	    if (subjectList != null && subjectList.size() > 0) {
	        for (Subject subject : subjectList) {
	            subject.onNext(event);
	        }
	    }
	}
	```
	
	`EventBus` 接收事件需要通过 `onEvent` 开头的方法来遍历获取，第一次遍历会缓存，仅查找 `onEvent` 开头的方法，同时忽略一些特定 SDK 的方法，可以提高一些效率。在使用 `RxJava` 接收事件的时候，根据传递的事件类型返回特定类型(eventType)的被观察者就可以了，当然，被观察者可能是个集合。
	
	```Java
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
	```

------

最后，在生命周期结束的时候，处理一下 `RxJava` 的取消订阅就可以了，以保证不会引起内存泄露。




