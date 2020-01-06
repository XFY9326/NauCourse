package tool.xfy9326.naucourse.methods.io;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Objects;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.beans.course.Course;
import tool.xfy9326.naucourse.beans.info.TopicInfo;
import tool.xfy9326.naucourse.methods.async.AlstuMethod;
import tool.xfy9326.naucourse.methods.async.ExamMethod;
import tool.xfy9326.naucourse.methods.async.HistoryScoreMethod;
import tool.xfy9326.naucourse.methods.async.JwcInfoMethod;
import tool.xfy9326.naucourse.methods.async.LevelExamMethod;
import tool.xfy9326.naucourse.methods.async.PersonMethod;
import tool.xfy9326.naucourse.methods.async.SchoolTimeMethod;
import tool.xfy9326.naucourse.methods.async.ScoreMethod;
import tool.xfy9326.naucourse.methods.async.SuspendCourseMethod;
import tool.xfy9326.naucourse.methods.async.TableMethod;
import tool.xfy9326.naucourse.methods.compute.InfoMethod;
import tool.xfy9326.naucourse.methods.compute.NextClassMethod;
import tool.xfy9326.naucourse.methods.net.SecurityMethod;
import tool.xfy9326.naucourse.tools.FileUtils;

/**
 * Created by 10696 on 2018/4/19.
 */

public class DataMethod {
    private static final String DATA_VERSION_CODE = "dataVersionCode";
    private static final String ENCRYPTED_FILE_PREFIX = ".txn";
    private static final String NOT_ENCRYPTED_FILE_PREFIX = ".tnd";

    @SuppressWarnings("SameParameterValue")
    public static String readAssetsText(Context context, String path) {
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(context.getResources().getAssets().open(path));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line;
            StringBuilder result = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }
            reader.close();
            inputStreamReader.close();
            return result.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取离线数据
     *
     * @param context   Context
     * @param fileClass JavaBean Class
     * @param fileName  缓存数据文件名
     * @return JavaBean对象
     */
    public static <T> Object getOfflineData(Context context, @NonNull Class<T> fileClass, String fileName, boolean needDecrypt) {
        Object object = null;
        String content = getOfflineContent(context, fileName, needDecrypt);
        if (content != null) {
            if (checkDataVersionCode(content, fileClass)) {
                try {
                    object = new Gson().fromJson(content, fileClass);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
        return object;
    }

    private static String getOfflineContent(Context context, String fileName, boolean needDecrypt) {
        String path = getOfflineDataFilePath(context, fileName, needDecrypt);
        File file = new File(path);
        if (file.exists()) {
            String data = FileUtils.readFile(path);
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
     * @param fileName    储存的文件名
     * @param checkTemp   是否检测缓存与要储存的数据相同
     * @param needEncrypt 需要加密
     * @return 是否保存成功
     */
    public static boolean saveOfflineData(final Context context, final Object o, final String fileName, boolean checkTemp, boolean needEncrypt) {
        if (o == null) {
            return false;
        } else {
            return saveOfflineContent(context, new Gson().toJson(o), fileName, checkTemp, needEncrypt);
        }
    }

    private static boolean saveOfflineContent(final Context context, final String data, final String fileName, boolean checkTemp, boolean needEncrypt) {
        String path = getOfflineDataFilePath(context, fileName, needEncrypt);
        String content = data;
        if (needEncrypt) {
            content = SecurityMethod.encryptData(context, data);
        }
        if (checkTemp) {
            String text = FileUtils.readFile(path);
            if (text != null) {
                text = text.replace("\n", "");
                return !text.equalsIgnoreCase(content) && FileUtils.writeFile(Objects.requireNonNull(content), path);
            }
        }
        return FileUtils.writeFile(Objects.requireNonNull(content), path);
    }

    static boolean saveExtraData(Object o, String path) {
        if (o != null) {
            String content = new Gson().toJson(o);
            if (content != null) {
                return FileUtils.writeFile(content, path);
            }
        }
        return false;
    }

    static ArrayList<Course> readExtraTableData(String path) {
        String content = FileUtils.readFile(path);
        return readExtraTableDataFromContent(content);
    }

    static ArrayList<Course> readExtraTableData(Context context, Uri uri) {
        String content = FileUtils.readFile(context, uri);
        return readExtraTableDataFromContent(content);
    }

    private static ArrayList<Course> readExtraTableDataFromContent(String content) {
        ArrayList<Course> o = null;
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
     * @param context  Context
     * @param fileName 离线数据的文件名
     */
    @SuppressWarnings("SameParameterValue")
    public static void deleteOfflineData(final Context context, final String fileName, boolean isEncrypt) {
        File file = new File(getOfflineDataFilePath(context, fileName, isEncrypt));
        if (file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }

    private static boolean checkDataVersionCode(String content, Class fileClass) {
        int nowVersionCode;
        String simpleName = fileClass.getSimpleName();
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
        } else if (simpleName.equals(SuspendCourseMethod.FILE_NAME)) {
            nowVersionCode = Config.DATA_VERSION_SUSPEND_COURSE;
        } else if (simpleName.equals(AlstuMethod.FILE_NAME)) {
            nowVersionCode = Config.DATA_VERSION_ALSTU_TOPIC;
        } else if (simpleName.equals(HistoryScoreMethod.FILE_NAME)) {
            nowVersionCode = Config.DATA_VERSION_COURSE_HISTORY_SCORE;
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

    private static String getOfflineDataFilePath(Context context, String fileName, boolean encryptAble) {
        return context.getFilesDir() + File.separator + fileName + (encryptAble ? ENCRYPTED_FILE_PREFIX : NOT_ENCRYPTED_FILE_PREFIX);
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