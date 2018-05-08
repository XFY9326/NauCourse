package tool.xfy9326.naucourse.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Fragments.HomeFragment;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.Methods.DataMethod;
import tool.xfy9326.naucourse.Methods.JwInfoMethod;
import tool.xfy9326.naucourse.Methods.JwcInfoMethod;
import tool.xfy9326.naucourse.Methods.NetMethod;
import tool.xfy9326.naucourse.Utils.JwTopic;
import tool.xfy9326.naucourse.Utils.JwcTopic;

/**
 * Created by 10696 on 2018/3/2.
 */

public class InfoAsync extends AsyncTask<Context, Void, Context> {
    private int JwcLoadSuccess = -1;
    private int JwLoadSuccess = -1;
    private int loadCode = Config.NET_WORK_GET_SUCCESS;
    @Nullable
    private JwcTopic jwcTopic;
    @Nullable
    private JwTopic jwTopic;

    public InfoAsync() {
        this.jwcTopic = null;
        this.jwTopic = null;
    }

    @Override
    protected Context doInBackground(final Context... context) {
        try {
            int loadTime = 0;
            HomeFragment homeFragment = BaseMethod.getApp(context[0]).getViewPagerAdapter().getHomeFragment();
            if (homeFragment != null) {
                loadTime = homeFragment.getLoadTime();
            }
            if (loadTime == 0) {
                //首次只加载离线数据
                jwcTopic = (JwcTopic) DataMethod.getOfflineData(context[0], JwcTopic.class, JwcInfoMethod.FILE_NAME);
                jwTopic = (JwTopic) DataMethod.getOfflineData(context[0], JwTopic.class, JwInfoMethod.FILE_NAME);
                JwcLoadSuccess = Config.NET_WORK_GET_SUCCESS;
                JwLoadSuccess = Config.NET_WORK_GET_SUCCESS;
                loadTime++;
                if (homeFragment != null) {
                    homeFragment.setLoadTime(loadTime);
                }
            } else {
                JwcInfoMethod jwcInfoMethod = new JwcInfoMethod(context[0]);
                JwcLoadSuccess = jwcInfoMethod.load();
                if (JwcLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                    jwcTopic = jwcInfoMethod.getJwcTopic(loadTime > 1);
                }

                JwInfoMethod jwInfoMethod = new JwInfoMethod(context[0]);
                JwLoadSuccess = jwInfoMethod.load();
                if (JwLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                    jwTopic = jwInfoMethod.getJwTopic(loadTime > 1);
                }

                loadTime++;
                homeFragment.setLoadTime(loadTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
            loadCode = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
        }
        return context[0];
    }

    @Override
    protected void onPostExecute(@NonNull Context context) {
        HomeFragment homeFragment = BaseMethod.getApp(context).getViewPagerAdapter().getHomeFragment();
        if (homeFragment != null) {
            if (NetMethod.checkNetWorkCode(context, new int[]{JwLoadSuccess, JwcLoadSuccess}, loadCode)) {
                homeFragment.InfoSet(jwcTopic, jwTopic);
            }
            homeFragment.lastViewSet(context);
        }
        System.gc();
        super.onPostExecute(context);
    }
}
