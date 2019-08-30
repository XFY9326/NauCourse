package tool.xfy9326.naucourse.activities.async;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
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

public class SuspendCourseActivity extends BaseAsyncActivity {
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private SuspendCourseAdapter suspendCourseAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suspend_course);
        BaseMethod.getApp(this).setSuspendCourseActivity(this);
        toolBarSet();
        viewSet();
    }

    private void viewSet() {
        recyclerView = findViewById(R.id.recyclerView_suspend_course_list);
        recyclerView.setFocusableInTouchMode(false);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        if (suspendCourseAdapter == null) {
            suspendCourseAdapter = new SuspendCourseAdapter(SuspendCourseActivity.this);
        }
        recyclerView.setAdapter(suspendCourseAdapter);

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

    @Override
    synchronized protected void getData() {
        BaseMethod.setRefreshing(swipeRefreshLayout, true);
        new SuspendCourseAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getApplicationContext());
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

    @Override
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
