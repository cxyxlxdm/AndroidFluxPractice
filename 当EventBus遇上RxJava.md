##引言
接触过 `EventBus` 和 `RxJava` 的都知道，可以用 `RxJava` 来实现 `EventBus`，网上随便一搜，就可以拿得到代码。但是究竟为什么可以这么做？却没有类似的文章作进一步的深度解析。（本文假定读者都已经了解 `EventBus` 和 `RxJava` 是什么，可以做什么。）

```Java
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

    private  <T> Observable<T> toObservable(final Class<T> type) {
        return subject.ofType(type);
    }

    public boolean hasObservers() {
        return subject.hasObservers();
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

更重要的一点，`RxBus` 的重点应该在 `Bus` 上，而不是 `RxJava` 上。用 `RxJava` 去实现 `EventBus` 的思想。因此，应该把分析 `EventBus` 作为一个重点。

我们来看看要实现一个 `EventBus` 需要满足什么条件。

1. 获取一个 `EventBus` 实例，可以用单例，也可以用 `Builder`；
2. 注册 `EventBus` 和取消注册 `EventBus`；
3. 发送和接收事件的方法。

##`RxJava` && `EventBus`
要实现 `EventBus` 需要满足的条件，在 `RxJava` 里是如何体现的呢？

首先我们需要明确的是，`EventBus` 里都有哪些角色：`Event`、`Subscriber`、`Publisher`，也就是说需要`Event`、`Observer`、`Observable`，Event 自不必说，在 `RxJava` 里既能充当`Observer`，又能充当`Observable`的对象就是 `Subject`，而 `Subject` 是线程非安全的，我们要构造一个线程安全的 `Subject` ，需要用到它的子类 `SerializedSubject`，而实际使用的时候，我们的观察者只需要订阅发生之后的，来自 `Observable` 的数据，因此还需要给 `SerializedSubject` 传入 `PublishSubject` 作为参数。

1. 获取 `Bus` 实例，一个单例即可，当然，`EventBus` 还提供了使用 `Builder` 创建实例的方法，可根据具体情况自行实现；

	```Java
	private static volatile RxBus instance;
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
	```
2. 注册和取消注册 `Bus`;

	`EventBus` 的注册过程，就是对接收某个事件的所有方法进行 `subscribe()` ，在 `subscribe()` 方法里拿到这些的方法，把这些方法存进 `subscriberMethods`（一个 List 集合）中，然后把事件的类名作为 key ，把 `subscriberMethods` 作为 value ，存进 `methodCache`（一个 HashMap 缓存）里，这样就不用每次都去反射了。这里需要注意一点，`EventBus` 里用 `methodCache` cache 下来的不是 `Observer` ，也不是 `Observable` ，而是 `Observable.subscribe(Observer)`，即 `Subscription` ，那么如果用 `RxJava` ，该怎么去实现这么个功能呢？在 RxJava 里有这样一个类 `CompositeSubscription` ，对应的是一个存储着 `Subscription` 的 `HashSet`，因此我们只需要将接收事件的方法 add 进一个 `CompositeSubscription` ，在生命周期结束的时候，再把 `CompositeSubscription` 取消订阅即可。

	明确了上面的流程，对 RxJava 的封装就好办了。我们只需要获取 `Subscription` 即可。注意，这里跟 `EventBus` 是有区别的，`EventBus` 的封装，是通过反射，获取所有接收事件的方法，然后注册，当然，现在的 `EventBus` 版本里这些反射几乎对性能没有任何影响了。现在我们用 `RxJava` 是不是也要用反射再去获取所有的 `Subscription` 呢？当然不是，EventBus 的机制其实类似于广播，在接收事件的地方是没有方法调用的，因此需要反射。但是 `RxBus` 则提供了调用接收事件的方法，因此只需要在 `Activity` 或 `Fragment` 里 new 出来 `CompositeSubscription` 对象，然后在需要接收事件的地方，用 `CompositeSubscription` 对象去 add 进对应的 `Subscription` 就可以了(这一点在下面发送接收事件一节还会提到)。

	对应的取消注册的过程就简单多了，在生命周期结束的地方，对 `CompositeSubscription` 取消注册即可，以避免内存泄露，而 `CompositeSubscription ` 的取消注册方法是可以自动取消 `HashSet` 里的所有 `Subscription` 的，因此无须对每个 `Subscription` 单独处理。

3. 发送和接收事件。

	`EventBus` 发送事件，就是 post 方法，在 `EventBus` 里有一个内部类 `PostingThreadState`， 通过 `postingState.eventQueue` 可以获取一个 List 集合，只要 `eventQueue` 不为空，就不断地从 `eventQueue` 里取出事件（当然，伴随有是否为主线程，是否正在发送等状态的判断），然后调用 `postSingleEvent` 方法，最后调 `postToSubscription` 把事件发出去，post 一个，就从 `eventQueue` 里 remove 一个，最终又来到了我们从刚接触 Android 就让人很头痛的 `Handler` ，这是一个叫 `HandlerPoster` 的类。说一千，道一万，对应的 `RxJava` 处理事件就是调方法 `onNext`。这样代码就有了。

	```Java
	public void post(Object object) {
        subject.onNext(object);
	}
	```

	`EventBus` 接收事件需要通过 `onEvent` 开头的方法来遍历获取，第一次遍历会缓存，仅查找 `onEvent` 开头的方法，同时忽略一些特定 SDK 的方法，可以提高一些效率。在使用 `RxJava` 接收事件的时候，根据传递的事件类型(eventType)可以获取对应类型的 `Observable<EventType>` ，那么问题就来了，在这里我们是不是要提供一个返回对应的 `Subscription` 的方法呢？答案是不能！因为我知道，接收事件处理事件是有可能在不同的线程里的，如果在这里我们就提供一个返回 `Subscription` 的方法，那后续的事件处理是在哪个线程呢？在这里就指定了 UI 线程或者异步线程，后面的具体的事件处理就可能会有问题。因此我们只需在需要接收事件的地方，调用方法即可，然后指定线程就可以了。这也是相对于 `Otto` 的一个优势。

    ```Java
	public <T> Observable<T> toObservable(final Class<T> type) {
    	return subject.ofType(type);
	}
    ```

	在 `Activity` 或 `Fragment` 里再去获取 `Subscription` 。

    ```Java
    private <T> Subscription toSubscription(Class<T> type, Action1<T> action1) {
        return RxBus.getInstance()
                .toObservable(type)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(action1);
    }
    ```

	最后，非要纠结 `EventBus` 的注册的话，将所有的 `Subscription` add 进 `CompositeSubscription` 就好了。最后，一定不要忘记对 `CompositeSubscription` 取消注册。

------
具体使用可以参照[AndroidFluxPractice](https://github.com/liangzhitao/AndroidFluxPractice)，Sample 里将 EventBus 替换为了 RxBus ，完美地实现了一模一样的效果。

参考：[http://www.jianshu.com/p/ca090f6e2fe2/](http://www.jianshu.com/p/ca090f6e2fe2/)





