package tool.xfy9326.naucourse.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.Nullable;

import java.net.SocketTimeoutException;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.activities.MoaActivity;
import tool.xfy9326.naucourse.methods.BaseMethod;
import tool.xfy9326.naucourse.methods.DataMethod;
import tool.xfy9326.naucourse.methods.NetMethod;
import tool.xfy9326.naucourse.methods.netInfoMethods.MoaMethod;
import tool.xfy9326.naucourse.utils.Moa;

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
                moa = (Moa) DataMethod.getOfflineData(context[0], Moa.class, MoaMethod.FILE_NAME, MoaMethod.IS_ENCRYPT);
                loadSuccess = Config.NET_WORK_GET_SUCCESS;
            } else {
                MoaMethod moaMethod = new MoaMethod(context[0]);
                loadSuccess = moaMethod.load();
                if (loadSuccess == Config.NET_WORK_GET_SUCCESS) {
                    moa = moaMethod.getData(loadTime > 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof SocketTimeoutException) {
                loadSuccess = Config.NET_WORK_ERROR_CODE_TIME_OUT;
            } else {
                loadSuccess = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
            }
            loadCode = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
        }
        if (moaActivity != null) {
            moaActivity.setLoadTime(++loadTime);
        }
        if (loadTime > 2) {
            NetMethod.showConnectErrorOnce = false;
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
