package tool.xfy9326.naucourse.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.net.SocketTimeoutException;
import java.util.Objects;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.activities.InfoDetailActivity;
import tool.xfy9326.naucourse.methods.BaseMethod;
import tool.xfy9326.naucourse.methods.InfoMethod;
import tool.xfy9326.naucourse.methods.NetMethod;
import tool.xfy9326.naucourse.methods.netInfoMethods.AlstuMethod;
import tool.xfy9326.naucourse.methods.netInfoMethods.JwcInfoMethod;
import tool.xfy9326.naucourse.methods.netInfoMethods.RSSInfoMethod;

/**
 * Created by 10696 on 2018/3/2.
 */

public class InfoDetailAsync extends AsyncTask<Context, Void, Context> {
    @Nullable
    private String data;
    private int loadSuccess = -1;
    private int loadCode = Config.NET_WORK_GET_SUCCESS;
    @Nullable
    private String infoSource;
    @Nullable
    private String infoUrl;

    public InfoDetailAsync() {
        this.data = null;
        this.infoSource = null;
        this.infoUrl = null;
    }

    /**
     * 设置加载详细信息来源与地址
     *
     * @param infoSource 信息来源
     * @param infoUrl    信息地址
     */
    public void setData(String infoSource, String infoUrl) {
        this.infoSource = infoSource;
        this.infoUrl = infoUrl;
    }

    @Override
    protected Context doInBackground(Context... context) {
        try {
            if (context[0] != null) {
                if (Objects.requireNonNull(infoSource).equals(InfoMethod.TOPIC_SOURCE_JWC)) {
                    JwcInfoMethod jwcInfoMethod = new JwcInfoMethod(context[0]);
                    loadSuccess = jwcInfoMethod.loadDetail(infoUrl);
                    if (loadSuccess == Config.NET_WORK_GET_SUCCESS) {
                        data = jwcInfoMethod.getDetailData();
                    }
                } else if (infoSource.equals(InfoMethod.TOPIC_SOURCE_RSS)) {
                    loadSuccess = RSSInfoMethod.loadDetail(context[0], infoUrl);
                    if (loadSuccess == Config.NET_WORK_GET_SUCCESS) {
                        data = RSSInfoMethod.getDetail(context[0]);
                    }
                } else if (infoSource.equals(InfoMethod.TOPIC_SOURCE_ALSTU)) {
                    AlstuMethod alstuMethod = new AlstuMethod(context[0]);
                    loadSuccess = alstuMethod.loadDetail(infoUrl);
                    if (loadSuccess == Config.NET_WORK_GET_SUCCESS) {
                        data = alstuMethod.getDetailData();
                    }
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
        return context[0];
    }

    @Override
    protected void onPostExecute(@NonNull Context context) {
        InfoDetailActivity infoDetailActivity = BaseMethod.getApp(context).getInfoDetailActivity();
        if (infoDetailActivity != null) {
            if (NetMethod.checkNetWorkCode(context, new int[]{loadSuccess}, loadCode, false)) {
                infoDetailActivity.infoDetailSet(data);
            }
        }
        System.gc();
        super.onPostExecute(context);
    }
}
