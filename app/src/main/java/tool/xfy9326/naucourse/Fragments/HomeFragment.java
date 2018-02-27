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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.Methods.InfoMethod;
import tool.xfy9326.naucourse.R;
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

        if (BaseMethod.isDataAutoUpdate(context)) {
            new InfoAsync().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, context);
        }

        swipeRefreshLayout = view.findViewById(R.id.swipeLayout_home);
        swipeRefreshLayout.setDistanceToTriggerSync(200);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (BaseMethod.isNetworkConnected(context)) {
                    new InfoAsync().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, context);
                } else {
                    Toast.makeText(context, R.string.network_error, Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });

        TextView textView_dateNow = view.findViewById(R.id.textView_dateNow);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        textView_dateNow.setText(simpleDateFormat.format(new Date()));
    }

    public void setNextCourse(String name, String location, String teacher) {
        TextView textView_nextClass = view.findViewById(R.id.textView_nextClass);
        TextView textView_nextLocation = view.findViewById(R.id.textView_nextLocation);
        TextView textView_nextTeacher = view.findViewById(R.id.textView_nextTeacher);

        textView_nextClass.setText(name);
        textView_nextLocation.setText(location);
        textView_nextTeacher.setText(teacher);

        TextView textView_noNextClass = view.findViewById(R.id.textView_noNextClass);
        textView_noNextClass.setVisibility(View.GONE);
        LinearLayout linearLayout_nextClass = view.findViewById(R.id.layout_nextClass);
        linearLayout_nextClass.setVisibility(View.VISIBLE);
    }

    private void InfoSet(JwcTopic jwcTopic, Context context) {
        if (jwcTopic != null) {
            if (infoAdapter == null) {
                infoAdapter = new InfoAdapter(context, jwcTopic);
                recyclerView.setAdapter(infoAdapter);
            } else {
                infoAdapter.updateJwcTopic(jwcTopic);
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

    @SuppressLint("StaticFieldLeak")
    class InfoAsync extends AsyncTask<Context, Void, Context> {
        boolean loadSuccess = false;
        private JwcTopic jwcTopic;

        InfoAsync() {
            jwcTopic = null;
        }

        @Override
        protected Context doInBackground(Context... context) {
            if (context[0] != null) {
                if (loadTime == 0) {
                    jwcTopic = (JwcTopic) BaseMethod.getOfflineData(context[0], JwcTopic.class, InfoMethod.FILE_NAME);
                    loadSuccess = true;
                    loadTime++;
                    return context[0];
                } else {
                    InfoMethod infoMethod = new InfoMethod(context[0]);
                    loadSuccess = infoMethod.load();
                    if (loadSuccess) {
                        jwcTopic = infoMethod.getJwcTopic();
                        loadTime++;
                        return context[0];
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Context context) {
            if (loadSuccess) {
                InfoSet(jwcTopic, context);
                if (swipeRefreshLayout != null) {
                    if (swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
                if (loadTime == 1 && BaseMethod.isNetworkConnected(context)) {
                    new InfoAsync().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, context);
                }
            }
            super.onPostExecute(context);
        }
    }

}
