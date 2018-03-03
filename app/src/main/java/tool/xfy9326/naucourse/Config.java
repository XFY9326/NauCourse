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
    public static final int REQUEST_ACTIVITY_SETTINGS_LOGIN_OUT = 2;

    public static final String INTENT_INFO_DETAIL_TITLE = "INFO_DETAIL_TITLE";
    public static final String INTENT_INFO_DETAIL_DATE = "INFO_DETAIL_DATE";
    public static final String INTENT_INFO_DETAIL_CLICK = "INFO_DETAIL_CLICK";
    public static final String INTENT_INFO_DETAIL_POST = "INFO_DETAIL_POST";
    public static final String INTENT_INFO_DETAIL_SOURCE = "INFO_DETAIL_SOURCE";
    public static final String INTENT_INFO_DETAIL_URL = "INFO_DETAIL_URL";
    public static final String INTENT_IS_LOGIN_OUT = "IS_LOGIN_OUT";

    public static final int DEFAULT_MAX_WEEK = 24;
    public static final int MAX_WEEK_DAY = 5;
    public static final int MAX_DAY_COURSE = 13;

    public static final String PREFERENCE_HAS_LOGIN = "HAS_LOGIN";
    public static final String PREFERENCE_REMEMBER_PW = "REMEMBER_PW";
    public static final String PREFERENCE_USER_ID = "USER_ID";
    public static final String PREFERENCE_USER_PW = "USER_PW";
    public static final String PREFERENCE_LOGIN_URL = "LOGIN_URL";
    public static final String PREFERENCE_LOGIN_OUT = "LOGIN_OUT";
    public static final String PREFERENCE_UPDATE_DATA_ON_START = "UPDATE_DATA_ON_START";
    public static final String PREFERENCE_ONLY_UPDATE_UNDER_WIFI = "ONLY_UPDATE_UNDER_WIFI";

    public static final boolean DEFAULT_PREFERENCE_HAS_LOGIN = false;
    public static final boolean DEFAULT_PREFERENCE_REMEMBER_PW = true;
    public static final String DEFAULT_PREFERENCE_USER_ID = "NULL";
    public static final String DEFAULT_PREFERENCE_USER_PW = "NULL";
    public static final boolean DEFAULT_PREFERENCE_UPDATE_DATA_ON_START = true;
    public static final boolean DEFAULT_PREFERENCE_ONLY_UPDATE_UNDER_WIFI = false;

    public static final String TAG_TEMP_SAVE_FAILED = "TEMP_SAVE_FAILED";

    public static final int NET_WORK_GET_SUCCESS = 0;
    public static final int NET_WORK_ERROR_CODE_CONNECT_ERROR = 1;
    public static final int NET_WORK_ERROR_CODE_CONNECT_USER_DATA = 2;
    public static final int NET_WORK_ERROR_CODE_CONNECT_NO_LOGIN = 3;
    public static final int NET_WORK_ERROR_CODE_GET_DATA_ERROR = 4;
}
