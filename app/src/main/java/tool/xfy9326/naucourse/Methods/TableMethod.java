package tool.xfy9326.naucourse.Methods;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Utils.Course;
import tool.xfy9326.naucourse.Utils.CourseDetail;

/**
 * Created by xfy9326 on 18-2-20.
 * 课程表信息获取与整理方法
 */

public class TableMethod {
    static final String FILE_NAME = "Course";
    private final Context context;
    private Document document;

    public TableMethod(Context context) {
        this.context = context;
        this.document = null;
    }

    public int load() throws Exception {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(Config.PREFERENCE_HAS_LOGIN, Config.DEFAULT_PREFERENCE_HAS_LOGIN)) {
            String data = LoginMethod.getData(context, "/Students/MyCourseScheduleTable.aspx");
            if (data != null) {
                if (LoginMethod.checkUserLogin(data)) {
                    document = Jsoup.parse(data);
                    return Config.NET_WORK_GET_SUCCESS;
                }
                return Config.NET_WORK_ERROR_CODE_CONNECT_USER_DATA;
            }
            return Config.NET_WORK_ERROR_CODE_GET_DATA_ERROR;
        }
        return Config.NET_WORK_ERROR_CODE_CONNECT_NO_LOGIN;
    }

    //数据分类方法：课程基本信息——上课周数——上课节数
    public ArrayList<Course> getCourseTable(boolean checkTemp) {
        ArrayList<Course> courseList = new ArrayList<>();
        Elements tags = document.body().getElementsByTag("tr");
        List<String> data = tags.eachText();
        for (String str : data) {
            if (str.contains("序号")) {
                continue;
            }
            if (str.contains("节次")) {
                break;
            }
            Course course = new Course();

            String course_Info = str.substring(2, str.indexOf(" 上课地点")).trim();
            String course_Detail = str.substring(str.indexOf("上课地点") + 5).trim();

            String[] info = course_Info.split(" ");
            String[] detail = course_Detail.split("上课地点：");

            if (info[2].contains("级")) {
                info[2] = info[2].substring(info[2].indexOf("级") + 1);
            }
            if (info[4].contains("级")) {
                info[4] = info[4].substring(info[4].indexOf("级") + 1);
            }

            course.setCourseId(info[0]);
            course.setCourseName(info[1]);
            course.setCourseClass(info[2]);
            course.setCourseScore(info[3]);
            course.setCourseCombinedClass(info[4]);
            course.setCourseType(info[5]);
            course.setCourseTeacher(info[6]);

            CourseDetail[] courseDetail_list = new CourseDetail[detail.length];

            for (int i = 0; i < detail.length; i++) {
                CourseDetail courseDetail = new CourseDetail();
                String[] temp = detail[i].trim().split("上课时间：");
                courseDetail.setLocation(temp[0].trim());
                String[] time_temp = temp[1].trim().split(" ");

                courseDetail.setWeekDay(Integer.valueOf(time_temp[2]));

                if (time_temp[0].contains("单")) {
                    courseDetail.setWeekMode(Config.COURSE_DETAIL_WEEKMODE_SINGLE);

                    courseDetail.setWeeks(new String[]{time_temp[0].substring(0, time_temp[0].indexOf("之"))});
                } else if (time_temp[0].contains("双")) {
                    courseDetail.setWeekMode(Config.COURSE_DETAIL_WEEKMODE_DOUBLE);

                    courseDetail.setWeeks(new String[]{time_temp[0].substring(0, time_temp[0].indexOf("之"))});
                } else if (time_temp[0].contains("第")) {
                    courseDetail.setWeekMode(Config.COURSE_DETAIL_WEEKMODE_ONCE_MORE);

                    String weekStr = time_temp[0].substring(1, time_temp[0].indexOf("周")).trim();
                    if (weekStr.contains(",")) {
                        String[] weekArr = weekStr.split(",");
                        courseDetail.setWeeks(weekArr);
                    } else {
                        courseDetail.setWeeks(new String[]{weekStr});
                    }
                } else {
                    courseDetail.setWeekMode(Config.COURSE_DETAIL_WEEKMODE_ONCE);

                    courseDetail.setWeeks(new String[]{time_temp[0].substring(0, time_temp[0].indexOf("周")).trim()});
                }

                String courseTime = time_temp[4].substring(0, time_temp[4].indexOf("节"));
                if (courseTime.contains(",")) {
                    courseDetail.setCourseTime(courseTime.split(","));
                } else {
                    courseDetail.setCourseTime(new String[]{courseTime.trim()});
                }

                courseDetail_list[i] = courseDetail;
            }

            course.setCourseDetail(courseDetail_list);

            courseList.add(course);
        }

        if (BaseMethod.saveOfflineData(context, courseList, FILE_NAME, checkTemp)) {
            return courseList;
        } else {
            return null;
        }
    }

}
