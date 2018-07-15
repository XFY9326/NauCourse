package tool.xfy9326.naucourse;

import android.os.Environment;

import java.io.File;

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

    public static final int RE_LOGIN_SUCCESS = 0;
    public static final int RE_LOGIN_FAILED = 1;
    public static final int RE_LOGIN_TRYING = 2;

    public static final int REQUEST_ACTIVITY_LOGIN = 1;

    public static final int HANDLER_RELOAD_TABLE = 0;
    public static final int HANDLER_RELOAD_TABLE_DATA = 1;

    public static final int VIEWPAGER_TABLE_PAGE = 1;

    public static final String PICTURE_DICTIONARY_PATH = Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_PICTURES + File.separator + "NAU_Course" + File.separator;
    public static final String COURSE_TABLE_FILE_NAME = "CourseTable.jpeg";

    public static final String FILE_PROVIDER_AUTH = "tool.xfy9326.naucourse.provider";

    public static final String INTENT_INFO_DETAIL = "INFO_DETAIL";
    public static final String INTENT_IS_ONLY_INIT = "IS_ONLY_INIT";
    public static final String INTENT_NEXT_CLASS_DATA = "NEXT_CLASS_DATA";
    public static final String INTENT_JUST_LOGIN = "JUST_LOGIN";
    public static final String INTENT_STUDENT_INFO = "STUDENT_INFO";
    public static final String INTENT_STUDENT_LEARN_PROCESS = "STUDENT_LEARN_PROCESS";
    public static final String INTENT_EDIT_COURSE_ITEM = "EDIT_COURSE";
    public static final String INTENT_ADD_COURSE = "ADD_COURSE";
    public static final String INTENT_EDIT_COURSE = "EDIT_COURSE";

    public static final int DEFAULT_MAX_WEEK = 24;
    public static final int MAX_WEEK_DAY = 7;
    public static final int MAX_DAY_COURSE = 13;

    public static final String PREFERENCE_HAS_LOGIN = "HAS_LOGIN";
    public static final String PREFERENCE_REMEMBER_PW = "REMEMBER_PW";
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
    public static final String PREFERENCE_SHOW_WEEKEND = "SHOW_WEEKEND";
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
    public static final String PREFERENCE_PERSONAL_INFO_LOAD_DATE = "PERSONAL_INFO_LOAD_DATE";
    public static final String PREFERENCE_ASYNC_PERSONAL_INFO_BY_DAY = "ASYNC_PERSONAL_INFO_BY_DAY";
    public static final String PREFERENCE_COURSE_TABLE_LOAD_DATE = "COURSE_TABLE_LOAD_DATE";
    public static final String PREFERENCE_AUTO_UPDATE_COURSE_TABLE = "AUTO_UPDATE_COURSE_TABLE";
    public static final String PREFERENCE_SHOW_NO_THIS_WEEK_CLASS = "SHOW_NO_THIS_WEEK_CLASS";
    public static final String PREFERENCE_AUTO_UPDATE_COURSE_TABLE_ALERT = "AUTO_UPDATE_COURSE_TABLE_ALERT";

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
    public static final boolean DEFAULT_PREFERENCE_SHOW_WIDE_TABLE = false;
    public static final boolean DEFAULT_PREFERENCE_COURSE_TABLE_CELL_COLOR = true;
    public static final boolean DEFAULT_PREFERENCE_COURSE_TABLE_SHOW_BACKGROUND = false;
    public static final float DEFAULT_PREFERENCE_CHANGE_TABLE_TRANSPARENCY = 0.8f;
    public static final boolean DEFAULT_PREFERENCE_COURSE_TABLE_SHOW_SINGLE_COLOR = false;
    public static final boolean DEFAULT_PREFERENCE_ASYNC_PERSONAL_INFO_BY_DAY = true;
    public static final boolean DEFAULT_PREFERENCE_AUTO_UPDATE_COURSE_TABLE = true;
    public static final boolean DEFAULT_PREFERENCE_SHOW_NO_THIS_WEEK_CLASS = false;
    public static final boolean DEFAULT_PREFERENCE_AUTO_UPDATE_COURSE_TABLE_ALERT = false;

    //网络连接错误代码
    public static final int NET_WORK_GET_SUCCESS = 0;
    public static final int NET_WORK_ERROR_CODE_CONNECT_ERROR = 1;
    public static final int NET_WORK_ERROR_CODE_CONNECT_USER_DATA = 2;
    public static final int NET_WORK_ERROR_CODE_CONNECT_NO_LOGIN = 3;
    public static final int NET_WORK_ERROR_CODE_GET_DATA_ERROR = 4;

    //离线数据版本号
    public static final int DATA_VERSION_COURSE = 2;
    public static final int DATA_VERSION_COURSE_SCORE = 1;
    public static final int DATA_VERSION_EXAM = 1;
    public static final int DATA_VERSION_JWC_TOPIC = 1;
    public static final int DATA_VERSION_JW_TOPIC = 1;
    public static final int DATA_VERSION_NEXT_COURSE = 1;
    public static final int DATA_VERSION_SCHOOL_TIME = 1;
    public static final int DATA_VERSION_STUDENT_INFO = 1;
    public static final int DATA_VERSION_STUDENT_LEARN_PROCESS = 1;
    public static final int DATA_VERSION_STUDENT_SCORE = 1;
    public static final int DATA_VERSION_LEVEL_EXAM = 1;
    public static final int DATA_VERSION_MOA = 1;
    public static final int DATA_VERSION_SUSPEND_COURSE = 1;
}
