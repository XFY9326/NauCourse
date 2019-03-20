package tool.xfy9326.naucourse.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import java.util.LinkedHashMap;
import java.util.List;

import androidx.annotation.Nullable;
import tool.xfy9326.naucourse.Activities.CourseSearchActivity;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.Methods.NetInfoMethods.CourseSearchMethod;
import tool.xfy9326.naucourse.Methods.NetMethod;

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
                courseSearchActivity.lastBaseViewSet();
            }
        }
        System.gc();
        super.onPostExecute(context);
    }
}
