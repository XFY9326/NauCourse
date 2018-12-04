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
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    private ArrayList<Course> courses;
    private SchoolTime schoolTime;
    private int weekNum;

    // 列 行
    private String[][] table;
    private String[][] id_table;
    private boolean[][] this_week_no_show_table;
    @Nullable
    private List<String> course_time;

    public CourseMethod(Context context, ArrayList<Course> courses, @NonNull SchoolTime schoolTime) {
        this.context = context;
        this.courses = courses;
        this.course_time = null;
        updateTableCourse(courses, schoolTime, false);
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
    @NonNull
    private static NextCourse getNextClass(@NonNull Context context, String[][] this_week_table, String[][] this_week_id_table, boolean[][] this_week_no_show_table, @NonNull ArrayList<Course> courses) {
        NextCourse nextCourse = new NextCourse();
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        int weekDayNum = calendar.get(Calendar.DAY_OF_WEEK) == 1 ? 7 : calendar.get(Calendar.DAY_OF_WEEK) - 1;

        String[] today = this_week_table[weekDayNum];
        String[] todayId = this_week_id_table[weekDayNum];
        boolean[] todayNoShow = this_week_no_show_table[weekDayNum];
        String[] startTimes = context.getResources().getStringArray(R.array.course_start_time);
        String[] finishTimes = context.getResources().getStringArray(R.array.course_finish_time);
        long nowTime = calendar.getTimeInMillis();
        String lastId = "";
        String findCourseId = "";
        String course_startTime = "";
        String course_endTime = "";
        long todayFinalCourseTime = 0;

        for (int i = 0; i < finishTimes.length; i++) {
            if (today[i + 1] != null && !todayNoShow[i + 1]) {
                String[] time_temp = finishTimes[i].split(":");
                calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(time_temp[0]));
                calendar.set(Calendar.MINUTE, Integer.valueOf(time_temp[1]));
                long courseTime = calendar.getTimeInMillis();
                if (courseTime > nowTime) {
                    if (!findCourseId.equals(todayId[i + 1]) && !findCourseId.equals("")) {
                        break;
                    }
                    nextCourse.setCourseLocation(today[i + 1].substring(today[i + 1].indexOf("@") + 1));
                    nextCourse.setCourseId(todayId[i + 1]);
                    course_endTime = finishTimes[i];
                    if (!lastId.equals(todayId[i + 1])) {
                        findCourseId = todayId[i + 1];
                    }
                    lastId = todayId[i + 1];
                }
                todayFinalCourseTime = courseTime;
            }
        }

        //课程开始时间回溯
        for (int i = 0; i < finishTimes.length; i++) {
            if (todayId[i + 1] != null && !todayNoShow[i + 1] && todayId[i + 1].equalsIgnoreCase(nextCourse.getCourseId())) {
                course_startTime = startTimes[i];
                break;
            }
        }

        nextCourse.setDataVersionCode(Config.DATA_VERSION_NEXT_COURSE);

        if (nowTime > todayFinalCourseTime) {
            return nextCourse;
        }

        nextCourse.setCourseTime(course_startTime + "~" + course_endTime);

        if (nextCourse.getCourseId() != null) {
            for (Course course : courses) {
                if (Objects.requireNonNull(course.getCourseId()).equals(nextCourse.getCourseId())) {
                    nextCourse.setCourseTeacher(course.getCourseTeacher());
                    nextCourse.setCourseName(course.getCourseName());
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
    @NonNull
    public NextCourse getNextClass(int weekNum) {
        if (this.weekNum != weekNum || table == null) {
            getTable(weekNum, schoolTime.getStartTime());
        }
        return getNextClass(context, table, id_table, this_week_no_show_table, courses);
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
     * 获取单双周课程信息
     *
     * @return 对应的二维数组
     */
    public boolean[][] getTableShowData() {
        return this_week_no_show_table;
    }

    /**
     * 设置需要计算的周数并计算全部课程
     *
     * @param schoolTime  SchoolTime对象
     * @param noCheckSame 不在检查到周数相同时放弃计算数据
     */
    public void updateTableCourse(ArrayList<Course> courses, @NonNull SchoolTime schoolTime, boolean noCheckSame) {
        this.schoolTime = schoolTime;
        this.courses = courses;
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
    private void setTableTimeLine(List<String> course_time, @NonNull List<String> course_weekDay) {
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
        boolean showAllWeekMode = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Config.PREFERENCE_SHOW_NO_THIS_WEEK_CLASS, Config.DEFAULT_PREFERENCE_SHOW_NO_THIS_WEEK_CLASS);

        table = new String[Config.MAX_WEEK_DAY + 1][Config.MAX_DAY_COURSE + 1];
        id_table = new String[Config.MAX_WEEK_DAY + 1][Config.MAX_DAY_COURSE + 1];
        this_week_no_show_table = new boolean[Config.MAX_WEEK_DAY + 1][Config.MAX_DAY_COURSE + 1];
        String termCode = TimeMethod.getNowShowTerm(context, schoolTime);
        for (Course course : courses) {
            //过滤非显示的学期的课程
            if (termCode != null && !termCode.equals(course.getCourseTerm())) {
                continue;
            }
            CourseDetail[] courseDetail = course.getCourseDetail();
            for (CourseDetail detail : Objects.requireNonNull(courseDetail)) {
                int mode = detail.getWeekMode();
                if (showAllWeekMode) {
                    boolean defaultNoShow = !weekCheck(detail, weekNum) || isDoubleWeek && mode == Config.COURSE_DETAIL_WEEKMODE_SINGLE || !isDoubleWeek && mode == Config.COURSE_DETAIL_WEEKMODE_DOUBLE;
                    courseSet(course, detail, weekNum, startSchoolDate, defaultNoShow);
                    continue;
                }
                if (isDoubleWeek && mode == Config.COURSE_DETAIL_WEEKMODE_DOUBLE) {
                    if (weekCheck(detail, weekNum)) {
                        courseSet(course, detail, weekNum, startSchoolDate, false);
                    }
                    continue;
                }
                if (!isDoubleWeek && mode == Config.COURSE_DETAIL_WEEKMODE_SINGLE) {
                    if (weekCheck(detail, weekNum)) {
                        courseSet(course, detail, weekNum, startSchoolDate, false);
                    }
                    continue;
                }
                if (mode == Config.COURSE_DETAIL_WEEKMODE_ONCE_MORE || mode == Config.COURSE_DETAIL_WEEKMODE_ONCE) {
                    if (weekCheck(detail, weekNum)) {
                        courseSet(course, detail, weekNum, startSchoolDate, false);
                    }
                }
            }
        }

    }

    //检查是否在该周上课
    private boolean weekCheck(CourseDetail detail, int weekNum) {
        String[] weeks = detail.getWeeks();
        for (String week : Objects.requireNonNull(weeks)) {
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
    private void courseSet(@NonNull Course course, CourseDetail detail, int weekNum, String startSchoolDate, boolean defaultNoShowClass) {
        String[] courseTimes = detail.getCourseTime();
        int startSchoolWeekDay = getStartSchoolWeekDay(startSchoolDate);
        for (String courseTime : Objects.requireNonNull(courseTimes)) {
            if (weekNum == 1 && detail.getWeekDay() < startSchoolWeekDay) {
                continue;
            }
            if (courseTime.contains("-")) {
                String[] time = courseTime.split("-");
                int t_start = Integer.valueOf(time[0]);
                int t_end = Integer.valueOf(time[1]);
                for (int t = t_start; t <= t_end; t++) {
                    if (table[detail.getWeekDay()][t] != null && defaultNoShowClass) {
                        continue;
                    }
                    table[detail.getWeekDay()][t] = getShowDetail(course, detail);
                    id_table[detail.getWeekDay()][t] = course.getCourseId();
                    this_week_no_show_table[detail.getWeekDay()][t] = defaultNoShowClass;
                }
            } else {
                if (table[detail.getWeekDay()][Integer.valueOf(courseTime)] != null && defaultNoShowClass) {
                    continue;
                }
                table[detail.getWeekDay()][Integer.valueOf(courseTime)] = getShowDetail(course, detail);
                id_table[detail.getWeekDay()][Integer.valueOf(courseTime)] = course.getCourseId();
                this_week_no_show_table[detail.getWeekDay()][Integer.valueOf(courseTime)] = defaultNoShowClass;
            }
        }
    }

    //获取需要显示的信息
    private String getShowDetail(@NonNull Course course, @NonNull CourseDetail courseDetail) {
        String mid = "\n";
        if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Config.PREFERENCE_SHOW_WIDE_TABLE, Config.DEFAULT_PREFERENCE_SHOW_WIDE_TABLE)) {
            mid = "\n\n";
        }
        return Objects.requireNonNull(course.getCourseName()).replace("@", "").trim() + mid + "@" + Objects.requireNonNull(courseDetail.getLocation()).replace("@", "").trim();
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
