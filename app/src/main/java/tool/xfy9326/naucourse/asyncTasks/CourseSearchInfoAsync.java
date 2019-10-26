package tool.xfy9326.naucourse.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.Nullable;

import java.net.SocketTimeoutException;
import java.util.LinkedHashMap;
import java.util.List;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.activities.CourseSearchActivity;
import tool.xfy9326.naucourse.methods.BaseMethod;
import tool.xfy9326.naucourse.methods.NetMethod;
import tool.xfy9326.naucourse.methods.netInfoMethods.CourseSearchMethod;

public class CourseSearchInfoAsync extends AsyncTask<Object, Void, Context> {
    private int courseSearchLoadSuccess = -1;
    private int loadCode = Config.NET_WORK_GET_SUCCESS;
    @Nullable
    private LinkedHashMap<String, String> searchTypeList;
    @Nullable
    private List<String> termList;
    @Nullable
    private List<String> roomList;
    @Nullable
    private List<String> deptList;

    public CourseSearchInfoAsync() {
        this.searchTypeList = null;
        this.termList = null;
        this.roomList = null;
        this.deptList = null;
    }

    @Override
    protected Context doInBackground(Object... objects) {
        if (objects.length == 2 && objects[0] instanceof CourseSearchMethod && objects[1] instanceof Context) {
            try {
                CourseSearchMethod courseSearchMethod = (CourseSearchMethod) objects[0];
                courseSearchLoadSuccess = courseSearchMethod.load();
                if (courseSearchLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                    searchTypeList = courseSearchMethod.getSearchTypeList();
                    termList = courseSearchMethod.getTermList();
                    roomList = courseSearchMethod.getRoomList();
                    deptList = courseSearchMethod.getDeptList();
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (e instanceof SocketTimeoutException) {
                    courseSearchLoadSuccess = Config.NET_WORK_ERROR_CODE_TIME_OUT;
                } else {
                    courseSearchLoadSuccess = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
                }
                loadCode = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
            }
            return (Context) objects[1];
        }
        return null;
    }

    @Override
    protected void onPostExecute(Context context) {
        if (context != null) {
            CourseSearchActivity courseSearchActivity = BaseMethod.getApp(context).getCourseSearchActivity();
            if (courseSearchActivity != null) {
                if (NetMethod.checkNetWorkCode(context, new int[]{courseSearchLoadSuccess}, loadCode, false)) {
                    courseSearchActivity.setBaseSearchView(searchTypeList, termList, roomList, deptList);
                }
                courseSearchActivity.lastViewSet();
            }
        }
        System.gc();
        super.onPostExecute(context);
    }
}
