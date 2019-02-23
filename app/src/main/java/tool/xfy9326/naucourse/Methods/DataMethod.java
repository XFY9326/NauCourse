package tool.xfy9326.naucourse.Methods;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.InfoMethods.AlstuMethod;
import tool.xfy9326.naucourse.Methods.InfoMethods.ExamMethod;
import tool.xfy9326.naucourse.Methods.InfoMethods.JwcInfoMethod;
import tool.xfy9326.naucourse.Methods.InfoMethods.LevelExamMethod;
import tool.xfy9326.naucourse.Methods.InfoMethods.MoaMethod;
import tool.xfy9326.naucourse.Methods.InfoMethods.PersonMethod;
import tool.xfy9326.naucourse.Methods.InfoMethods.SchoolTimeMethod;
import tool.xfy9326.naucourse.Methods.InfoMethods.ScoreMethod;
import tool.xfy9326.naucourse.Methods.InfoMethods.SuspendCourseMethod;
import tool.xfy9326.naucourse.Methods.InfoMethods.TableMethod;
import tool.xfy9326.naucourse.Tools.IO;
import tool.xfy9326.naucourse.Utils.Course;

/**
 * Created by 10696 on 2018/4/19.
 */

public class DataMethod {
    /**
     * 获取离线数据
     *
     * @param context    Context
     * @param file_class JavaBean Class
     * @param FILE_NAME  缓存数据文件名
     * @return JavaBean对象
     */
    public static Object getOfflineData(Context context, @NonNull Class file_class, String FILE_NAME) {
        Object object = null;
        String content = getOfflineData(context, FILE_NAME);
        if (content != null) {
            if (checkDataVersionCode(content, file_class)) {
                try {
                    object = new Gson().fromJson(content, file_class);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
        return object;
    }

    public static String getOfflineData(Context context, String FILE_NAME) {
        String path = getOfflineDataFilePath(context, FILE_NAME);
        File file = new File(path);
        if (file.exists()) {
            String data = IO.readFile(path);
            return SecurityMethod.decryptData(context, data);
        }
        return null;
    }

    /**
     * 获取离线课表数据
     *
     * @param context Context
     * @return 课表信息列表
     */
    public static ArrayList<Course> getOfflineTableData(Context context) {
        ArrayList<Course> result = null;
        String content = getOfflineData(context, TableMethod.FILE_NAME);
        if (content != null && !content.isEmpty()) {
            Type type = new TypeToken<ArrayList<Course>>() {
            }.getType();
            try {
                result = new Gson().fromJson(content, type);
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
            }
        }
        return result;
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
    public static boolean saveOfflineData(final Context context, final Object o, final String FILE_NAME, boolean checkTemp) {
        return saveOfflineData(context, new Gson().toJson(o), FILE_NAME, checkTemp);
    }

    @SuppressWarnings("UnusedReturnValue")
    public static boolean saveOfflineData(final Context context, final String data, final String FILE_NAME, boolean checkTemp) {
        String path = getOfflineDataFilePath(context, FILE_NAME);
        String content = SecurityMethod.encryptData(context, data);
        if (checkTemp) {
            String text = IO.readFile(path);
            if (text != null) {
                text = text.replace("\n", "");
                return !text.equalsIgnoreCase(content) && IO.writeFile(Objects.requireNonNull(content), path);
            }
        }
        return IO.writeFile(Objects.requireNonNull(content), path);
    }

    /**
     * 删除离线数据
     *
     * @param context   Context
     * @param FILE_NAME 离线数据的文件名
     */
    @SuppressWarnings("SameParameterValue")
    public static void deleteOfflineData(final Context context, final String FILE_NAME) {
        File file = new File(getOfflineDataFilePath(context, FILE_NAME));
        if (file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }

    private static boolean checkDataVersionCode(String content, Class file_class) {
        int nowVersionCode;
        switch (file_class.getSimpleName()) {
            case ScoreMethod.FILE_NAME:
                nowVersionCode = Config.DATA_VERSION_COURSE_SCORE;
                break;
            case ExamMethod.FILE_NAME:
                nowVersionCode = Config.DATA_VERSION_EXAM;
                break;
            case JwcInfoMethod.FILE_NAME:
                nowVersionCode = Config.DATA_VERSION_JWC_TOPIC;
                break;
            case NextClassMethod.NEXT_COURSE_FILE_NAME:
                nowVersionCode = Config.DATA_VERSION_NEXT_COURSE;
                break;
            case SchoolTimeMethod.FILE_NAME:
                nowVersionCode = Config.DATA_VERSION_SCHOOL_TIME;
                break;
            case PersonMethod.FILE_NAME_DATA:
                nowVersionCode = Config.DATA_VERSION_STUDENT_INFO;
                break;
            case PersonMethod.FILE_NAME_PROCESS:
                nowVersionCode = Config.DATA_VERSION_STUDENT_LEARN_PROCESS;
                break;
            case PersonMethod.FILE_NAME_SCORE:
                nowVersionCode = Config.DATA_VERSION_STUDENT_SCORE;
                break;
            case LevelExamMethod.FILE_NAME:
                nowVersionCode = Config.DATA_VERSION_LEVEL_EXAM;
                break;
            case MoaMethod.FILE_NAME:
                nowVersionCode = Config.DATA_VERSION_MOA;
                break;
            case SuspendCourseMethod.FILE_NAME:
                nowVersionCode = Config.DATA_VERSION_SUSPEND_COURSE;
                break;
            case AlstuMethod.FILE_NAME:
                nowVersionCode = Config.DATA_VERSION_ALSTU_TOPIC;
                break;
            default:
                nowVersionCode = 0;
                break;
        }

        if (nowVersionCode > 0) {
            try {
                JSONObject jsonObject = new JSONObject(content);
                if (jsonObject.has("dataVersionCode") && !jsonObject.isNull("dataVersionCode")) {
                    int dataVersionCode = jsonObject.getInt("dataVersionCode");
                    if (dataVersionCode == nowVersionCode) {
                        return true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private static String getOfflineDataFilePath(Context context, String FILE_NAME) {
        return context.getFilesDir() + File.separator + FILE_NAME + ".txn";
    }

    public static class InfoData {
        public static boolean[] getInfoChannel(Context context) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            return new boolean[]{
                    sharedPreferences.getBoolean(Config.PREFERENCE_INFO_CHANNEL_SELECTED_JWC_SYSTEM, Config.DEFAULT_PREFERENCE_INFO_CHANNEL_SELECTED_JWC_SYSTEM),
                    sharedPreferences.getBoolean(Config.PREFERENCE_INFO_CHANNEL_SELECTED_JW, Config.DEFAULT_PREFERENCE_INFO_CHANNEL_SELECTED_JW),
                    sharedPreferences.getBoolean(Config.PREFERENCE_INFO_CHANNEL_SELECTED_XW, Config.DEFAULT_PREFERENCE_INFO_CHANNEL_SELECTED_XW),
                    sharedPreferences.getBoolean(Config.PREFERENCE_INFO_CHANNEL_SELECTED_TW, Config.DEFAULT_PREFERENCE_INFO_CHANNEL_SELECTED_TW),
                    sharedPreferences.getBoolean(Config.PREFERENCE_INFO_CHANNEL_SELECTED_XXB, Config.DEFAULT_PREFERENCE_INFO_CHANNEL_SELECTED_XXB),
                    sharedPreferences.getBoolean(Config.PREFERENCE_INFO_CHANNEL_SELECTED_ALSTU, Config.DEFAULT_PREFERENCE_INFO_CHANNEL_SELECTED_ALSTU)
            };
        }

        public static void setInfoChannel(Context context, boolean[] channel) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            sharedPreferences.edit().putBoolean(Config.PREFERENCE_INFO_CHANNEL_SELECTED_JWC_SYSTEM, channel[0])
                    .putBoolean(Config.PREFERENCE_INFO_CHANNEL_SELECTED_JW, channel[1])
                    .putBoolean(Config.PREFERENCE_INFO_CHANNEL_SELECTED_XW, channel[2])
                    .putBoolean(Config.PREFERENCE_INFO_CHANNEL_SELECTED_TW, channel[3])
                    .putBoolean(Config.PREFERENCE_INFO_CHANNEL_SELECTED_XXB, channel[4])
                    .putBoolean(Config.PREFERENCE_INFO_CHANNEL_SELECTED_ALSTU, channel[5]).apply();
        }
    }
}
