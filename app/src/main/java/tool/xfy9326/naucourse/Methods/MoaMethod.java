package tool.xfy9326.naucourse.Methods;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Utils.Moa;

public class MoaMethod {
    public static final String FILE_NAME = "Moa";
    public static final String Academic_Report = "xsbg";
    public static final int MOA_PAST_SHOW_MONTH = 2;
    private static final String URL = "http://moa.nau.edu.cn:8080/OAMobile/xsdt/xsdt/tj";
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
    private final Context context;
    private String Url_Data;

    public MoaMethod(Context context) {
        this.context = context;
        this.Url_Data = null;
    }

    public static int getScrollPosition(Moa moa) {
        String[] time = moa.getTime();
        int result = 0;
        try {
            long nowTime = new Date().getTime();
            for (int i = 0; i < time.length; i++) {
                long t = simpleDateFormat.parse(time[i]).getTime();
                if (nowTime < t) {
                    break;
                } else {
                    result = i;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
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
                ArrayList<String> applyUnit = new ArrayList<>();

                Calendar calendar = Calendar.getInstance(Locale.CHINA);
                calendar.setTime(new Date());
                calendar.add(Calendar.MONTH, -MOA_PAST_SHOW_MONTH);
                Date past_date = calendar.getTime();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);

                    String report_time = jsonObject.getString("REPORTTIME") + " " + jsonObject.getString("REPORTTIMEF");
                    if (simpleDateFormat.parse(report_time).before(past_date)) {
                        continue;
                    }

                    id.add(String.valueOf(jsonObject.getInt("ID")));
                    title.add(jsonObject.getString("TITLE"));
                    reporter.add(jsonObject.getString("REPORTER"));

                    String type_temp = jsonObject.getString("LB");
                    type.add(type_temp);
                    if (type_temp.equalsIgnoreCase(Academic_Report)) {
                        reporter.add(jsonObject.getString("REPORTER") + jsonObject.getString("REPORTERJOB"));
                    } else {
                        reporter.add(jsonObject.getString("FIELD1"));
                    }
                    location.add(jsonObject.getString("REPORTROOMNAME"));
                    time.add(report_time);
                    applyUnit.add(jsonObject.getString("APPLYUNIT"));
                }

                for (int i = 0; i < id.size(); i++) {
                    for (int j = 1; j < id.size() - i; j++) {
                        long t1 = simpleDateFormat.parse(time.get(j)).getTime();
                        long t2 = simpleDateFormat.parse(time.get(j - 1)).getTime();
                        if (t1 < t2) {
                            Collections.swap(id, j, j - 1);
                            Collections.swap(time, j, j - 1);
                            Collections.swap(applyUnit, j, j - 1);
                            Collections.swap(location, j, j - 1);
                            Collections.swap(title, j, j - 1);
                            Collections.swap(reporter, j, j - 1);
                            Collections.swap(type, j, j - 1);
                        }
                    }
                }

                moa.setId(id.toArray(new String[id.size()]));
                moa.setTime(time.toArray(new String[time.size()]));
                moa.setApplyUnit(applyUnit.toArray(new String[applyUnit.size()]));
                moa.setLocation(location.toArray(new String[location.size()]));
                moa.setTitle(title.toArray(new String[title.size()]));
                moa.setReporter(reporter.toArray(new String[reporter.size()]));
                moa.setType(type.toArray(new String[type.size()]));

                moa.setCount(id.size());

                moa.setDataVersionCode(Config.DATA_VERSION_MOA);

                if (DataMethod.saveOfflineData(context, moa, FILE_NAME, checkTemp)) {
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
