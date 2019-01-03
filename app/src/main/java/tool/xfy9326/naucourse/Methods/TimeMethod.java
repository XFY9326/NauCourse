package tool.xfy9326.naucourse.Methods;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Utils.SchoolTime;

/**
 * Created by 10696 on 2018/4/19.
 */

public class TimeMethod {

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
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
            Date date = simpleDateFormat.parse(useTermStr);
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
        } catch (ParseException e) {
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
    public static int getMaxWeekNum(@Nullable SchoolTime schoolTime) {
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
    public static int getNowWeekNum(@Nullable SchoolTime schoolTime) {
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
    @NonNull
    static List<String> getWeekDayArray(Context context, int week_num, String startSchoolDate) {
        List<String> week = new ArrayList<>();
        String[] num = context.getResources().getStringArray(R.array.week_number);
        String[] week_day_date = getWeekDayDate(week_num, startSchoolDate);
        for (int i = 1; i <= Config.MAX_WEEK_DAY; i++) {
            week.add((context.getString(R.string.week_day, num[i - 1]) + "\n" + week_day_date[i - 1]).trim());
        }
        return week;
    }

    @NonNull
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
    @NonNull
    static List<String> getCourseTimeArray(Context context) {
        List<String> day = new ArrayList<>();
        String[] time = context.getResources().getStringArray(R.array.course_start_time);
        for (int i = 1; i <= Config.MAX_DAY_COURSE; i++) {
            day.add(time[i - 1] + "\n" + i);
        }
        return day;
    }
}
