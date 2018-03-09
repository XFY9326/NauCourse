package tool.xfy9326.naucourse.Methods;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Utils.Course;
import tool.xfy9326.naucourse.Utils.SchoolTime;

/**
 * Created by 10696 on 2018/3/9.
 * 非Fragment内获取下一节课
 */

public class NextClassMethod {

    public static String[] getNextClassArray(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(Config.PREFERENCE_HAS_LOGIN, Config.DEFAULT_PREFERENCE_HAS_LOGIN)) {
            SchoolTime schoolTime = (SchoolTime) BaseMethod.getOfflineData(context, SchoolTime.class, TimeMethod.FILE_NAME);
            ArrayList<Course> courses = BaseMethod.getOfflineTableData(context);

            int weekNum = BaseMethod.getNowWeekNum(schoolTime);

            if (schoolTime != null && courses != null && weekNum != 0) {
                schoolTime.setWeekNum(weekNum);
                CourseMethod courseMethod = new CourseMethod(context, courses, schoolTime);
                return courseMethod.getNextClass(weekNum);
            }
        }
        return new String[5];
    }

}
