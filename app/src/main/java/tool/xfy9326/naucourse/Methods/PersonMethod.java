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
import tool.xfy9326.naucourse.Utils.StudentLearnProcess;
import tool.xfy9326.naucourse.Utils.StudentScore;

/**
 * Created by xfy9326 on 18-2-21.
 * 学生信息获取方法
 */

public class PersonMethod {
    public static final String FILE_NAME_SCORE = "StudentScore";
    public static final String FILE_NAME_PROCESS = "StudentLearnProcess";
    public static final String FILE_NAME_DATA = "StudentInfo";
    private final Context context;
    private Document document;

    public PersonMethod(Context context) {
        this.context = context;
        this.document = null;
    }

    public int load() throws Exception {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(Config.PREFERENCE_HAS_LOGIN, Config.DEFAULT_PREFERENCE_HAS_LOGIN)) {
            String data = LoginMethod.getData(context, "/Students/StudentIndex.aspx", true);
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

    public void saveScoreTemp() {
        getUserScore(false);
    }

    public StudentLearnProcess getUserProcess(boolean checkTemp) {
        StudentLearnProcess studentLearnProcess = new StudentLearnProcess();
        Elements tags = document.body().getElementsByTag("tr");
        List<String> data = tags.eachText();
        for (String str : data) {
            if (str.contains("必修课: 目标学分：")) {
                str = str.replace("必修课: ", "");
                studentLearnProcess.setScoreBXAim(str.substring(str.indexOf("：") + 1, str.indexOf("，")).trim());
                str = str.substring(str.indexOf("，") + 1, str.length());
                studentLearnProcess.setScoreBXNow(str.substring(str.indexOf("：") + 1, str.indexOf("，")).trim());
                str = str.substring(str.indexOf("，") + 1, str.length());
                studentLearnProcess.setScoreBXStill(str.substring(str.indexOf("：") + 1, str.length()).trim());
            } else if (str.contains("专选课: 目标学分：")) {
                str = str.replace("专选课: ", "");
                studentLearnProcess.setScoreZXAim(str.substring(str.indexOf("：") + 1, str.indexOf("，")).trim());
                str = str.substring(str.indexOf("，") + 1, str.length());
                studentLearnProcess.setScoreZXNow(str.substring(str.indexOf("：") + 1, str.indexOf("，")).trim());
                str = str.substring(str.indexOf("，") + 1, str.length());
                studentLearnProcess.setScoreZXStill(str.substring(str.indexOf("：") + 1, str.length()).trim());
            } else if (str.contains("任选课: 目标学分：")) {
                str = str.replace("任选课: ", "");
                studentLearnProcess.setScoreRXAim(str.substring(str.indexOf("：") + 1, str.indexOf("，")).trim());
                str = str.substring(str.indexOf("，") + 1, str.length());
                studentLearnProcess.setScoreRXNow(str.substring(str.indexOf("：") + 1, str.indexOf("，")).trim());
                str = str.substring(str.indexOf("，") + 1, str.length());
                studentLearnProcess.setScoreRXAward(str.substring(str.indexOf("：") + 1, str.indexOf("，")).trim());
                str = str.substring(str.indexOf("，") + 1, str.length());
                studentLearnProcess.setScoreRXStill(str.substring(str.indexOf("：") + 1, str.length()).trim());
            } else if (str.contains("实践课: 目标学分：")) {
                str = str.replace("实践课: ", "");
                studentLearnProcess.setScoreSJAim(str.substring(str.indexOf("：") + 1, str.indexOf("，")).trim());
                str = str.substring(str.indexOf("，") + 1, str.length());
                studentLearnProcess.setScoreSJNow(str.substring(str.indexOf("：") + 1, str.indexOf("，")).trim());
                str = str.substring(str.indexOf("，") + 1, str.length());
                studentLearnProcess.setScoreSJStill(str.substring(str.indexOf("：") + 1, str.length()).trim());
                break;
            }
        }
        if (BaseMethod.saveOfflineData(context, studentLearnProcess, FILE_NAME_PROCESS, checkTemp)) {
            return studentLearnProcess;
        } else {
            return null;
        }
    }

    public StudentScore getUserScore(boolean checkTemp) {
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
        if (BaseMethod.saveOfflineData(context, studentScore, FILE_NAME_SCORE, checkTemp)) {
            return studentScore;
        } else {
            return null;
        }
    }

    public StudentInfo getUserData(boolean checkTemp) {
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
        if (BaseMethod.saveOfflineData(context, studentInfo, FILE_NAME_DATA, checkTemp)) {
            return studentInfo;
        } else {
            return null;
        }
    }

}
