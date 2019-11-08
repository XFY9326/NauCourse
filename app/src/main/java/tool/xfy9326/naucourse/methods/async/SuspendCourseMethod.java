package tool.xfy9326.naucourse.methods.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.beans.course.SuspendCourse;
import tool.xfy9326.naucourse.methods.io.DataMethod;
import tool.xfy9326.naucourse.methods.net.NetMethod;

public class SuspendCourseMethod extends BaseInfoMethod<SuspendCourse> {
    public static final String FILE_NAME = SuspendCourse.class.getSimpleName();
    public static final boolean IS_ENCRYPT = false;
    private static final String URL = "http://jwc.nau.edu.cn/SuspendCourseInfo.aspx";
    private Document document;

    public SuspendCourseMethod(@NonNull Context context) {
        super(context);
    }

    @Override
    public int load() throws Exception {
        String data = NetMethod.loadUrl(context, URL);
        System.gc();
        if (data != null) {
            this.document = Jsoup.parse(data);
            return Config.NET_WORK_GET_SUCCESS;
        }
        return Config.NET_WORK_ERROR_CODE_GET_DATA_ERROR;
    }

    public void saveTemp() {
        getData(false);
    }

    @SuppressWarnings({"ToArrayCallWithZeroLengthArrayArgument"})
    @Nullable
    @Override
    public SuspendCourse getData(boolean checkTemp) {
        SuspendCourse suspendCourse = new SuspendCourse();

        if (document != null) {
            Element elementInfo = document.getElementById("Term");
            suspendCourse.setTerm(elementInfo.text());
            elementInfo = document.getElementById("StartDate");
            suspendCourse.setTermStart(elementInfo.text());
            elementInfo = document.getElementById("EndDate");
            suspendCourse.setTermEnd(elementInfo.text());

            ArrayList<String> name = new ArrayList<>();
            ArrayList<String> course = new ArrayList<>();
            ArrayList<String> teacher = new ArrayList<>();

            ArrayList<String[]> detailTypeArr = new ArrayList<>();
            ArrayList<String[]> detailClassArr = new ArrayList<>();
            ArrayList<String[]> detailLocationArr = new ArrayList<>();
            ArrayList<String[]> detailDateArr = new ArrayList<>();

            ArrayList<String> detailType = new ArrayList<>();
            ArrayList<String> detailClass = new ArrayList<>();
            ArrayList<String> detailLocation = new ArrayList<>();
            ArrayList<String> detailDate = new ArrayList<>();

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
                                    detailTypeArr.add(detailType.toArray(new String[detailType.size()]));
                                    detailClassArr.add(detailClass.toArray(new String[detailClass.size()]));
                                    detailLocationArr.add(detailLocation.toArray(new String[detailLocation.size()]));
                                    detailDateArr.add(detailDate.toArray(new String[detailDate.size()]));

                                    detailType.clear();
                                    detailClass.clear();
                                    detailLocation.clear();
                                    detailDate.clear();

                                    subjectLine = 0;
                                    detailLine = 0;
                                } else {
                                    detailType.add(element.text());
                                    detailLine++;
                                }
                                break;
                            case 1:
                                detailClass.add(element.text());
                                detailLine++;
                                break;
                            case 2:
                                detailLocation.add(element.text());
                                detailLine++;
                                break;
                            case 3:
                                detailDate.add(element.text());
                                detailLine = 0;
                                break;
                            default:
                        }
                        subjectLine++;
                        break;
                }
            }

            if (detailType.size() > 0) {
                detailTypeArr.add(detailType.toArray(new String[detailType.size()]));
                detailClassArr.add(detailClass.toArray(new String[detailClass.size()]));
                detailLocationArr.add(detailLocation.toArray(new String[detailLocation.size()]));
                detailDateArr.add(detailDate.toArray(new String[detailDate.size()]));
            }

            suspendCourse.setName(name.toArray(new String[name.size()]));
            suspendCourse.setCourse(course.toArray(new String[course.size()]));
            suspendCourse.setTeacher(teacher.toArray(new String[teacher.size()]));
            suspendCourse.setCount(itemCount);

            suspendCourse.setDetail_type(detailTypeArr.toArray(new String[detailTypeArr.size()][]));
            suspendCourse.setDetail_class(detailClassArr.toArray(new String[detailClassArr.size()][]));
            suspendCourse.setDetail_location(detailLocationArr.toArray(new String[detailLocationArr.size()][]));
            suspendCourse.setDetail_date(detailDateArr.toArray(new String[detailDateArr.size()][]));

            suspendCourse.setDataVersionCode(Config.DATA_VERSION_SUSPEND_COURSE);

            if (DataMethod.saveOfflineData(context, suspendCourse, FILE_NAME, checkTemp, IS_ENCRYPT)) {
                return suspendCourse;
            } else {
                return null;
            }
        }
        return null;
    }

}
