package tool.xfy9326.naucourse.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import java.util.List;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.activities.CourseSearchActivity;
import tool.xfy9326.naucourse.methods.BaseMethod;
import tool.xfy9326.naucourse.methods.netInfoMethods.CourseSearchMethod;
import tool.xfy9326.naucourse.utils.CourseSearchDetail;
import tool.xfy9326.naucourse.utils.CourseSearchInfo;

public class CourseSearchAsync extends AsyncTask<Object, Void, Context> {
    private List<CourseSearchDetail> courseSearchDetail = null;

    @Override
    protected Context doInBackground(Object... objects) {
        if (objects.length == 3 && objects[0] instanceof CourseSearchInfo && objects[1] instanceof CourseSearchMethod && objects[2] instanceof Context) {
            CourseSearchInfo courseSearchInfo = (CourseSearchInfo) objects[0];
            try {
                CourseSearchMethod courseSearchMethod = (CourseSearchMethod) objects[1];
                if (courseSearchMethod.load() == Config.NET_WORK_GET_SUCCESS) {
                    courseSearchDetail = courseSearchMethod.getCourseSearchDetail(courseSearchInfo);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return (Context) objects[2];
        }
        return null;
    }

    @Override
    protected void onPostExecute(Context context) {
        if (context != null) {
            CourseSearchActivity courseSearchActivity = BaseMethod.getApp(context).getCourseSearchActivity();
            if (courseSearchActivity != null) {
                courseSearchActivity.setSearchResult(courseSearchDetail);
                courseSearchActivity.lastViewSet();
            }
            System.gc();
        }
        super.onPostExecute(context);
    }

}
