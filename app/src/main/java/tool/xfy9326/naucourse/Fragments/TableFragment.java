package tool.xfy9326.naucourse.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.gridlayout.widget.GridLayout;
import tool.xfy9326.naucourse.Activities.CourseActivity;
import tool.xfy9326.naucourse.AsyncTasks.TableAsync;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.Methods.CourseMethod;
import tool.xfy9326.naucourse.Methods.CourseViewMethod;
import tool.xfy9326.naucourse.Methods.ImageMethod;
import tool.xfy9326.naucourse.Methods.NetMethod;
import tool.xfy9326.naucourse.Methods.PermissionMethod;
import tool.xfy9326.naucourse.Methods.TimeMethod;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Receivers.CourseUpdateReceiver;
import tool.xfy9326.naucourse.Utils.Course;
import tool.xfy9326.naucourse.Utils.CourseDetail;
import tool.xfy9326.naucourse.Utils.NextCourse;
import tool.xfy9326.naucourse.Utils.SchoolTime;
import tool.xfy9326.naucourse.Views.ViewPagerAdapter;
import tool.xfy9326.naucourse.Widget.NextClassWidget;

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
    private int lastSetWeekNumber;
    private boolean view_set = false;
    private boolean inVacation;

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
        this.lastSetWeekNumber = 0;
        this.inVacation = false;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        this.context = context;
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.context = null;
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
        if (!view_set) {
            ViewSet();
            view_set = true;
        }
        if (context != null && lastSetWeekNumber > 0) {
            updateAllNextCourseView(context);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.view_set = false;
        this.view = null;
        this.context = null;
        this.courses = null;
        this.schoolTime = null;
        this.course_table_layout = null;
        this.courseMethod = null;
        this.courseViewMethod = null;
        this.spinner_week = null;
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
                Intent intent = new Intent(getActivity(), CourseActivity.class);
                startActivity(intent);
            } else if (item.getItemId() == R.id.menu_table_share) {
                shareTable();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void ViewSet() {
        if (view != null) {
            course_table_layout = view.findViewById(R.id.course_table_layout);

            CardView cardView_date = view.findViewById(R.id.cardview_table_date);
            cardView_date.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    reloadTable(false);
                }
            });

            getData();
        }
    }

    synchronized private void getData() {
        if (context != null) {
            if (loadTime == 0) {
                new TableAsync().execute(context.getApplicationContext());
            } else {
                new TableAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, context.getApplicationContext());
            }
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
    public void CourseSet(@Nullable ArrayList<Course> courses, @Nullable final SchoolTime schoolTime, @Nullable final Context context, boolean isDataReload) {
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
                TextView textView_date = view.findViewById(R.id.textView_table_date);
                Calendar calendar = Calendar.getInstance(Locale.CHINA);
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH) + 1;
                String weekDay = getResources().getStringArray(R.array.week_number)[calendar.get(Calendar.DAY_OF_WEEK) == 1 ? 6 : calendar.get(Calendar.DAY_OF_WEEK) - 2];
                String week = context.getString(R.string.week, weekNum);
                if (schoolTime.getWeekNum() == 0) {
                    week = context.getString(R.string.time_vacation);
                }
                textView_date.setText(context.getString(R.string.table_date, year, month, weekDay, week));

                if (spinner_week == null || isDataReload) {
                    List<String> weekArr = TimeMethod.getWeekArray(context, schoolTime);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, weekArr);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner_week = view.findViewById(R.id.spinner_table_week_chose);
                    if (lastSelect < weekArr.size()) {
                        spinner_week.setSelection(lastSelect);
                    }
                    spinner_week.setAdapter(adapter);
                    spinner_week.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

                //提前显示下一周的课表
                if (schoolTime.getWeekNum() != 0) {
                    int weekDayNum = calendar.get(Calendar.DAY_OF_WEEK);
                    if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Config.PREFERENCE_SHOW_NEXT_WEEK, Config.DEFAULT_PREFERENCE_SHOW_NEXT_WEEK)) {
                        if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Config.PREFERENCE_SHOW_WEEKEND, Config.DEFAULT_PREFERENCE_SHOW_WEEKEND)) {
                            if ((weekDayNum == Calendar.SUNDAY || weekDayNum == Calendar.SATURDAY) && weekNum + 1 <= TimeMethod.getMaxWeekNum(schoolTime)) {
                                schoolTime.setWeekNum(++weekNum);
                            }
                        }
                    }
                }

                setTableData(schoolTime, isDataReload, hasCustomBackground);

                if (lastSelect != weekNum - 1) {
                    spinner_week.setSelection(weekNum - 1);
                    lastSelect = weekNum - 1;
                }

                updateAllNextCourseView(context);
            }
        }
    }

    private void updateAllNextCourseView(@NonNull Context context) {
        //主页面下一节课设置
        ViewPagerAdapter viewPagerAdapter = BaseMethod.getApp(context).getViewPagerAdapter();
        if (viewPagerAdapter != null) {
            HomeFragment homeFragment = viewPagerAdapter.getHomeFragment();
            if (homeFragment != null) {
                if (inVacation) {
                    NextCourse nextCourse = new NextCourse();
                    nextCourse.setInVacation(true);
                    homeFragment.setNextCourse(nextCourse);
                } else if (courseMethod != null) {
                    NextCourse nextCourse = courseMethod.getNextClass(lastSetWeekNumber);
                    homeFragment.setNextCourse(nextCourse);
                }
            }
        }

        if (loadTime == 1) {
            //初始化自动更新
            context.sendBroadcast(new Intent(context, CourseUpdateReceiver.class).setAction(CourseUpdateReceiver.UPDATE_ACTION).putExtra(Config.INTENT_IS_ONLY_INIT, true));
        } else {
            //更新小部件
            context.sendBroadcast(new Intent(NextClassWidget.ACTION_ON_CLICK));
        }
    }

    private void setTableData(@NonNull SchoolTime schoolTime, boolean isDataReload, boolean hasCustomBackground) {
        if (view != null) {
            if (courseViewMethod == null) {
                CardView cardView_course = view.findViewById(R.id.cardView_courseTable);
                courseViewMethod = new CourseViewMethod(context, courses);
                courseViewMethod.setTableView(course_table_layout, cardView_course.getWidth(), cardView_course.getHeight());
            }
            if (isDataReload) {
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
                courseMethod.updateTableCourse(courses, schoolTime, isDataReload);
            }
            if (view != null) {
                ImageView imageView_table_background = view.findViewById(R.id.imageView_table_background);
                if (hasCustomBackground) {
                    Bitmap bitmap = ImageMethod.getTableBackgroundBitmap(getActivity());
                    if (bitmap != null) {
                        imageView_table_background.refreshDrawableState();
                        imageView_table_background.setImageBitmap(bitmap);
                    } else {
                        hasCustomBackground = false;
                    }
                } else {
                    imageView_table_background.setImageDrawable(null);
                    imageView_table_background.refreshDrawableState();
                    if (course_table_layout != null) {
                        course_table_layout.setBackgroundColor(Color.LTGRAY);
                    }
                }
            }
            courseViewMethod.updateCourseTableView(courses, courseMethod.getTableData(), courseMethod.getTableIdData(), courseMethod.getTableShowData(), isDataReload, hasCustomBackground);
        }
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
            if (course.getCourseScore() != null && !course.getCourseScore().isEmpty()) {
                textView_score.setText(context.getString(R.string.course_card_score, course.getCourseScore()));
            } else {
                textView_score.setVisibility(View.GONE);
            }
            if (course.getCourseType() != null && !course.getCourseType().isEmpty()) {
                textView_type.setText(context.getString(R.string.course_card_type, course.getCourseType()));
            } else {
                textView_type.setVisibility(View.GONE);
            }
            if (course.getCourseClass() != null && !course.getCourseClass().isEmpty()) {
                textView_class.setText(context.getString(R.string.course_card_class, course.getCourseClass()));
            } else {
                textView_class.setVisibility(View.GONE);
            }
            if (course.getCourseCombinedClass() != null && !course.getCourseCombinedClass().isEmpty()) {
                textView_class_combined.setText(context.getString(R.string.course_card_combined_class, course.getCourseCombinedClass()));
            } else {
                textView_class_combined.setVisibility(View.GONE);
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            CourseDetail[] details = course.getCourseDetail();
            if (details != null) {
                for (CourseDetail detail : details) {
                    String[] course_weeks = detail.getWeeks();
                    String weekmode = "";
                    if (detail.getWeekMode() == Config.COURSE_DETAIL_WEEKMODE_SINGLE) {
                        weekmode = context.getString(R.string.course_card_time_week_mode, context.getString(R.string.single_week_mode));
                    } else if (detail.getWeekMode() == Config.COURSE_DETAIL_WEEKMODE_DOUBLE) {
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
            }
            builder.setView(view_dialog);
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
                CourseSet(courses, schoolTime, getActivity(), false);
            }
        }
    }

    public void lastViewSet(Context context) {
        if (isAdded()) {
            //离线数据加载完成，开始拉取网络数据
            if (loadTime == 1 && NetMethod.isNetworkConnected(context) && BaseMethod.isDataAutoUpdate(context)) {
                boolean update_day = true;
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                if (sharedPreferences.getBoolean(Config.PREFERENCE_AUTO_UPDATE_COURSE_TABLE, Config.DEFAULT_PREFERENCE_AUTO_UPDATE_COURSE_TABLE)) {
                    Calendar calendar = Calendar.getInstance(Locale.CHINA);
                    long course_table_load_date = sharedPreferences.getLong(Config.PREFERENCE_COURSE_TABLE_AUTO_LOAD_DATE, 0);
                    calendar.setTimeInMillis(course_table_load_date);
                    int load_day = calendar.get(Calendar.DAY_OF_YEAR);
                    int load_year = calendar.get(Calendar.YEAR);
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    int now_day = calendar.get(Calendar.DAY_OF_YEAR);
                    int now_year = calendar.get(Calendar.YEAR);

                    if (load_year == now_year && now_day == load_day) {
                        update_day = false;
                    } else {
                        sharedPreferences.edit().putLong(Config.PREFERENCE_COURSE_TABLE_AUTO_LOAD_DATE, System.currentTimeMillis()).apply();
                    }
                }

                if (update_day) {
                    getData();
                }
            }
        }
    }

    private void shareTable() {
        if (view != null && getActivity() != null) {
            if (PermissionMethod.checkStoragePermission(getActivity(), 0)) {
                View tableView = view.findViewById(R.id.course_table_layout);

                int width = tableView.getWidth();
                int height = tableView.getHeight();
                if (width > 0 && height > 0 && view_set) {
                    final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);

                    canvas.drawColor(Color.WHITE);

                    tableView.layout(0, 0, width, height);
                    tableView.draw(canvas);

                    try {
                        final String path = Config.PICTURE_DICTIONARY_PATH + Config.COURSE_TABLE_FILE_NAME;
                        if (ImageMethod.saveBitmap(bitmap, path, false) && context != null && isAdded() && getActivity() != null) {
                            LayoutInflater layoutInflater = getLayoutInflater();
                            View view = layoutInflater.inflate(R.layout.dialog_share_table, (ViewGroup) getActivity().findViewById(R.id.layout_dialog_share_table));
                            PhotoView photoView = view.findViewById(R.id.photoView_share_table);
                            photoView.setImageDrawable(new BitmapDrawable(getResources(), bitmap));

                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setView(view);
                            builder.setTitle(R.string.share_course_table);
                            builder.setPositiveButton(R.string.share, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (getActivity() != null) {
                                        Uri photoURI = FileProvider.getUriForFile(getActivity(), Config.FILE_PROVIDER_AUTH, new File(path));
                                        Intent intent = new Intent(Intent.ACTION_SEND);
                                        intent.setType("image/*");
                                        intent.putExtra(Intent.EXTRA_STREAM, photoURI);
                                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                        startActivity(Intent.createChooser(intent, getString(R.string.share_course_table)));
                                    }
                                }
                            });
                            builder.setNegativeButton(android.R.string.cancel, null);
                            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    if (!bitmap.isRecycled()) {
                                        bitmap.recycle();
                                    }
                                }
                            });
                            builder.show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), R.string.course_table_share_error, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), R.string.course_table_share_error, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), R.string.permission_error, Toast.LENGTH_SHORT).show();
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
