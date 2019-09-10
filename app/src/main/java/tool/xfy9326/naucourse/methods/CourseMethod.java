package tool.xfy9326.naucourse.methods;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.utils.Course;
import tool.xfy9326.naucourse.utils.CourseDetail;
import tool.xfy9326.naucourse.utils.NextCourse;
import tool.xfy9326.naucourse.utils.SchoolTime;

/**
 * Created by xfy9326 on 18-2-22.
 * 课程表课程排序方法
 */

public class CourseMethod {
    private final Context context;
    private ArrayList<Course> courses;
    private SchoolTime schoolTime;
    private int weekNum;

    /**
     * 列 行
     */
    private String[][] table;
    private String[][] idTable;
    private boolean[][] thisWeekNoShowTable;
    @Nullable
    private List<String> courseTime;

    public CourseMethod(Context context, ArrayList<Course> courses, @NonNull SchoolTime schoolTime) {
        this.context = context;
        this.courses = courses;
        this.courseTime = null;
        updateTableCourse(courses, schoolTime, false);
    }

    /**
     * 获取下一节课的信息
     * 仅在内部使用
     *
     * @param context         Context
     * @param thisWeekTable   课程信息二维数组
     * @param thisWeekIdTable 课程ID二维数组
     * @param courses         课程信息列表
     * @return NextCourse对象
     */
    @NonNull
    private static NextCourse getNextClass(@NonNull Context context, String[][] thisWeekTable, String[][] thisWeekIdTable, boolean[][] thisWeekNoShowTable, @NonNull ArrayList<Course> courses) {
        NextCourse nextCourse = new NextCourse();
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        int weekDayNum = calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ? 7 : calendar.get(Calendar.DAY_OF_WEEK) - 1;

        String[] today = thisWeekTable[weekDayNum];
        String[] todayId = thisWeekIdTable[weekDayNum];
        boolean[] todayNoShow = thisWeekNoShowTable[weekDayNum];
        String[] startTimes = context.getResources().getStringArray(R.array.course_start_time);
        String[] finishTimes = context.getResources().getStringArray(R.array.course_finish_time);
        long nowTime = calendar.getTimeInMillis();
        String lastId = "";
        String findCourseId = "";
        String courseStartTime = "";
        String courseEndTime = "";
        long todayFinalCourseTime = 0;

        for (int i = 0; i < finishTimes.length; i++) {
            if (today[i + 1] != null && !todayNoShow[i + 1]) {
                String[] timeTemp = finishTimes[i].split(":");
                calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(timeTemp[0]));
                calendar.set(Calendar.MINUTE, Integer.valueOf(timeTemp[1]));
                long courseTime = calendar.getTimeInMillis();
                if (courseTime > nowTime) {
                    if (!findCourseId.equals(todayId[i + 1]) && !"".equals(findCourseId)) {
                        break;
                    }
                    nextCourse.setCourseLocation(today[i + 1].substring(today[i + 1].indexOf("@") + 1));
                    nextCourse.setCourseId(todayId[i + 1]);
                    courseEndTime = finishTimes[i];
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
                courseStartTime = startTimes[i];
                break;
            }
        }

        nextCourse.setDataVersionCode(Config.DATA_VERSION_NEXT_COURSE);

        if (nowTime > todayFinalCourseTime) {
            return nextCourse;
        }

        nextCourse.setCourseTime(courseStartTime + "~" + courseEndTime);

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

    public static boolean hasWeekendCourse(ArrayList<Course> courses) {
        for (Course course : courses) {
            if (course != null && course.getCourseDetail() != null) {
                for (CourseDetail courseDetail : course.getCourseDetail()) {
                    if (courseDetail.getWeekDay() == 6 || courseDetail.getWeekDay() == 7) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Nullable
    static String getCourseJson(@NonNull Course course) {
        try {
            return new Gson().toJson(course);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    static Course getCourseByJson(@NonNull String json) {
        try {
            Type type = new TypeToken<Course>() {
            }.getType();
            return new Gson().fromJson(json, type);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return null;
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
        return getNextClass(context, table, idTable, thisWeekNoShowTable, courses);
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
        return idTable;
    }

    /**
     * 获取单双周课程信息
     *
     * @return 对应的二维数组
     */
    public boolean[][] getTableShowData() {
        return thisWeekNoShowTable;
    }

    /**
     * 设置需要计算的周数并计算全部课程
     *
     * @param schoolTime SchoolTime对象
     * @param checkSame  检查到周数相同时放弃计算数据
     */
    public void updateTableCourse(ArrayList<Course> courses, @NonNull SchoolTime schoolTime, boolean checkSame) {
        this.schoolTime = schoolTime;
        this.courses = courses;
        //假期中默认显示第一周
        int weekNum = schoolTime.getWeekNum();
        if (weekNum == 0) {
            weekNum = 1;
        }
        if (checkSame) {
            if (weekNum == this.weekNum) {
                return;
            }
        }
        List<String> weekDayArray = TimeMethod.getWeekDayArray(context, weekNum, schoolTime.getStartTime());
        getTable(weekNum, schoolTime.getStartTime());
        if (courseTime == null) {
            courseTime = TimeMethod.getCourseTimeArray(context);
        }
        setTableTimeLine(courseTime, weekDayArray);
        this.weekNum = weekNum;
    }

    /**
     * 设置表格的上课时间列与上课日期行
     *
     * @param courseTime    课程时间
     * @param courseWeekDay 课程星期
     */
    private void setTableTimeLine(List<String> courseTime, @NonNull List<String> courseWeekDay) {
        table[0][0] = context.getString(R.string.date);
        for (int i = 0; i < courseTime.size(); i++) {
            table[0][i + 1] = courseTime.get(i).trim();
        }
        for (int i = 0; i < Config.MAX_WEEK_DAY; i++) {
            table[i + 1][0] = courseWeekDay.get(i).trim();
        }
    }

    /**
     * 获取该周信息表格
     *
     * @param weekNum         星期数
     * @param startSchoolDate 开学日期
     */
    synchronized private void getTable(int weekNum, String startSchoolDate) {
        boolean isDoubleWeek = weekNum % 2 == 0;
        boolean showAllWeekMode = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Config.PREFERENCE_SHOW_NO_THIS_WEEK_CLASS, Config.DEFAULT_PREFERENCE_SHOW_NO_THIS_WEEK_CLASS);

        table = new String[Config.MAX_WEEK_DAY + 1][Config.MAX_DAY_COURSE + 1];
        idTable = new String[Config.MAX_WEEK_DAY + 1][Config.MAX_DAY_COURSE + 1];
        thisWeekNoShowTable = new boolean[Config.MAX_WEEK_DAY + 1][Config.MAX_DAY_COURSE + 1];
        String termCode = TimeMethod.getNowShowTerm(context, schoolTime);
        for (Course course : courses) {
            //过滤非显示的学期的课程
            if (termCode != null && !termCode.equals(course.getCourseTerm())) {
                continue;
            }
            CourseDetail[] courseDetail = course.getCourseDetail();
            if (courseDetail != null) {
                for (CourseDetail detail : courseDetail) {
                    if (detail != null) {
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
                int tStart = Integer.valueOf(time[0]);
                int tEnd = Integer.valueOf(time[1]);
                for (int t = tStart; t <= tEnd; t++) {
                    if (table[detail.getWeekDay()][t] != null && defaultNoShowClass) {
                        continue;
                    }
                    table[detail.getWeekDay()][t] = getShowDetail(course, detail);
                    idTable[detail.getWeekDay()][t] = course.getCourseId();
                    thisWeekNoShowTable[detail.getWeekDay()][t] = defaultNoShowClass;
                }
            } else {
                int courseTimeInt = Integer.valueOf(courseTime);
                if (courseTimeInt > 0 && courseTimeInt <= Config.MAX_DAY_COURSE) {
                    if (table[detail.getWeekDay()][courseTimeInt] != null && defaultNoShowClass) {
                        continue;
                    }
                    table[detail.getWeekDay()][courseTimeInt] = getShowDetail(course, detail);
                    idTable[detail.getWeekDay()][courseTimeInt] = course.getCourseId();
                    thisWeekNoShowTable[detail.getWeekDay()][courseTimeInt] = defaultNoShowClass;
                }
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
            Date date = TimeMethod.parseDateSDF(startSchoolDate);
            Calendar calendar = Calendar.getInstance(Locale.CHINA);
            calendar.setTime(date);
            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                return 7;
            } else {
                return calendar.get(Calendar.DAY_OF_WEEK) - 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }
}
