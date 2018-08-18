package tool.xfy9326.naucourse.Methods.InfoMethods;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.Methods.DataMethod;
import tool.xfy9326.naucourse.Methods.LoginMethod;
import tool.xfy9326.naucourse.Utils.Course;
import tool.xfy9326.naucourse.Utils.CourseDetail;

/**
 * Created by xfy9326 on 18-2-20.
 * 课程表信息获取与整理方法
 */

public class TableMethod {
    public static final String FILE_NAME = "Course";
    private final Context context;
    @Nullable
    private Document document;

    public TableMethod(Context context) {
        this.context = context;
        this.document = null;
    }

    public int load() throws Exception {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(Config.PREFERENCE_HAS_LOGIN, Config.DEFAULT_PREFERENCE_HAS_LOGIN)) {
            String data = LoginMethod.getData(context, "/Students/MyCourseScheduleTable.aspx", true);
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
    @Nullable
    public ArrayList<Course> getCourseTable(boolean needSave) {
        ArrayList<Course> courseList = new ArrayList<>();
        if (document != null) {
            long allCourseTerm = 0;
            Elements termElement = document.body().getElementsByClass("tdTitle");
            List<String> termList = termElement.eachText();
            for (String str : termList) {
                if (str.contains("在修课程")) {
                    if (str.contains(" ") && str.contains("-")) {
                        String[] termData = str.split(" ");
                        //仅支持四位数的年份，仅支持一年两学期制
                        long year = Integer.valueOf(termData[0].substring(0, termData[0].indexOf("-")));
                        year = year * 10000L + year + 1L;
                        long term = 0;
                        String termStr = termData[0].substring(termData[0].indexOf("第") + 1, termData[0].indexOf("第") + 2);
                        if (termStr.equalsIgnoreCase("一")) {
                            term = 1;
                        } else if (termStr.equalsIgnoreCase("二")) {
                            term = 2;
                        }
                        allCourseTerm = year * 10L + term;
                    }
                    break;
                }
            }

            Elements tags = document.body().getElementsByTag("tr");
            List<String> data = tags.eachText();
            for (String str : data) {
                //同步的课程学期数必定一致
                if (str.contains("序号")) {
                    continue;
                }
                if (str.contains("节次")) {
                    break;
                }
                Course course = new Course();

                course.setCourseTerm(String.valueOf(allCourseTerm));

                String course_Info = str.substring(2, str.indexOf(" 上课地点")).trim();
                String course_Detail = str.substring(str.indexOf("上课地点") + 5).trim();

                String[] info = course_Info.split(" ");
                String[] detail = course_Detail.split("上课地点：");

                course.setCourseId(info[0]);
                course.setCourseName(info[1]);
                course.setCourseClass(info[2]);
                course.setCourseScore(info[3]);
                course.setCourseCombinedClass(info[4]);
                course.setCourseType(info[5]);
                course.setCourseTeacher(info[6]);
                course.setDataVersionCode(Config.DATA_VERSION_COURSE);

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
                    } else if (time_temp[0].trim().startsWith("第")) {
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

                //颜色随机
                int[] colorList = BaseMethod.getColorArray(context);
                Random random = new Random();
                int num = random.nextInt(colorList.length) % (colorList.length + 1);
                course.setCourseColor(colorList[num]);

                courseList.add(course);
            }

            if (needSave) {
                DataMethod.saveOfflineData(context, courseList, TableMethod.FILE_NAME, false);
            }
        }
        return courseList;
    }

}
