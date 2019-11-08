package tool.xfy9326.naucourse.methods.async;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lib.xfy9326.nausso.NauSSOClient;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.beans.course.Course;
import tool.xfy9326.naucourse.beans.course.CourseDetail;
import tool.xfy9326.naucourse.methods.BaseMethod;
import tool.xfy9326.naucourse.methods.io.DataMethod;
import tool.xfy9326.naucourse.methods.net.NetMethod;

/**
 * Created by xfy9326 on 18-2-20.
 * 课程表信息获取与整理方法
 */

public class TableMethod extends BaseInfoMethod<ArrayList<Course>> {
    public static final String FILE_NAME = Course.class.getSimpleName();
    public static final boolean IS_ENCRYPT = true;
    @Nullable
    private Document document;

    public TableMethod(@NonNull Context context) {
        super(context);
    }

    @Override
    public int load() throws IOException, InterruptedException {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(Config.PREFERENCE_HAS_LOGIN, Config.DEFAULT_PREFERENCE_HAS_LOGIN)) {
            String data = NetMethod.loadUrlFromLoginClient(context, NauSSOClient.JWC_SERVER_URL + "/Students/MyCourseScheduleTable.aspx", true);
            if (data != null) {
                if (NauSSOClient.checkUserLogin(data)) {
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
    @Override
    public ArrayList<Course> getData(boolean checkTemp) {
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
                        if ("一".equalsIgnoreCase(termStr)) {
                            term = 1;
                        } else if ("二".equalsIgnoreCase(termStr)) {
                            term = 2;
                        }
                        allCourseTerm = year * 10L + term;
                    }
                    break;
                }
            }

            Elements tags = document.body().getElementById("content").getElementsByTag("tr");
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

                String courseInfo = str.substring(2, str.indexOf(" 上课地点")).trim();
                String courseDetail = str.substring(str.indexOf("上课地点") + 5).trim();

                String[] info = courseInfo.split(" ");
                String[] detail = courseDetail.split("上课地点：");

                if (info.length >= 7) {
                    course.setCourseId(info[0]);
                    for (int i = 2; i < info.length - 5; i++) {
                        info[1] += info[i];
                    }
                    course.setCourseName(info[1]);
                    course.setCourseClass(info[info.length - 5]);
                    course.setCourseScore(info[info.length - 4]);
                    course.setCourseCombinedClass(info[info.length - 3]);
                    course.setCourseType(info[info.length - 2]);
                    course.setCourseTeacher(info[info.length - 1]);
                } else {
                    return courseList;
                }

                CourseDetail[] courseDetailList = new CourseDetail[detail.length];

                for (int i = 0; i < detail.length; i++) {
                    CourseDetail courseDetailNew = new CourseDetail();
                    String[] temp = detail[i].trim().split("上课时间：");
                    courseDetailNew.setLocation(temp[0].trim());
                    String[] timeTemp = temp[1].trim().split(" ");

                    courseDetailNew.setWeekDay(Integer.valueOf(timeTemp[2]));

                    timeTemp[0] = timeTemp[0].trim();
                    if (timeTemp[0].contains("单")) {
                        courseDetailNew.setWeekMode(Config.COURSE_DETAIL_WEEKMODE_SINGLE);
                        courseDetailNew.setWeeks(new String[]{timeTemp[0].substring(0, timeTemp[0].indexOf("之"))});
                    } else if (timeTemp[0].contains("双")) {
                        courseDetailNew.setWeekMode(Config.COURSE_DETAIL_WEEKMODE_DOUBLE);
                        courseDetailNew.setWeeks(new String[]{timeTemp[0].substring(0, timeTemp[0].indexOf("之"))});
                    } else if (timeTemp[0].startsWith("第")) {
                        courseDetailNew.setWeekMode(Config.COURSE_DETAIL_WEEKMODE_ONCE_MORE);
                        String weekStr = timeTemp[0].substring(1, timeTemp[0].indexOf("周")).trim();
                        if (weekStr.contains(",")) {
                            String[] weekArr = weekStr.split(",");
                            courseDetailNew.setWeeks(weekArr);
                        } else {
                            courseDetailNew.setWeeks(new String[]{weekStr});
                        }
                    } else {
                        courseDetailNew.setWeekMode(Config.COURSE_DETAIL_WEEKMODE_ONCE);
                        courseDetailNew.setWeeks(new String[]{timeTemp[0].substring(0, timeTemp[0].indexOf("周")).trim()});
                    }

                    String courseTime = timeTemp[4].substring(0, timeTemp[4].indexOf("节"));
                    if (courseTime.contains(",")) {
                        courseDetailNew.setCourseTime(courseTime.split(","));
                    } else {
                        courseDetailNew.setCourseTime(new String[]{courseTime.trim()});
                    }

                    courseDetailList[i] = courseDetailNew;
                }

                course.setCourseDetail(courseDetailList);


                course.setCourseColor(BaseMethod.getRandomColor(context));

                courseList.add(course);
            }

            //保存数据
            if (checkTemp) {
                DataMethod.saveOfflineData(context, courseList, FILE_NAME, false, IS_ENCRYPT);
            }
        }
        return courseList;
    }

}
