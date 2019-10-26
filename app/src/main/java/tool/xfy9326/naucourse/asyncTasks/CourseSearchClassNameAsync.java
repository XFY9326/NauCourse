package tool.xfy9326.naucourse.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import java.util.List;

import tool.xfy9326.naucourse.activities.CourseSearchActivity;
import tool.xfy9326.naucourse.methods.BaseMethod;
import tool.xfy9326.naucourse.methods.netInfoMethods.CourseSearchMethod;

public class CourseSearchClassNameAsync extends AsyncTask<Object, Void, Context> {
    private List<String> classNameList = null;

    @Override
    protected Context doInBackground(Object... objects) {
        if (objects.length == 3 && objects[0] instanceof CourseSearchMethod && objects[1] instanceof String && objects[2] instanceof Context) {
            String term = (String) objects[1];
            try {
                CourseSearchMethod courseSearchMethod = (CourseSearchMethod) objects[0];
                classNameList = courseSearchMethod.getClassNameList(term);
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
                courseSearchActivity.setClassNameList(classNameList);
                courseSearchActivity.lastViewSet();
            }
            System.gc();
        }
        super.onPostExecute(context);
    }
}
