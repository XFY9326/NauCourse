package tool.xfy9326.naucourse.asyncTasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import androidx.annotation.Nullable;

import java.net.SocketTimeoutException;
import java.util.LinkedHashMap;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.activities.SchoolCalendarActivity;
import tool.xfy9326.naucourse.methods.BaseMethod;
import tool.xfy9326.naucourse.methods.ImageMethod;
import tool.xfy9326.naucourse.methods.NetMethod;
import tool.xfy9326.naucourse.methods.netInfoMethods.SchoolCalendarMethod;

public class SchoolCalendarAsync extends AsyncTask<Context, Void, Context> {
    private int jwLoadSuccess = -1;
    private int imageLoadSuccess = -1;
    private int listLoadSuccess = -1;
    private int loadCode = Config.NET_WORK_GET_SUCCESS;
    @Nullable
    private Bitmap bitmap;
    @Nullable
    private LinkedHashMap<String, String> calendarList;

    @Override
    protected Context doInBackground(Context... context) {
        int loadTime = 0;
        SchoolCalendarActivity schoolCalendarActivity = BaseMethod.getApp(context[0]).getSchoolCalendarActivity();
        try {
            if (schoolCalendarActivity != null) {
                loadTime = schoolCalendarActivity.getLoadTime();
            }
            if (loadTime == 0) {
                //首次只加载离线数据
                bitmap = ImageMethod.getSchoolCalendarImage(context[0]);
                jwLoadSuccess = Config.NET_WORK_GET_SUCCESS;
                imageLoadSuccess = Config.NET_WORK_GET_SUCCESS;
                listLoadSuccess = Config.NET_WORK_GET_SUCCESS;
            } else {
                SchoolCalendarMethod schoolCalendarMethod = new SchoolCalendarMethod(context[0]);
                jwLoadSuccess = schoolCalendarMethod.load();
                listLoadSuccess = schoolCalendarMethod.loadCalendarList();
                if (jwLoadSuccess == Config.NET_WORK_GET_SUCCESS && listLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                    calendarList = schoolCalendarMethod.getCalendarUrlList();

                    imageLoadSuccess = schoolCalendarMethod.loadSchoolCalendarImage(true);
                    if (imageLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                        bitmap = schoolCalendarMethod.getSchoolCalendarImage();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof SocketTimeoutException) {
                jwLoadSuccess = Config.NET_WORK_ERROR_CODE_TIME_OUT;
                imageLoadSuccess = Config.NET_WORK_ERROR_CODE_TIME_OUT;
                listLoadSuccess = Config.NET_WORK_ERROR_CODE_TIME_OUT;
            } else {
                jwLoadSuccess = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
                imageLoadSuccess = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
                listLoadSuccess = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
            }
            loadCode = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
        }
        if (schoolCalendarActivity != null) {
            schoolCalendarActivity.setLoadTime(++loadTime);
        }
        if (loadTime > 2) {
            NetMethod.showConnectErrorOnce = false;
        }
        return context[0];
    }

    @Override
    protected void onPostExecute(Context context) {
        SchoolCalendarActivity schoolCalendarActivity = BaseMethod.getApp(context).getSchoolCalendarActivity();
        if (schoolCalendarActivity != null) {
            if (NetMethod.checkNetWorkCode(context, new int[]{jwLoadSuccess, imageLoadSuccess, listLoadSuccess}, loadCode, false)) {
                schoolCalendarActivity.setCalendarData(calendarList, bitmap);
            }
            schoolCalendarActivity.lastViewSet(context);
        }
        System.gc();
        super.onPostExecute(context);
    }
}
