package tool.xfy9326.naucourse.Activities;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import tool.xfy9326.naucourse.AsyncTasks.ScoreAsync;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.Methods.NetMethod;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Utils.CourseScore;
import tool.xfy9326.naucourse.Utils.StudentScore;
import tool.xfy9326.naucourse.Views.RecyclerViews.ScoreAdapter;

/**
 * Created by 10696 on 2018/3/2.
 */

public class ScoreActivity extends AppCompatActivity {
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    @Nullable
    private ScoreAdapter scoreAdapter;
    private int loadTime = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);
        BaseMethod.getApp(this).setScoreActivity(this);
        ToolBarSet();
        ViewSet();
    }

    @Override
    protected void onDestroy() {
        BaseMethod.getApp(this).setScoreActivity(null);
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
        recyclerView = findViewById(R.id.recyclerView_score_term);
        recyclerView.setFocusableInTouchMode(false);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        swipeRefreshLayout = findViewById(R.id.swipeLayout_score);
        swipeRefreshLayout.setDistanceToTriggerSync(200);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (NetMethod.isNetworkConnected(ScoreActivity.this)) {
                    getData();
                } else {
                    Snackbar.make(findViewById(R.id.layout_score_content), R.string.network_error, Snackbar.LENGTH_SHORT).show();
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

    public void setMainScore(@Nullable StudentScore studentScore, @Nullable CourseScore courseScore) {
        if (studentScore != null && courseScore != null) {
            ((TextView) findViewById(R.id.textView_scoreXF)).setText(getString(R.string.score_XF, studentScore.getScoreXF()));
            ((TextView) findViewById(R.id.textView_scoreJD)).setText(getString(R.string.score_JD, studentScore.getScoreJD()));
            ((TextView) findViewById(R.id.textView_scoreNP)).setText(getString(R.string.score_NP, studentScore.getScoreNP() == null ? getString(R.string.data_loading) : studentScore.getScoreNP()));
            ((TextView) findViewById(R.id.textView_scoreZP)).setText(getString(R.string.score_ZP, studentScore.getScoreZP() == null ? getString(R.string.data_loading) : studentScore.getScoreZP()));
            ((TextView) findViewById(R.id.textView_scoreBP)).setText(getString(R.string.score_BP, studentScore.getScoreBP() == null ? getString(R.string.data_loading) : studentScore.getScoreBP()));

            if (waitForEvaluate(courseScore)) {
                findViewById(R.id.textView_wait_for_evaluate).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.textView_wait_for_evaluate).setVisibility(View.GONE);
            }

            if (scoreAdapter == null) {
                scoreAdapter = new ScoreAdapter(ScoreActivity.this, courseScore);
                recyclerView.setAdapter(scoreAdapter);
            } else {
                scoreAdapter.updateData(courseScore);
            }
        }
    }

    synchronized private void getData() {
        if (loadTime == 0) {
            new ScoreAsync().execute(getApplicationContext());
        } else {
            new ScoreAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getApplicationContext());
        }
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

    private boolean waitForEvaluate(CourseScore courseScore) {
        if (courseScore != null && courseScore.getScoreTotal() != null) {
            for (String score : courseScore.getScoreTotal()) {
                if (score.contains("测评")) {
                    return true;
                }
            }
        }
        return false;
    }

}
