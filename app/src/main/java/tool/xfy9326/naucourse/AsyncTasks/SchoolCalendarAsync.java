package tool.xfy9326.naucourse.AsyncTasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.Nullable;

import tool.xfy9326.naucourse.Activities.SchoolCalendarActivity;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.Methods.ImageMethod;
import tool.xfy9326.naucourse.Methods.JwInfoMethod;
import tool.xfy9326.naucourse.Methods.NetMethod;

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
                loadTime++;
                if (schoolCalendarActivity != null) {
                    schoolCalendarActivity.setLoadTime(loadTime);
                }
            } else {
                JwInfoMethod jwInfoMethod = new JwInfoMethod(context[0]);
                jwLoadSuccess = jwInfoMethod.load();
                if (jwLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                    imageLoadSuccess = jwInfoMethod.loadSchoolCalendarImage(true);
                    if (imageLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                        bitmap = jwInfoMethod.getSchoolCalendarImage();
                    }
                }
                loadTime++;
                schoolCalendarActivity.setLoadTime(loadTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
            loadCode = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
            loadTime++;
            BaseMethod.getApp(context[0]).getSchoolCalendarActivity().setLoadTime(loadTime);
        }
        return context[0];
    }

    @Override
    protected void onPostExecute(Context context) {
        SchoolCalendarActivity schoolCalendarActivity = BaseMethod.getApp(context).getSchoolCalendarActivity();
        if (schoolCalendarActivity != null) {
            if (NetMethod.checkNetWorkCode(context, new int[]{jwLoadSuccess, imageLoadSuccess}, loadCode)) {
                schoolCalendarActivity.setCalendarView(bitmap);
            }
            schoolCalendarActivity.lastViewSet(context);
        }
        System.gc();
        super.onPostExecute(context);
    }
}
