package tool.xfy9326.naucourse.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.bin.david.form.core.SmartTable;
import com.bin.david.form.core.TableConfig;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.Methods.CourseMethod;
import tool.xfy9326.naucourse.Methods.TableMethod;
import tool.xfy9326.naucourse.Methods.TimeMethod;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Utils.Course;
import tool.xfy9326.naucourse.Utils.CourseDetail;
import tool.xfy9326.naucourse.Utils.SchoolTime;
import tool.xfy9326.naucourse.Utils.TableLine;
import tool.xfy9326.naucourse.Views.NextClassWidget;

/**
 * Created by xfy9326 on 18-2-20.
 */

public class TableFragment extends Fragment {
    private View view;
    private Context context;
    private SmartTable<TableLine> courseTable;
    private int loadTime = 0;

    public TableFragment() {
        this.view = null;
        this.context = null;
        this.courseTable = null;
    }

    @Override
    public void onAttach(Context context) {
        this.context = context;
        super.onAttach(context);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ViewSet();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_table, container, false);
        return view;
    }

    private void ViewSet() {
        courseTable = view.findViewById(R.id.course_table);
        courseTable.setZoom(false);

        TableConfig tableConfig = courseTable.getConfig();
        tableConfig.setShowXSequence(false);
        tableConfig.setShowYSequence(false);
        tableConfig.setShowTableTitle(false);
        tableConfig.setVerticalPadding(5);
        tableConfig.setHorizontalPadding(5);
        tableConfig.setColumnTitleBackgroundColor(Color.LTGRAY);

        if (loadTime == 0) {
            getData();
        }
    }

    synchronized public void UpdateCourseTable() {
        getData();
    }

    private void CourseSet(ArrayList<Course> courses, SchoolTime schoolTime, final Context context) {
        if (context != null && courses != null && schoolTime != null) {
            int weekNum;
            boolean inVacation = false;
            schoolTime.setWeekNum(BaseMethod.getNowWeekNum(schoolTime));

            if (schoolTime.getWeekNum() == 0) {
                weekNum = 1;
                inVacation = true;
            } else {
                weekNum = schoolTime.getWeekNum();
            }

            final int nowWeek = weekNum;

            TextView textView_date = view.findViewById(R.id.textView_table_date);
            Calendar calendar = Calendar.getInstance(Locale.CHINA);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            String week = context.getString(R.string.week, weekNum);
            if (schoolTime.getWeekNum() == 0) {
                week = context.getString(R.string.time_vacation);
            }
            String time = context.getString(R.string.time_now) + context.getString(R.string.table_date, year, month) + " " + week;
            textView_date.setText(time);

            final CourseMethod courseMethod = new CourseMethod(context, courses, schoolTime);

            final Spinner spinner_week = view.findViewById(R.id.spinner_table_week_chose);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, BaseMethod.getWeekArray(context, schoolTime));
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner_week.setAdapter(adapter);
            spinner_week.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                private int lastSelect = nowWeek - 1;

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (lastSelect == position) {
                        spinner_week.setSelection(lastSelect);
                    } else {
                        if (courseMethod.updateCourseTableView(position + 1)) {
                            lastSelect = position;
                        } else {
                            spinner_week.setSelection(lastSelect);
                        }
                    }

                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
            spinner_week.setSelection(weekNum - 1);

            courseMethod.setTableView(courseTable);

            if (!inVacation) {
                HomeFragment homeFragment = BaseMethod.getBaseApplication(context).getViewPagerAdapter().getHomeFragment();
                String[] courseNext = courseMethod.getNextClass(weekNum);
                if (courseNext[0] != null) {
                    homeFragment.setNextCourse(courseNext[0], courseNext[1], courseNext[2], courseNext[3]);
                }
            }

            courseMethod.setOnCourseTableClickListener(new CourseMethod.OnCourseTableItemClickListener() {
                @Override
                public void OnItemClick(Course course) {
                    CourseCardSet(context, course);
                }
            });

            if (loadTime > 0) {
                context.sendBroadcast(new Intent(NextClassWidget.ACTION_ON_CLICK));
            }
        }
    }

    //表格中的课程详细信息显示
    private void CourseCardSet(Context context, Course course) {
        if (getActivity() != null) {
            LayoutInflater layoutInflater = getLayoutInflater();
            View view_dialog = layoutInflater.inflate(R.layout.dialog_course_card, (ViewGroup) getActivity().findViewById(R.id.layout_course_card));

            LinearLayout linearLayout_content = view_dialog.findViewById(R.id.layout_course_card_content);
            TextView textView_name = view_dialog.findViewById(R.id.textView_course_card_name);

            TextView textView_id = view_dialog.findViewById(R.id.textView_course_card_id);
            TextView textView_teacher = view_dialog.findViewById(R.id.textView_course_card_teacher);
            TextView textView_type = view_dialog.findViewById(R.id.textView_course_card_type);
            TextView textView_score = view_dialog.findViewById(R.id.textView_course_card_score);
            TextView textView_class = view_dialog.findViewById(R.id.textView_course_card_class);
            TextView textView_class_combined = view_dialog.findViewById(R.id.textView_course_card_combined_class);


            textView_name.setText(course.getCourseName());

            textView_id.setText(context.getString(R.string.course_card_id, course.getCourseId()));
            textView_teacher.setText(context.getString(R.string.course_card_teacher, course.getCourseTeacher()));
            textView_score.setText(context.getString(R.string.course_card_score, course.getCourseScore()));
            textView_type.setText(context.getString(R.string.course_card_type, course.getCourseType()));
            textView_class.setText(context.getString(R.string.course_card_class, course.getCourseClass()));
            textView_class_combined.setText(context.getString(R.string.course_card_combined_class, course.getCourseCombinedClass()));

            AlertDialog.Builder builder = new AlertDialog.Builder(context);

            CourseDetail[] details = course.getCourseDetail();
            for (CourseDetail detail : details) {
                String[] course_weeks = detail.getWeeks();
                String weekmode = "";
                if (detail.getWeekMode() == Config.COURSE_DETAIL_WEEKMODE_SINGLE) {
                    weekmode = context.getString(R.string.course_card_time_week_mode, context.getString(R.string.single_week_mode));
                } else if (detail.getWeekMode() == Config.COURSE_DETAIL_WEEKMODE_SINGLE) {
                    weekmode = context.getString(R.string.course_card_time_week_mode, context.getString(R.string.double_week_mode));
                }
                for (String course_week : course_weeks) {
                    String[] course_times = detail.getCourseTime();
                    String[] week_num = context.getResources().getStringArray(R.array.week_number);
                    for (String course_time : course_times) {
                        View view_card = layoutInflater.inflate(R.layout.item_course_card, (ViewGroup) getActivity().findViewById(R.id.layout_course_card_item));
                        String time = context.getString(R.string.course_card_time, course_week, weekmode, week_num[detail.getWeekDay() - 1], course_time);
                        String location = context.getString(R.string.course_card_location, detail.getLocation());
                        ((TextView) view_card.findViewById(R.id.textView_course_card_time)).setText(time);
                        ((TextView) view_card.findViewById(R.id.textView_course_card_location)).setText(location);
                        linearLayout_content.addView(view_card);
                    }
                }
            }

            builder.setView(view_dialog);
            builder.show();
        }
    }

    private void getData() {
        new TableAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, context);
    }

    @SuppressLint("StaticFieldLeak")
    class TableAsync extends AsyncTask<Context, Void, Context> {
        int tableLoadSuccess = -1;
        int timeLoadSuccess = -1;
        int loadCode = Config.NET_WORK_GET_SUCCESS;
        private ArrayList<Course> course;
        private SchoolTime schoolTime;

        TableAsync() {
            course = null;
            schoolTime = null;
        }

        @Override
        protected Context doInBackground(Context... context) {
            try {
                if (loadTime == 0) {
                    //首次只加载离线数据
                    schoolTime = (SchoolTime) BaseMethod.getOfflineData(context[0], SchoolTime.class, TimeMethod.FILE_NAME);
                    course = BaseMethod.getOfflineTableData(context[0]);
                    tableLoadSuccess = Config.NET_WORK_GET_SUCCESS;
                    timeLoadSuccess = Config.NET_WORK_GET_SUCCESS;
                    loadTime++;
                } else {
                    TableMethod tableMethod = new TableMethod(context[0]);
                    tableLoadSuccess = tableMethod.load();
                    if (tableLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                        course = tableMethod.getCourseTable();
                    }

                    TimeMethod timeMethod = new TimeMethod(context[0]);
                    timeLoadSuccess = timeMethod.load();
                    if (timeLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                        schoolTime = timeMethod.getSchoolTime();
                    }
                    if (tableLoadSuccess == Config.NET_WORK_GET_SUCCESS && timeLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                        loadTime++;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                loadCode = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
            }
            return context[0];
        }

        @Override
        protected void onPostExecute(Context context) {
            if (BaseMethod.checkNetWorkCode(context, new int[]{tableLoadSuccess, timeLoadSuccess}, loadCode)) {
                CourseSet(course, schoolTime, context);
            }
            //离线数据加载完成，开始拉取网络数据
            if (loadTime == 1 && BaseMethod.isNetworkConnected(context) && BaseMethod.isDataAutoUpdate(context)) {
                getData();
            }
            super.onPostExecute(context);
        }
    }
}
