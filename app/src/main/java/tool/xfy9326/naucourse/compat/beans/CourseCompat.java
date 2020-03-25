package tool.xfy9326.naucourse.compat.beans;

import androidx.annotation.Nullable;

@SuppressWarnings({"FieldCanBeLocal", "ConstantConditions", "CanBeFinal"})
public class CourseCompat {
    @Nullable
    private final CourseDetailCompat[] courseDetail = null;
    private String courseId = null;
    private String courseName = null;
    private String courseTeacher = null;
    private String courseClass = null;
    private String courseCombinedClass = null;
    private String courseScore = null;
    private String courseType = null;
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

    public String getCourseTerm() {
        return courseTerm;
    }
}

