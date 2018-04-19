package tool.xfy9326.naucourse.Utils;

/**
 * Created by xfy9326 on 18-2-21.
 * 课程信息
 */

public class Course {
    private String courseId = null;
    private String courseName = null;
    private String courseTeacher = null;
    private String courseClass = null;
    private String courseCombinedClass = null;
    private String courseScore = null;
    private String courseType = null;
    private int dataVersionCode = 0;

    private CourseDetail[] courseDetail = null;

    public Course() {
    }

    public String getCourseClass() {
        return courseClass;
    }

    public void setCourseClass(String courseClass) {
        this.courseClass = courseClass;
    }

    public String getCourseCombinedClass() {
        return courseCombinedClass;
    }

    public void setCourseCombinedClass(String courseCombinedClass) {
        this.courseCombinedClass = courseCombinedClass;
    }

    public String getCourseScore() {
        return courseScore;
    }

    public void setCourseScore(String courseScore) {
        this.courseScore = courseScore;
    }

    public String getCourseType() {
        return courseType;
    }

    public void setCourseType(String courseType) {
        this.courseType = courseType;
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

    public int getDataVersionCode() {
        return dataVersionCode;
    }

    public void setDataVersionCode(int dataVersionCode) {
        this.dataVersionCode = dataVersionCode;
    }
}
