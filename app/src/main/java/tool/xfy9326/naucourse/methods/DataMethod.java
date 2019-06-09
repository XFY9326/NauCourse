package tool.xfy9326.naucourse.methods;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Objects;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.methods.netInfoMethods.AlstuMethod;
import tool.xfy9326.naucourse.methods.netInfoMethods.ExamMethod;
import tool.xfy9326.naucourse.methods.netInfoMethods.JwcInfoMethod;
import tool.xfy9326.naucourse.methods.netInfoMethods.LevelExamMethod;
import tool.xfy9326.naucourse.methods.netInfoMethods.MoaMethod;
import tool.xfy9326.naucourse.methods.netInfoMethods.PersonMethod;
import tool.xfy9326.naucourse.methods.netInfoMethods.SchoolTimeMethod;
import tool.xfy9326.naucourse.methods.netInfoMethods.ScoreMethod;
import tool.xfy9326.naucourse.methods.netInfoMethods.SuspendCourseMethod;
import tool.xfy9326.naucourse.methods.netInfoMethods.TableMethod;
import tool.xfy9326.naucourse.tools.IO;
import tool.xfy9326.naucourse.utils.Course;
import tool.xfy9326.naucourse.utils.TopicInfo;

/**
 * Created by 10696 on 2018/4/19.
 */

public class DataMethod {
    private static final String DATA_VERSION_CODE = "dataVersionCode";

    /**
     * 获取离线数据
     *
     * @param context    Context
     * @param file_class JavaBean Class
     * @param FILE_NAME  缓存数据文件名
     * @return JavaBean对象
     */
    public static <T> Object getOfflineData(Context context, @NonNull Class<T> file_class, String FILE_NAME, boolean needDecrypt) {
        Object object = null;
        String content = getOfflineContent(context, FILE_NAME, needDecrypt);
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

    private static String getOfflineContent(Context context, String FILE_NAME, boolean needDecrypt) {
        String path = getOfflineDataFilePath(context, FILE_NAME, needDecrypt);
        File file = new File(path);
        if (file.exists()) {
            String data = IO.readFile(path);
            if (needDecrypt) {
                return SecurityMethod.decryptData(context, data);
            } else {
                return data;
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
        ArrayList<Course> result = null;
        String content = getOfflineContent(context, TableMethod.FILE_NAME, TableMethod.IS_ENCRYPT);
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

    public static ArrayList<TopicInfo> getOfflineTopicInfo(Context context) {
        ArrayList<TopicInfo> result = null;
        String content = getOfflineContent(context, InfoMethod.FILE_NAME, InfoMethod.IS_ENCRYPT);
        if (content != null && !content.isEmpty()) {
            Type type = new TypeToken<ArrayList<TopicInfo>>() {
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
     * @param context     Context
     * @param o           JavaBean对象
     * @param FILE_NAME   储存的文件名
     * @param checkTemp   是否检测缓存与要储存的数据相同
     * @param needEncrypt 需要加密
     * @return 是否保存成功
     */
    public static boolean saveOfflineData(final Context context, final Object o, final String FILE_NAME, boolean checkTemp, boolean needEncrypt) {
        if (o == null) {
            return false;
        } else {
            return saveOfflineContent(context, new Gson().toJson(o), FILE_NAME, checkTemp, needEncrypt);
        }
    }

    private static boolean saveOfflineContent(final Context context, final String data, final String FILE_NAME, boolean checkTemp, boolean needEncrypt) {
        String path = getOfflineDataFilePath(context, FILE_NAME, needEncrypt);
        String content = data;
        if (needEncrypt) {
            content = SecurityMethod.encryptData(context, data);
        }
        if (checkTemp) {
            String text = IO.readFile(path);
            if (text != null) {
                text = text.replace("\n", "");
                return !text.equalsIgnoreCase(content) && IO.writeFile(Objects.requireNonNull(content), path);
            }
        }
        return IO.writeFile(Objects.requireNonNull(content), path);
    }

    static boolean saveExtraData(Object o, String path) {
        if (o != null) {
            String content = new Gson().toJson(o);
            if (content != null) {
                return IO.writeFile(content, path);
            }
        }
        return false;
    }

    static ArrayList<Course> readExtraTableData(String path) {
        ArrayList<Course> o = null;
        String content = IO.readFile(path);
        Type type = new TypeToken<ArrayList<Course>>() {
        }.getType();
        if (content != null) {
            try {
                o = new Gson().fromJson(content, type);
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
            }
        }
        return o;
    }

    /**
     * 删除离线数据
     *
     * @param context   Context
     * @param FILE_NAME 离线数据的文件名
     */
    @SuppressWarnings("SameParameterValue")
    public static void deleteOfflineData(final Context context, final String FILE_NAME, boolean isEncrypt) {
        File file = new File(getOfflineDataFilePath(context, FILE_NAME, isEncrypt));
        if (file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }

    private static boolean checkDataVersionCode(String content, Class file_class) {
        int nowVersionCode;
        String simpleName = file_class.getSimpleName();
        if (simpleName.equals(ScoreMethod.FILE_NAME)) {
            nowVersionCode = Config.DATA_VERSION_COURSE_SCORE;
        } else if (simpleName.equals(ExamMethod.FILE_NAME)) {
            nowVersionCode = Config.DATA_VERSION_EXAM;
        } else if (simpleName.equals(JwcInfoMethod.FILE_NAME)) {
            nowVersionCode = Config.DATA_VERSION_JWC_TOPIC;
        } else if (simpleName.equals(NextClassMethod.NEXT_COURSE_FILE_NAME)) {
            nowVersionCode = Config.DATA_VERSION_NEXT_COURSE;
        } else if (simpleName.equals(SchoolTimeMethod.FILE_NAME)) {
            nowVersionCode = Config.DATA_VERSION_SCHOOL_TIME;
        } else if (simpleName.equals(PersonMethod.FILE_NAME_DATA)) {
            nowVersionCode = Config.DATA_VERSION_STUDENT_INFO;
        } else if (simpleName.equals(PersonMethod.FILE_NAME_PROCESS)) {
            nowVersionCode = Config.DATA_VERSION_STUDENT_LEARN_PROCESS;
        } else if (simpleName.equals(PersonMethod.FILE_NAME_SCORE)) {
            nowVersionCode = Config.DATA_VERSION_STUDENT_SCORE;
        } else if (simpleName.equals(LevelExamMethod.FILE_NAME)) {
            nowVersionCode = Config.DATA_VERSION_LEVEL_EXAM;
        } else if (simpleName.equals(MoaMethod.FILE_NAME)) {
            nowVersionCode = Config.DATA_VERSION_MOA;
        } else if (simpleName.equals(SuspendCourseMethod.FILE_NAME)) {
            nowVersionCode = Config.DATA_VERSION_SUSPEND_COURSE;
        } else if (simpleName.equals(AlstuMethod.FILE_NAME)) {
            nowVersionCode = Config.DATA_VERSION_ALSTU_TOPIC;
        } else {
            nowVersionCode = 0;
        }

        if (nowVersionCode > 0) {
            try {
                JSONObject jsonObject = new JSONObject(content);
                if (jsonObject.has(DATA_VERSION_CODE) && !jsonObject.isNull(DATA_VERSION_CODE)) {
                    int dataVersionCode = jsonObject.getInt(DATA_VERSION_CODE);
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

    private static String getOfflineDataFilePath(Context context, String FILE_NAME, boolean encryptAble) {
        return context.getFilesDir() + File.separator + FILE_NAME + (encryptAble ? ".txn" : ".tnd");
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
