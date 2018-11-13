package tool.xfy9326.naucourse.Methods.InfoMethods;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.Nullable;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.DataMethod;
import tool.xfy9326.naucourse.Methods.LoginMethod;
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
            String data = LoginMethod.getData(context, "/Students/MyCourse.aspx", true);
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
        Elements elements = Objects.requireNonNull(document).body().getElementsByTag("tr");
        List<String> text = elements.eachText();
        boolean startData = false;
        for (String str : text) {
            if (str.contains("序号")) {
                startData = true;
                continue;
            }
            if (str.contains("成绩录入形式")) {
                break;
            }
            if (startData) {
                String[] data = str.trim().split(" ");
                scoreCourseId.add(data[1]);
                scoreCourseName.add(data[2]);
                scoreXf.add(data[3]);
                scoreCommon.add(data[5]);
                scoreMid.add(data[6]);
                scoreFinal.add(data[7]);
                scoreTotal.add(data[8]);

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
