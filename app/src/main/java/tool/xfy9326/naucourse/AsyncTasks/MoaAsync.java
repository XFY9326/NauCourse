package tool.xfy9326.naucourse.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.Nullable;
import tool.xfy9326.naucourse.Activities.MoaActivity;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.Methods.DataMethod;
import tool.xfy9326.naucourse.Methods.InfoMethods.MoaMethod;
import tool.xfy9326.naucourse.Methods.NetMethod;
import tool.xfy9326.naucourse.Utils.Moa;

public class MoaAsync extends AsyncTask<Context, Void, Context> {
    private int loadSuccess = -1;
    private int loadCode = Config.NET_WORK_GET_SUCCESS;
    @Nullable
    private Moa moa;

    public MoaAsync() {
        this.moa = null;
    }

    @Override
    protected Context doInBackground(Context... context) {
        int loadTime = 0;
        MoaActivity moaActivity = BaseMethod.getApp(context[0]).getMoaActivity();
        try {
            if (moaActivity != null) {
                loadTime = moaActivity.getLoadTime();
            }
            if (loadTime == 0) {
                //首次只加载离线数据
                moa = (Moa) DataMethod.getOfflineData(context[0], Moa.class, MoaMethod.FILE_NAME);
                loadSuccess = Config.NET_WORK_GET_SUCCESS;
                loadTime++;
                if (moaActivity != null) {
                    moaActivity.setLoadTime(loadTime);
                }
            } else {
                MoaMethod moaMethod = new MoaMethod(context[0]);
                loadSuccess = moaMethod.load();
                if (loadSuccess == Config.NET_WORK_GET_SUCCESS) {
                    moa = moaMethod.getMoaList(loadTime > 1);
                }
                loadTime++;
                moaActivity.setLoadTime(loadTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
            loadCode = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
            if (moaActivity != null) {
                loadTime++;
                moaActivity.setLoadTime(loadTime);
            }
        }
        if (loadTime > 2) {
            BaseMethod.getApp(context[0]).setShowConnectErrorOnce(false);
        }
        return context[0];
    }

    @Override
    protected void onPostExecute(Context context) {
        MoaActivity moaActivity = BaseMethod.getApp(context).getMoaActivity();
        if (moaActivity != null) {
            if (NetMethod.checkNetWorkCode(context, new int[]{loadSuccess}, loadCode, false)) {
                moaActivity.setMoa(moa);
            }
            moaActivity.lastViewSet(context);
        }
        System.gc();
        super.onPostExecute(context);
    }
}
