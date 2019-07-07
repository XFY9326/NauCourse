package tool.xfy9326.naucourse.methods.netInfoMethods;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Objects;

import lib.xfy9326.nausso.NauSSOClient;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.methods.DataMethod;
import tool.xfy9326.naucourse.methods.NetMethod;
import tool.xfy9326.naucourse.utils.HistoryScore;

public class HistoryScoreMethod extends BaseInfoMethod<HistoryScore> {
    public static final String FILE_NAME = HistoryScore.class.getSimpleName();
    public static final boolean IS_ENCRYPT = true;
    @Nullable
    private Document document;

    public HistoryScoreMethod(Context context) {
        super(context);
    }

    @Override
    public int load() throws Exception {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(Config.PREFERENCE_HAS_LOGIN, Config.DEFAULT_PREFERENCE_HAS_LOGIN)) {
            String data = NetMethod.loadUrlFromLoginClient(context, NauSSOClient.JWC_SERVER_URL + "/Students/MyCourseHistory.aspx", true);
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

    public void saveTemp() {
        getData(false);
    }

    @Nullable
    @Override
    public HistoryScore getData(boolean checkTemp) {
        ArrayList<String> id = new ArrayList<>();
        ArrayList<String> name = new ArrayList<>();
        ArrayList<String> studyScore = new ArrayList<>();
        ArrayList<String> score = new ArrayList<>();
        ArrayList<String> creditWeight = new ArrayList<>();
        ArrayList<String> term = new ArrayList<>();
        ArrayList<String> courseProperty = new ArrayList<>();
        ArrayList<String> courseType = new ArrayList<>();

        Elements elements = Objects.requireNonNull(document).body().getElementById("MajorApplyList").getElementsByTag("td");
        int count = 0;
        boolean startData = false;
        for (int i = 0; i < elements.size(); i++) {
            String str = elements.get(i).text().trim();
            if (str.contains("总学分：")) {
                break;
            }
            if (str.contains("历年成绩修学信息")) {
                startData = true;
                continue;
            }
            if (startData) {
                switch (count) {
                    case 1:
                        id.add(str);
                        break;
                    case 2:
                        name.add(str);
                        break;
                    case 3:
                        studyScore.add(str);
                        break;
                    case 4:
                        score.add(str);
                        break;
                    case 5:
                        creditWeight.add(str);
                        break;
                    case 6:
                        term.add(str);
                        break;
                    case 7:
                        courseProperty.add(str);
                        break;
                    case 8:
                        courseType.add(str);
                        break;
                }
                if (count == 8) {
                    count = 0;
                    i += 2;
                } else {
                    count++;
                }
            }
        }

        HistoryScore historyScore = new HistoryScore();
        historyScore.setCourseAmount(id.size());
        historyScore.setId(id.toArray(new String[]{}));
        historyScore.setName(name.toArray(new String[]{}));
        historyScore.setStudyScore(studyScore.toArray(new String[]{}));
        historyScore.setScore(score.toArray(new String[]{}));
        historyScore.setCreditWeight(creditWeight.toArray(new String[]{}));
        historyScore.setTerm(term.toArray(new String[]{}));
        historyScore.setCourseProperty(courseProperty.toArray(new String[]{}));
        historyScore.setCourseType(courseType.toArray(new String[]{}));

        historyScore.setDataVersionCode(Config.DATA_VERSION_COURSE_HISTORY_SCORE);
        if (DataMethod.saveOfflineData(context, historyScore, FILE_NAME, checkTemp, IS_ENCRYPT)) {
            return historyScore;
        } else {
            return null;
        }
    }
}
