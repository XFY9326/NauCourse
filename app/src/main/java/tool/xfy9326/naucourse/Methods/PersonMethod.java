package tool.xfy9326.naucourse.Methods;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.List;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Utils.StudentInfo;
import tool.xfy9326.naucourse.Utils.StudentScore;

/**
 * Created by xfy9326 on 18-2-21.
 * 学生信息获取方法
 */

public class PersonMethod {
    public static final String FILE_NAME_SCORE = "StudentScore";
    public static final String FILE_NAME_DATA = "StudentInfo";
    private final Context context;
    private Document document;

    public PersonMethod(Context context) {
        this.context = context;
        this.document = null;
    }

    public boolean load() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(Config.PREFERENCE_HAS_LOGIN, Config.DEFAULT_PREFERENCE_HAS_LOGIN)) {
            String data = LoginMethod.getData(context, "/Students/StudentIndex.aspx");
            if (data != null) {
                document = Jsoup.parse(data);
                return true;
            }
        }
        return false;
    }

    public StudentScore getUserScore() {
        boolean nextScore = false;
        StudentScore studentScore = new StudentScore();
        Elements tags = document.body().getElementsByTag("tr");
        List<String> data = tags.eachText();
        for (String str : data) {
            if (str.contains("学分绩点") && str.contains("必修学分")) {
                nextScore = true;
                continue;
            }
            if (nextScore) {
                String[] scores = str.split(" ");
                studentScore.setScoreXF(scores[6]);
                studentScore.setScoreJD(scores[8]);
                break;
            }
        }
        tags = document.body().getElementsByTag("span");
        data = tags.eachText();
        for (String str : data) {
            if (str.contains("同年级排名：")) {
                studentScore.setScoreNP(str.substring(6).trim());
                continue;
            }
            if (str.contains("同专业排名：")) {
                studentScore.setScoreZP(str.substring(6).trim());
                continue;
            }
            if (str.contains("同班级排名：")) {
                studentScore.setScoreBP(str.substring(6).trim());
            }
        }
        BaseMethod.saveOfflineData(context, studentScore, FILE_NAME_SCORE);
        return studentScore;
    }

    public StudentInfo getUserData() {
        StudentInfo studentInfo = new StudentInfo();
        Elements tags = document.body().getElementsByTag("tr");
        List<String> data = tags.eachText();
        for (String str : data) {
            if (str.contains("学生学号：")) {
                studentInfo.setStd_id(str.substring(5).trim());
                continue;
            }
            if (str.contains("学生姓名：")) {
                studentInfo.setStd_name(str.substring(5, str.indexOf("【")).trim());
                continue;
            }
            if (str.contains("所在年级：")) {
                studentInfo.setStd_grade(str.substring(5).trim() + "级");
                continue;
            }
            if (str.contains("学院归属：")) {
                studentInfo.setStd_collage(str.substring(5, str.indexOf("【")).trim());
                continue;
            }
            if (str.contains("专业信息：")) {
                studentInfo.setStd_major(str.substring(5, str.indexOf("【")).trim());
                continue;
            }
            if (str.contains("专业方向：")) {
                studentInfo.setStd_direction(str.substring(5).trim());
                continue;
            }
            if (str.contains("当前班级：")) {
                studentInfo.setStd_class(str.substring(str.indexOf("级", 5) + 1).trim());
            }
        }
        BaseMethod.saveOfflineData(context, studentInfo, FILE_NAME_DATA);
        return studentInfo;
    }

}
