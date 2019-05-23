package tool.xfy9326.naucourse.methods.netInfoMethods;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.methods.BaseMethod;
import tool.xfy9326.naucourse.methods.NetMethod;
import tool.xfy9326.naucourse.utils.Course;
import tool.xfy9326.naucourse.utils.CourseDetail;
import tool.xfy9326.naucourse.utils.CourseSearchDetail;
import tool.xfy9326.naucourse.utils.CourseSearchInfo;

public class CourseSearchMethod {
    private static final String PAGE_URL = "http://jwc.nau.edu.cn/coursearrangeInfosearch.aspx";
    private static final String SEARCH_URL = "http://jwc.nau.edu.cn/GetCourseArrangeInfo.ashx";
    private static final String CLASS_NAME_URL = "http://jwc.nau.edu.cn/GetClassNameListByTerm.ashx";
    private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();
    private Document document;
    private final OkHttpClient client;

    public CourseSearchMethod() {
        OkHttpClient.Builder client_builder = new OkHttpClient.Builder();
        client_builder.cookieJar(new CookieJar() {
            @Override
            public void saveFromResponse(HttpUrl httpUrl, List<Cookie> list) {
                cookieStore.put(httpUrl.host(), list);
            }

            @Override
            public List<Cookie> loadForRequest(HttpUrl httpUrl) {
                List<Cookie> cookies = cookieStore.get(httpUrl.host());
                return cookies != null ? cookies : new ArrayList<>();
            }
        });
        client = client_builder.build();
    }

    @NonNull
    public static Course convertToCourse(Context context, CourseSearchDetail courseSearchDetail) {
        Course course = new Course();
        CourseDetail courseDetail = new CourseDetail();
        course.setCourseName(courseSearchDetail.getName());
        course.setCourseClass(courseSearchDetail.getClassName());
        course.setCourseCombinedClass(courseSearchDetail.getCombinedClassName());
        course.setCourseScore(courseSearchDetail.getScore());
        course.setCourseTeacher(courseSearchDetail.getTeacher());
        course.setCourseTerm(courseSearchDetail.getTerm());

        course.setCourseColor(BaseMethod.getRandomColor(context));
        String id = Config.SEARCH_COURSE_PREFIX + "-"
                + courseSearchDetail.getTerm()
                + courseSearchDetail.getTeacherId()
                + courseSearchDetail.getWeekDay()
                + courseSearchDetail.getStartCourseTime()
                + courseSearchDetail.getEndCourseTime();
        course.setCourseId(id);

        courseDetail.setWeekDay(courseSearchDetail.getWeekDay());
        courseDetail.setWeekMode(courseSearchDetail.getWeekMode());
        courseDetail.setLocation(courseSearchDetail.getRoomName());
        courseDetail.setCourseTime(new String[]{courseSearchDetail.getStartCourseTime() + "-" + courseSearchDetail.getEndCourseTime()});
        courseDetail.setWeeks(courseSearchDetail.getWeeks());
        course.setCourseDetail(new CourseDetail[]{courseDetail});

        return course;
    }

    public int load() throws Exception {
        HashMap<String, String> head = new HashMap<>();
        head.put("Referer", "http://jwc.nau.edu.cn/Login.aspx");
        String data = NetMethod.loadUrl(client, PAGE_URL, head);
        System.gc();
        if (data != null) {
            this.document = Jsoup.parse(data);
            return Config.NET_WORK_GET_SUCCESS;
        }
        return Config.NET_WORK_ERROR_CODE_GET_DATA_ERROR;
    }

    @Nullable
    public LinkedHashMap<String, String> getSearchTypeList() {
        if (document != null) {
            LinkedHashMap<String, String> hashMap = new LinkedHashMap<>();
            Element element = document.body().getElementsByAttributeValue("name", "SearchType").get(0);
            Elements elements = element.getElementsByTag("option");
            for (Element option : elements) {
                String value = option.attr("value");
                String text = option.text();
                if (value != null && !value.isEmpty() && text != null && !text.isEmpty()) {
                    hashMap.put(value, text);
                }
            }
            return hashMap;
        }
        return null;
    }

    @Nullable
    public List<String> getTermList() {
        return getOptionList("Term");
    }

    @Nullable
    public List<String> getRoomList() {
        return getOptionList("roomList");
    }

    @Nullable
    public List<String> getDeptList() {
        return getOptionList("deptList");
    }

    @Nullable
    private List<String> getOptionList(String optionId) {
        if (document != null) {
            ArrayList<String> list = new ArrayList<>();
            Element element = document.body().getElementById(optionId);
            Elements elements = element.getElementsByTag("option");
            for (Element option : elements) {
                String value = option.attr("value");
                if (value != null && !value.isEmpty()) {
                    list.add(value);
                }
            }
            return list;
        }
        return null;
    }

    @Nullable
    private String getTK() {
        if (document != null) {
            Element div = document.body().getElementById("token");
            return div.text();
        }
        return null;
    }

    @Nullable
    public List<String> getClassNameList(String term) {
        ArrayList<String> classNameList = new ArrayList<>();
        String data;
        try {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("term", term);
            HashMap<String, String> head = new HashMap<>();
            head.put("Referer", "http://jwc.nau.edu.cn/coursearrangeInfosearch.aspx");
            head.put("Origin", "http://jwc.nau.edu.cn");
            data = NetMethod.postUrl(client, CLASS_NAME_URL, hashMap, head);
            System.gc();
            if (data == null) {
                return null;
            }
            JSONArray jsonArray = new JSONArray(data);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject != null && jsonObject.has("ClassName")) {
                    classNameList.add(jsonObject.getString("ClassName"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return classNameList;
    }

    @Nullable
    public List<CourseSearchDetail> getCourseSearchDetail(CourseSearchInfo courseSearchInfo) {
        ArrayList<CourseSearchDetail> courseSearchDetails = new ArrayList<>();
        String tk = getTK();
        if (tk == null) {
            return null;
        }
        String data;
        try {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("term", courseSearchInfo.getTerm());
            hashMap.put("searchType", courseSearchInfo.getSearchType());
            hashMap.put("value", courseSearchInfo.getValue());
            hashMap.put("tk", tk);

            HashMap<String, String> head = new HashMap<>();
            head.put("Referer", "http://jwc.nau.edu.cn/coursearrangeInfosearch.aspx");
            head.put("Origin", "http://jwc.nau.edu.cn");
            data = NetMethod.postUrl(client, SEARCH_URL, hashMap, head);
            System.gc();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        try {
            JSONArray jsonArray = new JSONArray(data);
            for (int i = 0; i < jsonArray.length(); i++) {
                CourseSearchDetail courseSearchDetail = new CourseSearchDetail();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject != null) {
                    courseSearchDetail.setClassName(jsonObject.getString("ClassName"));
                    courseSearchDetail.setWeekDay(Integer.parseInt(jsonObject.getString("Week")));
                    courseSearchDetail.setStartCourseTime(Integer.parseInt(jsonObject.getString("Bjc")));
                    courseSearchDetail.setEndCourseTime(Integer.parseInt(jsonObject.getString("Ejc")));
                    courseSearchDetail.setName(jsonObject.getString("CourseName"));
                    courseSearchDetail.setScore(jsonObject.getString("Credit"));
                    courseSearchDetail.setRoomName(jsonObject.getString("RoomName"));
                    courseSearchDetail.setTeacher(jsonObject.getString("TeacName"));
                    courseSearchDetail.setTeacherGrade(jsonObject.getString("ProfessionPosition"));
                    courseSearchDetail.setCombinedClassName(jsonObject.getString("TeacClass"));
                    courseSearchDetail.setStuNum(Integer.parseInt(jsonObject.getString("StuNumber")));
                    courseSearchDetail.setCollage(jsonObject.getString("TeachingDept"));
                    courseSearchDetail.setTeacherId(jsonObject.getString("TeacID"));
                    courseSearchDetail.setWeekDes(jsonObject.getString("WeekChiDes"));
                    courseSearchDetail.setTerm(courseSearchInfo.getTerm());

                    String courseTime = jsonObject.getString("WeekChiDes");
                    if (courseTime.contains("单")) {
                        courseSearchDetail.setWeekMode(Config.COURSE_DETAIL_WEEKMODE_SINGLE);
                        courseSearchDetail.setWeeks(new String[]{courseTime.substring(0, courseTime.indexOf("之"))});
                    } else if (courseTime.contains("双")) {
                        courseSearchDetail.setWeekMode(Config.COURSE_DETAIL_WEEKMODE_DOUBLE);
                        courseSearchDetail.setWeeks(new String[]{courseTime.substring(0, courseTime.indexOf("之"))});
                    } else if (courseTime.trim().startsWith("第")) {
                        courseSearchDetail.setWeekMode(Config.COURSE_DETAIL_WEEKMODE_ONCE_MORE);
                        String weekStr = courseTime.substring(1, courseTime.indexOf("周")).trim();
                        if (weekStr.contains(",")) {
                            String[] weekArr = weekStr.split(",");
                            courseSearchDetail.setWeeks(weekArr);
                        } else {
                            courseSearchDetail.setWeeks(new String[]{weekStr});
                        }
                    } else {
                        courseSearchDetail.setWeekMode(Config.COURSE_DETAIL_WEEKMODE_ONCE);
                        courseSearchDetail.setWeeks(new String[]{courseTime.substring(0, courseTime.indexOf("周")).trim()});
                    }
                }
                courseSearchDetails.add(courseSearchDetail);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return courseSearchDetails;
    }

}
