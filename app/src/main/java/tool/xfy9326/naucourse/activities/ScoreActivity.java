package tool.xfy9326.naucourse.activities;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.asyncTasks.ScoreAsync;
import tool.xfy9326.naucourse.methods.BaseMethod;
import tool.xfy9326.naucourse.methods.NetMethod;
import tool.xfy9326.naucourse.utils.CourseScore;
import tool.xfy9326.naucourse.utils.HistoryScore;
import tool.xfy9326.naucourse.utils.StudentScore;
import tool.xfy9326.naucourse.views.ScoreSwipeRefreshLayout;
import tool.xfy9326.naucourse.views.ScoreViewPagerAdapter;

/**
 * Created by 10696 on 2018/3/2.
 */

public class ScoreActivity extends AppCompatActivity {
    private ScoreSwipeRefreshLayout swipeRefreshLayout;
    private ScoreViewPagerAdapter scoreViewPagerAdapter;
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
        setSupportActionBar(findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void ViewSet() {
        TabLayout tabLayout = findViewById(R.id.tabLayout_score);
        ViewPager viewPager = findViewById(R.id.viewPaper_score);
        if (scoreViewPagerAdapter == null) {
            scoreViewPagerAdapter = new ScoreViewPagerAdapter(getSupportFragmentManager());
        }
        viewPager.setAdapter(scoreViewPagerAdapter);
        viewPager.setOffscreenPageLimit(ScoreViewPagerAdapter.ITEM_COUNT);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition(), true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        tabLayout.setupWithViewPager(viewPager);
        TabLayout.Tab tabItem_0 = tabLayout.getTabAt(0);
        TabLayout.Tab tabItem_1 = tabLayout.getTabAt(1);
        if (tabItem_0 != null && tabItem_1 != null) {
            tabItem_0.setText(R.string.current_score_detail);
            tabItem_1.setText(R.string.history_score_detail);
        }

        swipeRefreshLayout = findViewById(R.id.swipeLayout_score);
        swipeRefreshLayout.setDistanceToTriggerSync(200);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (NetMethod.isNetworkConnected(ScoreActivity.this)) {
                getData();
            } else {
                Snackbar.make(findViewById(R.id.layout_score_content), R.string.network_error, Snackbar.LENGTH_SHORT).show();
                swipeRefreshLayout.post(() -> swipeRefreshLayout.setRefreshing(false));
            }
        });
        if (loadTime == 0) {
            getData();
        }
    }

    public void setStudentScore(@Nullable StudentScore studentScore, @Nullable CourseScore courseScore, @Nullable HistoryScore historyScore) {
        if (studentScore != null) {
            ((TextView) findViewById(R.id.textView_scoreXF)).setText(getString(R.string.score_XF, studentScore.getScoreXF()));
            ((TextView) findViewById(R.id.textView_scoreJD)).setText(getString(R.string.score_JD, studentScore.getScoreJD()));
            ((TextView) findViewById(R.id.textView_scoreZP)).setText(getString(R.string.score_ZP, studentScore.getScoreZP() == null ? getString(R.string.data_loading) : studentScore.getScoreZP()));
            TextView textView_scoreNP = findViewById(R.id.textView_scoreNP);
            TextView textView_scoreBP = findViewById(R.id.textView_scoreBP);

            if (studentScore.getScoreNP() != null) {
                textView_scoreNP.setText(getString(R.string.score_NP, studentScore.getScoreNP()));
                textView_scoreNP.setVisibility(View.VISIBLE);
            } else {
                textView_scoreNP.setVisibility(View.GONE);
            }

            if (studentScore.getScoreBP() != null) {
                textView_scoreBP.setText(getString(R.string.score_BP, studentScore.getScoreBP()));
                textView_scoreNP.setVisibility(View.VISIBLE);
            } else {
                textView_scoreBP.setVisibility(View.GONE);
            }

            if (waitForEvaluate(courseScore)) {
                Snackbar.make(findViewById(R.id.layout_score_content), R.string.wait_for_evaluate, Snackbar.LENGTH_SHORT).show();
            }
        }
        if (scoreViewPagerAdapter != null) {
            if (courseScore != null) {
                scoreViewPagerAdapter.getCurrentScoreFragment().setScore(courseScore);
            }
            if (historyScore != null) {
                scoreViewPagerAdapter.getHistoryScoreFragment().setScore(historyScore);
            }
        }
    }

    synchronized private void getData() {
        BaseMethod.setRefreshing(swipeRefreshLayout, true);
        new ScoreAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getApplicationContext());
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
