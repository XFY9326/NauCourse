package tool.xfy9326.naucourse.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.net.SocketTimeoutException;
import java.util.ArrayList;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.activities.CourseActivity;
import tool.xfy9326.naucourse.methods.BaseMethod;
import tool.xfy9326.naucourse.methods.NetMethod;
import tool.xfy9326.naucourse.methods.netInfoMethods.TableMethod;
import tool.xfy9326.naucourse.utils.Course;

/**
 * Created by 10696 on 2018/3/2.
 */

public class CourseListAsync extends AsyncTask<Context, Void, Context> {
    private int tableLoadSuccess = -1;
    private int loadCode = Config.NET_WORK_GET_SUCCESS;
    @Nullable
    private ArrayList<Course> course;
    private boolean syncFinish = false;

    public CourseListAsync() {
        this.course = null;
    }

    @Override
    protected Context doInBackground(Context... context) {
        try {
            TableMethod tableMethod = new TableMethod(context[0]);
            tableLoadSuccess = tableMethod.load();
            if (!syncFinish && tableLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                course = tableMethod.getData(false);
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
    protected void onPostExecute(@NonNull Context context) {
        if (!syncFinish) {
            NetMethod.showConnectErrorOnce = false;
            CourseActivity courseActivity = BaseMethod.getApp(context).getCourseActivity();
            if (NetMethod.checkNetWorkCode(context, new int[]{tableLoadSuccess}, loadCode, false)) {
                if (courseActivity != null && !syncFinish) {
                    courseActivity.addCourseList(course, true, false, false, true);
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
