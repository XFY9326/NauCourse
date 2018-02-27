package tool.xfy9326.naucourse.Methods;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import tool.xfy9326.naucourse.BaseApplication;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Tools.AES;
import tool.xfy9326.naucourse.Tools.IO;
import tool.xfy9326.naucourse.Utils.Course;
import tool.xfy9326.naucourse.Utils.SchoolTime;

/**
 * Created by xfy9326 on 18-2-20.
 * 基础杂项方法
 */

public class BaseMethod {
    private static long DoubleClickTime = 0;

    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo != null) {
                    return networkInfo.isAvailable();
                }
            }
        }
        return false;
    }

    public static void doubleClickExit(Activity activity) {
        long time = System.currentTimeMillis();
        if (time - DoubleClickTime > 800) {
            DoubleClickTime = time;
            Toast.makeText(activity, activity.getString(R.string.double_click_exit), Toast.LENGTH_SHORT).show();
        } else {
            DoubleClickTime = time;
            activity.finish();
        }
    }

    public static BaseApplication getBaseApplication(Context context) {
        return (BaseApplication) context.getApplicationContext();
    }

    public static boolean isDataAutoUpdate(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Config.PREFERENCE_UPDATE_DATA_ON_START, Config.DEFAULT_PREFERENCE_UPDATE_DATA_ON_START);
    }

    //获取最大周数，返回周数数组
    public static List<String> getWeekArray(Context context, SchoolTime schoolTime) {
        int max_week = 0;
        List<String> week = new ArrayList<>();
        if (schoolTime != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
            try {
                Date startDate = simpleDateFormat.parse(schoolTime.getStartTime());
                long startDay = startDate.getTime();
                long endDay = simpleDateFormat.parse(schoolTime.getEndTime()).getTime();

                Calendar calendar_start = Calendar.getInstance(Locale.CHINA);
                calendar_start.setTime(startDate);

                while (startDay > endDay) {
                    calendar_start.add(Calendar.DATE, 7);
                    startDay = calendar_start.getTimeInMillis();
                    max_week++;
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (max_week == 0) {
            max_week = Config.DEFAULT_MAX_WEEK;
        }
        for (int i = 1; i <= max_week; i++) {
            week.add(context.getString(R.string.week, i));
        }
        return week;
    }

    //获取第几周星期几的日期
    static List<String> getWeekDayArray(Context context, int week_num, String startSchoolDate) {
        List<String> week = new ArrayList<>();
        String[] num = context.getResources().getStringArray(R.array.week_number);
        String[] week_day_date = getWeekDayDate(week_num, startSchoolDate);
        for (int i = 1; i <= Config.MAX_WEEK_DAY; i++) {
            week.add(context.getString(R.string.week_day, num[i - 1]) + "  " + week_day_date[i - 1]);
        }
        return week;
    }

    private static String[] getWeekDayDate(int week_num, String startSchoolDate) {
        String[] weekDayDate = new String[Config.MAX_WEEK_DAY];
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        try {
            Date startDate = simpleDateFormat.parse(startSchoolDate);

            Calendar calendar = Calendar.getInstance(Locale.CHINA);
            calendar.setTime(startDate);
            calendar.setFirstDayOfWeek(Calendar.MONDAY);
            calendar.add(Calendar.DATE, calendar.getFirstDayOfWeek() - calendar.get(Calendar.DAY_OF_WEEK));

            calendar.add(Calendar.DATE, (week_num - 1) * 7);

            for (int i = 0; i < Config.MAX_WEEK_DAY; i++) {
                weekDayDate[i] = (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH);
                calendar.add(Calendar.DATE, 1);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return weekDayDate;
    }

    //获取上课时间
    static List<String> getCourseTimeArray(Context context) {
        List<String> day = new ArrayList<>();
        String[] time = context.getResources().getStringArray(R.array.course_time);
        for (int i = 1; i <= Config.MAX_DAY_COURSE; i++) {
            day.add(i + "\n" + time[i - 1]);
        }
        return day;
    }

    public static Object getOfflineData(Context context, Class file_class, String FILE_NAME) {
        String path = context.getFilesDir() + File.separator + FILE_NAME;
        File file = new File(path);
        if (file.exists()) {
            String data = IO.readFile(path);
            String id = PreferenceManager.getDefaultSharedPreferences(context).getString(Config.PREFERENCE_USER_ID, Config.DEFAULT_PREFERENCE_USER_ID);
            return new Gson().fromJson(AES.decrypt(data, id), file_class);
        } else {
            return null;
        }
    }

    public static ArrayList<Course> getOfflineTableData(Context context) {
        String path = context.getFilesDir() + File.separator + TableMethod.FILE_NAME;
        File file = new File(path);
        if (file.exists()) {
            String data = IO.readFile(path);
            String id = PreferenceManager.getDefaultSharedPreferences(context).getString(Config.PREFERENCE_USER_ID, Config.DEFAULT_PREFERENCE_USER_ID);
            Type type = new TypeToken<ArrayList<Course>>() {
            }.getType();
            return new Gson().fromJson(AES.decrypt(data, id), type);
        } else {
            return null;
        }
    }

    static void saveOfflineData(final Context context, final Object o, final String FILE_NAME) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String id = PreferenceManager.getDefaultSharedPreferences(context).getString(Config.PREFERENCE_USER_ID, Config.DEFAULT_PREFERENCE_USER_ID);
                String data = new Gson().toJson(o);
                if (!IO.writeFile(AES.encrypt(data, id), context.getFilesDir() + File.separator + FILE_NAME)) {
                    Log.d(Config.TAG_TEMP_SAVE_FAILED, FILE_NAME);
                }
            }
        }).start();
    }
}
