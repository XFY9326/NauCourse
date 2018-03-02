package tool.xfy9326.naucourse.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.Methods.JwInfoMethod;
import tool.xfy9326.naucourse.Methods.JwcInfoMethod;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Utils.JwTopic;
import tool.xfy9326.naucourse.Utils.JwcTopic;
import tool.xfy9326.naucourse.Views.InfoAdapter;

/**
 * Created by xfy9326 on 18-2-20.
 */

public class HomeFragment extends Fragment {
    private View view;
    private Context context;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ViewSet();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        return view;
    }

    private void ViewSet() {
        recyclerView = view.findViewById(R.id.recyclerView_information);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        //保证从其他视图返回时列表位置不变
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
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

        TextView textView_dateNow = view.findViewById(R.id.textView_dateNow);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        textView_dateNow.setText(simpleDateFormat.format(new Date()));
    }

    public void setNextCourse(String name, String location, String teacher, String time) {
        TextView textView_nextClass = view.findViewById(R.id.textView_nextClass);
        TextView textView_nextLocation = view.findViewById(R.id.textView_nextLocation);
        TextView textView_nextTeacher = view.findViewById(R.id.textView_nextTeacher);
        TextView textView_nextTime = view.findViewById(R.id.textView_nextTime);

        textView_nextClass.setText(name);
        textView_nextLocation.setText(location);
        textView_nextTeacher.setText(teacher);
        textView_nextTime.setText(time);

        TextView textView_noNextClass = view.findViewById(R.id.textView_noNextClass);
        textView_noNextClass.setVisibility(View.GONE);
        LinearLayout linearLayout_nextClass = view.findViewById(R.id.layout_nextClass);
        linearLayout_nextClass.setVisibility(View.VISIBLE);
    }

    private void InfoSet(JwcTopic jwcTopic, JwTopic jwTopic, Context context) {
        if (context != null && jwcTopic != null && jwTopic != null) {
            if (infoAdapter == null) {
                infoAdapter = new InfoAdapter(context, jwcTopic, jwTopic);
                recyclerView.setAdapter(infoAdapter);
            } else {
                infoAdapter.updateJwcTopic(jwcTopic, jwTopic);
            }
        }
    }

    private void getPositionAndOffset() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        View topView = layoutManager.getChildAt(0);
        if (topView != null) {
            lastOffset = topView.getTop();
            lastPosition = layoutManager.getPosition(topView);
        }
    }

    private void scrollToPosition() {
        if (recyclerView.getLayoutManager() != null && lastPosition >= 0) {
            ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(lastPosition, lastOffset);
        }
    }

    private void getData() {
        new InfoAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, context);
    }

    @SuppressLint("StaticFieldLeak")
    class InfoAsync extends AsyncTask<Context, Void, Context> {
        int JwcLoadSuccess = -1;
        int JwLoadSuccess = -1;
        int loadCode = Config.NET_WORK_GET_SUCCESS;
        private JwcTopic jwcTopic;
        private JwTopic jwTopic;

        InfoAsync() {
            jwcTopic = null;
            jwTopic = null;
        }

        @Override
        protected Context doInBackground(final Context... context) {
            try {
                if (loadTime == 0) {
                    //首次只加载离线数据
                    jwcTopic = (JwcTopic) BaseMethod.getOfflineData(context[0], JwcTopic.class, JwcInfoMethod.FILE_NAME);
                    jwTopic = (JwTopic) BaseMethod.getOfflineData(context[0], JwTopic.class, JwInfoMethod.FILE_NAME);
                    JwcLoadSuccess = Config.NET_WORK_GET_SUCCESS;
                    JwLoadSuccess = Config.NET_WORK_GET_SUCCESS;
                    loadTime++;
                } else {
                    JwcInfoMethod jwcInfoMethod = new JwcInfoMethod(context[0]);
                    JwcLoadSuccess = jwcInfoMethod.load();
                    if (JwcLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                        jwcTopic = jwcInfoMethod.getJwcTopic();
                    }

                    JwInfoMethod jwInfoMethod = new JwInfoMethod(context[0]);
                    JwLoadSuccess = jwInfoMethod.load();
                    if (JwLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                        jwTopic = jwInfoMethod.getJwTopic();
                    }

                    if (JwcLoadSuccess == Config.NET_WORK_GET_SUCCESS && JwLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                        loadTime++;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                loadCode = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
            }
            return context[0];
        }

        @Override
        protected void onPostExecute(Context context) {
            if (BaseMethod.checkNetWorkCode(context, new int[]{JwLoadSuccess, JwcLoadSuccess}, loadCode)) {
                InfoSet(jwcTopic, jwTopic, context);
            }
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
            super.onPostExecute(context);
        }
    }

}
