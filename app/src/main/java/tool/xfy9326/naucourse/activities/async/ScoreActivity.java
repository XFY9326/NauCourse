package tool.xfy9326.naucourse.activities.async;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.asyncTasks.ScoreAsync;
import tool.xfy9326.naucourse.beans.score.CourseScore;
import tool.xfy9326.naucourse.beans.score.CreditCountCourse;
import tool.xfy9326.naucourse.beans.score.HistoryScore;
import tool.xfy9326.naucourse.beans.score.StudentScore;
import tool.xfy9326.naucourse.methods.BaseMethod;
import tool.xfy9326.naucourse.methods.compute.CreditCountMethod;
import tool.xfy9326.naucourse.methods.net.NetMethod;
import tool.xfy9326.naucourse.views.ScoreSwipeRefreshLayout;
import tool.xfy9326.naucourse.views.recyclerAdapters.CreditCountAdapter;
import tool.xfy9326.naucourse.views.viewPagerAdapters.ScoreViewPagerAdapter;

/**
 * Created by 10696 on 2018/3/2.
 */

public class ScoreActivity extends BaseAsyncActivity {
    private ScoreSwipeRefreshLayout swipeRefreshLayout;
    private ScoreViewPagerAdapter scoreViewPagerAdapter;
    private CourseScore courseScore;
    private List<CreditCountCourse> historyCreditCourse;
    private boolean isLoading = true;

    private static boolean waitForEvaluate(CourseScore courseScore) {
        if (courseScore != null && courseScore.getScoreTotal() != null) {
            for (String score : courseScore.getScoreTotal()) {
                if (score.contains("测评")) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);
        BaseMethod.getApp(this).setScoreActivity(this);
        toolBarSet();
        viewSet();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_score, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_score_credit) {
            if (!isLoading && courseScore != null && historyCreditCourse != null) {
                ArrayList<CreditCountCourse> countCourses = CreditCountMethod.getCreditCountCourse(courseScore);
                if (countCourses.size() > 0) {
                    showCreditCountDialog(countCourses);
                } else {
                    Snackbar.make(findViewById(R.id.layout_score_content), R.string.credit_count_course_empty, Snackbar.LENGTH_SHORT).show();
                }
            } else {
                Snackbar.make(findViewById(R.id.layout_score_content), R.string.data_is_loading, Snackbar.LENGTH_SHORT).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void showCreditCountDialog(final ArrayList<CreditCountCourse> creditCountCourses) {
        final CreditCountAdapter adapter = new CreditCountAdapter(this, creditCountCourses);
        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_score_credit, findViewById(R.id.layout_dialog_score_credit));
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView_dialog_score_credit);
        recyclerView.setFocusableInTouchMode(false);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.credit_course_choose);
        builder.setView(view);
        builder.setPositiveButton(R.string.calculate, (dialog, which) -> {
            ArrayList<CreditCountCourse> current = adapter.getResult();
            if (current.size() == 0) {
                Snackbar.make(findViewById(R.id.layout_score_content), R.string.credit_no_select, Snackbar.LENGTH_SHORT).show();
            } else {
                ArrayList<CreditCountCourse> courseList = CreditCountMethod.combineCreditCourse(current, historyCreditCourse);
                float credit = CreditCountMethod.getCredit(courseList);
                AlertDialog.Builder newBuilder = new AlertDialog.Builder(this);
                newBuilder.setTitle(R.string.credit_calculator);
                newBuilder.setMessage(getString(R.string.credit_calculate_result, credit));
                newBuilder.setPositiveButton(android.R.string.yes, null);
                newBuilder.show();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        Dialog dialog = builder.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        }
    }

    @Override
    protected void onDestroy() {
        BaseMethod.getApp(this).setScoreActivity(null);
        System.gc();
        super.onDestroy();
    }

    private void viewSet() {
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
        TabLayout.Tab tabItem0 = tabLayout.getTabAt(0);
        TabLayout.Tab tabItem1 = tabLayout.getTabAt(1);
        if (tabItem0 != null && tabItem1 != null) {
            tabItem0.setText(R.string.current_score_detail);
            tabItem1.setText(R.string.history_score_detail);
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
            TextView textViewScoreNP = findViewById(R.id.textView_scoreNP);
            TextView textViewScoreBP = findViewById(R.id.textView_scoreBP);

            if (studentScore.getScoreNP() != null) {
                textViewScoreNP.setText(getString(R.string.score_NP, studentScore.getScoreNP()));
                textViewScoreNP.setVisibility(View.VISIBLE);
            } else {
                textViewScoreNP.setVisibility(View.GONE);
            }

            if (studentScore.getScoreBP() != null) {
                textViewScoreBP.setText(getString(R.string.score_BP, studentScore.getScoreBP()));
                textViewScoreNP.setVisibility(View.VISIBLE);
            } else {
                textViewScoreBP.setVisibility(View.GONE);
            }

            if (waitForEvaluate(courseScore)) {
                Snackbar.make(findViewById(R.id.layout_score_content), R.string.wait_for_evaluate, Snackbar.LENGTH_SHORT).show();
            }
        }
        if (courseScore != null) {
            this.courseScore = courseScore;
        }
        if (historyScore != null) {
            historyCreditCourse = CreditCountMethod.getHistoryCreditCourse(historyScore);
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

    @Override
    synchronized protected void getData() {
        isLoading = true;
        BaseMethod.setRefreshing(swipeRefreshLayout, true);
        new ScoreAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getApplicationContext());
    }

    @Override
    public void lastViewSet(Context context) {
        //离线数据加载完成，开始拉取网络数据
        if (loadTime == 1 && NetMethod.isNetworkConnected(context) && BaseMethod.isDataAutoUpdate(context)) {
            getData();
        } else {
            BaseMethod.setRefreshing(swipeRefreshLayout, false);
        }
        isLoading = false;
    }

}
