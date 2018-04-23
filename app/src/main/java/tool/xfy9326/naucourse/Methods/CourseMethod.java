package tool.xfy9326.naucourse.Methods;

import android.content.Context;
import android.preference.PreferenceManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Utils.Course;
import tool.xfy9326.naucourse.Utils.CourseDetail;
import tool.xfy9326.naucourse.Utils.NextCourse;
import tool.xfy9326.naucourse.Utils.SchoolTime;

/**
 * Created by xfy9326 on 18-2-22.
 * 课程表课程排序方法
 */

public class CourseMethod {
    private final Context context;
    private final ArrayList<Course> courses;
    private SchoolTime schoolTime;
    private int weekNum;

    // 列 行
    private String[][] table;
    private String[][] id_table;
    private List<String> course_time;

    public CourseMethod(Context context, ArrayList<Course> courses, SchoolTime schoolTime) {
        this.context = context;
        this.courses = courses;
        this.course_time = null;
        updateTableCourse(schoolTime, false);
    }

    /**
     * 获取下一节课的信息
     * 仅在内部使用
     *
     * @param context            Context
     * @param this_week_table    课程信息二维数组
     * @param this_week_id_table 课程ID二维数组
     * @param courses            课程信息列表
     * @return NextCourse对象
     */
    private static NextCourse getNextClass(Context context, String[][] this_week_table, String[][] this_week_id_table, ArrayList<Course> courses) {
        NextCourse nextCourse = new NextCourse();
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        int weekDayNum = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (weekDayNum == 0) {
            weekDayNum = 7;
        }

        String[] today = this_week_table[weekDayNum];
        String[] todayId = this_week_id_table[weekDayNum];
        String[] startTimes = context.getResources().getStringArray(R.array.course_start_time);
        String[] times = context.getResources().getStringArray(R.array.course_finish_time);
        long nowTime = calendar.getTimeInMillis();
        String lastId = "";
        String findCourseId = "";
        String course_startTime = "";
        String course_endTime = "";
        long todayFinalCourseTime = 0;
        String mid = "\n";
        if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Config.PREFERENCE_SHOW_WIDE_TABLE, Config.DEFAULT_PREFERENCE_SHOW_WIDE_TABLE)) {
            mid = "\n\n";
        }
        for (int i = 0; i < times.length; i++) {
            if (today[i + 1] != null) {
                String[] time_temp = times[i].split(":");
                calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(time_temp[0]));
                calendar.set(Calendar.MINUTE, Integer.valueOf(time_temp[1]));
                long courseTime = calendar.getTimeInMillis();
                if (courseTime > nowTime) {
                    if (!findCourseId.equals(todayId[i + 1]) && !findCourseId.equals("")) {
                        break;
                    }
                    nextCourse.setCourseName(today[i + 1].substring(0, today[i + 1].indexOf(mid)));
                    nextCourse.setCourseLocation(today[i + 1].substring(today[i + 1].indexOf("@") + 1));
                    nextCourse.setCourseId(todayId[i + 1]);
                    course_endTime = times[i];
                    if (!lastId.equals(todayId[i + 1])) {
                        course_startTime = startTimes[i];
                        findCourseId = todayId[i + 1];
                    }
                    lastId = todayId[i + 1];
                }
                todayFinalCourseTime = courseTime;
            }
        }

        nextCourse.setDataVersionCode(Config.DATA_VERSION_NEXT_COURSE);

        if (nowTime > todayFinalCourseTime) {
            return nextCourse;
        }

        nextCourse.setCourseTime(course_startTime + "~" + course_endTime);

        if (nextCourse.getCourseId() != null) {
            for (Course course : courses) {
                if (course.getCourseId().equals(nextCourse.getCourseId())) {
                    nextCourse.setCourseTeacher(course.getCourseTeacher());
                    break;
                }
            }
        }
        System.gc();
        return nextCourse;
    }

    /**
     * 获取下一节课的信息
     * 对外接口
     *
     * @param weekNum 周数
     * @return 下一节课的信息
     */
    public NextCourse getNextClass(int weekNum) {
        if (this.weekNum != weekNum || table == null) {
            getTable(weekNum, schoolTime.getStartTime());
        }
        return getNextClass(context, table, id_table, courses);
    }

    /**
     * 获取课程信息二维数组
     *
     * @return 课程信息二维数组
     */
    public String[][] getTableData() {
        return table;
    }

    /**
     * 获取课程信息对应ID的二维数组
     *
     * @return 对应ID的二维数组
     */
    public String[][] getTableIdData() {
        return id_table;
    }

    /**
     * 设置需要计算的周数并计算全部课程
     *
     * @param schoolTime  SchoolTime对象
     * @param noCheckSame 不在检查到周数相同时放弃计算数据
     */
    public void updateTableCourse(SchoolTime schoolTime, boolean noCheckSame) {
        this.schoolTime = schoolTime;
        //假期中默认显示第一周
        int weekNum = schoolTime.getWeekNum();
        if (weekNum == 0) {
            weekNum = 1;
        }
        if (weekNum != this.weekNum || noCheckSame) {
            if (course_time == null) {
                course_time = TimeMethod.getCourseTimeArray(context);
            }
            List<String> week_day = TimeMethod.getWeekDayArray(context, weekNum, schoolTime.getStartTime());
            getTable(weekNum, schoolTime.getStartTime());
            setTableTimeLine(course_time, week_day);
            this.weekNum = weekNum;
        }
    }

    //设置表格的上课时间列与上课日期行
    private void setTableTimeLine(List<String> course_time, List<String> course_weekDay) {
        table[0][0] = context.getString(R.string.date);
        for (int i = 0; i < course_time.size(); i++) {
            table[0][i + 1] = course_time.get(i).trim();
        }
        for (int i = 0; i < Config.MAX_WEEK_DAY; i++) {
            table[i + 1][0] = course_weekDay.get(i).trim();
        }
    }

    //获取该周信息表格
    synchronized private void getTable(int weekNum, String startSchoolDate) {
        boolean isDoubleWeek = weekNum % 2 == 0;

        table = new String[Config.MAX_WEEK_DAY + 1][Config.MAX_DAY_COURSE + 1];
        id_table = new String[Config.MAX_WEEK_DAY + 1][Config.MAX_DAY_COURSE + 1];
        for (Course course : courses) {
            CourseDetail[] courseDetail = course.getCourseDetail();
            for (CourseDetail detail : courseDetail) {
                int mode = detail.getWeekMode();
                if (isDoubleWeek && mode == Config.COURSE_DETAIL_WEEKMODE_DOUBLE) {
                    if (weekCheck(detail, weekNum)) {
                        courseSet(course, detail, weekNum, startSchoolDate);
                    }
                    continue;
                }
                if (!isDoubleWeek && mode == Config.COURSE_DETAIL_WEEKMODE_SINGLE) {
                    if (weekCheck(detail, weekNum)) {
                        courseSet(course, detail, weekNum, startSchoolDate);
                    }
                    continue;
                }
                if (mode == Config.COURSE_DETAIL_WEEKMODE_ONCE_MORE || mode == Config.COURSE_DETAIL_WEEKMODE_ONCE) {
                    if (weekCheck(detail, weekNum)) {
                        courseSet(course, detail, weekNum, startSchoolDate);
                    }
                }
            }
        }

    }

    //检查是否在该周上课
    private boolean weekCheck(CourseDetail detail, int weekNum) {
        String[] weeks = detail.getWeeks();
        for (String week : weeks) {
            if (week.contains("-")) {
                String[] weekdays = week.split("-");
                int start = Integer.valueOf(weekdays[0]);
                int end = Integer.valueOf(weekdays[1]);
                if (weekNum >= start && weekNum <= end) {
                    return true;
                }
            } else {
                if (weekNum == Integer.valueOf(week)) {
                    return true;
                }
            }
        }
        return false;
    }

    //设置课程到二维数组
    private void courseSet(Course course, CourseDetail detail, int weekNum, String startSchoolDate) {
        String[] courseTimes = detail.getCourseTime();
        int startSchoolWeekDay = getStartSchoolWeekDay(startSchoolDate);
        for (String courseTime : courseTimes) {
            if (weekNum == 1 && detail.getWeekDay() < startSchoolWeekDay) {
                continue;
            }
            if (courseTime.contains("-")) {
                String[] time = courseTime.split("-");
                int t_start = Integer.valueOf(time[0]);
                int t_end = Integer.valueOf(time[1]);
                for (int t = t_start; t <= t_end; t++) {
                    table[detail.getWeekDay()][t] = getShowDetail(course, detail);
                    id_table[detail.getWeekDay()][t] = course.getCourseId();
                }
            } else {
                table[detail.getWeekDay()][Integer.valueOf(courseTime)] = getShowDetail(course, detail);
                id_table[detail.getWeekDay()][Integer.valueOf(courseTime)] = course.getCourseId();
            }
        }
    }

    //获取需要显示的信息
    private String getShowDetail(Course course, CourseDetail courseDetail) {
        String mid = "\n";
        if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Config.PREFERENCE_SHOW_WIDE_TABLE, Config.DEFAULT_PREFERENCE_SHOW_WIDE_TABLE)) {
            mid = "\n\n";
        }
        return course.getCourseName().trim() + mid + "@" + courseDetail.getLocation().trim();
    }

    //获取开学是周几
    private int getStartSchoolWeekDay(String startSchoolDate) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
            Date date = simpleDateFormat.parse(startSchoolDate);
            Calendar calendar = Calendar.getInstance(Locale.CHINA);
            calendar.setTime(date);
            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                return 7;
            } else {
                return calendar.get(Calendar.DAY_OF_WEEK) - 1;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 1;
    }

}
