package tool.xfy9326.naucourse.methods;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.methods.netInfoMethods.SchoolTimeMethod;
import tool.xfy9326.naucourse.utils.SchoolTime;

/**
 * Created by 10696 on 2018/4/19.
 */

public class TimeMethod {
    private static final SimpleDateFormat SDF_YMD = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
    private static final SimpleDateFormat SDF_YMD_HM = new SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.CHINA);
    private static String[] weekArray = null;

    static synchronized String getDateSDF(long time) {
        return SDF_YMD.format(new Date(time));
    }

    public static synchronized Date parseDateSDFHM(String str) throws Exception {
        return SDF_YMD_HM.parse(str);
    }

    public static synchronized Date parseDateSDF(String str) throws Exception {
        return SDF_YMD.parse(str);
    }

    public static synchronized String formatDateSDF(Date date) {
        return SDF_YMD.format(date);
    }

    static long getInfoDateLong(String date) {
        if (date != null) {
            date = date.trim();
            try {
                Date dat = parseDateSDF(date);
                if (dat != null) {
                    return dat.getTime();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    /**
     * 手动设置的学期替换检查与自动校准
     *
     * @param context     Context
     * @param schoolTime  SchoolTime
     * @param forceChange 强制更改替换学期
     * @return SchoolTime
     */
    public static SchoolTime termSetCheck(Context context, SchoolTime schoolTime, boolean forceChange) {
        if (schoolTime != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            String customStart = sharedPreferences.getString(Config.PREFERENCE_CUSTOM_TERM_START_DATE, null);
            String customEnd = sharedPreferences.getString(Config.PREFERENCE_CUSTOM_TERM_END_DATE, null);
            String oldStart = sharedPreferences.getString(Config.PREFERENCE_OLD_TERM_START_DATE, null);
            String oldEnd = sharedPreferences.getString(Config.PREFERENCE_OLD_TERM_END_DATE, null);
            if (customStart != null && customEnd != null) {
                if (forceChange) {
                    schoolTime.setStartTime(customStart);
                    schoolTime.setEndTime(customEnd);
                } else {
                    if (oldStart != null && oldEnd != null) {
                        if (!oldStart.equals(schoolTime.getStartTime()) || !oldEnd.equals(schoolTime.getEndTime())) {
                            sharedPreferences.edit().remove(Config.PREFERENCE_CUSTOM_TERM_START_DATE)
                                    .remove(Config.PREFERENCE_CUSTOM_TERM_END_DATE)
                                    .remove(Config.PREFERENCE_OLD_TERM_START_DATE)
                                    .remove(Config.PREFERENCE_OLD_TERM_END_DATE).apply();
                        } else {
                            schoolTime.setStartTime(customStart);
                            schoolTime.setEndTime(customEnd);
                        }
                    } else {
                        sharedPreferences.edit().remove(Config.PREFERENCE_CUSTOM_TERM_START_DATE)
                                .remove(Config.PREFERENCE_CUSTOM_TERM_END_DATE)
                                .remove(Config.PREFERENCE_OLD_TERM_START_DATE)
                                .remove(Config.PREFERENCE_OLD_TERM_END_DATE).apply();
                    }
                }
            }
        }
        return schoolTime;
    }

    public static String getNowShowTerm(Context context) {
        SchoolTime schoolTime = (SchoolTime) DataMethod.getOfflineData(context, SchoolTime.class, SchoolTimeMethod.FILE_NAME, SchoolTimeMethod.IS_ENCRYPT);
        schoolTime = TimeMethod.termSetCheck(context, schoolTime, false);
        return getNowShowTerm(context, schoolTime);
    }

    /**
     * 获取当前显示课程的学期代码
     * 注：通过学期开始日期获取
     *
     * @param context    Context
     * @param schoolTime SchoolTime
     * @return 学期代码
     */
    static String getNowShowTerm(Context context, SchoolTime schoolTime) {
        String useTermStr;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String customStart = sharedPreferences.getString(Config.PREFERENCE_CUSTOM_TERM_START_DATE, null);
        if (customStart == null && schoolTime == null) {
            return null;
        } else if (customStart != null) {
            useTermStr = customStart;
        } else {
            useTermStr = schoolTime.getStartTime();
        }
        try {
            Date date = parseDateSDF(useTermStr);
            Calendar calendar = Calendar.getInstance(Locale.CHINA);
            calendar.setTime(date);

            StringBuilder result = new StringBuilder();
            int year = calendar.get(Calendar.YEAR);

            if (calendar.get(Calendar.MONTH) + 1 > 6) {
                result.append(year).append(year + 1).append(1);
            } else {
                result.append(year - 1).append(year).append(2);
            }
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取周数数组
     *
     * @param context    Context
     * @param schoolTime SchoolTime
     * @return 周数列表
     */
    @NonNull
    public static List<String> getWeekArray(@NonNull Context context, SchoolTime schoolTime) {
        List<String> week = new ArrayList<>();
        int maxWeek = getMaxWeekNum(schoolTime);
        if (maxWeek == 0) {
            maxWeek = Config.DEFAULT_MAX_WEEK;
        }
        for (int i = 1; i <= maxWeek; i++) {
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
    public static int getMaxWeekNum(@Nullable SchoolTime schoolTime) {
        int maxWeek = 0;
        if (schoolTime != null) {
            try {
                Date startDate = parseDateSDF(schoolTime.getStartTime());
                long startDay = startDate.getTime();
                long endDay = parseDateSDF(schoolTime.getEndTime()).getTime();

                Calendar calendarStart = Calendar.getInstance(Locale.CHINA);
                calendarStart.setTime(startDate);

                while (startDay < endDay) {
                    calendarStart.add(Calendar.DATE, 7);
                    startDay = calendarStart.getTimeInMillis();
                    maxWeek++;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return maxWeek;
    }


    /**
     * 获取当前是第几周
     * 主要用于非联网更新状态，但是目前主要使用，减少网络更新
     *
     * @param schoolTime SchoolTime对象
     * @return 周数
     */
    public static int getNowWeekNum(@Nullable SchoolTime schoolTime) {
        int weekNum = 0;
        if (schoolTime != null) {
            try {
                Date startDate = parseDateSDF(schoolTime.getStartTime());
                Date endDate = parseDateSDF(schoolTime.getEndTime());

                Calendar calendarStart = Calendar.getInstance(Locale.CHINA);
                calendarStart.setTime(startDate);

                Calendar calendarEnd = Calendar.getInstance(Locale.CHINA);
                calendarEnd.setTime(endDate);
                calendarEnd.add(Calendar.DATE, 1);

                Calendar calendarNow = Calendar.getInstance(Locale.CHINA);
                calendarNow.setTime(new Date());

                if (calendarNow.getTimeInMillis() < calendarStart.getTimeInMillis() || calendarNow.getTimeInMillis() > calendarEnd.getTimeInMillis()) {
                    return 0;
                }

                calendarStart.add(Calendar.DATE, calendarStart.getFirstDayOfWeek() - calendarStart.get(Calendar.DAY_OF_WEEK) + 1);
                calendarNow.add(Calendar.DATE, -7);
                if (calendarNow.getTimeInMillis() < calendarStart.getTimeInMillis()) {
                    return 1;
                }

                long startDay = calendarStart.getTimeInMillis();
                long nowDay = System.currentTimeMillis();
                while (startDay < nowDay) {
                    calendarStart.add(Calendar.DATE, 7);
                    startDay = calendarStart.getTimeInMillis();
                    weekNum++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return weekNum;
    }

    /**
     * 获取指定周每一天的的日期
     *
     * @param context         Context
     * @param weekNum         周数
     * @param startSchoolDate 开学时间（SchoolTime）
     * @return 周数与日期的列表
     */
    @NonNull
    static List<String> getWeekDayArray(Context context, int weekNum, String startSchoolDate) {
        List<String> week = new ArrayList<>();
        String[] num = context.getResources().getStringArray(R.array.week_number);
        String[] weekDayDate = getWeekDayDate(weekNum, startSchoolDate);
        for (int i = 1; i <= Config.MAX_WEEK_DAY; i++) {
            week.add((context.getString(R.string.week_day, num[i - 1]) + "\n" + weekDayDate[i - 1]).trim());
        }
        return week;
    }

    @NonNull
    private static String[] getWeekDayDate(int weekNum, String startSchoolDate) {
        String[] weekDayDate = new String[Config.MAX_WEEK_DAY];
        try {
            Date startDate = parseDateSDF(startSchoolDate);

            Calendar calendar = Calendar.getInstance(Locale.CHINA);
            calendar.setTime(startDate);
            calendar.setFirstDayOfWeek(Calendar.MONDAY);
            calendar.add(Calendar.DATE, calendar.getFirstDayOfWeek() - calendar.get(Calendar.DAY_OF_WEEK));

            calendar.add(Calendar.DATE, (weekNum - 1) * 7);

            for (int i = 0; i < Config.MAX_WEEK_DAY; i++) {
                weekDayDate[i] = (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH);
                calendar.add(Calendar.DATE, 1);
            }

        } catch (Exception e) {
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
    @NonNull
    static List<String> getCourseTimeArray(Context context) {
        List<String> day = new ArrayList<>();
        String[] time = context.getResources().getStringArray(R.array.course_start_time);
        for (int i = 1; i <= Config.MAX_DAY_COURSE; i++) {
            day.add(time[i - 1] + "\n" + i);
        }
        return day;
    }

    /**
     * 获取一个星期的数组
     *
     * @param context Context
     * @return 星期显示数组
     */
    public static String[] getWeekStrArray(Context context) {
        if (weekArray == null) {
            weekArray = new String[Config.MAX_WEEK_DAY];
            String[] num = context.getResources().getStringArray(R.array.week_number);
            for (int i = 0; i < Config.MAX_WEEK_DAY; i++) {
                weekArray[i] = context.getString(R.string.week_day, num[i]);
            }
        }
        return weekArray;
    }

    public static CountingDown getCountingDownStr(@NonNull Context context, long now, long future) {
        CountingDown countingDown = new CountingDown();
        long temp = future - now;
        if (temp > 0) {
            long day = getShowTime(temp / (3600f * 24));
            if (day > 0) {
                countingDown.time = String.valueOf(day);
                countingDown.timeUnit = context.getString(R.string.day);
            } else {
                long hour = getShowTime(temp / 3600f);
                if (hour > 0) {
                    countingDown.time = String.valueOf(hour);
                    countingDown.timeUnit = context.getString(R.string.hour);
                } else {
                    long minute = getShowTime(temp / 60f);
                    if (minute > 0) {
                        countingDown.time = String.valueOf(minute);
                        countingDown.timeUnit = context.getString(R.string.minute);
                    } else {
                        countingDown.time = String.valueOf(0);
                        countingDown.timeUnit = context.getString(R.string.minute);
                    }
                }
            }
        }
        return countingDown;
    }

    private static long getShowTime(double time) {
        return time > 1 ? (long) Math.ceil(time) : 0;
    }

    public static class CountingDown {
        public String time = null;
        public String timeUnit = null;
    }
}
