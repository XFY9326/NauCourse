package tool.xfy9326.naucourse.Activities;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import tool.xfy9326.naucourse.AsyncTasks.LevelExamAsync;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.Methods.NetMethod;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Utils.LevelExam;
import tool.xfy9326.naucourse.Views.LevelExamAdapter;

public class LevelExamActivity extends AppCompatActivity {
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    @Nullable
    private LevelExamAdapter levelExamAdapter;
    private int loadTime = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_exam);
        BaseMethod.getApp(this).setLevelExamActivity(this);
        ToolBarSet();
        ViewSet();
    }

    @Override
    protected void onDestroy() {
        BaseMethod.getApp(this).setLevelExamActivity(null);
        System.gc();
        super.onDestroy();
    }

    private void ToolBarSet() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void ViewSet() {
        recyclerView = findViewById(R.id.recyclerView_level_exam);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        swipeRefreshLayout = findViewById(R.id.swipeLayout_level_exam);
        swipeRefreshLayout.setDistanceToTriggerSync(200);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (NetMethod.isNetworkConnected(LevelExamActivity.this)) {
                    getData();
                } else {
                    Snackbar.make(findViewById(R.id.layout_level_exam_content), R.string.network_error, Snackbar.LENGTH_SHORT).show();
                    swipeRefreshLayout.post(new Runnable() {
                        @Override
                        public void run() {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                }
            }
        });
        if (loadTime == 0) {
            getData();
        }
    }

    public void setLevelExam(@Nullable LevelExam levelExam) {
        if (levelExam != null) {
            if (levelExam.getExamAmount() > 0) {
                if (levelExamAdapter == null) {
                    levelExamAdapter = new LevelExamAdapter(LevelExamActivity.this, levelExam);
                    recyclerView.setAdapter(levelExamAdapter);
                } else {
                    levelExamAdapter.updateData(levelExam);
                }
            } else if (loadTime > 1 || !NetMethod.isNetworkConnected(this)) {
                Snackbar.make(findViewById(R.id.layout_level_exam_content), R.string.level_exam_empty, Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private void getData() {
        new LevelExamAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getApplicationContext());
    }

    public int getLoadTime() {
        return loadTime;
    }

    public void setLoadTime(int loadTime) {
        this.loadTime = loadTime;
    }

    public void lastViewSet(Context context) {
        if (swipeRefreshLayout != null) {
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        }
        //离线数据加载完成，开始拉取网络数据
        if (loadTime == 1 && NetMethod.isNetworkConnected(context) && BaseMethod.isDataAutoUpdate(context)) {
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                }
            });
            getData();
        }
    }

}
