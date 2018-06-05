package tool.xfy9326.naucourse.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

import tool.xfy9326.naucourse.Activities.AboutActivity;
import tool.xfy9326.naucourse.Activities.ExamActivity;
import tool.xfy9326.naucourse.Activities.LevelExamActivity;
import tool.xfy9326.naucourse.Activities.LoginActivity;
import tool.xfy9326.naucourse.Activities.MoaActivity;
import tool.xfy9326.naucourse.Activities.SchoolCalendarActivity;
import tool.xfy9326.naucourse.Activities.ScoreActivity;
import tool.xfy9326.naucourse.Activities.SettingsActivity;
import tool.xfy9326.naucourse.Activities.StudentInfoActivity;
import tool.xfy9326.naucourse.Activities.SuspendCourseActivity;
import tool.xfy9326.naucourse.Activities.WifiConnectActivity;
import tool.xfy9326.naucourse.AsyncTasks.StudentAsync;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.Methods.LoginMethod;
import tool.xfy9326.naucourse.Methods.NetMethod;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Utils.SchoolTime;
import tool.xfy9326.naucourse.Utils.StudentInfo;
import tool.xfy9326.naucourse.Utils.StudentLearnProcess;
import tool.xfy9326.naucourse.Views.NextClassWidget;

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

    public PersonFragment() {
        this.view = null;
        this.context = null;
        this.swipeRefreshLayout = null;
        this.studentInfo = null;
        this.studentLearnProcess = null;
    }

    @Override
    public void onAttach(Context context) {
        this.context = context;
        super.onAttach(context);
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
        ViewSet();
    }

    private void ViewSet() {
        swipeRefreshLayout = Objects.requireNonNull(view).findViewById(R.id.swipeLayout_person);
        swipeRefreshLayout.setDistanceToTriggerSync(200);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (NetMethod.isNetworkConnected(context)) {
                    getData();
                } else {
                    Toast.makeText(context, R.string.network_error, Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.post(new Runnable() {
                        @Override
                        public void run() {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                }
            }
        });

        if (loadTime == 0) {
            getData();
        }
        CardView cardView_stdInfo = view.findViewById(R.id.cardView_stdInfo);

        CardView cardView_settings = view.findViewById(R.id.cardView_settings);
        CardView cardView_login_out = view.findViewById(R.id.cardView_login_out);
        CardView cardView_about = view.findViewById(R.id.cardView_about);

        CardView cardView_score = view.findViewById(R.id.cardView_score_search);
        CardView cardView_exam = view.findViewById(R.id.cardView_exam);
        CardView cardView_wifi = view.findViewById(R.id.cardView_wifi);
        CardView cardView_levelExam = view.findViewById(R.id.cardView_level_exam);

        CardView cardView_school_calendar = view.findViewById(R.id.cardView_school_calendar);
        CardView cardView_suspend_course = view.findViewById(R.id.cardView_suspend_course);
        CardView cardView_moa = view.findViewById(R.id.cardView_moa);

        cardView_suspend_course.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    startActivity(new Intent(getActivity(), SuspendCourseActivity.class));
                }
            }
        });

        cardView_moa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    startActivity(new Intent(getActivity(), MoaActivity.class));
                }
            }
        });

        cardView_school_calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    startActivity(new Intent(getActivity(), SchoolCalendarActivity.class));
                }
            }
        });

        cardView_stdInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    if (studentInfo != null && studentLearnProcess != null) {
                        Intent intent = new Intent(getActivity(), StudentInfoActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(Config.INTENT_STUDENT_LEARN_PROCESS, studentLearnProcess);
                        intent.putExtra(Config.INTENT_STUDENT_INFO, studentInfo);
                        getActivity().startActivity(intent);
                    } else {
                        Toast.makeText(getActivity(), R.string.data_is_loading, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        cardView_wifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAdded() && getActivity() != null) {
                    Intent intent = new Intent(getActivity(), WifiConnectActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getActivity().startActivity(intent);
                }
            }
        });
        cardView_score.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    if (isAdded()) {
                        Intent intent = new Intent(getActivity(), ScoreActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getActivity().startActivity(intent);
                    }
                }
            }
        });
        cardView_exam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    if (isAdded()) {
                        Intent intent = new Intent(getActivity(), ExamActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getActivity().startActivity(intent);
                    }
                }
            }
        });
        cardView_levelExam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    if (isAdded()) {
                        Intent intent = new Intent(getActivity(), LevelExamActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getActivity().startActivity(intent);
                    }
                }
            }
        });

        cardView_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    if (isAdded()) {
                        Intent intent = new Intent(getActivity(), SettingsActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getActivity().startActivity(intent);
                    }
                }
            }
        });
        cardView_login_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    if (isAdded()) {
                        if (NetMethod.isNetworkConnected(getActivity())) {
                            loginOut(getActivity());
                        } else {
                            Toast.makeText(getActivity(), R.string.network_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
        cardView_about.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    if (isAdded()) {
                        Intent intent = new Intent(getActivity(), AboutActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getActivity().startActivity(intent);
                    }
                }
            }
        });
    }

    private void loginOut(final Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String userId = sharedPreferences.getString(Config.PREFERENCE_USER_ID, Config.DEFAULT_PREFERENCE_USER_ID);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.login_out);
        builder.setMessage(getString(R.string.ask_login_out, userId));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (LoginMethod.loginOut(context)) {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (isAdded()) {
                                            Toast.makeText(getActivity(), R.string.login_out_error, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        } else {
                            if (isAdded() && getActivity() != null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //小部件清空
                                        getActivity().sendBroadcast(new Intent(NextClassWidget.ACTION_ON_CLICK));
                                        //重启当前程序
                                        Intent intent = new Intent(context, LoginActivity.class);
                                        startActivityForResult(intent, Config.REQUEST_ACTIVITY_LOGIN);
                                        getActivity().finish();
                                    }
                                });
                            }
                        }
                    }
                }).start();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }

    public void PersonViewSet(@Nullable StudentInfo studentInfo, @Nullable StudentLearnProcess
            studentLearnProcess, @Nullable Context context) {
        if (isAdded()) {
            if (context != null && studentInfo != null) {
                ((TextView) Objects.requireNonNull(view).findViewById(R.id.textView_stdId)).setText(studentInfo.getStd_id());
                ((TextView) view.findViewById(R.id.textView_stdName)).setText(studentInfo.getStd_name());
                this.studentInfo = studentInfo;
            }
            if (studentLearnProcess != null) {
                this.studentLearnProcess = studentLearnProcess;
            }
        }
    }

    public void TimeTextSet(@Nullable SchoolTime schoolTime, @Nullable Context context) {
        if (isAdded()) {
            if (context != null && schoolTime != null) {
                ((TextView) Objects.requireNonNull(view).findViewById(R.id.textView_timeSchool)).setText(context.getString(R.string.time_school, schoolTime.getStartTime(), schoolTime.getEndTime()));
            }
        }
    }

    public void lastViewSet(Context context) {
        if (isAdded()) {
            if (swipeRefreshLayout != null) {
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.post(new Runnable() {
                        @Override
                        public void run() {
                            if (isAdded()) {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        }
                    });
                }
            }
            //离线数据加载完成，开始拉取网络数据
            if (loadTime == 1 && NetMethod.isNetworkConnected(context) && BaseMethod.isDataAutoUpdate(context)) {
                swipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(true);
                    }
                });
                getData();
            }
        }
    }

    private void getData() {
        new StudentAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Objects.requireNonNull(context).getApplicationContext());
    }

    public int getLoadTime() {
        return loadTime;
    }

    public void setLoadTime(int loadTime) {
        this.loadTime = loadTime;
    }

}
