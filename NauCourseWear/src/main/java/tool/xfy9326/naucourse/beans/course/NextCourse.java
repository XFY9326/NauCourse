package tool.xfy9326.naucourse.beans.course;

import androidx.annotation.Nullable;

import tool.xfy9326.naucourse.beans.BaseData;

/**
 * Created by 10696 on 2018/3/10.
 * 储存下一节课的信息
 */

public class NextCourse extends BaseData {
    @Nullable
    private String courseId;
    @Nullable
    private String courseName;
    @Nullable
    private String courseTeacher;
    @Nullable
    private String courseLocation;
    @Nullable
    private String courseTime;
    private boolean inVacation;

    public NextCourse() {
        this.inVacation = true;
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
    public String getCourseLocation() {
        return courseLocation;
    }

    public void setCourseLocation(@Nullable String courseLocation) {
        this.courseLocation = courseLocation;
    }

    @Nullable
    public String getCourseTime() {
        return courseTime;
    }

    public void setCourseTime(@Nullable String courseTime) {
        this.courseTime = courseTime;
    }

    public boolean isInVacation() {
        return inVacation;
    }

    public void setInVacation(boolean inVacation) {
        this.inVacation = inVacation;
    }
}
