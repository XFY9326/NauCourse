package tool.xfy9326.naucourse.Methods;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Objects;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Fragments.HomeFragment;
import tool.xfy9326.naucourse.Tools.AES;
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
        String path = context.getFilesDir() + File.separator + FILE_NAME;
        File file = new File(path);
        if (file.exists()) {
            String data = IO.readFile(path);
            String id = PreferenceManager.getDefaultSharedPreferences(context).getString(Config.PREFERENCE_USER_ID, Config.DEFAULT_PREFERENCE_USER_ID);
            String content = AES.decrypt(data, id);
            if (checkDataVersionCode(content, file_class)) {
                return new Gson().fromJson(content, file_class);
            }
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
        String path = context.getFilesDir() + File.separator + TableMethod.FILE_NAME;
        File file = new File(path);
        if (file.exists()) {
            String data = IO.readFile(path);
            String id = PreferenceManager.getDefaultSharedPreferences(context).getString(Config.PREFERENCE_USER_ID, Config.DEFAULT_PREFERENCE_USER_ID);
            Type type = new TypeToken<ArrayList<Course>>() {
            }.getType();
            System.gc();
            String content = AES.decrypt(data, id);
            return new Gson().fromJson(content, type);
        }
        return null;
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
        File file = new File(context.getFilesDir() + File.separator + FILE_NAME);
        if (file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }

    private static boolean checkDataVersionCode(String content, Class file_class) {
        int nowVersionCode;
        switch (file_class.getSimpleName()) {
            case TableMethod.FILE_NAME:
                nowVersionCode = Config.DATA_VERSION_COURSE;
                break;
            case ScoreMethod.FILE_NAME:
                nowVersionCode = Config.DATA_VERSION_COURSE_SCORE;
                break;
            case ExamMethod.FILE_NAME:
                nowVersionCode = Config.DATA_VERSION_EXAM;
                break;
            case JwcInfoMethod.FILE_NAME:
                nowVersionCode = Config.DATA_VERSION_JWC_TOPIC;
                break;
            case JwInfoMethod.FILE_NAME:
                nowVersionCode = Config.DATA_VERSION_JW_TOPIC;
                break;
            case HomeFragment.NEXT_COURSE_FILE_NAME:
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
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
