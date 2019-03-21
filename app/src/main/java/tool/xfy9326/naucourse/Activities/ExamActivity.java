package tool.xfy9326.naucourse.Activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import tool.xfy9326.naucourse.AsyncTasks.ExamAsync;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.Methods.NetMethod;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Utils.Exam;
import tool.xfy9326.naucourse.Views.RecyclerAdapters.ExamAdapter;

/**
 * Created by 10696 on 2018/3/3.
 */

public class ExamActivity extends AppCompatActivity {
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    @Nullable
    private ExamAdapter examAdapter;
    private int loadTime = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam);
        BaseMethod.getApp(this).setExamActivity(this);
        ToolBarSet();
        ViewSet();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_exam, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.menu_exam_hide_out_of_date);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean(Config.PREFERENCE_HIDE_OUT_OF_DATE_EXAM, Config.DEFAULT_PREFERENCE_HIDE_OUT_OF_DATE_EXAM)) {
            menuItem.setIcon(R.drawable.ic_visible);
        } else {
            menuItem.setIcon(R.drawable.ic_invisible);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_exam_hide_out_of_date:
                if (NetMethod.isNetworkConnected(ExamActivity.this)) {
                    getData();
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                    if (sharedPreferences.getBoolean(Config.PREFERENCE_HIDE_OUT_OF_DATE_EXAM, Config.DEFAULT_PREFERENCE_HIDE_OUT_OF_DATE_EXAM)) {
                        sharedPreferences.edit().putBoolean(Config.PREFERENCE_HIDE_OUT_OF_DATE_EXAM, false).apply();
                    } else {
                        sharedPreferences.edit().putBoolean(Config.PREFERENCE_HIDE_OUT_OF_DATE_EXAM, true).apply();
                    }
                    invalidateOptionsMenu();
                } else {
                    Snackbar.make(findViewById(R.id.layout_exam_content), R.string.network_error, Snackbar.LENGTH_SHORT).show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        BaseMethod.getApp(this).setExamActivity(null);
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
        recyclerView = findViewById(R.id.recyclerView_exam);
        recyclerView.setFocusableInTouchMode(false);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        swipeRefreshLayout = findViewById(R.id.swipeLayout_exam);
        swipeRefreshLayout.setDistanceToTriggerSync(200);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (NetMethod.isNetworkConnected(ExamActivity.this)) {
                    getData();
                } else {
                    Snackbar.make(findViewById(R.id.layout_exam_content), R.string.network_error, Snackbar.LENGTH_SHORT).show();
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

    public void setExam(@Nullable Exam exam) {
        if (exam != null) {
            if (exam.getExamMount() > 0) {
                if (examAdapter == null) {
                    examAdapter = new ExamAdapter(ExamActivity.this, exam);
                    recyclerView.setAdapter(examAdapter);
                } else {
                    examAdapter.updateData(exam);
                }
            } else if (loadTime > 1 || !NetMethod.isNetworkConnected(this)) {
                Snackbar.make(findViewById(R.id.layout_exam_content), R.string.no_exam, Snackbar.LENGTH_SHORT).show();
                if (examAdapter != null && examAdapter.getItemCount() != 0) {
                    examAdapter.clearAdapter();
                }
            }
        }
    }

    synchronized private void getData() {
        BaseMethod.setRefreshing(swipeRefreshLayout, true);
        if (loadTime == 0) {
            new ExamAsync().execute(getApplicationContext());
        } else {
            new ExamAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getApplicationContext());
        }
    }

    public int getLoadTime() {
        return loadTime;
    }

    public void setLoadTime(int loadTime) {
        this.loadTime = loadTime;
    }

    public void lastViewSet(Context context) {
        //离线数据加载完成，开始拉取网络数据
        if (loadTime == 1 && NetMethod.isNetworkConnected(context) && BaseMethod.isDataAutoUpdate(context)) {
            getData();
        } else {
            BaseMethod.setRefreshing(swipeRefreshLayout, false);
        }
    }

}
