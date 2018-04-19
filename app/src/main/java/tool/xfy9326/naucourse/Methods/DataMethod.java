package tool.xfy9326.naucourse.Methods;

import android.content.Context;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;

import tool.xfy9326.naucourse.Config;
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
    public static Object getOfflineData(Context context, Class file_class, String FILE_NAME) {
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

    private static boolean checkDataVersionCode(String content, Class file_class) {
        int nowVersionCode;
        switch (file_class.getSimpleName()) {
            case "Course":
                nowVersionCode = Config.DATA_VERSION_COURSE;
                break;
            case "CourseScore":
                nowVersionCode = Config.DATA_VERSION_COURSE_SCORE;
                break;
            case "Exam":
                nowVersionCode = Config.DATA_VERSION_EXAM;
                break;
            case "JwcTopic":
                nowVersionCode = Config.DATA_VERSION_JWC_TOPIC;
                break;
            case "JwTopic":
                nowVersionCode = Config.DATA_VERSION_JW_TOPIC;
                break;
            case "NextCourse":
                nowVersionCode = Config.DATA_VERSION_NEXT_COURSE;
                break;
            case "SchoolTime":
                nowVersionCode = Config.DATA_VERSION_SCHOOL_TIME;
                break;
            case "StudentInfo":
                nowVersionCode = Config.DATA_VERSION_STUDENT_INFO;
                break;
            case "StudentLearnProcess":
                nowVersionCode = Config.DATA_VERSION_STUDENT_LEARN_PROCESS;
                break;
            case "StudentScore":
                nowVersionCode = Config.DATA_VERSION_STUDENT_SCORE;
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
