package tool.xfy9326.naucourse.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Fragments.HomeFragment;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.Methods.DataMethod;
import tool.xfy9326.naucourse.Methods.InfoMethod;
import tool.xfy9326.naucourse.Methods.NetInfoMethods.AlstuMethod;
import tool.xfy9326.naucourse.Methods.NetInfoMethods.JwcInfoMethod;
import tool.xfy9326.naucourse.Methods.NetInfoMethods.RSSInfoMethod;
import tool.xfy9326.naucourse.Methods.NetMethod;
import tool.xfy9326.naucourse.Tools.RSSReader;
import tool.xfy9326.naucourse.Utils.AlstuTopic;
import tool.xfy9326.naucourse.Utils.JwcTopic;
import tool.xfy9326.naucourse.Utils.TopicInfo;
import tool.xfy9326.naucourse.Views.ViewPagerAdapter;

/**
 * Created by 10696 on 2018/3/2.
 */

public class InfoAsync extends AsyncTask<Context, Void, Context> {
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

    @Override
    protected Context doInBackground(final Context... context) {
        int loadTime = 0;
        ViewPagerAdapter viewPagerAdapter = BaseMethod.getApp(context[0]).getViewPagerAdapter();
        if (viewPagerAdapter != null) {
            HomeFragment homeFragment = viewPagerAdapter.getHomeFragment();
            if (homeFragment != null) {
                loadTime = homeFragment.getLoadTime();
            }
            final boolean[] infoChannel = DataMethod.InfoData.getInfoChannel(context[0]);
            final boolean showJwcTopic = infoChannel[JwcInfoMethod.TYPE_JWC];
            final boolean showAlstuTopic = infoChannel[AlstuMethod.TYPE_ALSTU];

            final ExecutorService executorService = BaseMethod.getApp(context[0]).getExecutorService();
            if (loadTime == 0) {
                //首次只加载离线数据
                topicInfoList = DataMethod.getOfflineTopicInfo(context[0]);

                JwcLoadSuccess = Config.NET_WORK_GET_SUCCESS;
                alstuLoadSuccess = Config.NET_WORK_GET_SUCCESS;
                rssLoadSuccess = Config.NET_WORK_GET_SUCCESS;
            } else {
                Future[] futures = new Future[3];
                futures[0] = executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (showJwcTopic) {
                                JwcInfoMethod jwcInfoMethod = new JwcInfoMethod(context[0]);
                                JwcLoadSuccess = jwcInfoMethod.load();
                                if (JwcLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                                    jwcTopic = jwcInfoMethod.getJwcTopic();
                                }
                            } else {
                                jwcTopic = new JwcTopic();
                                JwcLoadSuccess = Config.NET_WORK_GET_SUCCESS;
                            }
                        } catch (Exception e) {
                            JwcLoadSuccess = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
                            e.printStackTrace();
                        }
                    }
                });
                futures[1] = executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (showAlstuTopic) {
                                AlstuMethod alstuMethod = new AlstuMethod(context[0]);
                                alstuLoadSuccess = alstuMethod.load();
                                if (alstuLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                                    alstuTopic = alstuMethod.getAlstuTopic();
                                }
                            } else {
                                alstuTopic = new AlstuTopic();
                                alstuLoadSuccess = Config.NET_WORK_GET_SUCCESS;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            alstuLoadSuccess = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
                        }
                    }
                });
                futures[2] = executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            RSSInfoMethod rssInfoMethod = new RSSInfoMethod(context[0], executorService);
                            rssLoadSuccess = rssInfoMethod.load();
                            if (rssLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                                rssObjects = rssInfoMethod.getRSSObject();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            rssLoadSuccess = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
                        }
                    }
                });

                for (Future future : futures) {
                    try {
                        future.get(Config.TASK_RUN_MAX_SECOND, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (loadCode != Config.NET_WORK_ERROR_CODE_CONNECT_ERROR) {
                    topicInfoList = InfoMethod.combineData(context[0], jwcTopic, alstuTopic, rssObjects);
                    if (!DataMethod.saveOfflineData(context[0], topicInfoList, InfoMethod.FILE_NAME, true, InfoMethod.IS_ENCRYPT)) {
                        topicInfoList = null;
                    }
                }
            }

            if (homeFragment != null) {
                homeFragment.setLoadTime(++loadTime);
            }
            if ((rssLoadSuccess == -1 || rssLoadSuccess == Config.NET_WORK_ERROR_CODE_CONNECT_ERROR) &&
                    (alstuLoadSuccess == -1 || alstuLoadSuccess == Config.NET_WORK_ERROR_CODE_CONNECT_ERROR) &&
                    (JwcLoadSuccess == -1 || JwcLoadSuccess == Config.NET_WORK_ERROR_CODE_CONNECT_ERROR) &&
                    (rssLoadSuccess != -1 && alstuLoadSuccess != -1 && JwcLoadSuccess != -1)) {
                loadCode = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
            }
            if (loadTime > 2) {
                BaseMethod.getApp(context[0]).setShowConnectErrorOnce(false);
            }
        }
        return context[0];
    }

    @Override
    protected void onPostExecute(@NonNull Context context) {
        ViewPagerAdapter viewPagerAdapter = BaseMethod.getApp(context).getViewPagerAdapter();
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
