package tool.xfy9326.naucourse.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.gridlayout.widget.GridLayout;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.activities.CourseActivity;
import tool.xfy9326.naucourse.asyncTasks.TableAsync;
import tool.xfy9326.naucourse.methods.BaseMethod;
import tool.xfy9326.naucourse.methods.CourseMethod;
import tool.xfy9326.naucourse.methods.CourseViewMethod;
import tool.xfy9326.naucourse.methods.DataMethod;
import tool.xfy9326.naucourse.methods.DialogMethod;
import tool.xfy9326.naucourse.methods.ImageMethod;
import tool.xfy9326.naucourse.methods.NetMethod;
import tool.xfy9326.naucourse.methods.NextClassMethod;
import tool.xfy9326.naucourse.methods.TimeMethod;
import tool.xfy9326.naucourse.receivers.CourseUpdateReceiver;
import tool.xfy9326.naucourse.utils.Course;
import tool.xfy9326.naucourse.utils.CourseDetail;
import tool.xfy9326.naucourse.utils.NextCourse;
import tool.xfy9326.naucourse.utils.SchoolTime;
import tool.xfy9326.naucourse.views.MainViewPagerAdapter;
import tool.xfy9326.naucourse.views.NestedHorizontalScrollView;
import tool.xfy9326.naucourse.widget.NextClassWidget;

/**
 * Created by xfy9326 on 18-2-20.
 */

public class TableFragment extends Fragment {
    @Nullable
    private View view;
    private int loadTime = 0;
    @Nullable
    private Context context;
    @Nullable
    private GridLayout courseTableLayout;
    @Nullable
    private ArrayList<Course> courses;
    @Nullable
    private SchoolTime schoolTime;
    @Nullable
    private CourseMethod courseMethod;
    @Nullable
    private CourseViewMethod courseViewMethod;
    @Nullable
    private Spinner spinnerWeek;
    private int lastSelect;
    private int lastSetWeekNumber;
    private boolean viewSet = false;
    private boolean inVacation;
    private int nowWeekNum = 0;
    private SharedPreferences sharedPreferences = null;

    public TableFragment() {
        this.view = null;
        this.context = null;
        this.courses = null;
        this.schoolTime = null;
        this.courseTableLayout = null;
        this.courseMethod = null;
        this.courseViewMethod = null;
        this.spinnerWeek = null;
        this.lastSelect = 0;
        this.lastSetWeekNumber = 0;
        this.inVacation = false;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        this.context = context;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        this.sharedPreferences = null;
        this.context = null;
        super.onDetach();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
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
        if (!viewSet) {
            viewSet();
            viewSet = true;
        }
        if (context != null && lastSetWeekNumber > 0) {
            updateAllNextCourseView(context);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.viewSet = false;
        this.view = null;
        this.context = null;
        this.courses = null;
        this.schoolTime = null;
        this.courseTableLayout = null;
        this.courseMethod = null;
        this.courseViewMethod = null;
        this.spinnerWeek = null;
        this.loadTime = 0;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_table, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (getActivity() != null) {
            if (item.getItemId() == R.id.menu_table_edit) {
                Intent intent = new Intent(getActivity(), CourseActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            } else if (item.getItemId() == R.id.menu_table_share) {
                shareTable();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void viewSet() {
        if (view != null) {
            courseTableLayout = view.findViewById(R.id.course_table_layout);

            CardView cardViewDate = view.findViewById(R.id.cardview_table_date);
            cardViewDate.setOnClickListener(v -> {
                if (nowWeekNum != 0 && nowWeekNum - 1 != lastSelect) {
                    reloadTable(false);
                }
            });

            getData();
        }
    }

    synchronized private void getData() {
        if (context != null) {
            new TableAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, context.getApplicationContext());
        }
    }

    /**
     * 设置课程
     *
     * @param courses      课程信息列表
     * @param schoolTime   SchoolTime对象
     * @param context      Context
     * @param isDataReload 是否是刷新本地信息
     */
    public void courseSet(@Nullable ArrayList<Course> courses, @Nullable final SchoolTime schoolTime, @Nullable final Context context, boolean isDataReload) {
        if (context != null && courses != null && schoolTime != null && courses.size() != 0) {
            if (isDataReload) {
                this.courses = courses;
                this.schoolTime = schoolTime;
            }

            int weekNum;
            schoolTime.setWeekNum(TimeMethod.getNowWeekNum(schoolTime));

            final boolean hasCustomBackground = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Config.PREFERENCE_COURSE_TABLE_SHOW_BACKGROUND, Config.DEFAULT_PREFERENCE_COURSE_TABLE_SHOW_BACKGROUND);

            //假期中默认显示第一周
            if (schoolTime.getWeekNum() == 0) {
                weekNum = 1;
                inVacation = true;
            } else {
                inVacation = false;
                weekNum = schoolTime.getWeekNum();
            }
            this.lastSetWeekNumber = weekNum;

            if (isAdded() && view != null && getActivity() != null) {
                TextView textViewDate = view.findViewById(R.id.textView_table_date);
                Calendar calendar = Calendar.getInstance(Locale.CHINA);
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH) + 1;
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                String weekDay = getResources().getStringArray(R.array.week_number)[dayOfWeek == 1 ? 6 : dayOfWeek - 2];
                String week = context.getString(R.string.week, weekNum);
                if (schoolTime.getWeekNum() == 0) {
                    week = context.getString(R.string.time_vacation);
                }
                textViewDate.setText(context.getString(R.string.table_date, year, month, weekDay, week));

                //提前显示下一周的课表
                if (schoolTime.getWeekNum() != 0) {
                    int weekDayNum = calendar.get(Calendar.DAY_OF_WEEK);
                    if (sharedPreferences != null && sharedPreferences.getBoolean(Config.PREFERENCE_SHOW_NEXT_WEEK, Config.DEFAULT_PREFERENCE_SHOW_NEXT_WEEK)) {
                        if ((weekDayNum == Calendar.SUNDAY || weekDayNum == Calendar.SATURDAY) && weekNum + 1 <= TimeMethod.getMaxWeekNum(schoolTime) && !CourseMethod.hasWeekendCourse(courses)) {
                            schoolTime.setWeekNum(++weekNum);
                        }
                    }
                }

                this.nowWeekNum = weekNum;

                if (lastSelect != weekNum - 1) {
                    lastSelect = weekNum - 1;
                    lastSetWeekNumber = lastSelect + 1;
                    if (spinnerWeek != null) {
                        spinnerWeek.setSelection(lastSelect);
                    }
                }

                if (spinnerWeek == null || isDataReload) {
                    List<String> weekArr = TimeMethod.getWeekArray(context, schoolTime);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, weekArr);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerWeek = view.findViewById(R.id.spinner_table_week_chose);
                    spinnerWeek.setAdapter(adapter);
                    if (lastSelect < weekArr.size()) {
                        spinnerWeek.setSelection(lastSelect);
                    }
                    spinnerWeek.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (lastSelect != position) {
                                schoolTime.setWeekNum(position + 1);
                                setTableData(schoolTime, false, hasCustomBackground);
                                lastSelect = position;
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });
                }

                setTableData(schoolTime, isDataReload, hasCustomBackground);

                NestedHorizontalScrollView horizontalScrollView = view.findViewById(R.id.nhScrollView_table);
                horizontalScrollView.setFitWidthNestedScroll(!sharedPreferences.getBoolean(Config.PREFERENCE_SHOW_WIDE_TABLE, Config.DEFAULT_PREFERENCE_SHOW_WIDE_TABLE));

                updateAllNextCourseView(context);
            }
        }
    }

    private void updateAllNextCourseView(@NonNull Context context) {
        //主页面下一节课设置
        MainViewPagerAdapter viewPagerAdapter = BaseMethod.getApp(context).getViewPagerAdapter();
        if (viewPagerAdapter != null) {
            HomeFragment homeFragment = viewPagerAdapter.getHomeFragment();
            if (homeFragment != null) {
                NextCourse nextCourse = null;
                if (inVacation) {
                    nextCourse = new NextCourse();
                    homeFragment.setNextCourse(nextCourse);
                } else if (courseMethod != null) {
                    nextCourse = courseMethod.getNextClass(lastSetWeekNumber);
                    nextCourse.setInVacation(false);
                    homeFragment.setNextCourse(nextCourse);
                }
                DataMethod.saveOfflineData(context, nextCourse, NextClassMethod.NEXT_COURSE_FILE_NAME, false, NextClassMethod.IS_ENCRYPT);
            }
        }

        if (loadTime == 1) {
            //初始化自动更新
            context.sendBroadcast(new Intent(context, CourseUpdateReceiver.class).setAction(CourseUpdateReceiver.UPDATE_ACTION).putExtra(Config.INTENT_IS_ONLY_INIT, true));
        } else {
            //更新小部件
            context.sendBroadcast(new Intent(NextClassWidget.ACTION_ON_UPDATE));
        }
    }

    int getTableWidth() {
        if (view != null && isAdded()) {
            CardView cardViewCourse = view.findViewById(R.id.cardView_courseTable);
            return cardViewCourse.getWidth();
        }
        return -1;
    }

    int getTableHeight() {
        if (view != null && isAdded()) {
            CardView cardViewCourse = view.findViewById(R.id.cardView_courseTable);
            return cardViewCourse.getHeight();
        }
        return -1;
    }

    private void setTableData(@NonNull SchoolTime schoolTime, boolean isDataReload, boolean hasCustomBackground) {
        if (view != null) {
            CardView cardViewCourse = view.findViewById(R.id.cardView_courseTable);
            LinearLayout layoutLoading = view.findViewById(R.id.layout_table_loading);
            if (layoutLoading.getVisibility() != View.GONE) {
                layoutLoading.setVisibility(View.GONE);
            }
            if (courseViewMethod == null) {
                courseViewMethod = new CourseViewMethod(context, courses);
                courseViewMethod.setTableView(courseTableLayout, cardViewCourse.getWidth(), cardViewCourse.getHeight());
            } else {
                courseViewMethod.updateTableSize(cardViewCourse.getWidth(), cardViewCourse.getHeight());
            }
            if (isDataReload) {
                courseViewMethod.setOnCourseTableClickListener(this::courseCardSet);
            }
            if (courseMethod == null) {
                courseMethod = new CourseMethod(context, courses, schoolTime);
            } else {
                courseMethod.updateTableCourse(courses, schoolTime, !isDataReload);
            }
            if (view != null) {
                ImageView imageViewTableBackground = view.findViewById(R.id.imageView_table_background);
                if (hasCustomBackground) {
                    Bitmap bitmap = ImageMethod.getTableBackgroundBitmap(getActivity());
                    if (bitmap != null) {
                        imageViewTableBackground.refreshDrawableState();
                        imageViewTableBackground.setImageBitmap(bitmap);
                    } else {
                        hasCustomBackground = false;
                    }
                } else {
                    imageViewTableBackground.setImageDrawable(null);
                    imageViewTableBackground.refreshDrawableState();
                    if (courseTableLayout != null && context != null) {
                        courseTableLayout.setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.table_background, context.getTheme()));
                    }
                }
            }
            courseViewMethod.updateCourseTableView(courses, courseMethod.getTableData(), courseMethod.getTableIdData(), courseMethod.getTableShowData(), isDataReload, hasCustomBackground);
        }
    }

    //表格中的课程详细信息显示
    private void courseCardSet(@NonNull Course course) {
        if (getActivity() != null) {
            LayoutInflater layoutInflater = getLayoutInflater();
            View viewDialog = layoutInflater.inflate(R.layout.dialog_course_card, getActivity().findViewById(R.id.layout_course_card));

            LinearLayout linearLayoutContent = viewDialog.findViewById(R.id.layout_course_card_content);
            TextView textViewName = viewDialog.findViewById(R.id.textView_course_card_name);

            TextView textViewId = viewDialog.findViewById(R.id.textView_course_card_id);
            TextView textViewTeacher = viewDialog.findViewById(R.id.textView_course_card_teacher);
            TextView textViewType = viewDialog.findViewById(R.id.textView_course_card_type);
            TextView textViewScore = viewDialog.findViewById(R.id.textView_course_card_score);
            TextView textViewClass = viewDialog.findViewById(R.id.textView_course_card_class);
            TextView textViewClassCombined = viewDialog.findViewById(R.id.textView_course_card_combined_class);

            textViewName.setText(course.getCourseName());

            textViewId.setText(Objects.requireNonNull(context).getString(R.string.course_card_id, course.getCourseId()));
            textViewTeacher.setText(context.getString(R.string.course_card_teacher, course.getCourseTeacher()));
            if (course.getCourseScore() != null && !course.getCourseScore().isEmpty()) {
                textViewScore.setText(context.getString(R.string.course_card_score, course.getCourseScore()));
            } else {
                textViewScore.setVisibility(View.GONE);
            }
            if (course.getCourseType() != null && !course.getCourseType().isEmpty()) {
                textViewType.setText(context.getString(R.string.course_card_type, course.getCourseType()));
            } else {
                textViewType.setVisibility(View.GONE);
            }
            if (course.getCourseClass() != null && !course.getCourseClass().isEmpty()) {
                textViewClass.setText(context.getString(R.string.course_card_class, course.getCourseClass()));
            } else {
                textViewClass.setVisibility(View.GONE);
            }
            if (course.getCourseCombinedClass() != null && !course.getCourseCombinedClass().isEmpty()) {
                textViewClassCombined.setText(context.getString(R.string.course_card_combined_class, course.getCourseCombinedClass()));
            } else {
                textViewClassCombined.setVisibility(View.GONE);
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            CourseDetail[] details = course.getCourseDetail();
            if (details != null) {
                for (CourseDetail detail : details) {
                    String[] courseWeeks = detail.getWeeks();
                    String weekmode = "";
                    if (detail.getWeekMode() == Config.COURSE_DETAIL_WEEKMODE_SINGLE) {
                        weekmode = context.getString(R.string.course_card_time_week_mode, context.getString(R.string.single_week_mode));
                    } else if (detail.getWeekMode() == Config.COURSE_DETAIL_WEEKMODE_DOUBLE) {
                        weekmode = context.getString(R.string.course_card_time_week_mode, context.getString(R.string.double_week_mode));
                    }
                    for (String courseWeek : Objects.requireNonNull(courseWeeks)) {
                        String[] courseTimes = detail.getCourseTime();
                        String[] weekNum = context.getResources().getStringArray(R.array.week_number);
                        for (String courseTime : Objects.requireNonNull(courseTimes)) {
                            View viewCard = layoutInflater.inflate(R.layout.item_course_card, getActivity().findViewById(R.id.layout_course_card_item));
                            String time = context.getString(R.string.course_card_time, courseWeek, weekmode, weekNum[detail.getWeekDay() - 1], courseTime);
                            String location = context.getString(R.string.course_card_location, detail.getLocation());
                            ((TextView) viewCard.findViewById(R.id.textView_course_card_time)).setText(time);
                            ((TextView) viewCard.findViewById(R.id.textView_course_card_location)).setText(location);
                            linearLayoutContent.addView(viewCard);
                        }
                    }
                }
            }
            builder.setView(viewDialog);
            builder.show();
        }
    }

    /**
     * 重新加载课程表
     */
    synchronized public void reloadTable(boolean dataReload) {
        if (isAdded()) {
            if (dataReload) {
                getData();
                Toast.makeText(getActivity(), R.string.course_table_reloading, Toast.LENGTH_SHORT).show();
            } else if (courses != null && schoolTime != null) {
                courseSet(courses, schoolTime, getActivity(), false);
            }
        }
    }

    public void lastViewSet(Context context, boolean mustReload) {
        if (isAdded()) {
            //离线数据加载完成，开始拉取网络数据
            if (loadTime == 1 && NetMethod.isNetworkConnected(context) && BaseMethod.isDataAutoUpdate(context)) {
                boolean updateDay = true;
                if (!mustReload && sharedPreferences != null) {
                    if (!sharedPreferences.getBoolean(Config.PREFERENCE_UPDATE_TABLE_EVERY_TIME, Config.DEFAULT_PREFERENCE_UPDATE_TABLE_EVERY_TIME) && sharedPreferences.getBoolean(Config.PREFERENCE_AUTO_UPDATE_COURSE_TABLE, Config.DEFAULT_PREFERENCE_AUTO_UPDATE_COURSE_TABLE)) {
                        Calendar calendar = Calendar.getInstance(Locale.CHINA);
                        long courseTableLoadDate = sharedPreferences.getLong(Config.PREFERENCE_COURSE_TABLE_AUTO_LOAD_DATE_TIME, 0);
                        calendar.setTimeInMillis(courseTableLoadDate);
                        int loadDay = calendar.get(Calendar.DAY_OF_YEAR);
                        int loadYear = calendar.get(Calendar.YEAR);
                        calendar.setTimeInMillis(System.currentTimeMillis());
                        int nowDay = calendar.get(Calendar.DAY_OF_YEAR);
                        int nowYear = calendar.get(Calendar.YEAR);

                        if (loadYear == nowYear && nowDay == loadDay) {
                            updateDay = false;
                        } else {
                            sharedPreferences.edit().putLong(Config.PREFERENCE_COURSE_TABLE_AUTO_LOAD_DATE_TIME, System.currentTimeMillis()).apply();
                        }
                    }
                }

                if (updateDay) {
                    getData();
                }
            }
        }
    }

    private void shareTable() {
        if (view != null && isAdded() && getActivity() != null) {
            Bitmap bitmap = null;
            View tableView = view.findViewById(R.id.course_table_layout);
            if (viewSet && tableView.getVisibility() == View.VISIBLE) {
                bitmap = ImageMethod.getViewBitmap(getActivity(), tableView);
            }
            DialogMethod.showImageShareDialog(getActivity(),
                    bitmap,
                    Config.COURSE_TABLE_IMAGE_FILE_NAME,
                    R.string.share_course_table,
                    R.string.course_table_share_error,
                    R.string.share_course_table);
        }
    }

    public int getLoadTime() {
        return loadTime;
    }

    public void setLoadTime(int loadTime) {
        this.loadTime = loadTime;
    }
}
