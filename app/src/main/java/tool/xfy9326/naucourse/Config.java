package tool.xfy9326.naucourse;

import android.os.Environment;

import java.io.File;

/**
 * Created by xfy9326 on 18-2-20.
 */

public class Config {
    @SuppressWarnings("unused")
    public static final int SUB_VERSION = BuildConfig.SUB_VERSION;
    @SuppressWarnings("unused")
    public static final String VERSION_TYPE = BuildConfig.VERSION_TYPE;

    //单周
    public static final int COURSE_DETAIL_WEEKMODE_SINGLE = 1;
    //双周
    public static final int COURSE_DETAIL_WEEKMODE_DOUBLE = 2;
    //仅一周
    public static final int COURSE_DETAIL_WEEKMODE_ONCE = 3;
    //多个连续的间断周或多个间断周
    public static final int COURSE_DETAIL_WEEKMODE_ONCE_MORE = 4;

    public static final int RE_LOGIN_SUCCESS = 0;
    public static final int RE_LOGIN_FAILED = 1;
    public static final int RE_LOGIN_TRYING = 2;

    public static final int REQUEST_ACTIVITY_LOGIN = 1;

    public static final int HANDLER_RELOAD_TABLE = 0;
    public static final int HANDLER_RELOAD_TABLE_DATA = 1;

    public static final int VIEWPAGER_TABLE_PAGE = 1;

    public static final int TASK_RUN_MAX_SECOND = 100;

    public static final int WATER_PRINT_TEXT_SIZE = 14;

    public static final String DATA_DIR_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "NauCourse" + File.separator;
    public static final String PICTURE_DICTIONARY_PATH = DATA_DIR_PATH + "picture" + File.separator;
    public static final String PICTURE_TEMP_DICTIONARY_PATH = PICTURE_DICTIONARY_PATH + ".temp" + File.separator;
    public static final String COURSE_TABLE_IMAGE_FILE_NAME = "CourseTable.jpeg";
    public static final String INFO_DETAIL_IMAGE_FILE_NAME = "InfoDetail.jpeg";
    public static final String SCHOOL_CALENDAR_IMAGE_FILE_NAME = "SchoolCalendar.jpeg";

    public static final String FILE_PROVIDER_AUTH = "tool.xfy9326.naucourse.provider";

    public static final String DEBUG = "debug";

    public static final String ASSETS_EULA_PATH = "EULA.txt";

    public static final String INTENT_INFO_DETAIL = "INFO_DETAIL";
    public static final String INTENT_IS_ONLY_INIT = "IS_ONLY_INIT";
    public static final String INTENT_NEXT_CLASS_DATA = "NEXT_CLASS_DATA";
    public static final String INTENT_JUST_LOGIN = "JUST_LOGIN";
    public static final String INTENT_STUDENT_INFO = "STUDENT_INFO";
    public static final String INTENT_STUDENT_LEARN_PROCESS = "STUDENT_LEARN_PROCESS";
    public static final String INTENT_EDIT_COURSE_ITEM = "EDIT_COURSE";
    public static final String INTENT_ADD_COURSE = "ADD_COURSE";
    public static final String INTENT_ADD_COURSE_TERM = "ADD_COURSE_TERM";
    public static final String INTENT_EDIT_COURSE = "EDIT_COURSE";
    public static final String INTENT_VIEW_PAGER_POSITION = "VIEW_PAGER_POSITION";

    public static final int DEFAULT_MAX_WEEK = 21;
    public static final int MAX_WEEK_DAY = 7;
    public static final int MAX_DAY_COURSE = 13;

    public static final String CUSTOM_COURSE_PREFIX = "Custom";
    public static final String SEARCH_COURSE_PREFIX = "Search";

    public static final String PREFERENCE_HAS_LOGIN = "HAS_LOGIN";
    public static final String PREFERENCE_USER_ID = "USER_ID";
    public static final String PREFERENCE_USER_PW = "USER_PW";
    public static final String PREFERENCE_LOGIN_URL = "LOGIN_URL";
    public static final String PREFERENCE_UPDATE_DATA_ON_START = "UPDATE_DATA_ON_START";
    public static final String PREFERENCE_ONLY_UPDATE_UNDER_WIFI = "ONLY_UPDATE_UNDER_WIFI";
    public static final String PREFERENCE_SHOW_NEXT_WEEK = "SHOW_NEXT_WEEK";
    public static final String PREFERENCE_LAST_NOTIFY_ID = "LAST_NOTIFY_ID";
    public static final String PREFERENCE_LAST_NOTIFY_TIME = "LAST_NOTIFY_TIME";
    public static final String PREFERENCE_CLASS_BEFORE_NOTIFY = "CLASS_BEFORE_NOTIFY";
    public static final String PREFERENCE_NOTIFY_NEXT_CLASS = "NOTIFY_NEXT_CLASS";
    public static final String PREFERENCE_DEFAULT_SHOW_TABLE_PAGE = "DEFAULT_SHOW_TABLE_PAGE";
    public static final String PREFERENCE_SHOW_WIDE_TABLE = "SHOW_WIDE_TABLE";
    public static final String PREFERENCE_COURSE_TABLE_CELL_COLOR = "COURSE_TABLE_CELL_COLOR";
    public static final String PREFERENCE_NETWORK_ACCOUNT = "NETWORK_ACCOUNT";
    public static final String PREFERENCE_NETWORK_PASSWORD = "NETWORK_PASSWORD";
    public static final String PREFERENCE_NETWORK_REMEMBER_PASSWORD = "NETWORK_REMEMBER_PASSWORD";
    public static final String PREFERENCE_COURSE_TABLE_SHOW_BACKGROUND = "COURSE_TABLE_SHOW_BACKGROUND";
    public static final String PREFERENCE_CHANGE_TABLE_TRANSPARENCY = "CHANGE_TABLE_TRANSPARENCY";
    public static final String PREFERENCE_COURSE_TABLE_SHOW_SINGLE_COLOR = "COURSE_TABLE_SHOW_SINGLE_COLOR";
    public static final String PREFERENCE_SCHOOL_CALENDAR_URL = "SCHOOL_CALENDAR_URL";
    public static final String PREFERENCE_PERSONAL_INFO_LOAD_DATE_TIME = "PERSONAL_INFO_LOAD_DATE_TIME";
    public static final String PREFERENCE_ASYNC_PERSONAL_INFO_BY_DAY = "ASYNC_PERSONAL_INFO_BY_DAY";
    public static final String PREFERENCE_COURSE_TABLE_AUTO_LOAD_DATE_TIME = "COURSE_TABLE_AUTO_LOAD_DATE_TIME";
    public static final String PREFERENCE_AUTO_UPDATE_COURSE_TABLE = "AUTO_UPDATE_COURSE_TABLE";
    public static final String PREFERENCE_SHOW_NO_THIS_WEEK_CLASS = "SHOW_NO_THIS_WEEK_CLASS";
    public static final String PREFERENCE_AUTO_UPDATE_COURSE_TABLE_ALERT = "AUTO_UPDATE_COURSE_TABLE_ALERT";
    public static final String PREFERENCE_AUTO_CHECK_UPDATE = "AUTO_CHECK_UPDATE";
    public static final String PREFERENCE_AUTO_CHECK_IMPORTANT_UPDATE = "AUTO_CHECK_IMPORTANT_UPDATE";
    public static final String PREFERENCE_CHECK_BETA_UPDATE = "CHECK_BETA_UPDATE";
    public static final String PREFERENCE_UPDATE_NOW = "UPDATE_NOW";
    public static final String PREFERENCE_ONLY_UPDATE_APPLICATION_UNDER_WIFI = "ONLY_UPDATE_APPLICATION_UNDER_WIFI";
    public static final String PREFERENCE_LAST_CHECK_VERSION = "LAST_CHECK_VERSION";
    public static final String PREFERENCE_CUSTOM_TERM_START_DATE = "CUSTOM_TERM_START_DATE";
    public static final String PREFERENCE_CUSTOM_TERM_END_DATE = "CUSTOM_TERM_END_DATE";
    public static final String PREFERENCE_OLD_TERM_START_DATE = "OLD_TERM_START_DATE";
    public static final String PREFERENCE_OLD_TERM_END_DATE = "OLD_TERM_END_DATE";
    public static final String PREFERENCE_SCHOOL_CALENDAR_ENLARGE_ALERT = "SCHOOL_CALENDAR_ENLARGE_ALERT";
    public static final String PREFERENCE_INFO_CHANNEL_SELECTED_JWC_SYSTEM = "INFO_CHANNEL_SELECTED_JWC_SYSTEM";
    public static final String PREFERENCE_INFO_CHANNEL_SELECTED_JW = "INFO_CHANNEL_SELECTED_JW";
    public static final String PREFERENCE_INFO_CHANNEL_SELECTED_XW = "INFO_CHANNEL_SELECTED_XW";
    public static final String PREFERENCE_INFO_CHANNEL_SELECTED_TW = "INFO_CHANNEL_SELECTED_TW";
    public static final String PREFERENCE_INFO_CHANNEL_SELECTED_XXB = "INFO_CHANNEL_SELECTED_XXB";
    public static final String PREFERENCE_INFO_CHANNEL_SELECTED_ALSTU = "INFO_CHANNEL_SELECTED_ALSTU";
    public static final String PREFERENCE_HIDE_OUT_OF_DATE_EXAM = "HIDE_OUT_OF_DATE_EXAM";
    public static final String PREFERENCE_UPDATE_TABLE_EVERY_TIME = "UPDATE_TABLE_EVERY_TIME";
    public static final String PREFERENCE_SCHOOL_VPN_MODE = "SCHOOL_VPN_MODE";
    public static final String PREFERENCE_SCHOOL_VPN_SMART_MODE = "SCHOOL_VPN_SMART_MODE";
    public static final String PREFERENCE_NEW_VERSION_INFO = "NEW_VERSION_INFO";
    public static final String PREFERENCE_EULA_ACCEPT = "EULA_ACCEPT_V1";
    public static final String PREFERENCE_NO_SHOW_LOGIN_ATTENTION = "NO_SHOW_LOGIN_ATTENTION";
    public static final String PREFERENCE_SHOW_HIDDEN_FUNCTION = "SHOW_HIDDEN_FUNCTION";
    public static final String PREFERENCE_DOWNLOAD_VPN_FILE_WARNING = "DOWNLOAD_VPN_FILE_WARNING";

    public static final boolean DEFAULT_PREFERENCE_HAS_LOGIN = false;
    public static final String DEFAULT_PREFERENCE_USER_ID = "NULL";
    public static final String DEFAULT_PREFERENCE_USER_PW = "NULL";
    public static final boolean DEFAULT_PREFERENCE_UPDATE_DATA_ON_START = true;
    public static final boolean DEFAULT_PREFERENCE_ONLY_UPDATE_UNDER_WIFI = false;
    public static final boolean DEFAULT_PREFERENCE_SHOW_NEXT_WEEK = true;
    public static final boolean DEFAULT_PREFERENCE_NOTIFY_NEXT_CLASS = true;
    public static final boolean DEFAULT_PREFERENCE_DEFAULT_SHOW_TABLE_PAGE = false;
    public static final boolean DEFAULT_PREFERENCE_SHOW_WIDE_TABLE = false;
    public static final boolean DEFAULT_PREFERENCE_COURSE_TABLE_CELL_COLOR = true;
    public static final boolean DEFAULT_PREFERENCE_COURSE_TABLE_SHOW_BACKGROUND = false;
    public static final float DEFAULT_PREFERENCE_CHANGE_TABLE_TRANSPARENCY = 0.8f;
    public static final boolean DEFAULT_PREFERENCE_COURSE_TABLE_SHOW_SINGLE_COLOR = false;
    public static final boolean DEFAULT_PREFERENCE_ASYNC_PERSONAL_INFO_BY_DAY = true;
    public static final boolean DEFAULT_PREFERENCE_AUTO_UPDATE_COURSE_TABLE = true;
    public static final boolean DEFAULT_PREFERENCE_SHOW_NO_THIS_WEEK_CLASS = true;
    public static final boolean DEFAULT_PREFERENCE_AUTO_UPDATE_COURSE_TABLE_ALERT = false;
    public static final boolean DEFAULT_PREFERENCE_AUTO_CHECK_UPDATE = true;
    public static final boolean DEFAULT_PREFERENCE_AUTO_CHECK_IMPORTANT_UPDATE = false;
    public static final boolean DEFAULT_PREFERENCE_CHECK_BETA_UPDATE = false;
    public static final boolean DEFAULT_PREFERENCE_ONLY_UPDATE_APPLICATION_UNDER_WIFI = false;
    public static final boolean DEFAULT_PREFERENCE_SCHOOL_CALENDAR_ENLARGE_ALERT = true;
    public static final boolean DEFAULT_PREFERENCE_INFO_CHANNEL_SELECTED_JWC_SYSTEM = true;
    public static final boolean DEFAULT_PREFERENCE_INFO_CHANNEL_SELECTED_JW = true;
    public static final boolean DEFAULT_PREFERENCE_INFO_CHANNEL_SELECTED_XW = true;
    public static final boolean DEFAULT_PREFERENCE_INFO_CHANNEL_SELECTED_TW = true;
    public static final boolean DEFAULT_PREFERENCE_INFO_CHANNEL_SELECTED_XXB = true;
    public static final boolean DEFAULT_PREFERENCE_INFO_CHANNEL_SELECTED_ALSTU = true;
    public static final boolean DEFAULT_PREFERENCE_HIDE_OUT_OF_DATE_EXAM = false;
    public static final boolean DEFAULT_PREFERENCE_UPDATE_TABLE_EVERY_TIME = false;
    public static final boolean DEFAULT_PREFERENCE_SCHOOL_VPN_MODE = false;
    public static final boolean DEFAULT_PREFERENCE_SCHOOL_VPN_SMART_MODE = true;
    public static final int DEFAULT_PREFERENCE_NEW_VERSION_INFO = 0;
    public static final boolean DEFAULT_PREFERENCE_EULA_ACCEPT = false;
    public static final boolean DEFAULT_PREFERENCE_NO_SHOW_LOGIN_ATTENTION = false;
    public static final boolean DEFAULT_PREFERENCE_SHOW_HIDDEN_FUNCTION = false;
    public static final boolean DEFAULT_PREFERENCE_DOWNLOAD_VPN_FILE_WARNING = true;

    //网络连接错误代码
    public static final int NET_WORK_GET_SUCCESS = 0;
    public static final int NET_WORK_ERROR_CODE_CONNECT_ERROR = 1;
    public static final int NET_WORK_ERROR_CODE_CONNECT_USER_DATA = 2;
    public static final int NET_WORK_ERROR_CODE_CONNECT_NO_LOGIN = 3;
    public static final int NET_WORK_ERROR_CODE_GET_DATA_ERROR = 4;
    public static final int NET_WORK_ERROR_CODE_TIME_OUT = 5;

    //离线数据版本号
    public static final int DATA_VERSION_COURSE_SCORE = 1;
    public static final int DATA_VERSION_EXAM = 1;
    public static final int DATA_VERSION_JWC_TOPIC = 1;
    public static final int DATA_VERSION_NEXT_COURSE = 1;
    public static final int DATA_VERSION_SCHOOL_TIME = 1;
    public static final int DATA_VERSION_STUDENT_INFO = 1;
    public static final int DATA_VERSION_STUDENT_LEARN_PROCESS = 1;
    public static final int DATA_VERSION_STUDENT_SCORE = 1;
    public static final int DATA_VERSION_LEVEL_EXAM = 1;
    public static final int DATA_VERSION_MOA = 1;
    public static final int DATA_VERSION_ALSTU_TOPIC = 1;
    public static final int DATA_VERSION_SUSPEND_COURSE = 1;

    public static final String DONATE_URL_ALIPAY = "https://www.xfy9326.top/api/donate/alipay.jpg";
    public static final String DONATE_URL_WECHAT = "https://www.xfy9326.top/api/donate/wechat.png";
    public static final String DONATE_URL_QQ_WALLET = "https://www.xfy9326.top/api/donate/qq_wallet.png";

    public static final String DONATE_PERSON_URL = "https://www.xfy9326.top/api/donate/naucourse/personList.php";
}
