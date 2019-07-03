package tool.xfy9326.naucourse.methods.netInfoMethods;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.List;
import java.util.Objects;

import lib.xfy9326.nausso.NauSSOClient;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.methods.DataMethod;
import tool.xfy9326.naucourse.methods.ImageMethod;
import tool.xfy9326.naucourse.methods.NetMethod;
import tool.xfy9326.naucourse.utils.StudentInfo;
import tool.xfy9326.naucourse.utils.StudentLearnProcess;
import tool.xfy9326.naucourse.utils.StudentScore;

/**
 * Created by xfy9326 on 18-2-21.
 * 学生信息获取方法
 */

public class PersonMethod extends BaseNetMethod {
    public static final String FILE_NAME_SCORE = "StudentScore";
    public static final String FILE_NAME_PROCESS = "StudentLearnProcess";
    public static final String FILE_NAME_DATA = "StudentInfo";
    public static final boolean IS_SCORE_ENCRYPT = true;
    public static final boolean IS_PROCESS_ENCRYPT = true;
    public static final boolean IS_DATA_ENCRYPT = true;
    private static final String stu_photo_url = "http://jwc.nau.edu.cn/Students/StuPhotoView.ashx?t=1";
    @Nullable
    private Document document;

    public PersonMethod(@NonNull Context context) {
        super(context);
    }

    private static String getDetailMsg(String str) {
        return str.substring(5, str.indexOf("【")).trim();
    }

    @Override
    public int load() throws Exception {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(Config.PREFERENCE_HAS_LOGIN, Config.DEFAULT_PREFERENCE_HAS_LOGIN)) {
            String data = NetMethod.loadUrlFromLoginClient(context, NauSSOClient.JWC_SERVER_URL + "/Students/StudentIndex.aspx", true);
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

    public void saveScoreTemp() {
        getUserScore(false);
        getUserProcess(false);
        getStuPhoto(true);
    }

    public void getStuPhoto(boolean forceRefresh) {
        boolean getPhoto = false;
        File file = new File(ImageMethod.getStuPhotoPath(context));
        if (!file.exists() || forceRefresh) {
            getPhoto = true;
        }
        if (getPhoto) {
            try {
                ImageMethod.downloadImage(context, stu_photo_url, ImageMethod.getStuPhotoPath(context), true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Nullable
    public StudentLearnProcess getUserProcess(boolean checkTemp) {
        StudentLearnProcess studentLearnProcess = new StudentLearnProcess();
        Elements tags = Objects.requireNonNull(document).body().getElementsByTag("tr");
        List<String> data = tags.eachText();
        for (String str : data) {
            if (str.contains("必修课: 目标学分：")) {
                str = str.replace("必修课: ", "");
                studentLearnProcess.setScoreBXAim(str.substring(str.indexOf("：") + 1, str.indexOf("，")).trim());
                str = str.substring(str.indexOf("，") + 1);
                studentLearnProcess.setScoreBXNow(str.substring(str.indexOf("：") + 1, str.indexOf("，")).trim());
                str = str.substring(str.indexOf("，") + 1);
                studentLearnProcess.setScoreBXStill(str.substring(str.indexOf("：") + 1).trim());
            } else if (str.contains("专选课: 目标学分：")) {
                str = str.replace("专选课: ", "");
                studentLearnProcess.setScoreZXAim(str.substring(str.indexOf("：") + 1, str.indexOf("，")).trim());
                str = str.substring(str.indexOf("，") + 1);
                studentLearnProcess.setScoreZXNow(str.substring(str.indexOf("：") + 1, str.indexOf("，")).trim());
                str = str.substring(str.indexOf("，") + 1);
                studentLearnProcess.setScoreZXStill(str.substring(str.indexOf("：") + 1).trim());
            } else if (str.contains("任选课: 目标学分：")) {
                str = str.replace("任选课: ", "");
                studentLearnProcess.setScoreRXAim(str.substring(str.indexOf("：") + 1, str.indexOf("，")).trim());
                str = str.substring(str.indexOf("，") + 1);
                studentLearnProcess.setScoreRXNow(str.substring(str.indexOf("：") + 1, str.indexOf("，")).trim());
                str = str.substring(str.indexOf("，") + 1);
                studentLearnProcess.setScoreRXAward(str.substring(str.indexOf("：") + 1, str.indexOf("，")).trim());
                str = str.substring(str.indexOf("，") + 1);
                studentLearnProcess.setScoreRXStill(str.substring(str.indexOf("：") + 1).trim());
            } else if (str.contains("实践课: 目标学分：")) {
                str = str.replace("实践课: ", "");
                studentLearnProcess.setScoreSJAim(str.substring(str.indexOf("：") + 1, str.indexOf("，")).trim());
                str = str.substring(str.indexOf("，") + 1);
                studentLearnProcess.setScoreSJNow(str.substring(str.indexOf("：") + 1, str.indexOf("，")).trim());
                str = str.substring(str.indexOf("，") + 1);
                studentLearnProcess.setScoreSJStill(str.substring(str.indexOf("：") + 1).trim());
                break;
            }
        }
        studentLearnProcess.setDataVersionCode(Config.DATA_VERSION_STUDENT_LEARN_PROCESS);
        if (DataMethod.saveOfflineData(context, studentLearnProcess, FILE_NAME_PROCESS, checkTemp, IS_PROCESS_ENCRYPT)) {
            return studentLearnProcess;
        } else {
            return null;
        }
    }

    @Nullable
    public StudentScore getUserScore(boolean checkTemp) {
        boolean nextScore = false;
        StudentScore studentScore = new StudentScore();
        Elements tags = Objects.requireNonNull(document).body().getElementsByTag("tr");
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
                break;
            }
        }

        studentScore.setDataVersionCode(Config.DATA_VERSION_STUDENT_SCORE);
        if (DataMethod.saveOfflineData(context, studentScore, FILE_NAME_SCORE, checkTemp, IS_SCORE_ENCRYPT)) {
            return studentScore;
        } else {
            return null;
        }
    }

    @Nullable
    public StudentInfo getUserData(boolean checkTemp) {
        StudentInfo studentInfo = new StudentInfo();
        Elements tags = Objects.requireNonNull(document).body().getElementsByTag("tr");
        List<String> data = tags.eachText();
        for (String str : data) {
            if (str.contains("学生学号：")) {
                studentInfo.setStd_id(str.substring(5).trim());
                continue;
            }
            if (str.contains("学生姓名：")) {
                studentInfo.setStd_name(getDetailMsg(str));
                continue;
            }
            if (str.contains("所在年级：")) {
                studentInfo.setStd_grade(str.substring(5).trim() + "级");
                continue;
            }
            if (str.contains("学院归属：")) {
                studentInfo.setStd_collage(getDetailMsg(str));
                continue;
            }
            if (str.contains("专业信息：")) {
                studentInfo.setStd_major(getDetailMsg(str));
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
        studentInfo.setDataVersionCode(Config.DATA_VERSION_STUDENT_INFO);
        if (DataMethod.saveOfflineData(context, studentInfo, FILE_NAME_DATA, checkTemp, IS_DATA_ENCRYPT)) {
            return studentInfo;
        } else {
            return null;
        }
    }

}
