package tool.xfy9326.naucourse.Utils;

import android.support.annotation.Nullable;

import java.io.Serializable;

/**
 * Created by xfy9326 on 18-2-21.
 * 课程信息详情
 */

public class CourseDetail implements Serializable {
    //Num-Num,Num...
    @Nullable
    private String weeks[] = null;
    private int weekDay = 0;
    //Num-Num,Num...
    @Nullable
    private String courseTime[] = null;
    private int weekMode = 0;
    @Nullable
    private String location = null;

    public CourseDetail() {
    }

    @Nullable
    public String[] getCourseTime() {
        return courseTime;
    }

    public void setCourseTime(@Nullable String[] courseTime) {
        this.courseTime = courseTime;
    }

    @Nullable
    public String[] getWeeks() {
        return weeks;
    }

    public void setWeeks(@Nullable String[] weeks) {
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

    @Nullable
    public String getLocation() {
        return location;
    }

    public void setLocation(@Nullable String location) {
        this.location = location;
    }
}
