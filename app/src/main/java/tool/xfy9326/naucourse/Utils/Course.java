package tool.xfy9326.naucourse.Utils;

import android.support.annotation.Nullable;

/**
 * Created by xfy9326 on 18-2-21.
 * 课程信息
 */

public class Course {
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
    private int dataVersionCode = 0;

    @Nullable
    private CourseDetail[] courseDetail = null;

    public Course() {
    }

    @Nullable
    public String getCourseClass() {
        return courseClass;
    }

    public void setCourseClass(@Nullable String courseClass) {
        this.courseClass = courseClass;
    }

    @Nullable
    public String getCourseCombinedClass() {
        return courseCombinedClass;
    }

    public void setCourseCombinedClass(@Nullable String courseCombinedClass) {
        this.courseCombinedClass = courseCombinedClass;
    }

    @Nullable
    public String getCourseScore() {
        return courseScore;
    }

    public void setCourseScore(@Nullable String courseScore) {
        this.courseScore = courseScore;
    }

    @Nullable
    public String getCourseType() {
        return courseType;
    }

    public void setCourseType(@Nullable String courseType) {
        this.courseType = courseType;
    }

    @Nullable
    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(@Nullable String courseId) {
        this.courseId = courseId;
    }

    @Nullable
    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(@Nullable String courseName) {
        this.courseName = courseName;
    }

    @Nullable
    public String getCourseTeacher() {
        return courseTeacher;
    }

    public void setCourseTeacher(@Nullable String courseTeacher) {
        this.courseTeacher = courseTeacher;
    }

    @Nullable
    public CourseDetail[] getCourseDetail() {
        return courseDetail;
    }

    public void setCourseDetail(@Nullable CourseDetail[] courseDetail) {
        this.courseDetail = courseDetail;
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
