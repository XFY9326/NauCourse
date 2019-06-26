package tool.xfy9326.naucourse.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.Nullable;

import java.net.SocketTimeoutException;
import java.util.ArrayList;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.activities.CourseActivity;
import tool.xfy9326.naucourse.methods.BaseMethod;
import tool.xfy9326.naucourse.methods.NetMethod;
import tool.xfy9326.naucourse.methods.netInfoMethods.TableNextMethod;
import tool.xfy9326.naucourse.utils.Course;

public class CourseNextListAsync extends AsyncTask<Context, Void, Context> {
    private int tableLoadSuccess = -1;
    private int loadCode = Config.NET_WORK_GET_SUCCESS;
    @Nullable
    private ArrayList<Course> course;
    private boolean syncFinish = false;

    public CourseNextListAsync() {
        this.course = null;
    }

    @Override
    protected Context doInBackground(Context... context) {
        try {
            TableNextMethod tableNextMethod = new TableNextMethod(context[0]);
            tableLoadSuccess = tableNextMethod.load();
            if (!syncFinish && tableLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                course = tableNextMethod.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof SocketTimeoutException) {
                tableLoadSuccess = Config.NET_WORK_ERROR_CODE_TIME_OUT;
            } else {
                tableLoadSuccess = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
            }
            loadCode = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
        }
        return context[0];
    }

    @Override
    protected void onCancelled() {
        syncFinish = true;
        super.onCancelled();
    }

    @Override
    protected void onPostExecute(Context context) {
        if (!syncFinish) {
            NetMethod.showConnectErrorOnce = false;
            CourseActivity courseActivity = BaseMethod.getApp(context).getCourseActivity();
            if (NetMethod.checkNetWorkCode(context, new int[]{tableLoadSuccess}, loadCode, false)) {
                if (courseActivity != null && !syncFinish) {
                    courseActivity.addCourseList(course, false, false, course == null, true);
                }
            } else {
                if (courseActivity != null) {
                    courseActivity.closeLoadingDialog();
                }
            }
            System.gc();
        }
        super.onPostExecute(context);
    }
}
