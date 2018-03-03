package tool.xfy9326.naucourse;

import android.app.Application;

import com.tencent.bugly.Bugly;

import lib.xfy9326.naujwc.NauJwcClient;
import tool.xfy9326.naucourse.Activities.ExamActivity;
import tool.xfy9326.naucourse.Activities.InfoDetailActivity;
import tool.xfy9326.naucourse.Activities.ScoreActivity;
import tool.xfy9326.naucourse.Views.ViewPagerAdapter;

/**
 * Created by xfy9326 on 18-2-20.
 */

public class BaseApplication extends Application {
    private NauJwcClient client;
    private ViewPagerAdapter viewPagerAdapter;
    private InfoDetailActivity infoDetailActivity;
    private ScoreActivity scoreActivity;
    private ExamActivity examActivity;

    @Override

    public void onCreate() {
        super.onCreate();
        if (!BuildConfig.DEBUG) {
            Bugly.init(this, "7b78d5ccdc", BuildConfig.DEBUG);
        }
        client = new NauJwcClient(this);
    }

    public NauJwcClient getClient() {
        return client;
    }

    public ViewPagerAdapter getViewPagerAdapter() {
        return viewPagerAdapter;
    }

    public void setViewPagerAdapter(ViewPagerAdapter viewPagerAdapter) {
        this.viewPagerAdapter = viewPagerAdapter;
    }

    public InfoDetailActivity getInfoDetailActivity() {
        return infoDetailActivity;
    }

    public void setInfoDetailActivity(InfoDetailActivity infoDetailActivity) {
        this.infoDetailActivity = infoDetailActivity;
    }

    public ScoreActivity getScoreActivity() {
        return scoreActivity;
    }

    public void setScoreActivity(ScoreActivity scoreActivity) {
        this.scoreActivity = scoreActivity;
    }

    public ExamActivity getExamActivity() {
        return examActivity;
    }

    public void setExamActivity(ExamActivity examActivity) {
        this.examActivity = examActivity;
    }
}
