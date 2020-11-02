package tool.xfy9326.naucourse.compat.beans;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

// 旧版本的课程数据
@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal", "CanBeFinal"})
public class CourseCompat {
    @Nullable
    private CourseDetailCompat[] courseDetail = null;
    @Nullable
    private String courseId = null;
    @Nullable
    private String courseName = null;
    @Nullable
    private String courseTeacher = null;
    @Nullable
    private String courseClass = null;
    @Nullable
    private String courseCombinedClass = null;
    @Nullable
    private String courseScore = null;
    @Nullable
    private String courseType = null;
    @NonNull
    private String courseTerm = "0";
    private int courseColor = -1;

    @Nullable
    public String getCourseClass() {
        return courseClass;
    }

    @Nullable
    public String getCourseCombinedClass() {
        return courseCombinedClass;
    }

    @Nullable
    public String getCourseScore() {
        return courseScore;
    }

    @Nullable
    public String getCourseType() {
        return courseType;
    }

    @Nullable
    public String getCourseId() {
        return courseId;
    }

    @Nullable
    public String getCourseName() {
        return courseName;
    }

    @Nullable
    public String getCourseTeacher() {
        return courseTeacher;
    }

    @Nullable
    public CourseDetailCompat[] getCourseDetail() {
        return courseDetail;
    }

    public int getCourseColor() {
        return courseColor;
    }

    @NonNull
    public String getCourseTerm() {
        return courseTerm;
    }
}

