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
import tool.xfy9326.naucourse.Utils.Exam;

/**
 * Created by 10696 on 2018/3/3.
 * 获取考试信息
 */

public class ExamMethod {
    public static final String FILE_NAME = "Exam";
    private final Context context;
    private Document document;

    public ExamMethod(Context context) {
        this.context = context;
        this.document = null;
    }

    public int load() throws Exception {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(Config.PREFERENCE_HAS_LOGIN, Config.DEFAULT_PREFERENCE_HAS_LOGIN)) {
            String data = LoginMethod.getData(context, "/Students/MyExamArrangeList.aspx", true);
            if (data != null) {
                document = Jsoup.parse(data);
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

    public void saveTemp() {
        getExam(false);
    }

    @SuppressWarnings("SameParameterValue")
    public Exam getExam(boolean checkTemp) {
        ArrayList<String> examId = new ArrayList<>();
        ArrayList<String> examName = new ArrayList<>();
        ArrayList<String> examType = new ArrayList<>();
        ArrayList<String> examTime = new ArrayList<>();
        ArrayList<String> examLocation = new ArrayList<>();

        Exam exam = new Exam();
        Elements elements = document.body().getElementsByTag("tr");
        List<String> text = elements.eachText();
        boolean startData = false;
        for (String str : text) {
            if (str.contains("序号")) {
                startData = true;
                continue;
            }
            if (startData) {
                String[] data = str.trim().split(" ");
                examId.add(data[1]);
                examName.add(data[2]);
                examType.add(data[8]);
                examTime.add(data[5]);
                examLocation.add(data[6]);
            }
        }

        exam.setExamMount(examId.size());
        exam.setExamId(examId.toArray(new String[]{}));
        exam.setExamTime(examTime.toArray(new String[]{}));
        exam.setExamName(examName.toArray(new String[]{}));
        exam.setExamType(examType.toArray(new String[]{}));
        exam.setExamLocation(examLocation.toArray(new String[]{}));

        if (BaseMethod.saveOfflineData(context, exam, FILE_NAME, checkTemp)) {
            return exam;
        } else {
            return null;
        }
    }
}
