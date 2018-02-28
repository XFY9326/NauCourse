package tool.xfy9326.naucourse.Fragments;

import android.annotation.SuppressLint;
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
import tool.xfy9326.naucourse.Activities.SettingsActivity;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.Methods.PersonMethod;
import tool.xfy9326.naucourse.Methods.TimeMethod;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Utils.SchoolTime;
import tool.xfy9326.naucourse.Utils.StudentInfo;
import tool.xfy9326.naucourse.Utils.StudentScore;

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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
                    new StudentAsync().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, context);
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
            new StudentAsync().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, context);
        }

        CardView cardView_settings = view.findViewById(R.id.cardView_settings);
        CardView cardView_about = view.findViewById(R.id.cardView_about);
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

    private void PersonTextSet(StudentInfo studentInfo, StudentScore studentScore, Context context) {
        if (context != null && studentInfo != null && studentScore != null) {
            ((TextView) view.findViewById(R.id.textView_stdId)).setText(context.getString(R.string.std_id, studentInfo.getStd_id()));
            ((TextView) view.findViewById(R.id.textView_stdClass)).setText(context.getString(R.string.std_class, studentInfo.getStd_class()));
            ((TextView) view.findViewById(R.id.textView_stdCollage)).setText(context.getString(R.string.std_collage, studentInfo.getStd_collage()));
            ((TextView) view.findViewById(R.id.textView_stdDirection)).setText(context.getString(R.string.std_direction, studentInfo.getStd_direction()));
            ((TextView) view.findViewById(R.id.textView_stdGrade)).setText(context.getString(R.string.std_grade, studentInfo.getStd_grade()));
            ((TextView) view.findViewById(R.id.textView_stdMajor)).setText(context.getString(R.string.std_major, studentInfo.getStd_major()));
            ((TextView) view.findViewById(R.id.textView_stdName)).setText(context.getString(R.string.std_name, studentInfo.getStd_name()));

            ((TextView) view.findViewById(R.id.textView_scoreXF)).setText(context.getString(R.string.score_XF, studentScore.getScoreXF()));
            ((TextView) view.findViewById(R.id.textView_scoreJD)).setText(context.getString(R.string.score_JD, studentScore.getScoreJD()));
            ((TextView) view.findViewById(R.id.textView_scoreNP)).setText(context.getString(R.string.score_NP, studentScore.getScoreNP()));
            ((TextView) view.findViewById(R.id.textView_scoreZP)).setText(context.getString(R.string.score_ZP, studentScore.getScoreZP()));
            ((TextView) view.findViewById(R.id.textView_scoreBP)).setText(context.getString(R.string.score_BP, studentScore.getScoreBP()));
        }
    }

    private void TimeTextSet(SchoolTime schoolTime, Context context) {
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

    @SuppressLint("StaticFieldLeak")
    class StudentAsync extends AsyncTask<Context, Void, Context> {
        boolean personLoadSuccess = false;
        boolean timeLoadSuccess = false;
        private StudentScore studentScore;
        private StudentInfo studentInfo;
        private SchoolTime schoolTime;

        StudentAsync() {
            studentScore = null;
            studentInfo = null;
            schoolTime = null;
        }

        @Override
        protected Context doInBackground(Context... context) {
            if (context[0] != null) {
                if (loadTime == 0) {
                    studentScore = (StudentScore) BaseMethod.getOfflineData(context[0], StudentScore.class, PersonMethod.FILE_NAME_SCORE);
                    studentInfo = (StudentInfo) BaseMethod.getOfflineData(context[0], StudentInfo.class, PersonMethod.FILE_NAME_DATA);
                    schoolTime = (SchoolTime) BaseMethod.getOfflineData(context[0], SchoolTime.class, TimeMethod.FILE_NAME);
                    personLoadSuccess = true;
                    timeLoadSuccess = true;
                    loadTime++;
                    return context[0];
                } else {
                    PersonMethod personMethod = new PersonMethod(context[0]);
                    personLoadSuccess = personMethod.load();
                    if (personLoadSuccess) {
                        studentScore = personMethod.getUserScore();
                        studentInfo = personMethod.getUserData();
                    }

                    TimeMethod timeMethod = new TimeMethod(context[0]);
                    timeLoadSuccess = timeMethod.load();
                    if (timeLoadSuccess) {
                        schoolTime = timeMethod.getSchoolTime();
                    }
                    if (personLoadSuccess || timeLoadSuccess) {
                        loadTime++;
                        return context[0];
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Context context) {
            if (personLoadSuccess) {
                PersonTextSet(studentInfo, studentScore, context);
            }
            if (timeLoadSuccess) {
                TimeTextSet(schoolTime, context);
            }
            if (personLoadSuccess || timeLoadSuccess) {
                if (swipeRefreshLayout != null) {
                    if (swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
                //离线数据加载完成，开始拉取网络数据
                if (loadTime == 1 && BaseMethod.isNetworkConnected(context) && BaseMethod.isDataAutoUpdate(context)) {

                    swipeRefreshLayout.setProgressViewOffset(false, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));
                    swipeRefreshLayout.setRefreshing(true);
                    new StudentAsync().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, context);

                }
            }
            super.onPostExecute(context);
        }
    }
}
