package tool.xfy9326.naucourse.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import tool.xfy9326.naucourse.AsyncTasks.InfoAsync;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.Methods.DataMethod;
import tool.xfy9326.naucourse.Methods.NetMethod;
import tool.xfy9326.naucourse.Methods.NextClassMethod;
import tool.xfy9326.naucourse.Methods.TimeMethod;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Utils.NextCourse;
import tool.xfy9326.naucourse.Utils.TopicInfo;
import tool.xfy9326.naucourse.Views.RecyclerAdapters.InfoAdapter;
import tool.xfy9326.naucourse.Widget.NextClassWidget;

/**
 * Created by xfy9326 on 18-2-20.
 */

public class HomeFragment extends Fragment {
    private static String[] infoTypeList;
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
    private boolean[] infoSelectList;

    public HomeFragment() {
        this.view = null;
        this.context = null;
        this.recyclerView = null;
        this.swipeRefreshLayout = null;
        this.infoAdapter = null;
        infoTypeList = new String[0];
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        this.context = context;
        infoTypeList = context.getResources().getStringArray(R.array.notification_type);
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
        this.loadTime = 0;
        this.view = null;
        this.context = null;
        this.recyclerView = null;
        this.swipeRefreshLayout = null;
        this.infoAdapter = null;
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
        if (view != null) {
            if (recyclerView == null) {
                recyclerView = view.findViewById(R.id.recyclerView_information);
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
            }
            scrollToPosition();
            view.findViewById(R.id.cardView_info_title).setOnClickListener(v -> {
                if (recyclerView != null && isAdded()) {
                    recyclerView.smoothScrollToPosition(0);
                }
            });

            if (swipeRefreshLayout == null) {
                swipeRefreshLayout = view.findViewById(R.id.swipeLayout_home);
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
            }

            if (loadTime == 0) {
                getData();
            }

            TextView textView_dateNow = view.findViewById(R.id.textView_dateNow);
            textView_dateNow.setText(TimeMethod.formatDateSDF(new Date()));

            loadTempNextCourse();

            CardView cardView_nextClass = view.findViewById(R.id.cardView_local_next_course);
            cardView_nextClass.setOnClickListener(v -> {
                if (context != null) {
                    setNextCourse();
                    context.sendBroadcast(new Intent(NextClassWidget.ACTION_ON_CLICK));
                }
            });

            view.findViewById(R.id.button_info_select).setOnClickListener(v -> setShowInfo());
        }
    }

    private void swipeAndGetInfo() {
        if (context != null && swipeRefreshLayout != null) {
            if (NetMethod.isNetworkConnected(context)) {
                swipeRefreshLayout.post(() -> swipeRefreshLayout.setRefreshing(true));
                getData();
            } else {
                Toast.makeText(context, R.string.network_error, Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.post(() -> swipeRefreshLayout.setRefreshing(false));
            }
        }
    }

    private void setShowInfo() {
        if (context != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.info_channel_select);
            infoSelectList = DataMethod.InfoData.getInfoChannel(context);
            builder.setMultiChoiceItems(infoTypeList, infoSelectList, (dialog, which, isChecked) -> infoSelectList[which] = isChecked);
            builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
                boolean allFalse = true;
                for (boolean checked : infoSelectList) {
                    if (checked) {
                        allFalse = false;
                        break;
                    }
                }
                if (context != null && isAdded()) {
                    if (allFalse) {
                        Toast.makeText(context, R.string.info_channel_select_warn, Toast.LENGTH_SHORT).show();
                    } else {
                        DataMethod.InfoData.setInfoChannel(context, infoSelectList);
                        swipeAndGetInfo();
                    }
                }
            });
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.show();
        }
    }

    //优先加载缓存中的下一节课
    private void loadTempNextCourse() {
        if (context != null) {
            NextCourse nextCourse = (NextCourse) DataMethod.getOfflineData(context, NextCourse.class, NextClassMethod.NEXT_COURSE_FILE_NAME, NextClassMethod.IS_ENCRYPT);
            if (nextCourse != null) {
                setNextCourse(nextCourse);
            }
        }
    }

    //内部刷新设置下一节课
    private void setNextCourse() {
        if (context != null) {
            NextCourse nextCourse = NextClassMethod.getNextClassArray(Objects.requireNonNull(getActivity()));
            DataMethod.saveOfflineData(context, nextCourse, NextClassMethod.NEXT_COURSE_FILE_NAME, false, NextClassMethod.IS_ENCRYPT);
            setNextCourse(nextCourse);
            System.gc();
        }
    }

    /**
     * 设置下一节课
     * 主要用于外部调用更新UI
     *
     * @param nextCourse NextCourse
     */
    void setNextCourse(NextCourse nextCourse) {
        if (isAdded() && context != null && view != null) {
            TextView textView_noNextClass = view.findViewById(R.id.textView_noNextClass);
            LinearLayout linearLayout_nextClass = view.findViewById(R.id.layout_nextClass);
            if (nextCourse.getCourseTime() == null) {
                if (nextCourse.isInVacation()) {
                    textView_noNextClass.setText(R.string.in_vacation);
                } else {
                    textView_noNextClass.setText(R.string.no_course);
                }
                linearLayout_nextClass.setVisibility(View.GONE);
                textView_noNextClass.setVisibility(View.VISIBLE);

                DataMethod.deleteOfflineData(context, NextClassMethod.NEXT_COURSE_FILE_NAME, NextClassMethod.IS_ENCRYPT);
            } else {
                String time = nextCourse.getCourseTime().replace("~", "\n~\n").trim();

                TextView textView_nextClass = view.findViewById(R.id.textView_nextClass);
                TextView textView_nextLocation = view.findViewById(R.id.textView_nextLocation);
                TextView textView_nextTeacher = view.findViewById(R.id.textView_nextTeacher);
                TextView textView_nextTime = view.findViewById(R.id.textView_nextTime);

                textView_nextClass.setText(nextCourse.getCourseName());
                textView_nextLocation.setText(nextCourse.getCourseLocation());
                textView_nextTeacher.setText(nextCourse.getCourseTeacher());
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

    public void InfoSet(@Nullable ArrayList<TopicInfo> topicInfoArrayList) {
        if (isAdded() && recyclerView != null) {
            if (topicInfoArrayList != null && getActivity() != null) {
                if (infoAdapter == null) {
                    infoAdapter = new InfoAdapter(getActivity(), topicInfoArrayList);
                    recyclerView.setAdapter(infoAdapter);
                } else {
                    infoAdapter.updateTopic(topicInfoArrayList);
                }
            }
        }
    }

    public void lastViewSet(Context context) {
        if (isAdded()) {
            //离线数据加载完成，开始拉取网络数据
            if (loadTime == 1 && NetMethod.isNetworkConnected(context) && BaseMethod.isDataAutoUpdate(context)) {
                getData();
            } else {
                BaseMethod.setRefreshing(swipeRefreshLayout, false);
            }
        }
    }

    //还原下拉的列表位置
    private void getPositionAndOffset() {
        if (recyclerView != null) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            View topView = null;
            if (layoutManager != null) {
                topView = layoutManager.getChildAt(0);
            }
            if (topView != null) {
                lastOffset = topView.getTop();
                lastPosition = layoutManager.getPosition(topView);
            }
        }
    }

    private void scrollToPosition() {
        if (recyclerView != null) {
            if (recyclerView.getLayoutManager() != null && lastPosition >= 0) {
                ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(lastPosition, lastOffset);
            }
        }
    }

    synchronized private void getData() {
        BaseMethod.setRefreshing(swipeRefreshLayout, true);
        if (context != null) {
            if (loadTime == 0) {
                new InfoAsync().execute(context.getApplicationContext());
            } else {
                new InfoAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, context.getApplicationContext());
            }
        }
    }

}
