package tool.xfy9326.naucourse;

import android.app.Application;

import com.tencent.bugly.Bugly;

import lib.xfy9326.naujwc.NauJwcClient;
import tool.xfy9326.naucourse.Activities.CourseActivity;
import tool.xfy9326.naucourse.Activities.ExamActivity;
import tool.xfy9326.naucourse.Activities.InfoDetailActivity;
import tool.xfy9326.naucourse.Activities.LevelExamActivity;
import tool.xfy9326.naucourse.Activities.MoaActivity;
import tool.xfy9326.naucourse.Activities.SchoolCalendarActivity;
import tool.xfy9326.naucourse.Activities.ScoreActivity;
import tool.xfy9326.naucourse.Activities.SuspendCourseActivity;
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
    private LevelExamActivity levelExamActivity;
    private CourseActivity courseActivity;
    private SchoolCalendarActivity schoolCalendarActivity;
    private SuspendCourseActivity suspendCourseActivity;
    private MoaActivity moaActivity;

    private boolean showLoginErrorOnce = false;
    private boolean showConnectErrorOnce = false;

    @Override

    public void onCreate() {
        super.onCreate();
        if (!BuildConfig.DEBUG) {
            Bugly.init(this, "7b78d5ccdc", false);
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

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isShowLoginErrorOnce() {
        return showLoginErrorOnce;
    }

    public void setShowLoginErrorOnce() {
        this.showLoginErrorOnce = true;
    }

    public LevelExamActivity getLevelExamActivity() {
        return levelExamActivity;
    }

    public void setLevelExamActivity(LevelExamActivity levelExamActivity) {
        this.levelExamActivity = levelExamActivity;
    }

    public CourseActivity getCourseActivity() {
        return courseActivity;
    }

    public void setCourseActivity(CourseActivity courseActivity) {
        this.courseActivity = courseActivity;
    }

    public SchoolCalendarActivity getSchoolCalendarActivity() {
        return schoolCalendarActivity;
    }

    public void setSchoolCalendarActivity(SchoolCalendarActivity schoolCalendarActivity) {
        this.schoolCalendarActivity = schoolCalendarActivity;
    }

    public SuspendCourseActivity getSuspendCourseActivity() {
        return suspendCourseActivity;
    }

    public void setSuspendCourseActivity(SuspendCourseActivity suspendCourseActivity) {
        this.suspendCourseActivity = suspendCourseActivity;
    }

    public MoaActivity getMoaActivity() {
        return moaActivity;
    }

    public void setMoaActivity(MoaActivity moaActivity) {
        this.moaActivity = moaActivity;
    }

    public boolean isShowConnectErrorOnce() {
        return showConnectErrorOnce;
    }

    public void setShowConnectErrorOnce(boolean showConnectErrorOnce) {
        this.showConnectErrorOnce = showConnectErrorOnce;
    }
}
