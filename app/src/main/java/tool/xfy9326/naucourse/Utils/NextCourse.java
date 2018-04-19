package tool.xfy9326.naucourse.Utils;

import java.io.Serializable;

/**
 * Created by 10696 on 2018/3/10.
 * 储存下一节课的信息
 */

public class NextCourse implements Serializable {
    private String courseId;
    private String courseName;
    private String courseTeacher;
    private String courseLocation;
    private String courseTime;
    private int dataVersionCode = 0;

    public NextCourse() {
        this.courseId = null;
        this.courseName = null;
        this.courseTeacher = null;
        this.courseLocation = null;
        this.courseTime = null;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseTeacher() {
        return courseTeacher;
    }

    public void setCourseTeacher(String courseTeacher) {
        this.courseTeacher = courseTeacher;
    }

    public String getCourseLocation() {
        return courseLocation;
    }

    public void setCourseLocation(String courseLocation) {
        this.courseLocation = courseLocation;
    }

    public String getCourseTime() {
        return courseTime;
    }

    public void setCourseTime(String courseTime) {
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
