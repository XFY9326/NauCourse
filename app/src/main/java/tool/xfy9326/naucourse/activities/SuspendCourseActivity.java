package tool.xfy9326.naucourse.activities;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.asyncTasks.SuspendCourseAsync;
import tool.xfy9326.naucourse.methods.BaseMethod;
import tool.xfy9326.naucourse.methods.NetMethod;
import tool.xfy9326.naucourse.utils.SuspendCourse;
import tool.xfy9326.naucourse.views.recyclerAdapters.SuspendCourseAdapter;

public class SuspendCourseActivity extends AppCompatActivity {
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private SuspendCourseAdapter suspendCourseAdapter;
    private int loadTime = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suspend_course);
        BaseMethod.getApp(this).setSuspendCourseActivity(this);
        ToolBarSet();
        ViewSet();
    }

    private void ToolBarSet() {
        setSupportActionBar(findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void ViewSet() {
        recyclerView = findViewById(R.id.recyclerView_suspend_course_list);
        recyclerView.setFocusableInTouchMode(false);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        swipeRefreshLayout = findViewById(R.id.swipeLayout_suspend_course);
        swipeRefreshLayout.setDistanceToTriggerSync(200);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (NetMethod.isNetworkConnected(SuspendCourseActivity.this)) {
                getData();
            } else {
                Snackbar.make(findViewById(R.id.layout_suspend_course_content), R.string.network_error, Snackbar.LENGTH_SHORT).show();
                swipeRefreshLayout.post(() -> swipeRefreshLayout.setRefreshing(false));
            }
        });
        if (loadTime == 0) {
            getData();
        }
    }

    synchronized private void getData() {
        BaseMethod.setRefreshing(swipeRefreshLayout, true);
        if (loadTime == 0) {
            new SuspendCourseAsync().execute(getApplicationContext());
        } else {
            new SuspendCourseAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getApplicationContext());
        }
    }

    public int getLoadTime() {
        return loadTime;
    }

    public void setLoadTime(int loadTime) {
        this.loadTime = loadTime;
    }

    public void setSuspendCourse(SuspendCourse suspendCourse) {
        if (suspendCourse != null && suspendCourse.getTerm() != null) {
            ((TextView) findViewById(R.id.textView_suspend_course_title)).setText(getString(R.string.suspend_course_title, suspendCourse.getTerm()));
            if (suspendCourse.getCount() > 0) {
                if (suspendCourseAdapter == null) {
                    suspendCourseAdapter = new SuspendCourseAdapter(SuspendCourseActivity.this, suspendCourse);
                    recyclerView.setAdapter(suspendCourseAdapter);
                } else {
                    suspendCourseAdapter.updateSuspendCourse(suspendCourse);
                }
            } else if (loadTime > 1 || !NetMethod.isNetworkConnected(this)) {
                Snackbar.make(findViewById(R.id.layout_suspend_course_content), R.string.suspend_course_empty, Snackbar.LENGTH_SHORT).show();
                if (suspendCourseAdapter != null && suspendCourseAdapter.getItemCount() != 0) {
                    suspendCourseAdapter.clearAdapter();
                }
            }
        }
    }

    public void lastViewSet(Context context) {
        //离线数据加载完成，开始拉取网络数据
        if (loadTime == 1 && NetMethod.isNetworkConnected(context) && BaseMethod.isDataAutoUpdate(context)) {
            getData();
        } else {
            BaseMethod.setRefreshing(swipeRefreshLayout, false);
        }
    }

    @Override
    protected void onDestroy() {
        BaseMethod.getApp(this).setSuspendCourseActivity(null);
        super.onDestroy();
    }
}
