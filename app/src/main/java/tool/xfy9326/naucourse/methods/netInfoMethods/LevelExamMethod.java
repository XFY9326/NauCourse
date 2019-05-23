package tool.xfy9326.naucourse.methods.netInfoMethods;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lib.xfy9326.nausso.NauSSOClient;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.methods.DataMethod;
import tool.xfy9326.naucourse.methods.LoginMethod;
import tool.xfy9326.naucourse.methods.NetMethod;
import tool.xfy9326.naucourse.utils.LevelExam;

public class LevelExamMethod {
    public static final String FILE_NAME = "LevelExam";
    public static final boolean IS_ENCRYPT = true;
    private final Context context;
    @Nullable
    private Document document;

    public LevelExamMethod(Context context) {
        this.context = context;
        this.document = null;
    }

    public int load() throws Exception {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(Config.PREFERENCE_HAS_LOGIN, Config.DEFAULT_PREFERENCE_HAS_LOGIN)) {
            String data = NetMethod.loadUrlFromLoginClient(context, NauSSOClient.JWC_SERVER_URL + "/Students/MyLevelExam.aspx", true);
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
        getLevelExam(false);
    }

    @SuppressWarnings({"ToArrayCallWithZeroLengthArrayArgument"})
    @Nullable
    public LevelExam getLevelExam(boolean checkTemp) {
        ArrayList<String> examType = new ArrayList<>();
        ArrayList<String> examName = new ArrayList<>();
        ArrayList<String> score1 = new ArrayList<>();
        ArrayList<String> score2 = new ArrayList<>();
        ArrayList<String> term = new ArrayList<>();
        ArrayList<String> ticketId = new ArrayList<>();
        ArrayList<String> certificateId = new ArrayList<>();

        LevelExam levelExam = new LevelExam();
        Elements elements = Objects.requireNonNull(document).body().getElementsByTag("td");
        List<Element> elementList = elements.subList(0, elements.size());
        boolean startData = false;
        int counter = 0;
        for (Element element : elementList) {
            if (element.text().contains("英语、计算机等级考试")) {
                startData = true;
                continue;
            }
            if (startData) {
                counter++;
                switch (counter) {
                    case 1:
                        continue;
                    case 2:
                        examType.add(element.text());
                        break;
                    case 3:
                        examName.add(element.text());
                        break;
                    case 4:
                        score1.add(element.text());
                        break;
                    case 5:
                        score2.add(element.text());
                        break;
                    case 6:
                        term.add(element.text());
                        break;
                    case 7:
                        ticketId.add(element.text());
                        break;
                    case 8:
                        certificateId.add(element.text());
                        break;
                    case 9:
                        counter = 0;
                        break;
                }
            }
        }
        levelExam.setExamAmount(examType.size());
        levelExam.setExamType(examType.toArray(new String[examType.size()]));
        levelExam.setExamName(examName.toArray(new String[examName.size()]));
        levelExam.setScore1(score1.toArray(new String[score1.size()]));
        levelExam.setScore2(score2.toArray(new String[score2.size()]));
        levelExam.setTerm(term.toArray(new String[term.size()]));
        levelExam.setTicketId(ticketId.toArray(new String[ticketId.size()]));
        levelExam.setCertificateId(certificateId.toArray(new String[certificateId.size()]));

        levelExam.setDataVersionCode(Config.DATA_VERSION_LEVEL_EXAM);
        if (DataMethod.saveOfflineData(context, levelExam, FILE_NAME, checkTemp, IS_ENCRYPT)) {
            return levelExam;
        } else {
            return null;
        }
    }
}
