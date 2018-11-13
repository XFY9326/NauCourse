package tool.xfy9326.naucourse.Utils;

import java.io.Serializable;

import androidx.annotation.Nullable;

/**
 * Created by 10696 on 2018/3/10.
 * 储存下一节课的信息
 */

public class NextCourse implements Serializable {
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
    private int dataVersionCode = 0;

    public NextCourse() {
        this.courseId = null;
        this.courseName = null;
        this.courseTeacher = null;
        this.courseLocation = null;
        this.courseTime = null;
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

    @SuppressWarnings("unused")
    public int getDataVersionCode() {
        return dataVersionCode;
    }

    @SuppressWarnings("SameParameterValue")
    public void setDataVersionCode(int dataVersionCode) {
        this.dataVersionCode = dataVersionCode;
    }

}
