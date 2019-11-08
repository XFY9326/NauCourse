package tool.xfy9326.naucourse.activities.async;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.asyncTasks.LevelExamAsync;
import tool.xfy9326.naucourse.beans.exam.LevelExam;
import tool.xfy9326.naucourse.methods.BaseMethod;
import tool.xfy9326.naucourse.methods.net.NetMethod;
import tool.xfy9326.naucourse.views.recyclerAdapters.LevelExamAdapter;

public class LevelExamActivity extends BaseAsyncActivity {
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    @Nullable
    private LevelExamAdapter levelExamAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_exam);
        BaseMethod.getApp(this).setLevelExamActivity(this);
        toolBarSet();
        viewSet();
    }

    @Override
    protected void onDestroy() {
        BaseMethod.getApp(this).setLevelExamActivity(null);
        System.gc();
        super.onDestroy();
    }

    private void viewSet() {
        recyclerView = findViewById(R.id.recyclerView_level_exam);
        recyclerView.setFocusableInTouchMode(false);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        if (levelExamAdapter == null) {
            levelExamAdapter = new LevelExamAdapter(LevelExamActivity.this);
        }
        recyclerView.setAdapter(levelExamAdapter);

        swipeRefreshLayout = findViewById(R.id.swipeLayout_level_exam);
        swipeRefreshLayout.setDistanceToTriggerSync(200);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (NetMethod.isNetworkConnected(LevelExamActivity.this)) {
                getData();
            } else {
                Snackbar.make(findViewById(R.id.layout_level_exam_content), R.string.network_error, Snackbar.LENGTH_SHORT).show();
                swipeRefreshLayout.post(() -> swipeRefreshLayout.setRefreshing(false));
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
                if (levelExamAdapter != null && levelExamAdapter.getItemCount() != 0) {
                    levelExamAdapter.clearAdapter();
                }
            }
        }
    }

    @Override
    synchronized protected void getData() {
        BaseMethod.setRefreshing(swipeRefreshLayout, true);
        new LevelExamAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getApplicationContext());
    }

    @Override
    public void lastViewSet(Context context) {
        //离线数据加载完成，开始拉取网络数据
        if (loadTime == 1 && NetMethod.isNetworkConnected(context) && BaseMethod.isDataAutoUpdate(context)) {
            getData();
        } else {
            BaseMethod.setRefreshing(swipeRefreshLayout, false);
        }
    }

}
