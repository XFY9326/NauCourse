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

import com.bin.david.form.core.SmartTable;
import com.bin.david.form.core.TableConfig;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import tool.xfy9326.naucourse.AsyncTasks.TableAsync;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.Methods.CourseMethod;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Receivers.UpdateReceiver;
import tool.xfy9326.naucourse.Utils.Course;
import tool.xfy9326.naucourse.Utils.CourseDetail;
import tool.xfy9326.naucourse.Utils.NextCourse;
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
    private CourseMethod courseMethod;
    private int loadTime = 0;

    public TableFragment() {
        this.view = null;
        this.context = null;
        this.courseTable = null;
        this.courseMethod = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_table, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_table_refresh) {
            Toast.makeText(getActivity(), R.string.updating, Toast.LENGTH_SHORT).show();
            getData();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Context context) {
        this.context = context;
        super.onAttach(context);
    }

    @Override
    public void onStart() {
        super.onStart();
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
        tableConfig.setVerticalPadding(8);
        tableConfig.setHorizontalPadding(8);

        if (loadTime == 0) {
            getData();
        }
    }

    public void CourseSet(ArrayList<Course> courses, SchoolTime schoolTime, final Context context) {
        if (context != null && courses != null && schoolTime != null) {
            int weekNum;
            boolean inVacation = false;
            schoolTime.setWeekNum(BaseMethod.getNowWeekNum(schoolTime));

            //假期中默认显示第一周
            if (schoolTime.getWeekNum() == 0) {
                weekNum = 1;
                inVacation = true;
            } else {
                weekNum = schoolTime.getWeekNum();
            }

            final int nowWeek = weekNum;

            if (isAdded()) {
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

                courseMethod = new CourseMethod(context, courses, schoolTime);

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

                int weekDayNum = calendar.get(Calendar.DAY_OF_WEEK);
                if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Config.PREFERENCE_SHOW_NEXT_WEEK, Config.DEFAULT_PREFERENCE_SHOW_NEXT_WEEK)) {
                    if ((weekDayNum == Calendar.SUNDAY || weekDayNum == Calendar.SATURDAY) && weekNum + 1 <= BaseMethod.getMaxWeekNum(schoolTime)) {
                        spinner_week.setSelection(weekNum);
                        courseMethod.updateCourseTableView(weekNum);
                    } else {
                        spinner_week.setSelection(weekNum - 1);
                    }
                } else {
                    spinner_week.setSelection(weekNum - 1);
                }

                courseMethod.setTableView(courseTable);

                if (!inVacation) {
                    HomeFragment homeFragment = BaseMethod.getBaseApplication(context).getViewPagerAdapter().getHomeFragment();
                    NextCourse nextCourse = courseMethod.getNextClass(weekNum);
                    homeFragment.setNextCourse(nextCourse.getCourseName(), nextCourse.getCourseLocation(), nextCourse.getCourseTeacher(), nextCourse.getCourseTime());
                }

                courseMethod.setOnCourseTableClickListener(new CourseMethod.OnCourseTableItemClickListener() {
                    @Override
                    public void OnItemClick(Course course) {
                        CourseCardSet(course);
                    }
                });

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

    //表格中的课程详细信息显示
    private void CourseCardSet(Course course) {
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

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

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

// --Commented out by Inspection START (2018/3/16 上午 11:25):
//    public void updateTable() {
//        if (isAdded()) {
//            getData();
//        }
//    }
// --Commented out by Inspection STOP (2018/3/16 上午 11:25)

    synchronized public boolean reloadTable() {
        return courseMethod != null && isAdded() && courseMethod.updateCourseTableView();
    }

    synchronized private void getData() {
        new TableAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, context.getApplicationContext());
    }

    public void lastViewSet(Context context) {
        if (isAdded()) {
            //离线数据加载完成，开始拉取网络数据
            if (loadTime == 1 && BaseMethod.isNetworkConnected(context) && BaseMethod.isDataAutoUpdate(context)) {
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
