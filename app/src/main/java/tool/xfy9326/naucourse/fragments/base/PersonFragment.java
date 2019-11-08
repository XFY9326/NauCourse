package tool.xfy9326.naucourse.fragments.base;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
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
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.Calendar;
import java.util.Locale;

import tool.xfy9326.naucourse.BuildConfig;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.activities.AboutActivity;
import tool.xfy9326.naucourse.activities.CourseSearchActivity;
import tool.xfy9326.naucourse.activities.LoginActivity;
import tool.xfy9326.naucourse.activities.async.ExamActivity;
import tool.xfy9326.naucourse.activities.async.LevelExamActivity;
import tool.xfy9326.naucourse.activities.async.SchoolCalendarActivity;
import tool.xfy9326.naucourse.activities.async.ScoreActivity;
import tool.xfy9326.naucourse.activities.async.StudentInfoActivity;
import tool.xfy9326.naucourse.activities.async.SuspendCourseActivity;
import tool.xfy9326.naucourse.activities.settings.CourseSettingsActivity;
import tool.xfy9326.naucourse.activities.settings.GlobalSettingsActivity;
import tool.xfy9326.naucourse.activities.settings.UpdateSettingsActivity;
import tool.xfy9326.naucourse.asyncTasks.StudentAsync;
import tool.xfy9326.naucourse.beans.SchoolTime;
import tool.xfy9326.naucourse.beans.student.StudentInfo;
import tool.xfy9326.naucourse.beans.student.StudentLearnProcess;
import tool.xfy9326.naucourse.methods.BaseMethod;
import tool.xfy9326.naucourse.methods.net.LoginMethod;
import tool.xfy9326.naucourse.methods.net.NetMethod;
import tool.xfy9326.naucourse.methods.net.SecurityMethod;
import tool.xfy9326.naucourse.methods.view.DialogMethod;
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
        viewSet();
    }

    private void viewSet() {
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
            CardView cardViewStdInfo = view.findViewById(R.id.cardView_stdInfo);

            CardView cardViewCourseSettings = view.findViewById(R.id.cardView_course_settings);
            CardView cardViewGlobalSettings = view.findViewById(R.id.cardView_global_settings);
            CardView cardViewUpdateSettings = view.findViewById(R.id.cardView_update);
            CardView cardViewLoginOut = view.findViewById(R.id.cardView_login_out);
            CardView cardViewAbout = view.findViewById(R.id.cardView_about);

            CardView cardViewScore = view.findViewById(R.id.cardView_score_search);
            CardView cardViewExam = view.findViewById(R.id.cardView_exam);
            CardView cardViewLevelExam = view.findViewById(R.id.cardView_level_exam);

            CardView cardViewSchoolCalendar = view.findViewById(R.id.cardView_school_calendar);
            CardView cardViewSuspendCourse = view.findViewById(R.id.cardView_suspend_course);

            CardView cardViewCourseSearch = view.findViewById(R.id.cardView_course_search);

            cardViewSuspendCourse.setOnClickListener(v -> {
                if (getActivity() != null && isAdded()) {
                    startActivity(new Intent(getActivity(), SuspendCourseActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                }
            });

            cardViewSchoolCalendar.setOnClickListener(v -> {
                if (getActivity() != null && isAdded()) {
                    startActivity(new Intent(getActivity(), SchoolCalendarActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                }
            });

            cardViewStdInfo.setOnClickListener(v -> {
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

            cardViewScore.setOnClickListener(v -> {
                if (getActivity() != null && isAdded()) {
                    Intent intent = new Intent(getActivity(), ScoreActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    getActivity().startActivity(intent);
                }
            });
            cardViewExam.setOnClickListener(v -> {
                if (getActivity() != null && isAdded()) {
                    Intent intent = new Intent(getActivity(), ExamActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    getActivity().startActivity(intent);
                }
            });
            cardViewLevelExam.setOnClickListener(v -> {
                if (getActivity() != null && isAdded()) {
                    Intent intent = new Intent(getActivity(), LevelExamActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    getActivity().startActivity(intent);
                }
            });

            cardViewCourseSettings.setOnClickListener(v -> {
                if (getActivity() != null && isAdded()) {
                    Intent intent = new Intent(getActivity(), CourseSettingsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    getActivity().startActivity(intent);
                }
            });
            cardViewGlobalSettings.setOnClickListener(v -> {
                if (getActivity() != null && isAdded()) {
                    Intent intent = new Intent(getActivity(), GlobalSettingsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    getActivity().startActivityForResult(intent, Config.REQUEST_GLOBAL_SETTINGS);
                }
            });
            cardViewUpdateSettings.setOnClickListener(v -> {
                if (getActivity() != null && isAdded()) {
                    Intent intent = new Intent(getActivity(), UpdateSettingsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    getActivity().startActivity(intent);
                }
            });
            cardViewLoginOut.setOnClickListener(v -> {
                if (getActivity() != null && isAdded()) {
                    if (NetMethod.isNetworkConnected(getActivity())) {
                        loginOut(getActivity());
                    } else {
                        Toast.makeText(getActivity(), R.string.network_error, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            cardViewAbout.setOnClickListener(v -> {
                if (getActivity() != null && isAdded()) {
                    Intent intent = new Intent(getActivity(), AboutActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    getActivity().startActivity(intent);
                }
            });
            cardViewCourseSearch.setOnClickListener(v -> {
                if (getActivity() != null && isAdded()) {
                    Intent intent = new Intent(getActivity(), CourseSearchActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    getActivity().startActivity(intent);
                }
            });
        }
    }

    private void unlockFunction() {
        if (isAdded() && view != null && context != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            if (BuildConfig.DEBUG || sharedPreferences.getBoolean(Config.PREFERENCE_SHOW_HIDDEN_FUNCTION, Config.DEFAULT_PREFERENCE_SHOW_HIDDEN_FUNCTION)) {
                view.findViewById(R.id.layout_fragment_function_list2).setVisibility(View.VISIBLE);
                view.findViewById(R.id.cardView_score_search).setVisibility(View.VISIBLE);
                view.findViewById(R.id.cardView_exam).setVisibility(View.VISIBLE);
            }
        }
    }

    private void loginOut(final Context context) {
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
                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (isAdded()) {
                                closeLoadingDialog();
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
                                if (isAdded()) {
                                    closeLoadingDialog();
                                }
                                //重启当前程序
                                Intent intent = new Intent(context, LoginActivity.class);
                                startActivityForResult(intent, Config.REQUEST_ACTIVITY_LOGIN);
                                getActivity().finish();
                            }

                        });
                    }
                }
            }).start();
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        if (isAdded() && getActivity() != null) {
            builder.show();
        }
    }

    private void closeLoadingDialog() {
        if (isAdded() && loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.cancel();
            loadingDialog = null;
        }
    }

    public void personViewSet(@Nullable StudentInfo studentInfo, @Nullable StudentLearnProcess
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

    public void timeTextSet(@Nullable SchoolTime schoolTime, @Nullable Context context) {
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
                boolean updateDay = true;

                if (!mustReload && sharedPreferences.getBoolean(Config.PREFERENCE_ASYNC_PERSONAL_INFO_BY_DAY, Config.DEFAULT_PREFERENCE_ASYNC_PERSONAL_INFO_BY_DAY)) {
                    Calendar calendar = Calendar.getInstance(Locale.CHINA);
                    long personalInfoLoadDate = sharedPreferences.getLong(Config.PREFERENCE_PERSONAL_INFO_LOAD_DATE_TIME, 0);
                    calendar.setTimeInMillis(personalInfoLoadDate);
                    int loadDay = calendar.get(Calendar.DAY_OF_YEAR);
                    int loadYear = calendar.get(Calendar.YEAR);
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    int nowDay = calendar.get(Calendar.DAY_OF_YEAR);
                    int nowYear = calendar.get(Calendar.YEAR);

                    if (loadYear == nowYear && nowDay == loadDay) {
                        updateDay = false;
                    } else {
                        sharedPreferences.edit().putLong(Config.PREFERENCE_PERSONAL_INFO_LOAD_DATE_TIME, System.currentTimeMillis()).apply();
                    }
                }

                if (updateDay) {
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
