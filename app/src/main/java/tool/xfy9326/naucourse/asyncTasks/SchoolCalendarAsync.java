package tool.xfy9326.naucourse.asyncTasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import androidx.annotation.Nullable;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.activities.SchoolCalendarActivity;
import tool.xfy9326.naucourse.methods.BaseMethod;
import tool.xfy9326.naucourse.methods.ImageMethod;
import tool.xfy9326.naucourse.methods.NetMethod;
import tool.xfy9326.naucourse.methods.netInfoMethods.SchoolCalendarMethod;

public class SchoolCalendarAsync extends AsyncTask<Context, Void, Context> {
    private int jwLoadSuccess = -1;
    private int imageLoadSuccess = -1;
    private int loadCode = Config.NET_WORK_GET_SUCCESS;
    @Nullable
    private Bitmap bitmap;

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
            } else {
                SchoolCalendarMethod schoolCalendarMethod = new SchoolCalendarMethod(context[0]);
                jwLoadSuccess = schoolCalendarMethod.load();
                if (jwLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                    imageLoadSuccess = schoolCalendarMethod.loadSchoolCalendarImage(true);
                    if (imageLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                        bitmap = schoolCalendarMethod.getSchoolCalendarImage();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            loadCode = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
        }
        if (schoolCalendarActivity != null) {
            schoolCalendarActivity.setLoadTime(++loadTime);
        }
        if (loadTime > 2) {
            BaseMethod.getApp(context[0]).setShowConnectErrorOnce(false);
        }
        return context[0];
    }

    @Override
    protected void onPostExecute(Context context) {
        SchoolCalendarActivity schoolCalendarActivity = BaseMethod.getApp(context).getSchoolCalendarActivity();
        if (schoolCalendarActivity != null) {
            if (NetMethod.checkNetWorkCode(context, new int[]{jwLoadSuccess, imageLoadSuccess}, loadCode, false)) {
                schoolCalendarActivity.setCalendarView(bitmap);
            }
            schoolCalendarActivity.lastViewSet(context);
        }
        System.gc();
        super.onPostExecute(context);
    }
}
