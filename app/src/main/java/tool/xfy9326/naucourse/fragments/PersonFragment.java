package tool.xfy9326.naucourse.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.Calendar;
import java.util.Locale;

import tool.xfy9326.naucourse.BuildConfig;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.activities.AboutActivity;
import tool.xfy9326.naucourse.activities.CourseSearchActivity;
import tool.xfy9326.naucourse.activities.CourseSettingsActivity;
import tool.xfy9326.naucourse.activities.ExamActivity;
import tool.xfy9326.naucourse.activities.GlobalSettingsActivity;
import tool.xfy9326.naucourse.activities.LevelExamActivity;
import tool.xfy9326.naucourse.activities.LoginActivity;
import tool.xfy9326.naucourse.activities.MoaActivity;
import tool.xfy9326.naucourse.activities.SchoolCalendarActivity;
import tool.xfy9326.naucourse.activities.ScoreActivity;
import tool.xfy9326.naucourse.activities.StudentInfoActivity;
import tool.xfy9326.naucourse.activities.SuspendCourseActivity;
import tool.xfy9326.naucourse.activities.UpdateSettingsActivity;
import tool.xfy9326.naucourse.asyncTasks.StudentAsync;
import tool.xfy9326.naucourse.methods.BaseMethod;
import tool.xfy9326.naucourse.methods.DialogMethod;
import tool.xfy9326.naucourse.methods.LoginMethod;
import tool.xfy9326.naucourse.methods.NetMethod;
import tool.xfy9326.naucourse.methods.SecurityMethod;
import tool.xfy9326.naucourse.utils.SchoolTime;
import tool.xfy9326.naucourse.utils.StudentInfo;
import tool.xfy9326.naucourse.utils.StudentLearnProcess;
import tool.xfy9326.naucourse.widget.NextClassWidget;

/**
 * Created by xfy9326 on 18-2-20.
 */

public class PersonFragment extends Fragment {
    @Nullable
    private View view;
    @Nullable
    private Context context;
    @Nullable
    private SwipeRefreshLayout swipeRefreshLayout;
    private int loadTime = 0;
    @Nullable
    private StudentInfo studentInfo;
    @Nullable
    private StudentLearnProcess studentLearnProcess;
    private Dialog loadingDialog;

    public PersonFragment() {
        this.view = null;
        this.context = null;
        this.swipeRefreshLayout = null;
        this.studentInfo = null;
        this.studentLearnProcess = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
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
    public void onDestroyView() {
        super.onDestroyView();
        this.view = null;
        this.context = null;
        this.swipeRefreshLayout = null;
        this.studentInfo = null;
        this.studentLearnProcess = null;
        this.loadTime = 0;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_person, container, false);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        unlockFunction();
        ViewSet();
    }

    private void ViewSet() {
        if (view != null) {
            swipeRefreshLayout = view.findViewById(R.id.swipeLayout_person);
            swipeRefreshLayout.setDistanceToTriggerSync(200);
            swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark);
            swipeRefreshLayout.setOnRefreshListener(() -> {
                if (NetMethod.isNetworkConnected(context)) {
                    getData();
                } else {
                    Toast.makeText(context, R.string.network_error, Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.post(() -> swipeRefreshLayout.setRefreshing(false));
                }
            });

            if (loadTime == 0) {
                getData();
            }
            CardView cardView_stdInfo = view.findViewById(R.id.cardView_stdInfo);

            CardView cardView_course_settings = view.findViewById(R.id.cardView_course_settings);
            CardView cardView_global_settings = view.findViewById(R.id.cardView_global_settings);
            CardView cardView_update_settings = view.findViewById(R.id.cardView_update);
            CardView cardView_login_out = view.findViewById(R.id.cardView_login_out);
            CardView cardView_about = view.findViewById(R.id.cardView_about);

            CardView cardView_score = view.findViewById(R.id.cardView_score_search);
            CardView cardView_exam = view.findViewById(R.id.cardView_exam);
            CardView cardView_levelExam = view.findViewById(R.id.cardView_level_exam);

            CardView cardView_school_calendar = view.findViewById(R.id.cardView_school_calendar);
            CardView cardView_suspend_course = view.findViewById(R.id.cardView_suspend_course);
            CardView cardView_moa = view.findViewById(R.id.cardView_moa);

            CardView cardView_course_search = view.findViewById(R.id.cardView_course_search);
            //CardView cardView_blank = view.findViewById(R.id.cardView_blank);

            cardView_suspend_course.setOnClickListener(v -> {
                if (getActivity() != null && isAdded()) {
                    startActivity(new Intent(getActivity(), SuspendCourseActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                }
            });

            cardView_moa.setOnClickListener(v -> {
                if (getActivity() != null && isAdded()) {
                    startActivity(new Intent(getActivity(), MoaActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                }
            });

            cardView_school_calendar.setOnClickListener(v -> {
                if (getActivity() != null && isAdded()) {
                    startActivity(new Intent(getActivity(), SchoolCalendarActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                }
            });

            cardView_stdInfo.setOnClickListener(v -> {
                if (getActivity() != null && isAdded()) {
                    if (studentInfo != null && studentLearnProcess != null) {
                        Intent intent = new Intent(getActivity(), StudentInfoActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        intent.putExtra(Config.INTENT_STUDENT_LEARN_PROCESS, studentLearnProcess);
                        intent.putExtra(Config.INTENT_STUDENT_INFO, studentInfo);
                        getActivity().startActivity(intent);
                    } else {
                        Toast.makeText(getActivity(), R.string.data_is_loading, Toast.LENGTH_SHORT).show();
                    }
                }
            });

            cardView_score.setOnClickListener(v -> {
                if (getActivity() != null && isAdded()) {
                    Intent intent = new Intent(getActivity(), ScoreActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    getActivity().startActivity(intent);
                }
            });
            cardView_exam.setOnClickListener(v -> {
                if (getActivity() != null && isAdded()) {
                    Intent intent = new Intent(getActivity(), ExamActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    getActivity().startActivity(intent);
                }
            });
            cardView_levelExam.setOnClickListener(v -> {
                if (getActivity() != null && isAdded()) {
                    Intent intent = new Intent(getActivity(), LevelExamActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    getActivity().startActivity(intent);
                }
            });

            cardView_course_settings.setOnClickListener(v -> {
                if (getActivity() != null && isAdded()) {
                    Intent intent = new Intent(getActivity(), CourseSettingsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    getActivity().startActivity(intent);
                }
            });
            cardView_global_settings.setOnClickListener(v -> {
                if (getActivity() != null && isAdded()) {
                    Intent intent = new Intent(getActivity(), GlobalSettingsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    getActivity().startActivity(intent);
                }
            });
            cardView_update_settings.setOnClickListener(v -> {
                if (getActivity() != null && isAdded()) {
                    Intent intent = new Intent(getActivity(), UpdateSettingsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    getActivity().startActivity(intent);
                }
            });
            cardView_login_out.setOnClickListener(v -> {
                if (getActivity() != null && isAdded()) {
                    if (NetMethod.isNetworkConnected(getActivity())) {
                        loginOut(getActivity());
                    } else {
                        Toast.makeText(getActivity(), R.string.network_error, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            cardView_about.setOnClickListener(v -> {
                if (getActivity() != null && isAdded()) {
                    Intent intent = new Intent(getActivity(), AboutActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    getActivity().startActivity(intent);
                }
            });
            cardView_course_search.setOnClickListener(v -> {
                if (getActivity() != null && isAdded()) {
                    Intent intent = new Intent(getActivity(), CourseSearchActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    getActivity().startActivity(intent);
                }
            });
        }
    }

    private void unlockFunction() {
        if (isAdded() && view != null) {
            CardView cardView_score = view.findViewById(R.id.cardView_score_search);
            CardView cardView_exam = view.findViewById(R.id.cardView_exam);
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            if (BuildConfig.DEBUG || sharedPreferences.getBoolean(Config.PREFERENCE_SHOW_HIDDEN_FUNCTION, Config.DEFAULT_PREFERENCE_SHOW_HIDDEN_FUNCTION)) {
                cardView_score.setVisibility(View.VISIBLE);
                cardView_exam.setVisibility(View.VISIBLE);
            }
        }
    }

    private void loginOut(final Context context) {
        if (getActivity() != null) {
            String userId = SecurityMethod.getUserId(context);
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.login_out);
            builder.setMessage(getString(R.string.ask_login_out, userId));
            builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
                if (getActivity() != null) {
                    loadingDialog = DialogMethod.showLoadingDialog(getActivity(), false, null);
                }
                new Thread(() -> {
                    if (LoginMethod.loginOut(context)) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                if (isAdded()) {
                                    Toast.makeText(getActivity(), R.string.login_out_error, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        if (isAdded() && getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                //小部件清空
                                if (getActivity() != null) {
                                    getActivity().sendBroadcast(new Intent(NextClassWidget.ACTION_ON_UPDATE));
                                }
                                //重启当前程序
                                Intent intent = new Intent(context, LoginActivity.class);
                                startActivityForResult(intent, Config.REQUEST_ACTIVITY_LOGIN);
                                if (getActivity() != null) {
                                    getActivity().finish();
                                }
                            });
                        }
                    }
                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (loadingDialog != null && loadingDialog.isShowing()) {
                                loadingDialog.cancel();
                                loadingDialog = null;
                            }
                        });
                    }
                }).start();
            });
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.show();
        }
    }

    public void PersonViewSet(@Nullable StudentInfo studentInfo, @Nullable StudentLearnProcess
            studentLearnProcess, @Nullable Context context) {
        if (isAdded() && view != null) {
            if (context != null && studentInfo != null) {
                ((TextView) view.findViewById(R.id.textView_stdId)).setText(studentInfo.getStd_id());
                ((TextView) view.findViewById(R.id.textView_stdName)).setText(studentInfo.getStd_name());
                this.studentInfo = studentInfo;
            }
            if (studentLearnProcess != null) {
                this.studentLearnProcess = studentLearnProcess;
            }
        }
    }

    public void TimeTextSet(@Nullable SchoolTime schoolTime, @Nullable Context context) {
        if (isAdded() && view != null) {
            if (context != null && schoolTime != null) {
                ((TextView) view.findViewById(R.id.textView_timeSchool)).setText(context.getString(R.string.time_school, schoolTime.getStartTime(), schoolTime.getEndTime()));
            }
        }
    }

    public void lastViewSet(Context context, boolean mustReload) {
        if (isAdded()) {
            //离线数据加载完成，开始拉取网络数据，数据每天更新
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            if (loadTime == 1 && NetMethod.isNetworkConnected(context) && BaseMethod.isDataAutoUpdate(context)) {
                boolean update_day = true;

                if (!mustReload && sharedPreferences.getBoolean(Config.PREFERENCE_ASYNC_PERSONAL_INFO_BY_DAY, Config.DEFAULT_PREFERENCE_ASYNC_PERSONAL_INFO_BY_DAY)) {
                    Calendar calendar = Calendar.getInstance(Locale.CHINA);
                    long personal_info_load_date = sharedPreferences.getLong(Config.PREFERENCE_PERSONAL_INFO_LOAD_DATE_TIME, 0);
                    calendar.setTimeInMillis(personal_info_load_date);
                    int load_day = calendar.get(Calendar.DAY_OF_YEAR);
                    int load_year = calendar.get(Calendar.YEAR);
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    int now_day = calendar.get(Calendar.DAY_OF_YEAR);
                    int now_year = calendar.get(Calendar.YEAR);

                    if (load_year == now_year && now_day == load_day) {
                        update_day = false;
                    } else {
                        sharedPreferences.edit().putLong(Config.PREFERENCE_PERSONAL_INFO_LOAD_DATE_TIME, System.currentTimeMillis()).apply();
                    }
                }

                if (update_day) {
                    getData();
                } else {
                    BaseMethod.setRefreshing(swipeRefreshLayout, false);
                }
            } else {
                BaseMethod.setRefreshing(swipeRefreshLayout, false);
            }
        }
    }

    synchronized private void getData() {
        BaseMethod.setRefreshing(swipeRefreshLayout, true);
        if (context != null) {
            new StudentAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, context.getApplicationContext());
        }
    }

    public int getLoadTime() {
        return loadTime;
    }

    public void setLoadTime(int loadTime) {
        this.loadTime = loadTime;
    }

}
