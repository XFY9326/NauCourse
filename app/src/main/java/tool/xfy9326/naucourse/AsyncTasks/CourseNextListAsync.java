package tool.xfy9326.naucourse.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import tool.xfy9326.naucourse.Activities.CourseActivity;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.Methods.InfoMethods.TableNextMethod;
import tool.xfy9326.naucourse.Methods.NetMethod;
import tool.xfy9326.naucourse.Utils.Course;

public class CourseNextListAsync extends AsyncTask<Context, Void, Context> {
    private int tableLoadSuccess = -1;
    private int loadCode = Config.NET_WORK_GET_SUCCESS;
    @Nullable
    private ArrayList<Course> course;

    public CourseNextListAsync() {
        this.course = null;
    }

    @Override
    protected Context doInBackground(Context... context) {
        try {
            TableNextMethod tableNextMethod = new TableNextMethod(context[0]);
            tableLoadSuccess = tableNextMethod.load();
            if (tableLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                course = tableNextMethod.getCourseTable();
            }
        } catch (Exception e) {
            e.printStackTrace();
            loadCode = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
        }
        return context[0];
    }

    @Override
    protected void onPostExecute(Context context) {
        BaseMethod.getApp(context).setShowConnectErrorOnce(false);
        CourseActivity courseActivity = BaseMethod.getApp(context).getCourseActivity();
        if (NetMethod.checkNetWorkCode(context, new int[]{tableLoadSuccess}, loadCode, false)) {
            if (courseActivity != null) {
                courseActivity.addCourseList(course, false, course == null);
            }
        } else {
            if (courseActivity != null) {
                courseActivity.closeLoadingDialog();
            }
        }
        System.gc();
        super.onPostExecute(context);
    }
}
