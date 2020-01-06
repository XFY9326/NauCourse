package tool.xfy9326.naucourse.beans.course;

import java.io.Serializable;

@SuppressWarnings("unused")
public class TodayCourses implements Serializable {
    private String[] courses;
    private String[] coursesTime;
    private NextCourse nextCourse;

    public NextCourse getNextCourse() {
        return nextCourse;
    }

    public void setNextCourse(NextCourse nextCourse) {
        this.nextCourse = nextCourse;
    }

    public String[] getCourses() {
        return courses;
    }

    public void setCourses(String[] courses) {
        this.courses = courses;
    }

    public String[] getCoursesTime() {
        return coursesTime;
    }

    public void setCoursesTime(String[] coursesTime) {
        this.coursesTime = coursesTime;
    }
}
