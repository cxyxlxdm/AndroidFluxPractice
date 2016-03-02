package cn.easydone.androidfluxpractice.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import cn.easydone.androidfluxpractice.R;
import cn.easydone.androidfluxpractice.action.UserActionCreator;
import cn.easydone.androidfluxpractice.bean.User;
import cn.easydone.androidfluxpractice.dispatcher.Dispatcher;
import cn.easydone.androidfluxpractice.store.UserStore;
import fr.castorflex.android.circularprogressbar.CircularProgressBar;

public class MainActivity extends AppCompatActivity {

    private UserActionCreator userActionCreator;
    private UserStore userStore;
    private UserAdapter userAdapter;
    private Dispatcher dispatcher;
    private List<User> userList;
    private CircularProgressBar progressBar;

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
        progressBar = (CircularProgressBar) findViewById(R.id.progress_bar);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        List<String> users = new ArrayList<>();
        users.add("liangzhitao");
        users.add("AlanCheen");
        users.add("yongjhih");
        users.add("zzz40500");
        users.add("greenrobot");
        users.add("nimengbo");

        userActionCreator.fetechData(users);
        userList = userStore.getUserList();
        userAdapter = new UserAdapter(userList, MainActivity.this);
        recyclerView.setAdapter(userAdapter);
    }

    @Subscribe
    public void onLoadStartChangeEvent(UserStore.LoadingStartChangeEvent changeEvent) {
        if (changeEvent != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    @Subscribe
    public void onInitRecyclerViewChangeEvent(UserStore.InitRecyclerViewChangeEvent changeEvent) {
        progressBar.setVisibility(View.GONE);
        if (changeEvent != null) {
            userList = userStore.getUserList();
            userAdapter.refreshUi(userList);
        }
    }

    @Subscribe
    public void onErrorChangeEvent(UserStore.ErrorChangeEvent changeEvent) {
        progressBar.setVisibility(View.GONE);
        if (changeEvent != null) {
            Throwable throwable = userStore.getThrowable();
            Toast.makeText(MainActivity.this, throwable.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        userStore.unRegister(this);
        dispatcher.unRegister(userStore);
    }
}
