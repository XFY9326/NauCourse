package tool.xfy9326.naucourse.Methods.InfoMethods;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Objects;

import androidx.annotation.Nullable;
import lib.xfy9326.nausso.NauSSOClient;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.DataMethod;
import tool.xfy9326.naucourse.Methods.LoginMethod;
import tool.xfy9326.naucourse.Methods.NetMethod;
import tool.xfy9326.naucourse.Utils.CourseScore;

/**
 * Created by 10696 on 2018/3/2.
 * 获取成绩信息
 */

public class ScoreMethod {
    public static final String FILE_NAME = "CourseScore";
    private final Context context;
    @Nullable
    private Document document;

    public ScoreMethod(Context context) {
        this.context = context;
        this.document = null;
    }

    public int load() throws Exception {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(Config.PREFERENCE_HAS_LOGIN, Config.DEFAULT_PREFERENCE_HAS_LOGIN)) {
            String data = NetMethod.loadUrlFromLoginClient(context, NauSSOClient.JWC_SERVER_URL + "/Students/MyCourse.aspx", true);
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

    public void saveTemp() {
        getCourseScore(false);
    }

    @Nullable
    public CourseScore getCourseScore(boolean checkTemp) {
        ArrayList<String> scoreCourseId = new ArrayList<>();
        ArrayList<String> scoreCourseName = new ArrayList<>();
        ArrayList<String> scoreCommon = new ArrayList<>();
        ArrayList<String> scoreMid = new ArrayList<>();
        ArrayList<String> scoreFinal = new ArrayList<>();
        ArrayList<String> scoreTotal = new ArrayList<>();
        ArrayList<String> scoreXf = new ArrayList<>();

        CourseScore courseScore = new CourseScore();
        Elements elements = Objects.requireNonNull(document).body().getElementsByTag("td");
        int count = 0;
        boolean startData = false;
        for (int i = 0; i < elements.size(); i++) {
            String str = elements.get(i).text();
            if (str.contains("在修课程列表")) {
                startData = true;
                continue;
            }
            if (str.contains("成绩录入形式")) {
                break;
            }
            if (startData) {
                switch (count) {
                    case 1:
                        scoreCourseId.add(str);
                        break;
                    case 2:
                        scoreCourseName.add(str);
                        break;
                    case 3:
                        scoreXf.add(str);
                        break;
                    case 5:
                        scoreCommon.add(str);
                        break;
                    case 6:
                        scoreMid.add(str);
                        break;
                    case 7:
                        scoreFinal.add(str);
                        break;
                    case 8:
                        scoreTotal.add(str);
                        break;
                }
                if (count == 8) {
                    count = 0;
                    i += 3;
                } else {
                    count++;
                }
            }
        }
        courseScore.setCourseAmount(scoreCourseId.size());
        courseScore.setScoreCourseId(scoreCourseId.toArray(new String[]{}));
        courseScore.setScoreCourseName(scoreCourseName.toArray(new String[]{}));
        courseScore.setScoreCourseXf(scoreXf.toArray(new String[]{}));
        courseScore.setScoreCommon(scoreCommon.toArray(new String[]{}));
        courseScore.setScoreMid(scoreMid.toArray(new String[]{}));
        courseScore.setScoreFinal(scoreFinal.toArray(new String[]{}));
        courseScore.setScoreTotal(scoreTotal.toArray(new String[]{}));

        courseScore.setDataVersionCode(Config.DATA_VERSION_COURSE_SCORE);
        if (DataMethod.saveOfflineData(context, courseScore, FILE_NAME, checkTemp)) {
            return courseScore;
        } else {
            return null;
        }
    }

}
