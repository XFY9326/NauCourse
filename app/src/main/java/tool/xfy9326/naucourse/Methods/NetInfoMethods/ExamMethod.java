package tool.xfy9326.naucourse.Methods.NetInfoMethods;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Objects;

import androidx.annotation.Nullable;
import lib.xfy9326.nausso.NauSSOClient;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.DataMethod;
import tool.xfy9326.naucourse.Methods.LoginMethod;
import tool.xfy9326.naucourse.Methods.NetMethod;
import tool.xfy9326.naucourse.Methods.TimeMethod;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Utils.Exam;

/**
 * Created by 10696 on 2018/3/3.
 * 获取考试信息
 */

public class ExamMethod {
    public static final String FILE_NAME = "Exam";
    public static final boolean IS_ENCRYPT = true;
    private final Context context;
    @Nullable
    private Document document;

    public ExamMethod(Context context) {
        this.context = context;
        this.document = null;
    }

    public int load() throws Exception {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(Config.PREFERENCE_HAS_LOGIN, Config.DEFAULT_PREFERENCE_HAS_LOGIN)) {
            String data = NetMethod.loadUrlFromLoginClient(context, NauSSOClient.JWC_SERVER_URL + "/Students/MyExamArrangeList.aspx", true);
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
        getExam(false);
    }

    @Nullable
    @SuppressWarnings({"SameParameterValue", "ConstantConditions"})
    public Exam getExam(boolean checkTemp) {
        ArrayList<String> examId = new ArrayList<>();
        ArrayList<String> examName = new ArrayList<>();
        ArrayList<String> examType = new ArrayList<>();
        ArrayList<String> examScore = new ArrayList<>();
        ArrayList<String> examTime = new ArrayList<>();
        ArrayList<String> examLocation = new ArrayList<>();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean hide_exam = sharedPreferences.getBoolean(Config.PREFERENCE_HIDE_OUT_OF_DATE_EXAM, Config.DEFAULT_PREFERENCE_HIDE_OUT_OF_DATE_EXAM);
        long now = System.currentTimeMillis() / 1000L;

        Exam exam = new Exam();
        Elements elements = Objects.requireNonNull(document).body().getElementsByTag("td");
        boolean startData = false;
        boolean needJump = false;
        int count = 0;
        for (Element element : elements) {
            String str = element.text().replace(" ", "");
            if (str.contains("考试日程列表")) {
                startData = true;
                continue;
            }
            if (startData) {
                if (str.isEmpty()) {
                    str = context.getString(R.string.not_publish);
                }
                switch (count) {
                    case 1:
                        examId.add(str);
                        break;
                    case 2:
                        examName.add(str);
                        break;
                    case 3:
                        examScore.add(str);
                        break;
                    case 5:
                        if (str.contains("日")) {
                            str = str.replace("日", "日 ");
                        }
                        if (hide_exam) {
                            long examEnd = -1;
                            if (str.contains("-") && str.contains(" ")) {
                                str = str.substring(0, str.indexOf(" ") + 1) + str.substring(str.indexOf("-") + 1);
                                try {
                                    examEnd = TimeMethod.parseDateSDFHM(str).getTime() / 1000L;
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                    examEnd = -1;
                                }
                            }
                            if (examEnd == -1 || examEnd < now) {
                                examId.remove(examId.size() - 1);
                                examName.remove(examName.size() - 1);
                                examScore.remove(examScore.size() - 1);
                                needJump = true;
                            }
                        }
                        examTime.add(str);
                        break;
                    case 6:
                        if (!needJump) {
                            examLocation.add(str);
                        }
                        break;
                    case 8:
                        if (!needJump) {
                            examType.add(str);
                        } else {
                            needJump = false;
                        }
                        break;
                }
                if (count == 8) {
                    count = 0;
                } else {
                    count++;
                }
            }
        }

        exam.setExamMount(examId.size());
        exam.setExamId(examId.toArray(new String[]{}));
        exam.setExamTime(examTime.toArray(new String[]{}));
        exam.setExamName(examName.toArray(new String[]{}));
        exam.setExamType(examType.toArray(new String[]{}));
        exam.setExamScore(examScore.toArray(new String[]{}));
        exam.setExamLocation(examLocation.toArray(new String[]{}));
        exam.setDataVersionCode(Config.DATA_VERSION_EXAM);

        if (DataMethod.saveOfflineData(context, exam, FILE_NAME, checkTemp, IS_ENCRYPT)) {
            return exam;
        } else {
            return null;
        }
    }
}