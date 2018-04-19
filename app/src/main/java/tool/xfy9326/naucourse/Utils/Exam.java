package tool.xfy9326.naucourse.Utils;

/**
 * Created by 10696 on 2018/3/3.
 * 考试信息
 */

public class Exam {
    private String[] examId;
    private String[] examName;
    private String[] examType;
    private String[] examTime;
    private String[] examLocation;
    private String[] examScore;
    private int examMount;
    private int dataVersionCode = 0;

    public Exam() {
        this.examId = null;
        this.examName = null;
        this.examType = null;
        this.examTime = null;
        this.examScore = null;
        this.examLocation = null;
        this.examMount = 0;
    }

    @SuppressWarnings("unused")
    public String[] getExamId() {
        return examId;
    }

    public void setExamId(String[] examId) {
        this.examId = examId;
    }

    public String[] getExamName() {
        return examName;
    }

    public void setExamName(String[] examName) {
        this.examName = examName;
    }

    public String[] getExamType() {
        return examType;
    }

    public void setExamType(String[] examType) {
        this.examType = examType;
    }

    public String[] getExamTime() {
        return examTime;
    }

    public void setExamTime(String[] examTime) {
        this.examTime = examTime;
    }

    public String[] getExamLocation() {
        return examLocation;
    }

    public void setExamLocation(String[] examLocation) {
        this.examLocation = examLocation;
    }

    public int getExamMount() {
        return examMount;
    }

    public void setExamMount(int examMount) {
        this.examMount = examMount;
    }

    public String[] getExamScore() {
        return examScore;
    }

    public void setExamScore(String[] examScore) {
        this.examScore = examScore;
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
