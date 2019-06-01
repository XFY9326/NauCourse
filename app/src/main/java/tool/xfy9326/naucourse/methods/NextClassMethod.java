package tool.xfy9326.naucourse.methods;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.methods.netInfoMethods.SchoolTimeMethod;
import tool.xfy9326.naucourse.utils.Course;
import tool.xfy9326.naucourse.utils.NextCourse;
import tool.xfy9326.naucourse.utils.SchoolTime;

/**
 * Created by 10696 on 2018/3/9.
 * 仅用于获取下一节课
 */

public class NextClassMethod {
    public static final String NEXT_COURSE_FILE_NAME = "NextCourse";
    public static final boolean IS_ENCRYPT = false;

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
            SchoolTime schoolTime = (SchoolTime) DataMethod.getOfflineData(context, SchoolTime.class, SchoolTimeMethod.FILE_NAME, SchoolTimeMethod.IS_ENCRYPT);
            ArrayList<Course> courses = DataMethod.getOfflineTableData(context);

            if (schoolTime != null && courses != null) {
                schoolTime = TimeMethod.termSetCheck(context, schoolTime, true);
                int weekNum = TimeMethod.getNowWeekNum(schoolTime);
                if (weekNum != 0) {
                    schoolTime.setWeekNum(weekNum);
                    CourseMethod courseMethod = new CourseMethod(context, courses, schoolTime);
                    nextCourse = courseMethod.getNextClass(weekNum);
                    nextCourse.setInVacation(false);
                } else {
                    nextCourse = new NextCourse();
                }
            }
        }
        if (nextCourse == null) {
            nextCourse = new NextCourse();
        }

        DataMethod.saveOfflineData(context, nextCourse, NEXT_COURSE_FILE_NAME, false, IS_ENCRYPT);
        return nextCourse;
    }

}