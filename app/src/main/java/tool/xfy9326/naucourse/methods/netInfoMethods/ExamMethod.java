package tool.xfy9326.naucourse.methods.netInfoMethods;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Objects;

import lib.xfy9326.nausso.NauSSOClient;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.methods.DataMethod;
import tool.xfy9326.naucourse.methods.NetMethod;
import tool.xfy9326.naucourse.methods.TimeMethod;
import tool.xfy9326.naucourse.utils.Exam;

/**
 * Created by 10696 on 2018/3/3.
 * 获取考试信息
 */

public class ExamMethod extends BaseInfoMethod<Exam> {
    public static final String FILE_NAME = Exam.class.getSimpleName();
    public static final boolean IS_ENCRYPT = true;
    @Nullable
    private Document document;

    public ExamMethod(@NonNull Context context) {
        super(context);
    }

    private static void setLastTimeText(Context context, long now, String startTime, ArrayList<String> examLastTime, ArrayList<String> examLastTimeUnit) {
        String lastTime = null;
        String lastTimeUnit = null;
        if (startTime != null) {
            long examTime = 0;
            try {
                examTime = TimeMethod.parseDateSDFHM(startTime).getTime() / 1000L;
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (examTime > now) {
                long day = (long) Math.ceil((examTime - now) / (3600f * 24));
                if (day > 0) {
                    lastTime = String.valueOf(day);
                    lastTimeUnit = context.getString(R.string.day);
                } else {
                    long hour = (long) Math.ceil((examTime - now) / 3600f);
                    if (hour > 0) {
                        lastTime = String.valueOf(hour);
                        lastTimeUnit = context.getString(R.string.hour);
                    } else {
                        long minute = (long) Math.ceil((examTime - now) / 60f);
                        if (minute > 0) {
                            lastTime = String.valueOf(minute);
                            lastTimeUnit = context.getString(R.string.minute);
                        } else {
                            lastTime = String.valueOf(0);
                            lastTimeUnit = context.getString(R.string.minute);
                        }
                    }
                }
            }
        }
        examLastTime.add(lastTime);
        examLastTimeUnit.add(lastTimeUnit);
    }

    @Override
    public int load() throws Exception {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(Config.PREFERENCE_HAS_LOGIN, Config.DEFAULT_PREFERENCE_HAS_LOGIN)) {
            String data = NetMethod.loadUrlFromLoginClient(context, NauSSOClient.JWC_SERVER_URL + "/Students/MyExamArrangeList.aspx", true);
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
    @SuppressWarnings({"SameParameterValue"})
    public Exam getData(boolean checkTemp) {
        ArrayList<String> examId = new ArrayList<>();
        ArrayList<String> examName = new ArrayList<>();
        ArrayList<String> examType = new ArrayList<>();
        ArrayList<String> examScore = new ArrayList<>();
        ArrayList<String> examTime = new ArrayList<>();
        ArrayList<String> examLocation = new ArrayList<>();
        ArrayList<String> examLastTime = new ArrayList<>();
        ArrayList<String> examLastTimeUnit = new ArrayList<>();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean hide_exam = sharedPreferences.getBoolean(Config.PREFERENCE_HIDE_OUT_OF_DATE_EXAM, Config.DEFAULT_PREFERENCE_HIDE_OUT_OF_DATE_EXAM);
        long now = System.currentTimeMillis();
        long lastTimeNow = now / 1000L;

        Exam exam = new Exam();
        Elements elements = Objects.requireNonNull(document).body().getElementsByTag("td");
        boolean startData = false;
        boolean needJump = false;
        int count = 0;
        String examIdStr = null;
        String examNameStr = null;
        String examScoreStr = null;
        String startDateTime;
        String endTime;
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
                        examIdStr = str;
                        break;
                    case 2:
                        examNameStr = str;
                        break;
                    case 3:
                        examScoreStr = str;
                        break;
                    case 5:
                        if (str.contains("日")) {
                            str = str.replace("日", "日 ");
                        }
                        if (str.contains("-")) {
                            String[] time = str.split("-");
                            startDateTime = time[0];
                            endTime = time[1];
                        } else {
                            startDateTime = null;
                            endTime = null;
                        }
                        if (hide_exam) {
                            long examEnd = -1;
                            if (endTime != null && str.contains(" ")) {
                                String time = str.substring(0, str.indexOf(" ") + 1) + endTime;
                                try {
                                    examEnd = TimeMethod.parseDateSDFHM(time).getTime();
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                    examEnd = -1;
                                }
                            }
                            if (examEnd != -1 && examEnd >= now) {
                                examId.add(examIdStr);
                                examName.add(examNameStr);
                                examScore.add(examScoreStr);
                                examTime.add(str);
                                setLastTimeText(context, lastTimeNow, startDateTime, examLastTime, examLastTimeUnit);
                            } else {
                                needJump = true;
                            }
                        } else {
                            examId.add(examIdStr);
                            examName.add(examNameStr);
                            examScore.add(examScoreStr);
                            examTime.add(str);
                            setLastTimeText(context, lastTimeNow, startDateTime, examLastTime, examLastTimeUnit);
                        }
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
        exam.setLast_time(examLastTime.toArray(new String[]{}));
        exam.setLast_time_unit(examLastTimeUnit.toArray(new String[]{}));
        exam.setDataVersionCode(Config.DATA_VERSION_EXAM);

        if (DataMethod.saveOfflineData(context, exam, FILE_NAME, checkTemp, IS_ENCRYPT)) {
            return exam;
        } else {
            return null;
        }
    }
}
