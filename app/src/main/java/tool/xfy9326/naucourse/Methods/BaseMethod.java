package tool.xfy9326.naucourse.Methods;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Field;
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

    /**
     * 强制允许SnackBar在辅助服务开启时显示动画
     *
     * @param view     CoordinatorLayout
     * @param textId   文字资源ID
     * @param duration 显示时长
     */

    @SuppressWarnings("SameParameterValue")
    public static void forceShowSnackbarWithAnimation(View view, int textId, int duration) {
        Snackbar snackbar = Snackbar.make(view, textId, duration);
        try {
            Field mAccessibilityManagerField = BaseTransientBottomBar.class.getDeclaredField("mAccessibilityManager");
            mAccessibilityManagerField.setAccessible(true);
            AccessibilityManager accessibilityManager = (AccessibilityManager) mAccessibilityManagerField.get(snackbar);
            Field mIsEnabledField = AccessibilityManager.class.getDeclaredField("mIsEnabled");
            mIsEnabledField.setAccessible(true);
            mIsEnabledField.setBoolean(accessibilityManager, false);
            mAccessibilityManagerField.set(snackbar, accessibilityManager);
        } catch (Exception e) {
            Log.d("Snackbar", "Reflection error: " + e.toString());
        }
        snackbar.show();
    }

    /**
     * 网络连接情况检测以及错误提示
     *
     * @param context         Context
     * @param dataLoadCode    单个数据请求错误代码
     * @param contentLoadCode 整体网络请求错误代码
     * @return 网络检查是否通过
     */
    public static boolean checkNetWorkCode(Context context, int[] dataLoadCode, int contentLoadCode) {
        if (contentLoadCode == Config.NET_WORK_ERROR_CODE_CONNECT_ERROR) {
            Toast.makeText(context, R.string.network_get_error, Toast.LENGTH_SHORT).show();
            return false;
        }
        for (int code : dataLoadCode) {
            if (code == Config.NET_WORK_ERROR_CODE_CONNECT_NO_LOGIN) {
                if (!getApp(context).isShowLoginErrorOnce()) {
                    Toast.makeText(context, R.string.user_login_error, Toast.LENGTH_LONG).show();
                    getApp(context).setShowLoginErrorOnce();
                }
                return false;
            }
            if (code == Config.NET_WORK_ERROR_CODE_CONNECT_USER_DATA) {
                if (!getApp(context).isShowLoginErrorOnce()) {
                    Toast.makeText(context, R.string.user_login_error, Toast.LENGTH_LONG).show();
                    getApp(context).setShowLoginErrorOnce();
                }
                return false;
            }
            if (code == Config.NET_WORK_ERROR_CODE_GET_DATA_ERROR) {
                Toast.makeText(context, R.string.data_get_error, Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    /**
     * 网络是否联接检测
     *
     * @param context Context
     * @return 网络是否联接
     */
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

    /**
     * WIFI网络检测
     *
     * @param context Context
     * @return WIFI网络是否联接
     */
    private static boolean isWifiNetWork(Context context) {
        if (context != null) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo != null) {
                    return networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
                }
            }
        }
        return false;
    }

    /**
     * 双击退出
     *
     * @param activity 需要退出的Activity
     */
    public static void doubleClickExit(Activity activity) {
        long time = System.currentTimeMillis();
        if (time - DoubleClickTime > 1200) {
            DoubleClickTime = time;
            if (!activity.isDestroyed()) {
                Toast.makeText(activity, activity.getString(R.string.double_click_exit), Toast.LENGTH_SHORT).show();
            }
        } else {
            DoubleClickTime = time;
            activity.finish();
        }
    }

    /**
     * 获取BaseApplication对象
     *
     * @param context Context
     * @return BaseApplication对象
     */
    public static BaseApplication getApp(Context context) {
        return (BaseApplication) context.getApplicationContext();
    }

    /**
     * 是否数据自动更新
     *
     * @param context Context
     * @return 是否数据自动更新
     */
    public static boolean isDataAutoUpdate(Context context) {
        boolean autoUpdate = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Config.PREFERENCE_UPDATE_DATA_ON_START, Config.DEFAULT_PREFERENCE_UPDATE_DATA_ON_START);
        if (isDataWifiAutoUpdate(context)) {
            return isWifiNetWork(context) && autoUpdate;
        } else {
            return autoUpdate;
        }
    }

    /**
     * 是否Wifi下数据自动更新
     *
     * @param context Context
     * @return 是否Wifi下数据自动更新
     */
    private static boolean isDataWifiAutoUpdate(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Config.PREFERENCE_ONLY_UPDATE_UNDER_WIFI, Config.DEFAULT_PREFERENCE_ONLY_UPDATE_UNDER_WIFI);
    }

    /**
     * 获取周数数组
     *
     * @param context    Context
     * @param schoolTime SchoolTime
     * @return 周数列表
     */
    public static List<String> getWeekArray(Context context, SchoolTime schoolTime) {
        List<String> week = new ArrayList<>();
        int max_week = getMaxWeekNum(schoolTime);
        if (max_week == 0) {
            max_week = Config.DEFAULT_MAX_WEEK;
        }
        for (int i = 1; i <= max_week; i++) {
            week.add(context.getString(R.string.week, i));
        }
        return week;
    }

    /**
     * 获取最大周数
     *
     * @param schoolTime SchoolTime对象
     * @return 最大周数
     */
    public static int getMaxWeekNum(SchoolTime schoolTime) {
        int max_week = 0;
        if (schoolTime != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
            try {
                Date startDate = simpleDateFormat.parse(schoolTime.getStartTime());
                long startDay = startDate.getTime();
                long endDay = simpleDateFormat.parse(schoolTime.getEndTime()).getTime();

                Calendar calendar_start = Calendar.getInstance(Locale.CHINA);
                calendar_start.setTime(startDate);

                while (startDay < endDay) {
                    calendar_start.add(Calendar.DATE, 7);
                    startDay = calendar_start.getTimeInMillis();
                    max_week++;
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return max_week;
    }


    /**
     * 获取当前是第几周
     * 主要用于非联网更新状态，但是目前主要使用，减少网络更新
     *
     * @param schoolTime SchoolTime对象
     * @return 周数
     */
    public static int getNowWeekNum(SchoolTime schoolTime) {
        int weekNum = 0;
        if (schoolTime != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
            try {
                Date startDate = simpleDateFormat.parse(schoolTime.getStartTime());
                Date endDate = simpleDateFormat.parse(schoolTime.getEndTime());

                Calendar calendar_start = Calendar.getInstance(Locale.CHINA);
                calendar_start.setTime(startDate);

                Calendar calendar_end = Calendar.getInstance(Locale.CHINA);
                calendar_end.setTime(endDate);
                calendar_end.add(Calendar.DATE, 1);

                Calendar calendar_now = Calendar.getInstance(Locale.CHINA);
                calendar_now.setTime(new Date());

                if (calendar_now.getTimeInMillis() < calendar_start.getTimeInMillis() || calendar_now.getTimeInMillis() > calendar_end.getTimeInMillis()) {
                    return 0;
                }

                calendar_start.add(Calendar.DATE, calendar_start.getFirstDayOfWeek() - calendar_start.get(Calendar.DAY_OF_WEEK) + 1);
                calendar_now.add(Calendar.DATE, -7);
                if (calendar_now.getTimeInMillis() < calendar_start.getTimeInMillis()) {
                    return 1;
                }

                long startDay = calendar_start.getTimeInMillis();
                long nowDay = new Date().getTime();
                while (startDay < nowDay) {
                    calendar_start.add(Calendar.DATE, 7);
                    startDay = calendar_start.getTimeInMillis();
                    weekNum++;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return weekNum;
    }

    /**
     * 获取指定周每一天的的日期
     *
     * @param context         Context
     * @param week_num        周数
     * @param startSchoolDate 开学时间（SchoolTime）
     * @return 周数与日期的列表
     */
    static List<String> getWeekDayArray(Context context, int week_num, String startSchoolDate) {
        List<String> week = new ArrayList<>();
        String[] num = context.getResources().getStringArray(R.array.week_number);
        String[] week_day_date = getWeekDayDate(week_num, startSchoolDate);
        for (int i = 1; i <= Config.MAX_WEEK_DAY; i++) {
            week.add(context.getString(R.string.week_day, num[i - 1]) + "\n" + week_day_date[i - 1]);
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

    /**
     * 获取上课的节数与时间列表
     *
     * @param context Context
     * @return 时间列表
     */
    static List<String> getCourseTimeArray(Context context) {
        List<String> day = new ArrayList<>();
        String[] time = context.getResources().getStringArray(R.array.course_start_time);
        for (int i = 1; i <= Config.MAX_DAY_COURSE; i++) {
            day.add(i + "\n" + time[i - 1]);
        }
        return day;
    }

    /**
     * 获取离线数据
     *
     * @param context    Context
     * @param file_class JavaBean Class
     * @param FILE_NAME  缓存数据文件名
     * @return JavaBean对象
     */
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

    /**
     * 获取离线课表数据
     *
     * @param context Context
     * @return 课表信息列表
     */
    public static ArrayList<Course> getOfflineTableData(Context context) {
        String path = context.getFilesDir() + File.separator + TableMethod.FILE_NAME;
        File file = new File(path);
        if (file.exists()) {
            String data = IO.readFile(path);
            String id = PreferenceManager.getDefaultSharedPreferences(context).getString(Config.PREFERENCE_USER_ID, Config.DEFAULT_PREFERENCE_USER_ID);
            Type type = new TypeToken<ArrayList<Course>>() {
            }.getType();
            System.gc();
            return new Gson().fromJson(AES.decrypt(data, id), type);
        } else {
            return null;
        }
    }

    /**
     * 保存离线数据
     *
     * @param context   Context
     * @param o         JavaBean对象
     * @param FILE_NAME 储存的文件名
     * @param checkTemp 是否检测缓存与要储存的数据相同
     * @return 是否保存成功
     */
    @SuppressWarnings("UnusedReturnValue")
    public static boolean saveOfflineData(final Context context, final Object o, final String FILE_NAME, boolean checkTemp) {
        String path = context.getFilesDir() + File.separator + FILE_NAME;
        String id = PreferenceManager.getDefaultSharedPreferences(context).getString(Config.PREFERENCE_USER_ID, Config.DEFAULT_PREFERENCE_USER_ID);
        String data = new Gson().toJson(o);
        String content = AES.encrypt(data, id);
        if (checkTemp) {
            String text = IO.readFile(path);
            if (text != null) {
                text = text.replace("\n", "");
                return !text.equalsIgnoreCase(content) && IO.writeFile(content, path);
            }
        }
        return IO.writeFile(content, path);
    }

    /**
     * 删除离线数据
     *
     * @param context   Context
     * @param FILE_NAME 离线数据的文件名
     */
    @SuppressWarnings("SameParameterValue")
    public static void deleteOfflineData(final Context context, final String FILE_NAME) {
        File file = new File(context.getFilesDir() + File.separator + FILE_NAME);
        if (file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }
}
