package tool.xfy9326.naucourse.methods;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import tool.xfy9326.naucourse.BuildConfig;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.beans.course.TodayCourses;

public class StorageCache {
    public static TodayCourses readTodayCoursesCache(Context context) {
        File cacheFile = getTodayCoursesCacheFile(context);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getInt(Config.SUPPORT_APP_VERSION_CODE, 0) == BuildConfig.SUPPORT_MIN_VERSION_CODE &&
                sharedPreferences.getInt(Config.SUPPORT_APP_SUB_VERSION, 0) == BuildConfig.SUPPORT_MIN_SUB_VERSION) {
            byte[] data = readBytesFromFile(cacheFile);
            if (data != null) {
                return CourseListUpdate.readTodayCourseFromBytes(data);
            }
        } else {
            deleteFile(cacheFile);
        }
        return null;
    }

    @SuppressWarnings("UnusedReturnValue")
    public static boolean saveTodayCoursesCache(Context context, TodayCourses todayCourses) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit()
                .putInt(Config.SUPPORT_APP_VERSION_CODE, BuildConfig.SUPPORT_MIN_VERSION_CODE)
                .putInt(Config.SUPPORT_APP_SUB_VERSION, BuildConfig.SUPPORT_MIN_SUB_VERSION).apply();

        byte[] data = CourseListUpdate.writeTodayCourseInBytes(todayCourses);
        File cacheFile = getTodayCoursesCacheFile(context);

        return writeBytesToFile(cacheFile, data);
    }

    private synchronized static byte[] readBytesFromFile(File file) {
        if (file.exists() && file.isFile()) {
            int length = (int) file.length();
            byte[] data = new byte[length];
            try {
                FileInputStream stream = new FileInputStream(file);
                int result = stream.read(data);
                stream.close();
                if (result == length) {
                    return data;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private synchronized static boolean writeBytesToFile(File file, byte[] data) {
        deleteFile(file);
        try {
            FileOutputStream stream = new FileOutputStream(file);
            stream.write(data);
            stream.flush();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    @SuppressWarnings("UnusedReturnValue")
    private static boolean deleteFile(File file) {
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    private static File getTodayCoursesCacheFile(Context context) {
        return new File(context.getCacheDir().getAbsolutePath() + File.separator + Config.TODAY_COURSE_LIST_CACHE);
    }
}
