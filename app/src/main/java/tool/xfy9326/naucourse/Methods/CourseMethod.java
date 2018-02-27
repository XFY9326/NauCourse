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

    public CourseMethod(Context context, ArrayList<Course> courses, SchoolTime schoolTime) {
        this.context = context;
        this.courses = courses;
        this.schoolTime = schoolTime;
        weekNum = schoolTime.getWeekNum();
        if (weekNum == 0) {
            weekNum = 1;
        }
        smartTable = null;
        onCourseTableClick = null;
        loadSuccess = false;
    }

    private static String[] getNextClass(Context context, String[][] this_week_table, String[][] this_week_id_table, ArrayList<Course> courses) {
        String[] result = new String[3];
        //仅限周一到周五的计算
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        int weekDayNum = calendar.get(Calendar.DAY_OF_WEEK) - 1;

        String[] today = this_week_table[weekDayNum];
        String[] todayId = this_week_id_table[weekDayNum];
        String[] times = context.getResources().getStringArray(R.array.course_finish_time);
        int nowTime = calendar.get(Calendar.HOUR) * 60 + calendar.get(Calendar.MINUTE);

        for (int i = 0; i < times.length; i++) {
            String[] time_temp = times[i].split(":");
            int courseTime = Integer.valueOf(time_temp[0]) * 60 + Integer.valueOf(time_temp[1]);
            if (courseTime > nowTime) {
                if (today[i] != null) {
                    result[0] = today[i].substring(today[i].indexOf("\n") + 1);
                    result[1] = today[i].substring(1, today[i].indexOf("\n"));
                    result[2] = todayId[i];
                    break;
                }
            }
        }
        if (result[2] != null) {
            for (Course course : courses) {
                if (course.getCourseId().equals(result[2])) {
                    result[2] = course.getCourseTeacher();
                    break;
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
            return true;
        }
        return false;
    }

    //课程名称 上课地点 授课教师
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
        if (smartTable != null && !loadSuccess) {
            setTableCourse();
            //表格数值加载到视图
            String title = context.getString(R.string.table_title);
            List<String> week_day = BaseMethod.getWeekDayArray(context, weekNum, schoolTime.getStartTime());

            //仅支持五天课程
            List<TableLine> tableLines = new ArrayList<>();
            for (int i = 0; i < Config.MAX_DAY_COURSE; i++) {
                String mo = table[1][i];
                String tu = table[2][i];
                String we = table[3][i];
                String th = table[4][i];
                String fr = table[5][i];
                TableLine tableLine = new TableLine(table[0][i], mo, tu, we, th, fr);
                tableLines.add(tableLine);
            }

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

            TableData<TableLine> tableData = new TableData<>(title, tableLines, columns);
            smartTable.setTableData(tableData);
            smartTable.notifyDataChanged();
            System.gc();
            loadSuccess = true;
        }
    }

    private void columnSet(Column<String> column) {
        column.setAutoMerge(true);
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

    private void setTableTimeLine(List<String> course_time) {
        for (int i = 0; i < course_time.size(); i++) {
            table[0][i] = course_time.get(i);
        }
    }

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

    private String getShowDetail(Course course, CourseDetail courseDetail) {
        return ("@" + courseDetail.getLocation() + "\n" + course.getCourseName()).trim();
    }

    public interface OnCourseTableItemClickListener {
        void OnItemClick(Course course);
    }

}
