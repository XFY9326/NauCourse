package tool.xfy9326.naucourse.Utils;

import androidx.annotation.Nullable;

/**
 * Created by 10696 on 2018/3/3.
 * 考试信息
 */

public class Exam extends BaseUtils {
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
    @Nullable
    private String[] last_time;
    @Nullable
    private String[] last_time_unit;
    private int examMount;

    public Exam() {
        this.examId = null;
        this.examName = null;
        this.examType = null;
        this.examTime = null;
        this.examScore = null;
        this.examLocation = null;
        this.last_time = null;
        this.last_time_unit = null;
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

    @Nullable
    public String[] getLast_time() {
        return last_time;
    }

    public void setLast_time(@Nullable String[] last_time) {
        this.last_time = last_time;
    }

    @Nullable
    public String[] getLast_time_unit() {
        return last_time_unit;
    }

    public void setLast_time_unit(@Nullable String[] last_time_unit) {
        this.last_time_unit = last_time_unit;
    }
}
