package tool.xfy9326.naucourse;

/**
 * Created by xfy9326 on 18-2-20.
 */

public class Config {
    //单周
    public static final int COURSE_DETAIL_WEEKMODE_SINGLE = 1;
    //双周
    public static final int COURSE_DETAIL_WEEKMODE_DOUBLE = 2;
    //仅一周
    public static final int COURSE_DETAIL_WEEKMODE_ONCE = 3;
    //多个连续的间断周或多个间断周
    public static final int COURSE_DETAIL_WEEKMODE_ONCE_MORE = 4;

    public static final int REQUEST_ACTIVITY_LOGIN = 1;

    public static final int RELOAD_TABLE_DELAY_TIME = 1500;

    public static final int HANDLER_RELOAD_TABLE = 0;

    public static final int VIEWPAGER_TABLE_PAGE = 1;

    public static final String INTENT_INFO_DETAIL_TITLE = "INFO_DETAIL_TITLE";
    public static final String INTENT_INFO_DETAIL_DATE = "INFO_DETAIL_DATE";
    public static final String INTENT_INFO_DETAIL_CLICK = "INFO_DETAIL_CLICK";
    public static final String INTENT_INFO_DETAIL_POST = "INFO_DETAIL_POST";
    public static final String INTENT_INFO_DETAIL_SOURCE = "INFO_DETAIL_SOURCE";
    public static final String INTENT_INFO_DETAIL_URL = "INFO_DETAIL_URL";
    public static final String INTENT_IS_ONLY_INIT = "IS_ONLY_INIT";
    public static final String INTENT_NEXT_CLASS_DATA = "NEXT_CLASS_DATA";
    public static final String INTENT_JUST_LOGIN = "JUST_LOGIN";

    public static final int DEFAULT_MAX_WEEK = 24;
    public static final int MAX_WEEK_DAY = 7;
    public static final int MAX_DAY_COURSE = 13;

    public static final String PREFERENCE_HAS_LOGIN = "HAS_LOGIN";
    public static final String PREFERENCE_REMEMBER_PW = "REMEMBER_PW";
    public static final String PREFERENCE_USER_ID = "USER_ID";
    public static final String PREFERENCE_USER_PW = "USER_PW";
    public static final String PREFERENCE_LOGIN_URL = "LOGIN_URL";
    public static final String PREFERENCE_LOGIN_OUT = "LOGIN_OUT";
    public static final String PREFERENCE_UPDATE_DATA_ON_START = "UPDATE_DATA_ON_START";
    public static final String PREFERENCE_ONLY_UPDATE_UNDER_WIFI = "ONLY_UPDATE_UNDER_WIFI";
    public static final String PREFERENCE_SHOW_NEXT_WEEK = "SHOW_NEXT_WEEK";
    public static final String PREFERENCE_LAST_NOTIFY_ID = "LAST_NOTIFY_ID";
    public static final String PREFERENCE_LAST_NOTIFY_TIME = "LAST_NOTIFY_TIME";
    public static final String PREFERENCE_CLASS_BEFORE_NOTIFY = "CLASS_BEFORE_NOTIFY";
    public static final String PREFERENCE_NOTIFY_NEXT_CLASS = "NOTIFY_NEXT_CLASS";
    public static final String PREFERENCE_SHOW_WEEKEND = "SHOW_WEEKEND";
    public static final String PREFERENCE_DEFAULT_SHOW_TABLE_PAGE = "DEFAULT_SHOW_TABLE_PAGE";

    public static final boolean DEFAULT_PREFERENCE_HAS_LOGIN = false;
    public static final boolean DEFAULT_PREFERENCE_REMEMBER_PW = true;
    public static final String DEFAULT_PREFERENCE_USER_ID = "NULL";
    public static final String DEFAULT_PREFERENCE_USER_PW = "NULL";
    public static final boolean DEFAULT_PREFERENCE_UPDATE_DATA_ON_START = true;
    public static final boolean DEFAULT_PREFERENCE_ONLY_UPDATE_UNDER_WIFI = false;
    public static final boolean DEFAULT_PREFERENCE_SHOW_NEXT_WEEK = true;
    public static final boolean DEFAULT_PREFERENCE_NOTIFY_NEXT_CLASS = false;
    public static final boolean DEFAULT_PREFERENCE_SHOW_WEEKEND = true;
    public static final boolean DEFAULT_PREFERENCE_DEFAULT_SHOW_TABLE_PAGE = false;

    //网络连接错误代码
    public static final int NET_WORK_GET_SUCCESS = 0;
    public static final int NET_WORK_ERROR_CODE_CONNECT_ERROR = 1;
    public static final int NET_WORK_ERROR_CODE_CONNECT_USER_DATA = 2;
    public static final int NET_WORK_ERROR_CODE_CONNECT_NO_LOGIN = 3;
    public static final int NET_WORK_ERROR_CODE_GET_DATA_ERROR = 4;
}
