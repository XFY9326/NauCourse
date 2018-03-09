package tool.xfy9326.naucourse.Methods;

import android.content.Context;
import android.graphics.Paint;

import com.bin.david.form.core.SmartTable;
import com.bin.david.form.data.column.Column;
import com.bin.david.form.data.table.TableData;
import com.bin.david.form.listener.OnColumnItemClickListener;

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
import tool.xfy9326.naucourse.Utils.SchoolTime;
import tool.xfy9326.naucourse.Utils.TableLine;

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
    private SmartTable<TableLine> smartTable;
    private boolean loadSuccess;
    // 列 行
    private String[][] table;
    private String[][] id_table;
    private List<TableLine> tableLines;
    private TableData<TableLine> tableData;

    public CourseMethod(Context context, ArrayList<Course> courses, SchoolTime schoolTime) {
        this.context = context;
        this.courses = courses;
        this.schoolTime = schoolTime;
        this.weekNum = schoolTime.getWeekNum();
        if (weekNum == 0) {
            weekNum = 1;
        }
        this.smartTable = null;
        this.onCourseTableClick = null;
        this.loadSuccess = false;
        this.tableLines = null;
        this.tableData = null;
    }

    private static String[] getNextClass(Context context, String[][] this_week_table, String[][] this_week_id_table, ArrayList<Course> courses) {
        String[] result = new String[5];
        //仅限周一到周五的计算
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        int weekDayNum = calendar.get(Calendar.DAY_OF_WEEK) - 1;

        if (weekDayNum > 0 && weekDayNum < 6) {
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
                if (today[i] != null) {
                    String[] time_temp = times[i].split(":");
                    calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(time_temp[0]));
                    calendar.set(Calendar.MINUTE, Integer.valueOf(time_temp[1]));
                    long courseTime = calendar.getTimeInMillis();
                    if (courseTime > nowTime) {
                        if (!findCourseId.equals(todayId[i]) && !findCourseId.equals("")) {
                            break;
                        }
                        result[0] = today[i].substring(today[i].indexOf("\n") + 1);
                        result[1] = today[i].substring(1, today[i].indexOf("\n"));
                        result[2] = todayId[i];
                        course_endTime = times[i];
                        if (!lastId.equals(todayId[i])) {
                            course_startTime = startTimes[i];
                            findCourseId = todayId[i];
                        }
                        lastId = todayId[i];
                    }
                    todayFinalCourseTime = courseTime;
                }
            }
            if (nowTime > todayFinalCourseTime) {
                return new String[5];
            }

            result[3] = course_startTime + "~" + course_endTime;

            result[4] = result[2];

            if (result[2] != null) {
                for (Course course : courses) {
                    if (course.getCourseId().equals(result[2])) {
                        result[2] = course.getCourseTeacher();
                        break;
                    }
                }
            }

        }
        return result;
    }

    public void setTableView(SmartTable<TableLine> smartTable) {
        this.smartTable = smartTable;
        loadView();
    }

    public boolean updateCourseTableView(int weekNum) {
        if (loadSuccess) {
            this.weekNum = weekNum;
            loadSuccess = false;
            loadView();
            smartTable.notifyDataChanged();
            return true;
        }
        return false;
    }

    //课程名称 上课地点 授课教师 上课时间 id
    public String[] getNextClass(int weekNum) {
        if (this.weekNum != weekNum || table == null) {
            getTable(weekNum, schoolTime.getStartTime());
        }
        return getNextClass(context, table, id_table, courses);
    }

    public void setOnCourseTableClickListener(OnCourseTableItemClickListener onCourseTableClick) {
        this.onCourseTableClick = onCourseTableClick;
    }

    synchronized private void loadView() {
        if (smartTable != null && !loadSuccess && weekNum != 0) {
            setTableCourse();
            //表格数值加载到视图, 仅支持五天课程
            if (tableLines != null) {
                tableLines.clear();
            } else {
                tableLines = new ArrayList<>();
            }
            for (int i = 0; i < Config.MAX_DAY_COURSE; i++) {
                String mo = table[1][i];
                String tu = table[2][i];
                String we = table[3][i];
                String th = table[4][i];
                String fr = table[5][i];
                TableLine tableLine = new TableLine(table[0][i], mo, tu, we, th, fr);
                tableLines.add(tableLine);
            }
            getTableData(tableLines);
            System.gc();
            loadSuccess = true;
        }
    }

    private void getTableData(List<TableLine> tableLines) {
        if (tableData == null) {
            String title = context.getString(R.string.table_title);
            tableData = new TableData<>(title, tableLines, getColumns());
            smartTable.setTableData(tableData);
        } else {
            //更新表格顶部的日期
            tableData.setColumns(getColumns());
            tableData.setT(tableLines);
        }
    }

    private List<Column> getColumns() {
        //仅支持周一到周五的计算
        List<String> week_day = BaseMethod.getWeekDayArray(context, weekNum, schoolTime.getStartTime());

        Column<String> columnDate = new Column<>(context.getString(R.string.date), "courseTime");
        columnDate.setFixed(true);
        Column<String> columnMo = new Column<>(week_day.get(0), "courseMo");
        columnSet(columnMo);
        Column<String> columnTu = new Column<>(week_day.get(1), "courseTu");
        columnSet(columnTu);
        Column<String> columnWe = new Column<>(week_day.get(2), "courseWe");
        columnSet(columnWe);
        Column<String> columnTh = new Column<>(week_day.get(3), "courseTh");
        columnSet(columnTh);
        Column<String> columnFr = new Column<>(week_day.get(4), "courseFr");
        columnSet(columnFr);

        List<Column> columns = new ArrayList<>();
        columns.add(columnDate);
        columns.add(columnMo);
        columns.add(columnTu);
        columns.add(columnWe);
        columns.add(columnTh);
        columns.add(columnFr);

        return columns;
    }

    private void columnSet(Column<String> column) {
        column.setAutoMerge(true);
        column.setMaxMergeCount(5);
        column.setAutoCount(false);
        column.setTextAlign(Paint.Align.CENTER);
        column.setOnColumnItemClickListener(new OnColumnItemClickListener<String>() {
            @Override
            public void onClick(Column<String> column, String value, String str, int position) {
                //仅支持五天课程
                if (onCourseTableClick != null) {
                    String fieldName = column.getFieldName();
                    String id = null;
                    switch (fieldName) {
                        case "courseMo":
                            id = id_table[1][position];
                            break;
                        case "courseTu":
                            id = id_table[2][position];
                            break;
                        case "courseWe":
                            id = id_table[3][position];
                            break;
                        case "courseTh":
                            id = id_table[4][position];
                            break;
                        case "courseFr":
                            id = id_table[5][position];
                            break;
                    }
                    if (id != null) {
                        for (Course course : courses) {
                            if (course.getCourseId().equals(id)) {
                                onCourseTableClick.OnItemClick(course);
                                break;
                            }
                        }
                    }
                }
            }
        });
    }

    //表格赋值
    private void setTableCourse() {
        List<String> course_time = BaseMethod.getCourseTimeArray(context);
        getTable(weekNum, schoolTime.getStartTime());
        setTableTimeLine(course_time);
        System.gc();
    }

    //设置表格的上课时间列
    private void setTableTimeLine(List<String> course_time) {
        for (int i = 0; i < course_time.size(); i++) {
            table[0][i] = course_time.get(i);
        }
    }

    //获取该周信息表格
    synchronized private void getTable(int weekNum, String startSchoolDate) {
        boolean isDoubleWeek = weekNum % 2 == 0;

        table = new String[Config.MAX_WEEK_DAY + 1][Config.MAX_DAY_COURSE];
        id_table = new String[Config.MAX_WEEK_DAY + 1][Config.MAX_DAY_COURSE];
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
                    table[detail.getWeekDay()][t - 1] = getShowDetail(course, detail);
                    id_table[detail.getWeekDay()][t - 1] = course.getCourseId();
                }
            } else {
                table[detail.getWeekDay()][Integer.valueOf(courseTime) - 1] = getShowDetail(course, detail);
                id_table[detail.getWeekDay()][Integer.valueOf(courseTime) - 1] = course.getCourseId();
            }
        }
    }

    private int getStartSchoolWeekDay(String startSchoolDate) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
            Date date = simpleDateFormat.parse(startSchoolDate);
            Calendar calendar = Calendar.getInstance(Locale.CHINA);
            calendar.setTime(date);
            //仅限周一到周五的计算
            return calendar.get(Calendar.DAY_OF_WEEK) - 1;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 1;
    }

    //表格插件问题，换行后下一行的会显示在上面，未来可能会修复（1.9.0未修复）
    private String getShowDetail(Course course, CourseDetail courseDetail) {
        return ("@" + courseDetail.getLocation() + "\n" + course.getCourseName()).trim();
    }

    public interface OnCourseTableItemClickListener {
        void OnItemClick(Course course);
    }

}
