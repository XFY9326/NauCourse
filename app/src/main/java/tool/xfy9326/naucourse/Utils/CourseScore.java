package tool.xfy9326.naucourse.Utils;

/**
 * Created by 10696 on 2018/3/2.
 * 成绩信息
 */

public class CourseScore {
    private String[] scoreCourseId;
    private String[] scoreCourseName;
    private String[] scoreCommon;
    private String[] scoreMid;
    private String[] scoreFinal;
    private String[] scoreTotal;
    private String[] scoreCourseXf;
    private int dataVersionCode = 0;

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

    public String[] getScoreCourseName() {
        return scoreCourseName;
    }

    public void setScoreCourseName(String[] scoreCourseName) {
        this.scoreCourseName = scoreCourseName;
    }

    public String[] getScoreCommon() {
        return scoreCommon;
    }

    public void setScoreCommon(String[] scoreCommon) {
        this.scoreCommon = scoreCommon;
    }

    public String[] getScoreMid() {
        return scoreMid;
    }

    public void setScoreMid(String[] scoreMid) {
        this.scoreMid = scoreMid;
    }

    public String[] getScoreFinal() {
        return scoreFinal;
    }

    public void setScoreFinal(String[] scoreFinal) {
        this.scoreFinal = scoreFinal;
    }

    public String[] getScoreTotal() {
        return scoreTotal;
    }

    public void setScoreTotal(String[] scoreTotal) {
        this.scoreTotal = scoreTotal;
    }

    @SuppressWarnings("unused")
    public int getDataVersionCode() {
        return dataVersionCode;
    }

    @SuppressWarnings("SameParameterValue")
    public void setDataVersionCode(int dataVersionCode) {
        this.dataVersionCode = dataVersionCode;
    }
}
