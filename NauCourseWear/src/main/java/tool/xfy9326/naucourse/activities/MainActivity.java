package tool.xfy9326.naucourse.activities;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.wear.widget.drawer.WearableActionDrawerView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import tool.xfy9326.naucourse.BuildConfig;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.beans.course.TodayCourses;
import tool.xfy9326.naucourse.methods.CourseListUpdate;
import tool.xfy9326.naucourse.methods.DeviceSupport;
import tool.xfy9326.naucourse.methods.DialogBuilder;
import tool.xfy9326.naucourse.methods.StorageCache;
import tool.xfy9326.naucourse.views.AdvancedRecyclerView;
import tool.xfy9326.naucourse.views.TodayCourseAdapter;

public class MainActivity extends WearableActivity implements DataApi.DataListener {
    private static final int REFRESH_TIME_OUT = 5000;
    private GoogleApiClient googleApiClient;
    private SwipeRefreshLayout refreshLayout;
    private TodayCourseAdapter todayCourseAdapter;
    private boolean waitingForAnswer = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();
        initGoogleApi();

        ViewSet();
    }

    private void initGoogleApi() {
        //虽然 Google Play 服务现在包含 Wear 应用的新 API，但中国版 Wear OS 应用应继续使用与 GoogleApiClient 相关的 API
        googleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Wearable.DataApi.addListener(googleApiClient, MainActivity.this);

                        getTodayCourseFromPhone();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                }).build();
        googleApiClient.connect();
    }

    private void ViewSet() {
        AdvancedRecyclerView recyclerView = findViewById(R.id.recyclerView_course_list);
        refreshLayout = findViewById(R.id.swipeLayout_course_list);
        final WearableActionDrawerView actionDrawer = findViewById(R.id.actionDrawer_expand_course_list);
        todayCourseAdapter = new TodayCourseAdapter(this, recyclerView);

        recyclerView.setFocusableInTouchMode(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setEmptyView(findViewById(R.id.textView_empty_course_list));
        recyclerView.setAdapter(todayCourseAdapter);
        recyclerView.setOnBottomCallback(new AdvancedRecyclerView.OnBottomCallback() {
            @Override
            public void onBottom() {
                if (actionDrawer.isClosed() && !actionDrawer.isPeeking()) {
                    actionDrawer.getController().peekDrawer();
                }
            }

            @Override
            public void onNotBottom() {
                if (actionDrawer.isPeeking() || actionDrawer.isOpened()) {
                    actionDrawer.getController().closeDrawer();
                }
            }
        });

        refreshLayout.setDistanceToTriggerSync(80);
        refreshLayout.setProgressBackgroundColorSchemeResource(R.color.dark_blue);
        refreshLayout.setColorSchemeColors(Color.WHITE);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getTodayCourseFromPhone();
            }
        });

        resetMenuIcon();
        actionDrawer.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.menu_expand_course_list) {
                    if (todayCourseAdapter.isExpanded()) {
                        todayCourseAdapter.setExpand(false);
                        actionDrawer.getMenu().getItem(0).setIcon(R.drawable.ic_more);
                    } else {
                        todayCourseAdapter.setExpand(true);
                        actionDrawer.getMenu().getItem(0).setIcon(R.drawable.ic_less);
                    }
                    actionDrawer.getController().closeDrawer();
                }
                return false;
            }
        });

        TodayCourses cache = StorageCache.readTodayCoursesCache(this);
        if (cache != null) {
            setCourseListEmptyView(cache);
            todayCourseAdapter.updateTodayCourses(cache);
        }
    }

    private void resetMenuIcon() {
        if (todayCourseAdapter != null) {
            WearableActionDrawerView actionDrawer = findViewById(R.id.actionDrawer_expand_course_list);
            if (todayCourseAdapter.isExpanded()) {
                actionDrawer.getMenu().getItem(0).setIcon(R.drawable.ic_less);
            } else {
                actionDrawer.getMenu().getItem(0).setIcon(R.drawable.ic_more);
            }
        }
    }

    private synchronized void stopRefreshing() {
        if (refreshLayout != null && refreshLayout.isRefreshing()) {
            refreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    refreshLayout.setRefreshing(false);
                }
            });
        }
    }

    private void setCourseListEmptyView(TodayCourses todayCourses) {
        AppCompatTextView emptyView = findViewById(R.id.textView_empty_course_list);
        if (todayCourses.getCourses().length == 0) {
            emptyView.setText(R.string.no_course_today);
        } else {
            emptyView.setText(R.string.no_course_data);
        }
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        for (DataEvent event : dataEventBuffer) {
            Uri uri = event.getDataItem().getUri();
            String path = uri != null ? uri.getPath() : null;

            if (Config.WEAR_TODAY_COURSE_LIST_PATH.equals(path)) {
                waitingForAnswer = false;
                DataMap map = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                if (map.getInt(Config.WEAR_MSG_SUPPORT_APP_VERSION_CODE, 0) >= BuildConfig.SUPPORT_MIN_VERSION_CODE &&
                        map.getInt(Config.WEAR_MSG_SUPPORT_APP_SUB_VERSION, 0) >= BuildConfig.SUPPORT_MIN_SUB_VERSION) {
                    if (map.getBoolean(Config.WEAR_MSG_NO_COURSE_DATA, false)) {
                        Toast.makeText(this, R.string.get_no_course_data, Toast.LENGTH_SHORT).show();
                    } else {
                        byte[] data = map.getByteArray(Config.WEAR_MSG_TODAY_COURSE_LIST);
                        if (data != null) {
                            TodayCourses todayCourses = CourseListUpdate.readTodayCourseFromBytes(data);
                            if (todayCourses != null) {
                                todayCourseAdapter.updateTodayCourses(todayCourses);
                                resetMenuIcon();
                                setCourseListEmptyView(todayCourses);
                                StorageCache.saveTodayCoursesCache(this, todayCourses);
                            } else {
                                Toast.makeText(this, R.string.data_sync_error, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, R.string.data_sync_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    DialogBuilder.buildSupportVersionCodeDialog(this).show();
                }

                stopRefreshing();
            }
        }
    }

    private void beginWaitingForAnswer() {
        waitingForAnswer = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(REFRESH_TIME_OUT);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (waitingForAnswer) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, R.string.data_get_error, Toast.LENGTH_SHORT).show();
                            stopRefreshing();
                        }
                    });
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
        super.onDestroy();
    }

    private void requestNewCourseData() {
        CourseListUpdate.requestNewCourseData(googleApiClient, new CourseListUpdate.onRequestCourseListUpdateListener() {
            @Override
            public void onResult(@Nullable String nodeId, boolean isSuccess) {
                if (!isSuccess) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, R.string.phone_connect_error, Toast.LENGTH_SHORT).show();
                            stopRefreshing();
                        }
                    });
                } else {
                    beginWaitingForAnswer();
                }
            }
        });
    }

    private void getTodayCourseFromPhone() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            refreshLayout.setRefreshing(true);
            DeviceSupport.checkDeviceSupport(MainActivity.this, googleApiClient, new DeviceSupport.onCheckAppSupportListener() {
                @Override
                public void onChecked(boolean hasConnectedDevice, boolean isSupportSystem, boolean hasSupportApp, final String nodeId) {
                    boolean needCancelRefresh = true;
                    if (hasConnectedDevice) {
                        if (isSupportSystem) {
                            if (!hasSupportApp) {
                                DialogBuilder.showBuilderInMain(MainActivity.this, DialogBuilder.buildNeedInstallSupportAppDialog(MainActivity.this, nodeId));
                            } else {
                                needCancelRefresh = false;
                                requestNewCourseData();
                            }
                        } else {
                            DialogBuilder.showBuilderInMain(MainActivity.this, DialogBuilder.buildOnlySupportAndroidDialog(MainActivity.this));
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, R.string.phone_connect_error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    if (needCancelRefresh) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                stopRefreshing();
                            }
                        });
                    }
                }
            });
        } else {
            initGoogleApi();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, R.string.google_client_connect_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}
