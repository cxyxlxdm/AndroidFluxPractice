package cn.easydone.androidfluxpractice.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import org.greenrobot.eventbus.Subscribe;

import cn.easydone.androidfluxpractice.R;
import cn.easydone.androidfluxpractice.action.UserActionCreator;
import cn.easydone.androidfluxpractice.dispatcher.Dispatcher;
import cn.easydone.androidfluxpractice.store.Store;
import cn.easydone.androidfluxpractice.store.UserStore;

public class MainActivity extends AppCompatActivity {

    private UserActionCreator userActionCreator;
    private UserStore userStore;
    private UserAdapter userAdapter;
    private Dispatcher dispatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initDependencies();
        setupView();
    }

    private void initDependencies() {
        userStore = UserStore.getInstance();
        userStore.register(this);
        dispatcher = Dispatcher.getInstance();
        dispatcher.register(userStore);
        userActionCreator = UserActionCreator.getInstance(dispatcher);
    }

    private void setupView() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        userActionCreator.refreshUserList(userStore.getUserList());
        userAdapter = new UserAdapter(userStore.getUserList(), MainActivity.this);
        recyclerView.setAdapter(userAdapter);
    }

    @Subscribe
    public void onStoreChange(Store.StoreChangeEvent changeEvent) {
        if (changeEvent != null) {
            userActionCreator.refreshUserList(userStore.getUserList());
            userAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        userStore.unRegister(this);
        dispatcher.unRegister(userStore);
    }
}
