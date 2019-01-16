package tool.xfy9326.naucourse.Utils;

import androidx.annotation.Nullable;

/**
 * Created by 10696 on 2018/3/2.
 * 成绩信息
 */

public class CourseScore extends BaseUtils {
    private String[] scoreCourseId;
    @Nullable
    private String[] scoreCourseName;
    @Nullable
    private String[] scoreCommon;
    @Nullable
    private String[] scoreMid;
    @Nullable
    private String[] scoreFinal;
    @Nullable
    private String[] scoreTotal;
    private String[] scoreCourseXf;

    private int courseAmount;

    public CourseScore() {
        this.scoreCourseName = null;
        this.scoreCommon = null;
        this.scoreMid = null;
        this.scoreFinal = null;
        this.scoreTotal = null;
        this.courseAmount = 0;
    }

    public String[] getScoreCourseXf() {
        return scoreCourseXf;
    }

    public void setScoreCourseXf(String[] scoreCourseXf) {
        this.scoreCourseXf = scoreCourseXf;
    }

    public int getCourseAmount() {
        return courseAmount;
    }

    public void setCourseAmount(int courseAmount) {
        this.courseAmount = courseAmount;
    }

    @SuppressWarnings("unused")
    public String[] getScoreCourseId() {
        return scoreCourseId;
    }

    public void setScoreCourseId(String[] scoreCourseId) {
        this.scoreCourseId = scoreCourseId;
    }

    @Nullable
    public String[] getScoreCourseName() {
        return scoreCourseName;
    }

    public void setScoreCourseName(@Nullable String[] scoreCourseName) {
        this.scoreCourseName = scoreCourseName;
    }

    @Nullable
    public String[] getScoreCommon() {
        return scoreCommon;
    }

    public void setScoreCommon(@Nullable String[] scoreCommon) {
        this.scoreCommon = scoreCommon;
    }

    @Nullable
    public String[] getScoreMid() {
        return scoreMid;
    }

    public void setScoreMid(@Nullable String[] scoreMid) {
        this.scoreMid = scoreMid;
    }

    @Nullable
    public String[] getScoreFinal() {
        return scoreFinal;
    }

    public void setScoreFinal(@Nullable String[] scoreFinal) {
        this.scoreFinal = scoreFinal;
    }

    @Nullable
    public String[] getScoreTotal() {
        return scoreTotal;
    }

    public void setScoreTotal(@Nullable String[] scoreTotal) {
        this.scoreTotal = scoreTotal;
    }
}
