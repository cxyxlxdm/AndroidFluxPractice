import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

/**
 * Created by Android Studio
 * User: liangzhitao
 * Date: 16/4/1
 * Time: 上午10:58
 * Description:
 */
public class FluxApplication extends Application {

    public final Context context = this;

    @Override
    public void onCreate() {
        super.onCreate();
        new Runnable() {
            @Override
            public void run() {
                // TODO: 16/4/1
            }
        }.run();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
