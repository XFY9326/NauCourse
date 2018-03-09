package tool.xfy9326.naucourse.Utils;

/**
 * Created by xfy9326 on 18-2-21.
 * 课程信息详情
 */

public class CourseDetail {
    private String weeks[] = null;
    private int weekDay = 0;
    private String courseTime[] = null;
    private int weekMode = 0;
    private String location = null;

    public CourseDetail() {
    }

    public String[] getCourseTime() {
        return courseTime;
    }

    public void setCourseTime(String[] courseTime) {
        this.courseTime = courseTime;
    }

    public String[] getWeeks() {
        return weeks;
    }

    public void setWeeks(String[] weeks) {
        this.weeks = weeks;
    }

    public int getWeekMode() {
        return weekMode;
    }

    public void setWeekMode(int weekMode) {
        this.weekMode = weekMode;
    }

    public int getWeekDay() {
        return weekDay;
    }

    public void setWeekDay(int weekDay) {
        this.weekDay = weekDay;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
