package tool.xfy9326.naucourse.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import tool.xfy9326.naucourse.AsyncTasks.TableAsync;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.Methods.CourseMethod;
import tool.xfy9326.naucourse.Methods.CourseViewMethod;
import tool.xfy9326.naucourse.Methods.NetMethod;
import tool.xfy9326.naucourse.Methods.TimeMethod;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Receivers.UpdateReceiver;
import tool.xfy9326.naucourse.Utils.Course;
import tool.xfy9326.naucourse.Utils.CourseDetail;
import tool.xfy9326.naucourse.Utils.NextCourse;
import tool.xfy9326.naucourse.Utils.SchoolTime;
import tool.xfy9326.naucourse.Views.NextClassWidget;

/**
 * Created by xfy9326 on 18-2-20.
 */

public class TableFragment extends Fragment {
    @Nullable
    private View view;
    @Nullable
    private Context context;
    private int loadTime = 0;
    @Nullable
    private GridLayout course_table_layout;
    @Nullable
    private ArrayList<Course> courses;
    @Nullable
    private SchoolTime schoolTime;
    @Nullable
    private CourseMethod courseMethod;
    @Nullable
    private CourseViewMethod courseViewMethod;
    @Nullable
    private Spinner spinner_week;
    private int lastSelect;

    public TableFragment() {
        this.view = null;
        this.context = null;
        this.courses = null;
        this.schoolTime = null;
        this.course_table_layout = null;
        this.courseMethod = null;
        this.courseViewMethod = null;
        this.spinner_week = null;
        this.lastSelect = 0;
    }

    @Override
    public void onAttach(Context context) {
        this.context = context;
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_table, container, false);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        ViewSet();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_table, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_table_refresh) {
            Toast.makeText(getActivity(), R.string.updating, Toast.LENGTH_SHORT).show();
            getData();
        }
        return super.onOptionsItemSelected(item);
    }

    private void ViewSet() {
        course_table_layout = Objects.requireNonNull(view).findViewById(R.id.course_table_layout);
        course_table_layout.setDrawingCacheEnabled(true);

        CardView cardView_date = view.findViewById(R.id.cardview_table_date);
        cardView_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reloadTable();
            }
        });

        if (loadTime == 0) {
            getData();
        }
    }

    /**
     * 设置课程
     *
     * @param courses    课程信息列表
     * @param schoolTime SchoolTime对象
     * @param context    Context
     * @param isReload   是否是刷新本地信息
     */
    public void CourseSet(@Nullable ArrayList<Course> courses, @Nullable SchoolTime schoolTime, @Nullable final Context context, boolean isReload) {
        if (context != null && courses != null && schoolTime != null) {
            if (!isReload) {
                this.courses = courses;
                this.schoolTime = schoolTime;
            }

            int weekNum;
            boolean inVacation = false;
            schoolTime.setWeekNum(TimeMethod.getNowWeekNum(schoolTime));

            //假期中默认显示第一周
            if (schoolTime.getWeekNum() == 0) {
                weekNum = 1;
                inVacation = true;
            } else {
                weekNum = schoolTime.getWeekNum();
            }

            if (isAdded()) {
                TextView textView_date = Objects.requireNonNull(view).findViewById(R.id.textView_table_date);
                Calendar calendar = Calendar.getInstance(Locale.CHINA);
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH) + 1;
                String week = context.getString(R.string.week, weekNum);
                if (schoolTime.getWeekNum() == 0) {
                    week = context.getString(R.string.time_vacation);
                }
                String time = context.getString(R.string.time_now) + context.getString(R.string.table_date, year, month) + " " + week;
                textView_date.setText(time);

                if (spinner_week == null) {
                    spinner_week = view.findViewById(R.id.spinner_table_week_chose);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, TimeMethod.getWeekArray(context, schoolTime));
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    Objects.requireNonNull(spinner_week).setSelection(lastSelect);
                    spinner_week.setAdapter(adapter);
                    spinner_week.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (lastSelect != position) {
                                Objects.requireNonNull(TableFragment.this.schoolTime).setWeekNum(position + 1);
                                setTableData(TableFragment.this.schoolTime, false);
                                lastSelect = position;
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });
                }

                //提前显示下一周的课表
                if (schoolTime.getWeekNum() != 0) {
                    int weekDayNum = calendar.get(Calendar.DAY_OF_WEEK);
                    if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Config.PREFERENCE_SHOW_NEXT_WEEK, Config.DEFAULT_PREFERENCE_SHOW_NEXT_WEEK)) {
                        if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Config.PREFERENCE_SHOW_WEEKEND, Config.DEFAULT_PREFERENCE_SHOW_WEEKEND)) {
                            if ((weekDayNum == Calendar.SUNDAY || weekDayNum == Calendar.SATURDAY) && weekNum + 1 <= TimeMethod.getMaxWeekNum(schoolTime)) {
                                weekNum++;
                                schoolTime.setWeekNum(weekNum);
                            }
                        }
                    }
                }

                setTableData(schoolTime, isReload);

                if (lastSelect != weekNum - 1) {
                    spinner_week.setSelection(weekNum - 1);
                    lastSelect = weekNum - 1;
                }

                //主页面下一节课设置
                if (!inVacation) {
                    HomeFragment homeFragment = BaseMethod.getApp(context).getViewPagerAdapter().getHomeFragment();
                    NextCourse nextCourse = Objects.requireNonNull(courseMethod).getNextClass(weekNum);
                    homeFragment.setNextCourse(nextCourse.getCourseName(), nextCourse.getCourseLocation(), nextCourse.getCourseTeacher(), nextCourse.getCourseTime());
                }

                //初始化自动更新
                if (getActivity() != null) {
                    getActivity().sendBroadcast(new Intent(context, UpdateReceiver.class).setAction(UpdateReceiver.UPDATE_ACTION).putExtra(Config.INTENT_IS_ONLY_INIT, true));
                }

                if (loadTime > 0) {
                    context.sendBroadcast(new Intent(NextClassWidget.ACTION_ON_CLICK));
                }
            }
        }
    }

    private void setTableData(@NonNull SchoolTime schoolTime, boolean isReload) {
        if (courseViewMethod == null) {
            CardView cardView_course = Objects.requireNonNull(view).findViewById(R.id.cardView_courseTable);
            courseViewMethod = new CourseViewMethod(context, courses);
            courseViewMethod.setTableView(course_table_layout, cardView_course.getWidth());
            courseViewMethod.setOnCourseTableClickListener(new CourseViewMethod.OnCourseTableItemClickListener() {
                @Override
                public void OnItemClick(@NonNull Course course) {
                    CourseCardSet(course);
                }
            });
        }
        if (courseMethod == null) {
            courseMethod = new CourseMethod(context, courses, schoolTime);
        } else {
            courseMethod.updateTableCourse(schoolTime, isReload);
        }
        courseViewMethod.updateCourseTableView(courseMethod.getTableData(), courseMethod.getTableIdData(), !isReload);
    }

    //表格中的课程详细信息显示
    private void CourseCardSet(@NonNull Course course) {
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

            textView_id.setText(Objects.requireNonNull(context).getString(R.string.course_card_id, course.getCourseId()));
            textView_teacher.setText(context.getString(R.string.course_card_teacher, course.getCourseTeacher()));
            textView_score.setText(context.getString(R.string.course_card_score, course.getCourseScore()));
            textView_type.setText(context.getString(R.string.course_card_type, course.getCourseType()));
            textView_class.setText(context.getString(R.string.course_card_class, course.getCourseClass()));
            textView_class_combined.setText(context.getString(R.string.course_card_combined_class, course.getCourseCombinedClass()));

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            CourseDetail[] details = course.getCourseDetail();
            for (CourseDetail detail : Objects.requireNonNull(details)) {
                String[] course_weeks = detail.getWeeks();
                String weekmode = "";
                if (detail.getWeekMode() == Config.COURSE_DETAIL_WEEKMODE_SINGLE) {
                    weekmode = context.getString(R.string.course_card_time_week_mode, context.getString(R.string.single_week_mode));
                } else if (detail.getWeekMode() == Config.COURSE_DETAIL_WEEKMODE_SINGLE) {
                    weekmode = context.getString(R.string.course_card_time_week_mode, context.getString(R.string.double_week_mode));
                }
                for (String course_week : Objects.requireNonNull(course_weeks)) {
                    String[] course_times = detail.getCourseTime();
                    String[] week_num = context.getResources().getStringArray(R.array.week_number);
                    for (String course_time : Objects.requireNonNull(course_times)) {
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

    /**
     * 重新加载课程表
     */
    synchronized public void reloadTable() {
        if (isAdded() && courses != null && schoolTime != null) {
            CourseSet(courses, schoolTime, context, true);
        }
    }

    synchronized private void getData() {
        new TableAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Objects.requireNonNull(context).getApplicationContext());
    }

    public void lastViewSet(Context context) {
        if (isAdded()) {
            //离线数据加载完成，开始拉取网络数据
            if (loadTime == 1 && NetMethod.isNetworkConnected(context) && BaseMethod.isDataAutoUpdate(context)) {
                getData();
            }
        }
    }

    public int getLoadTime() {
        return loadTime;
    }

    public void setLoadTime(int loadTime) {
        this.loadTime = loadTime;
    }

}
