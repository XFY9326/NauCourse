package tool.xfy9326.naucourse.Methods;

import android.content.Context;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.widget.GridLayout;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

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
 * 课程表课程排序与展示方法
 */

public class CourseMethod {
    private final Context context;
    private final ArrayList<Course> courses;
    private final SchoolTime schoolTime;

    private OnCourseTableItemClickListener onCourseTableClick;
    private int weekNum;
    private GridLayout course_table_layout;
    // 列 行
    private String[][] table;
    private String[][] id_table;
    private List<String> course_time;
    private int parent_width = 0;

    public CourseMethod(Context context, int parent_width, ArrayList<Course> courses, SchoolTime schoolTime) {
        this.context = context;
        this.courses = courses;
        this.schoolTime = schoolTime;
        //假期中默认显示第一周
        this.weekNum = schoolTime.getWeekNum();
        if (weekNum == 0) {
            weekNum = 1;
        }
        this.course_table_layout = null;
        this.onCourseTableClick = null;
        this.course_time = null;
        this.parent_width = parent_width;
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
                    nextCourse.setCourseName(today[i + 1].substring(0, today[i + 1].indexOf("\n")));
                    nextCourse.setCourseLocation(today[i + 1].substring(today[i + 1].indexOf("\n") + 2));
                    nextCourse.setCourseId(todayId[i + 1]);
                    course_endTime = times[i + 1];
                    if (!lastId.equals(todayId[i + 1])) {
                        course_startTime = startTimes[i];
                        findCourseId = todayId[i + 1];
                    }
                    lastId = todayId[i + 1];
                }
                todayFinalCourseTime = courseTime;
            }
        }
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

    public void setTableView(GridLayout gridLayout) {
        this.course_table_layout = gridLayout;
        loadView();
    }

    public void updateCourseTableView(int weekNum) {
        this.weekNum = weekNum;
        loadView();
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

    public void setOnCourseTableClickListener(OnCourseTableItemClickListener onCourseTableClick) {
        this.onCourseTableClick = onCourseTableClick;
    }

    synchronized private void loadView() {
        if (course_table_layout != null && weekNum != 0 && parent_width != 0) {
            setTableCourse();

            course_table_layout.removeAllViews();

            boolean showWeekend = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Config.PREFERENCE_SHOW_WEEKEND, Config.DEFAULT_PREFERENCE_SHOW_WEEKEND);

            int row_max = Config.MAX_DAY_COURSE + 1;
            int col_max = showWeekend ? Config.MAX_WEEK_DAY + 1 : Config.MAX_WEEK_DAY - 1;

            course_table_layout.setColumnCount(col_max);
            course_table_layout.setRowCount(row_max);
            course_table_layout.setMinimumWidth(parent_width);

            for (int col = 0; col < col_max; col++) {

                for (int row = 0; row < row_max; row++) {
                    int merge = 1;
                    String text = table[col][row];

                    if (col > 0 && row > 0) {
                        if (text != null && !text.isEmpty()) {
                            for (merge = 1; row + merge < row_max && merge <= row_max; merge++) {
                                if (table[col][row + merge] == null || !table[col][row + merge].equalsIgnoreCase(text)) {
                                    break;
                                }
                            }
                        }
                        row += merge - 1;
                    }

                    int weight_col = col == 0 ? 1 : 2;
                    GridLayout.Spec col_merge = GridLayout.spec(GridLayout.UNDEFINED, 1, weight_col);
                    GridLayout.Spec row_merge = GridLayout.spec(GridLayout.UNDEFINED, merge, 1);
                    GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams(row_merge, col_merge);
                    layoutParams.setGravity(Gravity.FILL);
                    layoutParams.setMargins(2, 2, 2, 2);

                    int width = col == 0 ? 0 : parent_width / col_max;
                    course_table_layout.addView(getCellView(text, width, col, row), layoutParams);
                }
            }
        }
    }

    private View getCellView(String text, int width, final int col, final int row) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setBackgroundColor(Color.WHITE);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (Course course : courses) {
                    if (course.getCourseId().equals(id_table[col][row])) {
                        onCourseTableClick.OnItemClick(course);
                        break;
                    }
                }
            }
        });

        TextView textView = new TextView(context);
        if (col == 0) {
            textView.setPadding(10, 5, 3, 10);
        } else {
            textView.setPadding(3, 5, 3, 5);
            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Config.PREFERENCE_SHOW_WIDE_TABLE, Config.DEFAULT_SHOW_WIDE_TABLE)) {
                width *= 2;
            }
            textView.setLayoutParams(new ViewGroup.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        textView.setGravity(Gravity.CENTER);
        textView.setSingleLine(false);
        textView.setText(text);
        textView.setTextSize(13);

        linearLayout.addView(textView);
        return linearLayout;
    }

    //表格赋值
    private void setTableCourse() {
        if (course_time == null) {
            course_time = BaseMethod.getCourseTimeArray(context);
        }
        List<String> week_day = BaseMethod.getWeekDayArray(context, weekNum, schoolTime.getStartTime());
        getTable(weekNum, schoolTime.getStartTime());
        setTableTimeLine(course_time, week_day);
        System.gc();
    }

    //设置表格的上课时间列
    private void setTableTimeLine(List<String> course_time, List<String> course_weekDay) {
        table[0][0] = context.getString(R.string.date);
        for (int i = 0; i < course_time.size(); i++) {
            table[0][i + 1] = course_time.get(i);
        }
        for (int i = 0; i < Config.MAX_WEEK_DAY; i++) {
            table[i + 1][0] = course_weekDay.get(i);
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
                return weekNum == Integer.valueOf(week);
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

    private String getShowDetail(Course course, CourseDetail courseDetail) {
        return (course.getCourseName()).trim() + "\n" + "@" + courseDetail.getLocation().trim();
    }

    public interface OnCourseTableItemClickListener {
        void OnItemClick(Course course);
    }

}
