package tool.xfy9326.naucourse.Utils;

/**
 * Created by xfy9326 on 18-2-21.
 */

public class Course {
    private String courseId = null;
    private String courseName = null;
    private String courseTeacher = null;
    private CourseDetail[] courseDetail = null;

    public Course() {
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

    public CourseDetail[] getCourseDetail() {
        return courseDetail;
    }

    public void setCourseDetail(CourseDetail[] courseDetail) {
        this.courseDetail = courseDetail;
    }
}
