package tool.xfy9326.naucourse.Methods;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.InfoMethods.SchoolTimeMethod;
import tool.xfy9326.naucourse.Utils.Course;
import tool.xfy9326.naucourse.Utils.NextCourse;
import tool.xfy9326.naucourse.Utils.SchoolTime;

/**
 * Created by 10696 on 2018/3/9.
 * 仅用于获取下一节课
 */

public class NextClassMethod {
    public static final String NEXT_COURSE_FILE_NAME = "NextCourse";

    /**
     * 获取下一节课的信息
     *
     * @param context Context
     * @return 下一节课的信息
     */
    @NonNull
    public static NextCourse getNextClassArray(@NonNull Context context) {
        NextCourse nextCourse = null;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(Config.PREFERENCE_HAS_LOGIN, Config.DEFAULT_PREFERENCE_HAS_LOGIN)) {
            SchoolTime schoolTime = (SchoolTime) DataMethod.getOfflineData(context, SchoolTime.class, SchoolTimeMethod.FILE_NAME);
            ArrayList<Course> courses = DataMethod.getOfflineTableData(context);

            if (schoolTime != null && courses != null) {
                schoolTime = TimeMethod.termSetCheck(context, schoolTime, true);
                int weekNum = TimeMethod.getNowWeekNum(schoolTime);
                if (weekNum != 0) {
                    schoolTime.setWeekNum(weekNum);
                    CourseMethod courseMethod = new CourseMethod(context, courses, schoolTime);
                    nextCourse = courseMethod.getNextClass(weekNum);
                } else {
                    nextCourse = new NextCourse();
                    nextCourse.setInVacation(true);
                }
            }
        }
        if (nextCourse == null) {
            nextCourse = new NextCourse();
        }
        return nextCourse;
    }

}
