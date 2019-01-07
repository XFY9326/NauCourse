package tool.xfy9326.naucourse.Methods.InfoMethods;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.annotation.Nullable;
import lib.xfy9326.nausso.NauSSOClient;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.Methods.LoginMethod;
import tool.xfy9326.naucourse.Methods.NetMethod;
import tool.xfy9326.naucourse.Utils.Course;
import tool.xfy9326.naucourse.Utils.CourseDetail;

public class TableNextMethod {
    private final Context context;
    @Nullable
    private Document document;

    public TableNextMethod(Context context) {
        this.context = context;
        this.document = null;
    }

    public int load() throws Exception {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(Config.PREFERENCE_HAS_LOGIN, Config.DEFAULT_PREFERENCE_HAS_LOGIN)) {
            String data = NetMethod.loadUrlFromLoginClient(context, NauSSOClient.JWC_SERVER_URL + "/Students/MyCourseScheduleTableNext.aspx", true);
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

    public ArrayList<Course> getCourseTable() {
        ArrayList<Course> courseList = new ArrayList<>();
        if (document != null) {
            boolean startCourse = false;
            Elements tags = document.body().getElementsByTag("tr");
            List<String> strList = tags.eachText();
            for (String courseStr : strList) {
                if (courseStr.contains("序号")) {
                    startCourse = true;
                } else if (startCourse && courseStr.contains(" ") && courseStr.contains("上课地点") && courseStr.contains("上课时间")) {
                    String[] data = courseStr.split(" ");
                    if (data.length <= 9) {
                        //数据不全
                        courseList = null;
                        break;
                    } else {
                        Course course = new Course();
                        course.setCourseTerm(data[data.length - 1]);

                        course.setCourseId(data[1]);
                        //可能会出问题的解决课程名称空格的方式
                        int i = 3;
                        for (; !data[i].contains("."); i++) {
                            data[2] += data[i];
                        }
                        course.setCourseName(data[2]);
                        //无教学班信息
                        course.setCourseScore(data[i]);
                        course.setCourseCombinedClass(data[i + 1]);
                        course.setCourseType(data[i + 3]);
                        course.setCourseTeacher(data[i + 4]);

                        course.setCourseDetail(getCourseDetailList(data, (data.length - 1 - 8) / 2));

                        //颜色随机
                        int[] colorList = BaseMethod.getColorArray(context);
                        Random random = new Random();
                        int num = random.nextInt(colorList.length) % (colorList.length + 1);
                        course.setCourseColor(colorList[num]);

                        if (courseList != null) {
                            courseList.add(course);
                        }
                    }
                } else if (!courseStr.contains("上课地点") && !courseStr.contains("上课时间") && !courseStr.contains("在修课程") && !courseStr.contains("序号")) {
                    if (courseList != null && courseList.size() == 0) {
                        //数据不全
                        courseList = null;
                    }
                }
            }
        }
        return courseList;
    }

    private CourseDetail[] getCourseDetailList(String[] data, int detail_length) {
        int detail_count = 0;
        CourseDetail[] courseDetail_list = new CourseDetail[detail_length];
        CourseDetail courseDetail = null;
        for (int i = 8; i < data.length - 1; i++) {
            if (courseDetail == null) {
                courseDetail = new CourseDetail();
            }
            if (data[i].contains("上课地点：")) {
                if (data[i].length() > 5) {
                    courseDetail.setLocation(data[i].substring(5));
                }
            } else if (data[i].contains("上课时间：")) {
                if (data[i].length() > 5) {
                    String mainTimeStr = data[i].substring(5).trim();
                    if (mainTimeStr.contains("单")) {
                        courseDetail.setWeekMode(Config.COURSE_DETAIL_WEEKMODE_SINGLE);

                        courseDetail.setWeeks(new String[]{mainTimeStr.substring(0, mainTimeStr.indexOf("之"))});
                    } else if (mainTimeStr.contains("双")) {
                        courseDetail.setWeekMode(Config.COURSE_DETAIL_WEEKMODE_DOUBLE);

                        courseDetail.setWeeks(new String[]{mainTimeStr.substring(0, mainTimeStr.indexOf("之"))});
                    } else if (mainTimeStr.startsWith("第")) {
                        courseDetail.setWeekMode(Config.COURSE_DETAIL_WEEKMODE_ONCE_MORE);
                        String weekStr = mainTimeStr.substring(1, mainTimeStr.indexOf("周")).trim();
                        if (weekStr.contains(",")) {
                            String[] weekArr = weekStr.split(",");
                            courseDetail.setWeeks(weekArr);
                        } else {
                            courseDetail.setWeeks(new String[]{weekStr});
                        }
                    } else {
                        courseDetail.setWeekMode(Config.COURSE_DETAIL_WEEKMODE_ONCE);
                        courseDetail.setWeeks(new String[]{mainTimeStr.substring(0, mainTimeStr.indexOf("周")).trim()});
                    }

                    mainTimeStr = mainTimeStr.substring(mainTimeStr.lastIndexOf("周") + 1);
                    String weekDay = mainTimeStr.substring(0, 1);
                    courseDetail.setWeekDay(Integer.valueOf(weekDay));

                    String courseTime = mainTimeStr.substring(2, mainTimeStr.indexOf("节"));
                    if (courseTime.contains(",")) {
                        courseDetail.setCourseTime(courseTime.split(","));
                    } else {
                        courseDetail.setCourseTime(new String[]{courseTime.trim()});
                    }

                    courseDetail_list[detail_count++] = courseDetail;
                    courseDetail = null;
                }
            } else {
                break;
            }
        }
        return courseDetail_list;
    }
}
