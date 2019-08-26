package tool.xfy9326.naucourse.methods.netInfoMethods;

import android.content.Context;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.methods.DataMethod;
import tool.xfy9326.naucourse.methods.NetMethod;
import tool.xfy9326.naucourse.methods.TimeMethod;
import tool.xfy9326.naucourse.utils.Moa;

public class MoaMethod extends BaseInfoMethod<Moa> {
    public static final String FILE_NAME = Moa.class.getSimpleName();
    public static final boolean IS_ENCRYPT = false;
    public static final String ACADEMIC_REPORT = "xsbg";
    public static final int MOA_PAST_SHOW_MONTH = 2;
    private static final String URL = "http://moa.nau.edu.cn:8080/OAMobile/xsdt/xsdt/tj";
    private String urlData;

    public MoaMethod(@NonNull Context context) {
        super(context);
        this.urlData = null;
    }

    public static int getScrollPosition(Moa moa) {
        Long[] time = moa.getTimeLong();
        int result = 0;
        long nowTime = System.currentTimeMillis();
        for (int i = 0; i < time.length; i++) {
            if (time[i] > 0) {
                if (nowTime > time[i]) {
                    break;
                } else {
                    result = i;
                }
            }
        }
        return result;
    }

    @Override
    public int load() throws Exception {
        String data = NetMethod.loadUrl(context, URL);
        if (data != null) {
            this.urlData = data;
            return Config.NET_WORK_GET_SUCCESS;
        }
        return Config.NET_WORK_ERROR_CODE_GET_DATA_ERROR;
    }

    public void saveTemp() {
        getData(false);
    }

    @SuppressWarnings("ToArrayCallWithZeroLengthArrayArgument")
    @Override
    public Moa getData(boolean checkTemp) {
        Moa moa = new Moa();
        if (urlData != null) {
            urlData = urlData.substring(urlData.indexOf("var msg = ") + 10, urlData.lastIndexOf("var obj = '[';"));
            try {
                JSONArray jsonArray = new JSONArray(urlData);

                ArrayList<String> id = new ArrayList<>();
                ArrayList<String> title = new ArrayList<>();
                ArrayList<String> reporter = new ArrayList<>();
                ArrayList<String> type = new ArrayList<>();
                ArrayList<String> location = new ArrayList<>();
                ArrayList<String> time = new ArrayList<>();
                ArrayList<Long> timeLong = new ArrayList<>();
                ArrayList<String> applyUnit = new ArrayList<>();

                Calendar calendar = Calendar.getInstance(Locale.CHINA);
                calendar.setTime(new Date());
                calendar.add(Calendar.MONTH, -MOA_PAST_SHOW_MONTH);
                long pastDate = calendar.getTimeInMillis();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);

                    String reportTime = jsonObject.getString("REPORTTIME") + " " + jsonObject.getString("REPORTTIMEF");
                    long reportTimeLong = TimeMethod.parseDateSDFHM2(reportTime).getTime();
                    if (reportTimeLong > pastDate) {
                        int addPosition = 0;
                        if (!timeLong.isEmpty()) {
                            for (; addPosition < timeLong.size(); addPosition++) {
                                if (reportTimeLong > timeLong.get(addPosition)) {
                                    break;
                                }
                            }
                        }

                        timeLong.add(addPosition, reportTimeLong);
                        id.add(addPosition, String.valueOf(jsonObject.getInt("ID")));
                        title.add(addPosition, jsonObject.getString("TITLE"));

                        String typeTemp = jsonObject.getString("LB");
                        type.add(addPosition, typeTemp);

                        if (typeTemp.equalsIgnoreCase(ACADEMIC_REPORT)) {
                            reporter.add(addPosition, jsonObject.getString("REPORTER") + " " + jsonObject.getString("REPORTERJOB"));
                        } else {
                            reporter.add(addPosition, jsonObject.getString("FIELD1"));
                        }
                        location.add(addPosition, jsonObject.getString("REPORTROOMNAME"));
                        time.add(addPosition, reportTime);
                        applyUnit.add(addPosition, jsonObject.getString("APPLYUNIT"));
                    }
                }

                moa.setId(id.toArray(new String[id.size()]));
                moa.setTime(time.toArray(new String[time.size()]));
                moa.setTimeLong(timeLong.toArray(new Long[timeLong.size()]));
                moa.setApplyUnit(applyUnit.toArray(new String[applyUnit.size()]));
                moa.setLocation(location.toArray(new String[location.size()]));
                moa.setTitle(title.toArray(new String[title.size()]));
                moa.setReporter(reporter.toArray(new String[reporter.size()]));
                moa.setType(type.toArray(new String[type.size()]));

                moa.setCount(id.size());

                moa.setDataVersionCode(Config.DATA_VERSION_MOA);

                if (DataMethod.saveOfflineData(context, moa, FILE_NAME, checkTemp, IS_ENCRYPT)) {
                    return moa;
                } else {
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
