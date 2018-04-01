package tool.xfy9326.naucourse.Activities;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import tool.xfy9326.naucourse.AsyncTasks.ScoreAsync;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Utils.CourseScore;
import tool.xfy9326.naucourse.Utils.StudentLearnProcess;
import tool.xfy9326.naucourse.Utils.StudentScore;
import tool.xfy9326.naucourse.Views.ScoreAdapter;

/**
 * Created by 10696 on 2018/3/2.
 */

public class ScoreActivity extends AppCompatActivity {
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private ScoreAdapter scoreAdapter;
    private StudentLearnProcess studentLearnProcess = null;
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
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        swipeRefreshLayout = findViewById(R.id.swipeLayout_score);
        swipeRefreshLayout.setDistanceToTriggerSync(200);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (BaseMethod.isNetworkConnected(ScoreActivity.this)) {
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

        CardView cardView = findViewById(R.id.cardView_learn_process);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                learnProcessView();
            }
        });

        if (loadTime == 0) {
            getData();
        }
    }

    public void setMainScore(StudentScore studentScore, StudentLearnProcess studentLearnProcess, CourseScore courseScore) {
        if (studentScore != null && courseScore != null && studentLearnProcess != null) {
            ((TextView) findViewById(R.id.textView_scoreXF)).setText(getString(R.string.score_XF, studentScore.getScoreXF()));
            ((TextView) findViewById(R.id.textView_scoreJD)).setText(getString(R.string.score_JD, studentScore.getScoreJD()));
            ((TextView) findViewById(R.id.textView_scoreNP)).setText(getString(R.string.score_NP, studentScore.getScoreNP()));
            ((TextView) findViewById(R.id.textView_scoreZP)).setText(getString(R.string.score_ZP, studentScore.getScoreZP()));
            ((TextView) findViewById(R.id.textView_scoreBP)).setText(getString(R.string.score_BP, studentScore.getScoreBP()));

            if (scoreAdapter == null) {
                scoreAdapter = new ScoreAdapter(ScoreActivity.this, courseScore);
                recyclerView.setAdapter(scoreAdapter);
            } else {
                scoreAdapter.updateData(courseScore);
            }

            this.studentLearnProcess = studentLearnProcess;
        }
    }

    private void learnProcessView() {
        if (studentLearnProcess != null) {
            LayoutInflater layoutInflater = getLayoutInflater();
            ScrollView scrollView = new ScrollView(ScoreActivity.this);
            scrollView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            scrollView.setVerticalScrollBarEnabled(false);
            scrollView.setPadding(15, 50, 15, 5);
            LinearLayout linearLayout = new LinearLayout(ScoreActivity.this);
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            for (int i = 0; i < 4; i++) {
                View view = layoutInflater.inflate(R.layout.item_learn_process, (ViewGroup) findViewById(R.id.layout_learn_process_item));
                String name = null;
                String aim = null;
                String now = null;
                String still = null;
                String award = null;
                int process = 0;
                switch (i) {
                    case 0:
                        name = getString(R.string.process_name, getString(R.string.process_bx));
                        aim = getString(R.string.process_aim, studentLearnProcess.getScoreBXAim());
                        now = getString(R.string.process_now, studentLearnProcess.getScoreBXNow());
                        still = getString(R.string.process_still, studentLearnProcess.getScoreBXStill());
                        process = (int) (Float.valueOf(studentLearnProcess.getScoreBXNow()) / Float.valueOf(studentLearnProcess.getScoreBXAim()) * 100);
                        break;
                    case 1:
                        name = getString(R.string.process_name, getString(R.string.process_zx));
                        aim = getString(R.string.process_aim, studentLearnProcess.getScoreZXAim());
                        now = getString(R.string.process_now, studentLearnProcess.getScoreZXNow());
                        still = getString(R.string.process_still, studentLearnProcess.getScoreZXStill());
                        process = (int) (Float.valueOf(studentLearnProcess.getScoreZXNow()) / Float.valueOf(studentLearnProcess.getScoreZXAim()) * 100);
                        break;
                    case 2:
                        name = getString(R.string.process_name, getString(R.string.process_rx));
                        aim = getString(R.string.process_aim, studentLearnProcess.getScoreRXAim());
                        now = getString(R.string.process_now, studentLearnProcess.getScoreRXNow());
                        still = getString(R.string.process_still, studentLearnProcess.getScoreRXStill());
                        award = getString(R.string.process_award, studentLearnProcess.getScoreRXAward());
                        process = (int) (Float.valueOf(studentLearnProcess.getScoreRXNow()) / Float.valueOf(studentLearnProcess.getScoreRXAim()) * 100);
                        break;
                    case 3:
                        name = getString(R.string.process_name, getString(R.string.process_sj));
                        aim = getString(R.string.process_aim, studentLearnProcess.getScoreSJAim());
                        now = getString(R.string.process_now, studentLearnProcess.getScoreSJNow());
                        still = getString(R.string.process_still, studentLearnProcess.getScoreSJStill());
                        process = (int) (Float.valueOf(studentLearnProcess.getScoreSJNow()) / Float.valueOf(studentLearnProcess.getScoreSJAim()) * 100);
                        break;
                }
                ((TextView) view.findViewById(R.id.textView_process_name)).setText(name);
                ((TextView) view.findViewById(R.id.textView_process_aim)).setText(aim);
                ((TextView) view.findViewById(R.id.textView_process_now)).setText(now);
                ((TextView) view.findViewById(R.id.textView_process_still)).setText(still);
                if (i == 2) {
                    TextView textView_award = view.findViewById(R.id.textView_process_award);
                    textView_award.setVisibility(View.VISIBLE);
                    textView_award.setText(award);
                }
                ProgressBar progressBar = view.findViewById(R.id.progressBar_process_learn);
                progressBar.setMax(100);
                progressBar.setProgress(process);
                linearLayout.addView(view);
            }

            scrollView.addView(linearLayout);

            AlertDialog.Builder builder = new AlertDialog.Builder(ScoreActivity.this);
            builder.setTitle(R.string.learn_process);
            builder.setView(scrollView);
            builder.show();
        } else {
            Toast.makeText(ScoreActivity.this, R.string.data_is_loading, Toast.LENGTH_SHORT).show();
        }
    }

    private void getData() {
        new ScoreAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getApplicationContext());
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
        if (loadTime == 1 && BaseMethod.isNetworkConnected(context) && BaseMethod.isDataAutoUpdate(context)) {
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
