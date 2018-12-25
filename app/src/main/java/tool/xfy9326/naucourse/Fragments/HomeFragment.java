package tool.xfy9326.naucourse.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseArray;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import tool.xfy9326.naucourse.AsyncTasks.InfoAsync;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.Methods.DataMethod;
import tool.xfy9326.naucourse.Methods.NetMethod;
import tool.xfy9326.naucourse.Methods.NextClassMethod;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Tools.RSSReader;
import tool.xfy9326.naucourse.Utils.AlstuTopic;
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
    private boolean[] infoSelectList;

    public HomeFragment() {
        this.view = null;
        this.context = null;
        this.recyclerView = null;
        this.swipeRefreshLayout = null;
        this.infoAdapter = null;
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
            view.findViewById(R.id.cardView_info_title).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (recyclerView != null && isAdded()) {
                        recyclerView.smoothScrollToPosition(0);
                    }
                }
            });

            if (swipeRefreshLayout == null) {
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
            }

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
                    if (context != null) {
                        setNextCourse();
                        context.sendBroadcast(new Intent(NextClassWidget.ACTION_ON_CLICK));
                    }
                }
            });

            view.findViewById(R.id.button_info_select).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setShowInfo();
                }
            });
        }
    }

    private void swipeAndGetInfo() {
        if (context != null && swipeRefreshLayout != null) {
            if (NetMethod.isNetworkConnected(context)) {
                swipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(true);
                    }
                });
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
    }

    private void setShowInfo() {
        if (context != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.info_channel_select);
            String[] infoList = new String[]{
                    context.getString(R.string.jw_system_info),
                    context.getString(R.string.jwc),
                    context.getString(R.string.xw),
                    context.getString(R.string.tw),
                    context.getString(R.string.xxb),
                    context.getString(R.string.alstu_system)
            };
            infoSelectList = DataMethod.InfoData.getInfoChannel(context);
            builder.setMultiChoiceItems(infoList, infoSelectList, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    infoSelectList[which] = isChecked;
                }
            });
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
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
                }
            });
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.show();
        }
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
        DataMethod.saveOfflineData(Objects.requireNonNull(context), nextCourse, NEXT_COURSE_FILE_NAME, false);
        setNextCourse(nextCourse.getCourseName(), nextCourse.getCourseLocation(), nextCourse.getCourseTeacher(), nextCourse.getCourseTime());
        System.gc();
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
    void setNextCourse(@Nullable String name, String location, String teacher, String time) {
        if (isAdded() && view != null) {
            TextView textView_noNextClass = view.findViewById(R.id.textView_noNextClass);
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

    public void InfoSet(@Nullable JwcTopic jwcTopic, @Nullable AlstuTopic alstuTopic, @Nullable SparseArray<RSSReader.RSSObject> rssObjects) {
        if (isAdded() && recyclerView != null) {
            if (!(jwcTopic == null && rssObjects == null)) {
                if (infoAdapter == null) {
                    infoAdapter = new InfoAdapter(getActivity(), jwcTopic, alstuTopic, rssObjects);
                    recyclerView.setAdapter(infoAdapter);
                } else {
                    infoAdapter.updateJwcTopic(jwcTopic, alstuTopic, rssObjects);
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
