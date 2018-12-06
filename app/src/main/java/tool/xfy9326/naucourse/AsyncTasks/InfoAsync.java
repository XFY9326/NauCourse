package tool.xfy9326.naucourse.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Fragments.HomeFragment;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.Methods.DataMethod;
import tool.xfy9326.naucourse.Methods.InfoMethods.AlstuMethod;
import tool.xfy9326.naucourse.Methods.InfoMethods.JwcInfoMethod;
import tool.xfy9326.naucourse.Methods.InfoMethods.RSSInfoMethod;
import tool.xfy9326.naucourse.Methods.NetMethod;
import tool.xfy9326.naucourse.Tools.RSSReader;
import tool.xfy9326.naucourse.Utils.AlstuTopic;
import tool.xfy9326.naucourse.Utils.JwcTopic;
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

    public InfoAsync() {
        this.jwcTopic = null;
        this.rssObjects = null;
        this.alstuTopic = null;
    }

    @Override
    protected Context doInBackground(final Context... context) {
        int loadTime = 0;
        ViewPagerAdapter viewPagerAdapter = BaseMethod.getApp(context[0]).getViewPagerAdapter();
        if (viewPagerAdapter != null) {
            HomeFragment homeFragment = viewPagerAdapter.getHomeFragment();
            try {
                if (homeFragment != null) {
                    loadTime = homeFragment.getLoadTime();
                }
                boolean[] infoChannel = DataMethod.InfoData.getInfoChannel(context[0]);
                boolean showJwcTopic = infoChannel[JwcInfoMethod.TYPE_JWC];
                boolean showAlstuTopic = infoChannel[AlstuMethod.TYPE_ALSTU];

                if (loadTime == 0) {
                    //首次只加载离线数据
                    if (showJwcTopic) {
                        jwcTopic = (JwcTopic) DataMethod.getOfflineData(context[0], JwcTopic.class, JwcInfoMethod.FILE_NAME);
                    } else {
                        jwcTopic = new JwcTopic();
                    }

                    if (showJwcTopic) {
                        alstuTopic = (AlstuTopic) DataMethod.getOfflineData(context[0], AlstuTopic.class, AlstuMethod.FILE_NAME);
                    } else {
                        alstuTopic = new AlstuTopic();
                    }

                    rssObjects = RSSInfoMethod.getOfflineRSSObject(context[0]);

                    JwcLoadSuccess = Config.NET_WORK_GET_SUCCESS;
                    alstuLoadSuccess = Config.NET_WORK_GET_SUCCESS;
                    rssLoadSuccess = Config.NET_WORK_GET_SUCCESS;
                    loadTime++;
                    if (homeFragment != null) {
                        homeFragment.setLoadTime(loadTime);
                    }
                } else {
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

                    RSSInfoMethod rssInfoMethod = new RSSInfoMethod(context[0]);
                    rssLoadSuccess = rssInfoMethod.load();
                    if (rssLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                        rssObjects = rssInfoMethod.getRSSObject();
                    }

                    loadTime++;
                    homeFragment.setLoadTime(loadTime);
                }
            } catch (Exception e) {
                e.printStackTrace();
                loadCode = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
                if (homeFragment != null) {
                    loadTime++;
                    homeFragment.setLoadTime(loadTime);
                }
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
                if (NetMethod.checkNetWorkCode(context, new int[]{rssLoadSuccess, alstuLoadSuccess, JwcLoadSuccess}, loadCode)) {
                    homeFragment.InfoSet(jwcTopic, alstuTopic, rssObjects);
                }
                homeFragment.lastViewSet(context);
            }
        }
        System.gc();
        super.onPostExecute(context);
    }
}
