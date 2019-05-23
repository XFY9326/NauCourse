package tool.xfy9326.naucourse.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
    private String info_source;
    @Nullable
    private String info_url;

    public InfoDetailAsync() {
        this.data = null;
        this.info_source = null;
        this.info_url = null;
    }

    /**
     * 设置加载详细信息来源与地址
     *
     * @param info_source 信息来源
     * @param info_url    信息地址
     */
    public void setData(String info_source, String info_url) {
        this.info_source = info_source;
        this.info_url = info_url;
    }

    @Override
    protected Context doInBackground(Context... context) {
        try {
            if (context[0] != null) {
                if (Objects.requireNonNull(info_source).equals(InfoMethod.TOPIC_SOURCE_JWC)) {
                    JwcInfoMethod jwcInfoMethod = new JwcInfoMethod(context[0]);
                    loadSuccess = jwcInfoMethod.loadDetail(info_url);
                    if (loadSuccess == Config.NET_WORK_GET_SUCCESS) {
                        data = jwcInfoMethod.getDetail();
                    }
                } else if (info_source.equals(InfoMethod.TOPIC_SOURCE_RSS)) {
                    loadSuccess = RSSInfoMethod.loadDetail(info_url);
                    if (loadSuccess == Config.NET_WORK_GET_SUCCESS) {
                        data = RSSInfoMethod.getDetail(context[0]);
                    }
                } else if (info_source.equals(InfoMethod.TOPIC_SOURCE_ALSTU)) {
                    AlstuMethod alstuMethod = new AlstuMethod(context[0]);
                    loadSuccess = alstuMethod.loadDetail(info_url);
                    if (loadSuccess == Config.NET_WORK_GET_SUCCESS) {
                        data = alstuMethod.getDetail();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            loadCode = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
        }
        return context[0];
    }

    @Override
    protected void onPostExecute(@NonNull Context context) {
        InfoDetailActivity infoDetailActivity = BaseMethod.getApp(context).getInfoDetailActivity();
        if (infoDetailActivity != null) {
            if (NetMethod.checkNetWorkCode(context, new int[]{loadSuccess}, loadCode, false)) {
                infoDetailActivity.InfoDetailSet(data);
            }
        }
        System.gc();
        super.onPostExecute(context);
    }
}
