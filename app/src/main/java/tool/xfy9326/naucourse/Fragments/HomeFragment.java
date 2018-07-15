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
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import tool.xfy9326.naucourse.AsyncTasks.InfoAsync;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.Methods.DataMethod;
import tool.xfy9326.naucourse.Methods.NetMethod;
import tool.xfy9326.naucourse.Methods.NextClassMethod;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Utils.JwTopic;
import tool.xfy9326.naucourse.Utils.JwcTopic;
import tool.xfy9326.naucourse.Utils.NextCourse;
import tool.xfy9326.naucourse.Views.RecyclerViews.InfoAdapter;
import tool.xfy9326.naucourse.Widget.NextClassWidget;

/**
 * Created by xfy9326 on 18-2-20.
 */

public class HomeFragment extends Fragment {
    public static final String NEXT_COURSE_FILE_NAME = "NextCourse";
    @Nullable
    private View view;
    @Nullable
    private Context context;
    @Nullable
    private RecyclerView recyclerView;
    @Nullable
    private SwipeRefreshLayout swipeRefreshLayout;
    @Nullable
    private InfoAdapter infoAdapter;
    private int loadTime = 0;
    private int lastOffset = 0;
    private int lastPosition = 0;

    public HomeFragment() {
        this.view = null;
        this.context = null;
        this.recyclerView = null;
        this.swipeRefreshLayout = null;
        this.infoAdapter = null;
    }

    @Override
    public void onAttach(Context context) {
        this.context = context;
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        ViewSet();
    }

    private void ViewSet() {
        recyclerView = Objects.requireNonNull(view).findViewById(R.id.recyclerView_information);
        recyclerView.setFocusableInTouchMode(false);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        //保证从其他视图返回时列表位置不变
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (recyclerView.getLayoutManager() != null) {
                    getPositionAndOffset();
                }
            }
        });
        scrollToPosition();

        swipeRefreshLayout = view.findViewById(R.id.swipeLayout_home);
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

        TextView textView_dateNow = view.findViewById(R.id.textView_dateNow);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        textView_dateNow.setText(simpleDateFormat.format(new Date()));

        loadTempNextCourse();

        CardView cardView_nextClass = view.findViewById(R.id.cardView_local_next_course);
        cardView_nextClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setNextCourse();
                Objects.requireNonNull(context).sendBroadcast(new Intent(NextClassWidget.ACTION_ON_CLICK));
            }
        });

    }

    //优先加载缓存中的下一节课
    private void loadTempNextCourse() {
        NextCourse nextCourse = (NextCourse) DataMethod.getOfflineData(Objects.requireNonNull(context), NextCourse.class, NEXT_COURSE_FILE_NAME);
        if (nextCourse != null) {
            setNextCourse(nextCourse.getCourseName(), nextCourse.getCourseLocation(), nextCourse.getCourseTeacher(), nextCourse.getCourseTime());
        }
    }

    //内部刷新设置下一节课
    private void setNextCourse() {
        NextCourse nextCourse = NextClassMethod.getNextClassArray(Objects.requireNonNull(getActivity()));
        if (DataMethod.saveOfflineData(Objects.requireNonNull(context), nextCourse, NEXT_COURSE_FILE_NAME, false)) {
            setNextCourse(nextCourse.getCourseName(), nextCourse.getCourseLocation(), nextCourse.getCourseTeacher(), nextCourse.getCourseTime());
        }
    }

    /**
     * 设置下一节课
     * 主要用于外部调用更新UI
     *
     * @param name     课程名称
     * @param location 上课地点
     * @param teacher  上课老师
     * @param time     上课时间
     */
    public void setNextCourse(@Nullable String name, String location, String teacher, String time) {
        if (isAdded()) {
            TextView textView_noNextClass = Objects.requireNonNull(view).findViewById(R.id.textView_noNextClass);
            LinearLayout linearLayout_nextClass = view.findViewById(R.id.layout_nextClass);
            if (name == null) {
                linearLayout_nextClass.setVisibility(View.GONE);
                textView_noNextClass.setVisibility(View.VISIBLE);
                DataMethod.deleteOfflineData(Objects.requireNonNull(context), NEXT_COURSE_FILE_NAME);
            } else {
                time = time.replace("~", "\n~\n").trim();

                TextView textView_nextClass = view.findViewById(R.id.textView_nextClass);
                TextView textView_nextLocation = view.findViewById(R.id.textView_nextLocation);
                TextView textView_nextTeacher = view.findViewById(R.id.textView_nextTeacher);
                TextView textView_nextTime = view.findViewById(R.id.textView_nextTime);

                textView_nextClass.setText(name);
                textView_nextLocation.setText(location);
                textView_nextTeacher.setText(teacher);
                textView_nextTime.setText(time);

                textView_noNextClass.setVisibility(View.GONE);
                linearLayout_nextClass.setVisibility(View.VISIBLE);
            }
        }
    }

    public int getLoadTime() {
        return loadTime;
    }

    public void setLoadTime(int loadTime) {
        this.loadTime = loadTime;
    }

    public void InfoSet(@Nullable JwcTopic jwcTopic, @Nullable JwTopic jwTopic) {
        if (isAdded()) {
            if (jwcTopic != null && jwTopic != null) {
                if (infoAdapter == null) {
                    infoAdapter = new InfoAdapter(getActivity(), jwcTopic, jwTopic);
                    Objects.requireNonNull(recyclerView).setAdapter(infoAdapter);
                } else {
                    infoAdapter.updateJwcTopic(jwcTopic, jwTopic);
                }
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

    //还原下拉的列表位置
    private void getPositionAndOffset() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) Objects.requireNonNull(recyclerView).getLayoutManager();
        View topView = layoutManager.getChildAt(0);
        if (topView != null) {
            lastOffset = topView.getTop();
            lastPosition = layoutManager.getPosition(topView);
        }
    }

    private void scrollToPosition() {
        if (Objects.requireNonNull(recyclerView).getLayoutManager() != null && lastPosition >= 0) {
            ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(lastPosition, lastOffset);
        }
    }

    synchronized private void getData() {
        if (context != null) {
            if (loadTime == 0) {
                new InfoAsync().execute(context.getApplicationContext());
            } else {
                new InfoAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, context.getApplicationContext());
            }
        }
    }

}
