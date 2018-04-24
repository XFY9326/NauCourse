package tool.xfy9326.naucourse.Utils;

import android.support.annotation.Nullable;

/**
 * Created by 10696 on 2018/3/3.
 * 考试信息
 */

public class Exam {
    @Nullable
    private String[] examId;
    @Nullable
    private String[] examName;
    @Nullable
    private String[] examType;
    @Nullable
    private String[] examTime;
    @Nullable
    private String[] examLocation;
    @Nullable
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

    @Nullable
    @SuppressWarnings("unused")
    public String[] getExamId() {
        return examId;
    }

    public void setExamId(@Nullable String[] examId) {
        this.examId = examId;
    }

    @Nullable
    public String[] getExamName() {
        return examName;
    }

    public void setExamName(@Nullable String[] examName) {
        this.examName = examName;
    }

    @Nullable
    public String[] getExamType() {
        return examType;
    }

    public void setExamType(@Nullable String[] examType) {
        this.examType = examType;
    }

    @Nullable
    public String[] getExamTime() {
        return examTime;
    }

    public void setExamTime(@Nullable String[] examTime) {
        this.examTime = examTime;
    }

    @Nullable
    public String[] getExamLocation() {
        return examLocation;
    }

    public void setExamLocation(@Nullable String[] examLocation) {
        this.examLocation = examLocation;
    }

    public int getExamMount() {
        return examMount;
    }

    public void setExamMount(int examMount) {
        this.examMount = examMount;
    }

    @Nullable
    public String[] getExamScore() {
        return examScore;
    }

    public void setExamScore(@Nullable String[] examScore) {
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
