package tool.xfy9326.naucourse.methods.compute;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import java.util.ArrayList;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.beans.SchoolTime;
import tool.xfy9326.naucourse.beans.course.Course;
import tool.xfy9326.naucourse.beans.course.TodayCourses;
import tool.xfy9326.naucourse.methods.async.SchoolTimeMethod;
import tool.xfy9326.naucourse.methods.io.DataMethod;

public class TodayCourseMethod {
    public static TodayCourses getTodayCourseList(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(Config.PREFERENCE_HAS_LOGIN, Config.DEFAULT_PREFERENCE_HAS_LOGIN)) {
            SchoolTime schoolTime = (SchoolTime) DataMethod.getOfflineData(context, SchoolTime.class, SchoolTimeMethod.FILE_NAME, SchoolTimeMethod.IS_ENCRYPT);
            ArrayList<Course> courses = DataMethod.getOfflineTableData(context);

            if (schoolTime != null && courses != null) {
                schoolTime = TimeMethod.termSetCheck(context, schoolTime, true);
                int weekNum = TimeMethod.getNowWeekNum(schoolTime);
                if (weekNum != 0) {
                    schoolTime.setWeekNum(weekNum);
                    CourseMethod courseMethod = new CourseMethod(context, courses, schoolTime);
                    return courseMethod.getTodayCourse(weekNum);
                }
            }
        }
        return null;
    }
}
