package tool.xfy9326.naucourse.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import tool.xfy9326.naucourse.Activities.AboutActivity;
import tool.xfy9326.naucourse.Activities.ScoreActivity;
import tool.xfy9326.naucourse.Activities.SettingsActivity;
import tool.xfy9326.naucourse.AsyncTasks.StudentAsync;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Utils.SchoolTime;
import tool.xfy9326.naucourse.Utils.StudentInfo;

/**
 * Created by xfy9326 on 18-2-20.
 */

public class PersonFragment extends Fragment {
    private View view;
    private Context context;
    private SwipeRefreshLayout swipeRefreshLayout;
    private int loadTime = 0;

    public PersonFragment() {
        this.view = null;
        this.context = null;
        this.swipeRefreshLayout = null;
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
        swipeRefreshLayout = view.findViewById(R.id.swipeLayout_person);
        swipeRefreshLayout.setDistanceToTriggerSync(200);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (BaseMethod.isNetworkConnected(context)) {
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

        CardView cardView_settings = view.findViewById(R.id.cardView_settings);
        CardView cardView_about = view.findViewById(R.id.cardView_about);
        CardView cardView_score = view.findViewById(R.id.cardView_score_search);
        cardView_score.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ScoreActivity.class);
                context.startActivity(intent);
            }
        });
        cardView_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SettingsActivity.class);
                startActivityForResult(intent, Config.REQUEST_ACTIVITY_SETTINGS_LOGIN_OUT);
            }
        });
        cardView_about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AboutActivity.class);
                context.startActivity(intent);
            }
        });
    }

    public void PersonTextSet(StudentInfo studentInfo, Context context) {
        if (context != null && studentInfo != null) {
            ((TextView) view.findViewById(R.id.textView_stdId)).setText(context.getString(R.string.std_id, studentInfo.getStd_id()));
            ((TextView) view.findViewById(R.id.textView_stdClass)).setText(context.getString(R.string.std_class, studentInfo.getStd_class()));
            ((TextView) view.findViewById(R.id.textView_stdCollage)).setText(context.getString(R.string.std_collage, studentInfo.getStd_collage()));
            ((TextView) view.findViewById(R.id.textView_stdDirection)).setText(context.getString(R.string.std_direction, studentInfo.getStd_direction()));
            ((TextView) view.findViewById(R.id.textView_stdGrade)).setText(context.getString(R.string.std_grade, studentInfo.getStd_grade()));
            ((TextView) view.findViewById(R.id.textView_stdMajor)).setText(context.getString(R.string.std_major, studentInfo.getStd_major()));
            ((TextView) view.findViewById(R.id.textView_stdName)).setText(context.getString(R.string.std_name, studentInfo.getStd_name()));
        }
    }

    public void TimeTextSet(SchoolTime schoolTime, Context context) {
        if (context != null && schoolTime != null) {
            schoolTime.setWeekNum(BaseMethod.getNowWeekNum(schoolTime));

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
            String time = context.getString(R.string.time_now) + simpleDateFormat.format(new Date());
            ((TextView) view.findViewById(R.id.textView_timeNow)).setText(time);

            String week;
            if (schoolTime.getWeekNum() == 0) {
                week = context.getString(R.string.time_week) + context.getString(R.string.time_vacation);
            } else {
                week = context.getString(R.string.time_week) + context.getString(R.string.week, schoolTime.getWeekNum());
            }
            ((TextView) view.findViewById(R.id.textView_timeWeek)).setText(week);

            ((TextView) view.findViewById(R.id.textView_timeSchoolStart)).setText(context.getString(R.string.time_school_start, schoolTime.getStartTime()));
            ((TextView) view.findViewById(R.id.textView_timeSchoolEnd)).setText(context.getString(R.string.time_school_end, schoolTime.getEndTime()));
        }
    }

    public void lastViewSet(Context context) {
        if (swipeRefreshLayout != null) {
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        }
        //离线数据加载完成，开始拉取网络数据
        if (loadTime == 1 && BaseMethod.isNetworkConnected(context) && BaseMethod.isDataAutoUpdate(context)) {
            swipeRefreshLayout.setProgressViewOffset(false, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));
            swipeRefreshLayout.setRefreshing(true);
            getData();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Config.REQUEST_ACTIVITY_SETTINGS_LOGIN_OUT) {
            if (data != null) {
                if (data.getBooleanExtra(Config.INTENT_IS_LOGIN_OUT, false)) {
                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void getData() {
        new StudentAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, context.getApplicationContext());
    }

    public int getLoadTime() {
        return loadTime;
    }

    public void setLoadTime(int loadTime) {
        this.loadTime = loadTime;
    }

}
