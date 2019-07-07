package tool.xfy9326.naucourse.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.fragments.HomeFragment;
import tool.xfy9326.naucourse.methods.BaseMethod;
import tool.xfy9326.naucourse.methods.DataMethod;
import tool.xfy9326.naucourse.methods.InfoMethod;
import tool.xfy9326.naucourse.methods.NetMethod;
import tool.xfy9326.naucourse.methods.netInfoMethods.AlstuMethod;
import tool.xfy9326.naucourse.methods.netInfoMethods.JwcInfoMethod;
import tool.xfy9326.naucourse.methods.netInfoMethods.RSSInfoMethod;
import tool.xfy9326.naucourse.tools.RSSReader;
import tool.xfy9326.naucourse.utils.AlstuTopic;
import tool.xfy9326.naucourse.utils.JwcTopic;
import tool.xfy9326.naucourse.utils.TopicInfo;
import tool.xfy9326.naucourse.views.MainViewPagerAdapter;

/**
 * Created by 10696 on 2018/3/2.
 */

public class InfoAsync extends AsyncTask<Context, Void, Context> {
    private static final int INFO_GET_MAX_TIME = 2;
    private int JwcLoadSuccess = -1;
    private int rssLoadSuccess = -1;
    private int alstuLoadSuccess = -1;
    private int loadCode = Config.NET_WORK_GET_SUCCESS;
    @Nullable
    private JwcTopic jwcTopic;
    @Nullable
    private AlstuTopic alstuTopic;
    @Nullable
    private SparseArray<RSSReader.RSSObject> rssObjects;
    private ArrayList<TopicInfo> topicInfoList;

    public InfoAsync() {
        this.jwcTopic = null;
        this.rssObjects = null;
        this.alstuTopic = null;
        this.topicInfoList = null;
    }

    private static boolean checkInfoData(JwcTopic jwcTopic, AlstuTopic alstuTopic, SparseArray<RSSReader.RSSObject> rssObjects) {
        return jwcTopic != null && alstuTopic != null && rssObjects != null && rssObjects.size() != 0;
    }

    @Override
    protected Context doInBackground(final Context... context) {
        int loadTime = 0;
        MainViewPagerAdapter viewPagerAdapter = BaseMethod.getApp(context[0]).getViewPagerAdapter();
        if (viewPagerAdapter != null) {
            HomeFragment homeFragment = viewPagerAdapter.getHomeFragment();
            if (homeFragment != null) {
                loadTime = homeFragment.getLoadTime();
            }
            if (loadTime == 0) {
                //首次只加载离线数据
                topicInfoList = DataMethod.getOfflineTopicInfo(context[0]);

                JwcLoadSuccess = Config.NET_WORK_GET_SUCCESS;
                alstuLoadSuccess = Config.NET_WORK_GET_SUCCESS;
                rssLoadSuccess = Config.NET_WORK_GET_SUCCESS;
            } else {
                int restartTime = 0;
                while (restartTime++ < INFO_GET_MAX_TIME) {
                    getInfoData(context[0]);
                    if (loadCode == Config.NET_WORK_GET_SUCCESS) {
                        if (!checkInfoData(jwcTopic, alstuTopic, rssObjects)) {
                            break;
                        }
                    } else {
                        break;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (loadCode == Config.NET_WORK_GET_SUCCESS) {
                    topicInfoList = InfoMethod.combineData(context[0], jwcTopic, alstuTopic, rssObjects);
                    if (!DataMethod.saveOfflineData(context[0], topicInfoList, InfoMethod.FILE_NAME, true, InfoMethod.IS_ENCRYPT)) {
                        topicInfoList = null;
                    }
                }
            }

            if (homeFragment != null) {
                homeFragment.setLoadTime(++loadTime);
            }
            if (loadTime > 2) {
                NetMethod.showConnectErrorOnce = false;
            }
        }
        return context[0];
    }

    private void getInfoData(Context context) {
        final boolean[] infoChannel = DataMethod.InfoData.getInfoChannel(context);
        final boolean showJwcTopic = infoChannel[JwcInfoMethod.TYPE_JWC];
        final boolean showAlstuTopic = infoChannel[AlstuMethod.TYPE_ALSTU];
        final ExecutorService executorService = BaseMethod.getApp(context).getExecutorService();

        Future[] futures = new Future[3];
        futures[0] = executorService.submit(() -> {
            try {
                if (showJwcTopic) {
                    JwcInfoMethod jwcInfoMethod = new JwcInfoMethod(context);
                    JwcLoadSuccess = jwcInfoMethod.load();
                    if (JwcLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                        jwcTopic = jwcInfoMethod.getData(false);
                    }
                } else {
                    jwcTopic = new JwcTopic();
                    JwcLoadSuccess = Config.NET_WORK_GET_SUCCESS;
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (e instanceof SocketTimeoutException) {
                    JwcLoadSuccess = Config.NET_WORK_ERROR_CODE_TIME_OUT;
                } else {
                    JwcLoadSuccess = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
                }
                loadCode = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
            }
        });
        futures[1] = executorService.submit(() -> {
            try {
                if (showAlstuTopic) {
                    AlstuMethod alstuMethod = new AlstuMethod(context);
                    alstuLoadSuccess = alstuMethod.load();
                    if (alstuLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                        alstuTopic = alstuMethod.getData(false);
                    }
                } else {
                    alstuTopic = new AlstuTopic();
                    alstuLoadSuccess = Config.NET_WORK_GET_SUCCESS;
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (e instanceof SocketTimeoutException) {
                    alstuLoadSuccess = Config.NET_WORK_ERROR_CODE_TIME_OUT;
                } else {
                    alstuLoadSuccess = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
                }
                loadCode = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
            }
        });
        futures[2] = executorService.submit(() -> {
            try {
                RSSInfoMethod rssInfoMethod = new RSSInfoMethod(context, executorService);
                rssLoadSuccess = rssInfoMethod.load();
                if (rssLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                    rssObjects = rssInfoMethod.getRSSObject();
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (e instanceof SocketTimeoutException) {
                    rssLoadSuccess = Config.NET_WORK_ERROR_CODE_TIME_OUT;
                } else {
                    rssLoadSuccess = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
                }
                loadCode = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
            }
        });

        for (Future future : futures) {
            try {
                future.get(Config.TASK_RUN_MAX_SECOND, TimeUnit.SECONDS);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPostExecute(@NonNull Context context) {
        MainViewPagerAdapter viewPagerAdapter = BaseMethod.getApp(context).getViewPagerAdapter();
        if (viewPagerAdapter != null) {
            HomeFragment homeFragment = viewPagerAdapter.getHomeFragment();
            if (homeFragment != null) {
                if (NetMethod.checkNetWorkCode(context, new int[]{rssLoadSuccess, alstuLoadSuccess, JwcLoadSuccess}, loadCode, true)) {
                    homeFragment.InfoSet(topicInfoList);
                }
                homeFragment.lastViewSet(context);
            }
        }
        System.gc();
        super.onPostExecute(context);
    }
}
