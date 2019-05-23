package tool.xfy9326.naucourse.methods.netInfoMethods;

import android.content.Context;

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

public class MoaMethod {
    public static final String FILE_NAME = "Moa";
    public static final boolean IS_ENCRYPT = false;
    public static final String Academic_Report = "xsbg";
    public static final int MOA_PAST_SHOW_MONTH = 2;
    private static final String URL = "http://moa.nau.edu.cn:8080/OAMobile/xsdt/xsdt/tj";
    private final Context context;
    private String Url_Data;

    public MoaMethod(Context context) {
        this.context = context;
        this.Url_Data = null;
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

    public int load() throws Exception {
        String data = NetMethod.loadUrl(URL);
        System.gc();
        if (data != null) {
            this.Url_Data = data;
            return Config.NET_WORK_GET_SUCCESS;
        }
        return Config.NET_WORK_ERROR_CODE_GET_DATA_ERROR;
    }

    public void saveTemp() {
        getMoaList(false);
    }

    @SuppressWarnings("ToArrayCallWithZeroLengthArrayArgument")
    public Moa getMoaList(boolean checkTemp) {
        Moa moa = new Moa();
        if (Url_Data != null) {
            Url_Data = Url_Data.substring(Url_Data.indexOf("var msg = ") + 10, Url_Data.lastIndexOf("var obj = '[';"));
            try {
                JSONArray jsonArray = new JSONArray(Url_Data);

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
                long past_date = calendar.getTimeInMillis();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);

                    String report_time = jsonObject.getString("REPORTTIME") + " " + jsonObject.getString("REPORTTIMEF");
                    long report_time_long = TimeMethod.parseDateSDFHM2(report_time).getTime();
                    if (report_time_long > past_date) {
                        int addPosition = 0;
                        if (!timeLong.isEmpty()) {
                            for (; addPosition < timeLong.size(); addPosition++) {
                                if (report_time_long > timeLong.get(addPosition)) {
                                    break;
                                }
                            }
                        }

                        timeLong.add(addPosition, report_time_long);
                        id.add(addPosition, String.valueOf(jsonObject.getInt("ID")));
                        title.add(addPosition, jsonObject.getString("TITLE"));

                        String type_temp = jsonObject.getString("LB");
                        type.add(addPosition, type_temp);

                        if (type_temp.equalsIgnoreCase(Academic_Report)) {
                            reporter.add(addPosition, jsonObject.getString("REPORTER") + " " + jsonObject.getString("REPORTERJOB"));
                        } else {
                            reporter.add(addPosition, jsonObject.getString("FIELD1"));
                        }
                        location.add(addPosition, jsonObject.getString("REPORTROOMNAME"));
                        time.add(addPosition, report_time);
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
