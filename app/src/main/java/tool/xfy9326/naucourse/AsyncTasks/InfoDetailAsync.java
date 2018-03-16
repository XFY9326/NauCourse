package tool.xfy9326.naucourse.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import tool.xfy9326.naucourse.Activities.InfoDetailActivity;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.Methods.JwInfoMethod;
import tool.xfy9326.naucourse.Methods.JwcInfoMethod;
import tool.xfy9326.naucourse.Views.InfoAdapter;

/**
 * Created by 10696 on 2018/3/2.
 */

public class InfoDetailAsync extends AsyncTask<Context, Void, Context> {
    private String data;
    private String[] extraFile;
    private int loadSuccess = -1;
    private int loadCode = Config.NET_WORK_GET_SUCCESS;
    private String info_source;
    private String info_url;

    public InfoDetailAsync() {
        this.data = null;
        this.extraFile = null;
        this.info_source = null;
        this.info_url = null;
    }

    public void setData(String info_source, String info_url) {
        this.info_source = info_source;
        this.info_url = info_url;
    }

    @Override
    protected Context doInBackground(Context... context) {
        try {
            if (context[0] != null) {
                if (info_source.equals(InfoAdapter.TOPIC_SOURCE_JWC)) {
                    JwcInfoMethod jwcInfoMethod = new JwcInfoMethod(context[0]);
                    loadSuccess = jwcInfoMethod.loadDetail(info_url);
                    if (loadSuccess == Config.NET_WORK_GET_SUCCESS) {
                        data = jwcInfoMethod.getDetail();
                    }
                } else if (info_source.equals(InfoAdapter.TOPIC_SOURCE_JW)) {
                    JwInfoMethod jwInfoMethod = new JwInfoMethod(context[0]);
                    loadSuccess = jwInfoMethod.loadDetail(info_url);
                    if (loadSuccess == Config.NET_WORK_GET_SUCCESS) {
                        data = jwInfoMethod.getDetail();
                        extraFile = jwInfoMethod.getExtraFileUrl();
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
    protected void onPostExecute(Context context) {
        InfoDetailActivity infoDetailActivity = BaseMethod.getBaseApplication(context).getInfoDetailActivity();
        if (infoDetailActivity != null) {
            if (BaseMethod.checkNetWorkCode(context, new int[]{loadSuccess}, loadCode)) {
                infoDetailActivity.InfoDetailSet(data, extraFile);
            }
        }
        System.gc();
        super.onPostExecute(context);
    }
}