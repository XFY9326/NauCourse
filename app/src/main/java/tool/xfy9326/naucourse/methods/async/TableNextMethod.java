package tool.xfy9326.naucourse.methods.async;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import lib.xfy9326.nausso.NauSSOClient;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.beans.course.Course;
import tool.xfy9326.naucourse.beans.course.CourseDetail;
import tool.xfy9326.naucourse.methods.BaseMethod;
import tool.xfy9326.naucourse.methods.net.NetMethod;

public class TableNextMethod extends BaseNetMethod {
    @Nullable
    private Document document;

    public TableNextMethod(@NonNull Context context) {
        super(context);
    }

    @Override
    public int load() throws Exception {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(Config.PREFERENCE_HAS_LOGIN, Config.DEFAULT_PREFERENCE_HAS_LOGIN)) {
            String data = NetMethod.loadUrlFromLoginClient(context, NauSSOClient.JWC_SERVER_URL + "/Students/MyCourseScheduleTableNext.aspx", true);
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

    public ArrayList<Course> getData() {
        ArrayList<Course> courseList = new ArrayList<>();
        if (document != null) {
            boolean startCourse = false;
            Elements tags = document.body().getElementById("content").getElementsByTag("tr");
            for (Element element : tags) {
                String courseStr = element.text();
                Elements detailElements = element.getElementsByTag("td");
                //详细信息不足
                if (detailElements != null && detailElements.size() > 2 && detailElements.size() < 10) {
                    courseList = null;
                    break;
                }
                if (courseStr.contains("序号")) {
                    startCourse = true;
                } else if (startCourse && courseStr.contains(" ") && courseStr.contains("上课地点") && courseStr.contains("上课时间")) {
                    String[] data = courseStr.split(" ");
                    Course course = new Course();
                    course.setCourseTerm(data[data.length - 1]);

                    course.setCourseId(data[1]);
                    //可能会出问题的解决课程名称空格的方式
                    int i = 3;
                    for (; !data[i].contains("."); i++) {
                        data[2] += " " + data[i];
                    }
                    course.setCourseName(data[2].trim());
                    //无教学班信息
                    course.setCourseScore(data[i]);
                    course.setCourseCombinedClass(data[i + 1]);
                    course.setCourseType(data[i + 3]);
                    course.setCourseTeacher(data[i + 4]);

                    CourseDetail[] courseDetails = getCourseDetailList(Arrays.copyOfRange(data, i + 5, data.length - 1), (data.length - 1 - i - 5) / 2);
                    if (courseDetails == null) {
                        courseList = null;
                        continue;
                    }
                    course.setCourseDetail(courseDetails);

                    //颜色随机
                    int[] colorList = BaseMethod.getColorArray(context);
                    Random random = new Random();
                    int num = random.nextInt(colorList.length) % (colorList.length + 1);
                    course.setCourseColor(colorList[num]);

                    if (courseList != null) {
                        courseList.add(course);
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

    private CourseDetail[] getCourseDetailList(String[] data, int detailLength) {
        int detailCount = 0;
        CourseDetail[] courseDetailList = new CourseDetail[detailLength];
        CourseDetail courseDetail = null;
        for (String datum : data) {
            if (courseDetail == null) {
                courseDetail = new CourseDetail();
            }
            if (datum.contains("上课地点")) {
                if (datum.length() > 5) {
                    courseDetail.setLocation(datum.substring(5));
                }
            } else if (datum.contains("上课时间")) {
                if (datum.length() > 5) {
                    String mainTimeStr = datum.substring(5).trim();
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

                    courseDetailList[detailCount++] = courseDetail;
                    courseDetail = null;
                }
            } else {
                break;
            }
        }
        return courseDetailList;
    }
}
