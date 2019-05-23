package tool.xfy9326.naucourse;

import android.app.Application;

import com.tencent.bugly.Bugly;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lib.xfy9326.nausso.NauSSOClient;
import tool.xfy9326.naucourse.activities.CourseActivity;
import tool.xfy9326.naucourse.activities.CourseSearchActivity;
import tool.xfy9326.naucourse.activities.ExamActivity;
import tool.xfy9326.naucourse.activities.InfoDetailActivity;
import tool.xfy9326.naucourse.activities.LevelExamActivity;
import tool.xfy9326.naucourse.activities.MoaActivity;
import tool.xfy9326.naucourse.activities.SchoolCalendarActivity;
import tool.xfy9326.naucourse.activities.ScoreActivity;
import tool.xfy9326.naucourse.activities.SuspendCourseActivity;
import tool.xfy9326.naucourse.views.ViewPagerAdapter;

/**
 * Created by xfy9326 on 18-2-20.
 */

public class BaseApplication extends Application {
    private NauSSOClient client;
    private ExecutorService executorService;
    private ViewPagerAdapter viewPagerAdapter;
    private WeakReference<InfoDetailActivity> infoDetailActivity;
    private WeakReference<ScoreActivity> scoreActivity;
    private WeakReference<ExamActivity> examActivity;
    private WeakReference<LevelExamActivity> levelExamActivity;
    private WeakReference<CourseActivity> courseActivity;
    private WeakReference<SchoolCalendarActivity> schoolCalendarActivity;
    private WeakReference<SuspendCourseActivity> suspendCourseActivity;
    private WeakReference<MoaActivity> moaActivity;
    private WeakReference<CourseSearchActivity> courseSearchActivity;

    private boolean showLoginErrorOnce = false;
    private boolean showConnectErrorOnce = false;

    @Override
    public void onCreate() {
        super.onCreate();
        if (!BuildConfig.DEBUG) {
            Bugly.init(this, "7b78d5ccdc", false);
        }
        client = new NauSSOClient(this);
        executorService = Executors.newCachedThreadPool();
    }

    public NauSSOClient getClient() {
        return client;
    }

    public ViewPagerAdapter getViewPagerAdapter() {
        return viewPagerAdapter;
    }

    public void setViewPagerAdapter(ViewPagerAdapter viewPagerAdapter) {
        this.viewPagerAdapter = viewPagerAdapter;
    }

    public InfoDetailActivity getInfoDetailActivity() {
        return infoDetailActivity.get();
    }

    public void setInfoDetailActivity(InfoDetailActivity infoDetailActivity) {
        this.infoDetailActivity = new WeakReference<>(infoDetailActivity);
    }

    public ScoreActivity getScoreActivity() {
        return scoreActivity.get();
    }

    public void setScoreActivity(ScoreActivity scoreActivity) {
        this.scoreActivity = new WeakReference<>(scoreActivity);
    }

    public ExamActivity getExamActivity() {
        return examActivity.get();
    }

    public void setExamActivity(ExamActivity examActivity) {
        this.examActivity = new WeakReference<>(examActivity);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isShowLoginErrorOnce() {
        return showLoginErrorOnce;
    }

    synchronized public void setShowLoginErrorOnce() {
        this.showLoginErrorOnce = true;
    }

    public LevelExamActivity getLevelExamActivity() {
        return levelExamActivity.get();
    }

    public void setLevelExamActivity(LevelExamActivity levelExamActivity) {
        this.levelExamActivity = new WeakReference<>(levelExamActivity);
    }

    public CourseActivity getCourseActivity() {
        return courseActivity.get();
    }

    public void setCourseActivity(CourseActivity courseActivity) {
        this.courseActivity = new WeakReference<>(courseActivity);
    }

    public SchoolCalendarActivity getSchoolCalendarActivity() {
        return schoolCalendarActivity.get();
    }

    public void setSchoolCalendarActivity(SchoolCalendarActivity schoolCalendarActivity) {
        this.schoolCalendarActivity = new WeakReference<>(schoolCalendarActivity);
    }

    public SuspendCourseActivity getSuspendCourseActivity() {
        return suspendCourseActivity.get();
    }

    public void setSuspendCourseActivity(SuspendCourseActivity suspendCourseActivity) {
        this.suspendCourseActivity = new WeakReference<>(suspendCourseActivity);
    }

    public MoaActivity getMoaActivity() {
        return moaActivity.get();
    }

    public void setMoaActivity(MoaActivity moaActivity) {
        this.moaActivity = new WeakReference<>(moaActivity);
    }

    public boolean isShowConnectErrorOnce() {
        return showConnectErrorOnce;
    }

    public void setShowConnectErrorOnce(boolean showConnectErrorOnce) {
        this.showConnectErrorOnce = showConnectErrorOnce;
    }

    @Override
    public void onTerminate() {
        client = null;
        if (!executorService.isShutdown()) {
            executorService.shutdownNow();
        }
        super.onTerminate();
    }

    public ExecutorService getExecutorService() {
        if (executorService == null) {
            executorService = Executors.newCachedThreadPool();
        }
        return executorService;
    }

    public CourseSearchActivity getCourseSearchActivity() {
        return courseSearchActivity.get();
    }

    public void setCourseSearchActivity(CourseSearchActivity courseSearchActivity) {
        this.courseSearchActivity = new WeakReference<>(courseSearchActivity);
    }
}
