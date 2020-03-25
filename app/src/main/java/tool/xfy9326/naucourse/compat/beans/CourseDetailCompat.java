package tool.xfy9326.naucourse.compat.beans;

import androidx.annotation.Nullable;

@SuppressWarnings({"FieldCanBeLocal", "CanBeFinal"})
public class CourseDetailCompat {
    //Num-Num,Num...
    private String[] weeks = null;
    private int weekDay = 0;
    //Num-Num,Num...
    private String[] courseTime = null;
    private int weekMode = 0;
    @Nullable
    private String location = null;

    @Nullable
    public String[] getCourseTime() {
        return courseTime;
    }

    @Nullable
    public String[] getWeeks() {
        return weeks;
    }

    public int getWeekMode() {
        return weekMode;
    }

    public int getWeekDay() {
        return weekDay;
    }

    @Nullable
    public String getLocation() {
        return location;
    }
}