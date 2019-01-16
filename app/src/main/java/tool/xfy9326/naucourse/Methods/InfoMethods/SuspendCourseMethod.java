package tool.xfy9326.naucourse.Methods.InfoMethods;

import android.content.Context;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.DataMethod;
import tool.xfy9326.naucourse.Methods.NetMethod;
import tool.xfy9326.naucourse.Utils.SuspendCourse;

public class SuspendCourseMethod {
    public static final String FILE_NAME = "SuspendCourse";
    private static final String URL = "http://jwc.nau.edu.cn/SuspendCourseInfo.aspx";
    private final Context context;
    private Document document;

    public SuspendCourseMethod(Context context) {
        this.context = context;
    }

    public int load() throws Exception {
        String data = NetMethod.loadUrl(URL);
        System.gc();
        if (data != null) {
            this.document = Jsoup.parse(data);
            return Config.NET_WORK_GET_SUCCESS;
        }
        return Config.NET_WORK_ERROR_CODE_GET_DATA_ERROR;
    }

    public void saveTemp() {
        getSuspendCourse(false);
    }

    @SuppressWarnings({"ToArrayCallWithZeroLengthArrayArgument", "ConstantConditions"})
    public SuspendCourse getSuspendCourse(boolean checkTemp) {
        SuspendCourse suspendCourse = new SuspendCourse();

        if (document != null) {
            Element element_info = document.getElementById("Term");
            suspendCourse.setTerm(element_info.text());
            element_info = document.getElementById("StartDate");
            suspendCourse.setTermStart(element_info.text());
            element_info = document.getElementById("EndDate");
            suspendCourse.setTermEnd(element_info.text());

            ArrayList<String> name = new ArrayList<>();
            ArrayList<String> course = new ArrayList<>();
            ArrayList<String> teacher = new ArrayList<>();

            ArrayList<String[]> detail_type_arr = new ArrayList<>();
            ArrayList<String[]> detail_class_arr = new ArrayList<>();
            ArrayList<String[]> detail_location_arr = new ArrayList<>();
            ArrayList<String[]> detail_date_arr = new ArrayList<>();

            ArrayList<String> detail_type = new ArrayList<>();
            ArrayList<String> detail_class = new ArrayList<>();
            ArrayList<String> detail_location = new ArrayList<>();
            ArrayList<String> detail_date = new ArrayList<>();

            Elements elements = document.getElementsByTag("td");

            int itemCount = 0;
            int subjectLine = 0;
            int detailLine = 0;
            for (Element element : elements) {
                switch (subjectLine) {
                    case 0:
                        subjectLine++;
                        continue;
                    case 1:
                        name.add(element.text());
                        itemCount++;
                        subjectLine++;
                        break;
                    case 2:
                        course.add(element.text());
                        subjectLine++;
                        break;
                    case 3:
                        teacher.add(element.text());
                        subjectLine++;
                        break;
                    default:

                        switch (detailLine) {
                            case 0:
                                if (element.hasAttr("rowspan")) {
                                    detail_type_arr.add(detail_type.toArray(new String[detail_type.size()]));
                                    detail_class_arr.add(detail_class.toArray(new String[detail_class.size()]));
                                    detail_location_arr.add(detail_location.toArray(new String[detail_location.size()]));
                                    detail_date_arr.add(detail_date.toArray(new String[detail_date.size()]));

                                    detail_type.clear();
                                    detail_class.clear();
                                    detail_location.clear();
                                    detail_date.clear();

                                    subjectLine = 0;
                                    detailLine = 0;
                                } else {
                                    detail_type.add(element.text());
                                    detailLine++;
                                }
                                break;
                            case 1:
                                detail_class.add(element.text());
                                detailLine++;
                                break;
                            case 2:
                                detail_location.add(element.text());
                                detailLine++;
                                break;
                            case 3:
                                detail_date.add(element.text());
                                detailLine = 0;
                                break;
                        }
                        subjectLine++;
                        break;
                }
            }

            if (detail_type.size() > 0) {
                detail_type_arr.add(detail_type.toArray(new String[detail_type.size()]));
                detail_class_arr.add(detail_class.toArray(new String[detail_class.size()]));
                detail_location_arr.add(detail_location.toArray(new String[detail_location.size()]));
                detail_date_arr.add(detail_date.toArray(new String[detail_date.size()]));
            }

            suspendCourse.setName(name.toArray(new String[name.size()]));
            suspendCourse.setCourse(course.toArray(new String[course.size()]));
            suspendCourse.setTeacher(teacher.toArray(new String[teacher.size()]));
            suspendCourse.setCount(itemCount);

            suspendCourse.setDetail_type(detail_type_arr.toArray(new String[detail_type_arr.size()][]));
            suspendCourse.setDetail_class(detail_class_arr.toArray(new String[detail_class_arr.size()][]));
            suspendCourse.setDetail_location(detail_location_arr.toArray(new String[detail_location_arr.size()][]));
            suspendCourse.setDetail_date(detail_date_arr.toArray(new String[detail_date_arr.size()][]));

            suspendCourse.setDataVersionCode(Config.DATA_VERSION_SUSPEND_COURSE);

            if (DataMethod.saveOfflineData(context, suspendCourse, FILE_NAME, checkTemp)) {
                return suspendCourse;
            } else {
                return null;
            }
        }
        return null;
    }

}
