package tool.xfy9326.naucourse.Methods;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import tool.xfy9326.naucourse.Tools.IO;

public class ImageMethod {

    /**
     * 获取背景图片
     *
     * @param context Context
     * @return Bitmap
     */
    public static Bitmap getTableBackgroundBitmap(Context context) {
        if (context != null && new File(getCourseTableBackgroundImagePath(context)).exists()) {
            return BitmapFactory.decodeFile(getCourseTableBackgroundImagePath(context));
        }
        return null;
    }

    public static String getCourseTableBackgroundImagePath(Context context) {
        return context.getFilesDir() + File.separator + "CourseTableBackgroundImage";
    }

    public static String getCourseTableBackgroundImageTempPath(Context context) {
        File path = context.getExternalCacheDir();
        if (path != null) {
            return path.getAbsolutePath() + File.separator + "CourseTableBackgroundImage";
        } else {
            return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "CourseTableBackgroundImage";
        }
    }

    public static Bitmap getSchoolCalendarImage(Context context) {
        if (context != null && new File(getSchoolCalendarImagePath(context)).exists()) {
            return BitmapFactory.decodeFile(getSchoolCalendarImagePath(context));
        }
        return null;
    }

    public static String getSchoolCalendarImagePath(Context context) {
        return context.getFilesDir() + File.separator + "SchoolCalendarImage";
    }

    public static boolean downloadImage(String URL, String downloadPath) throws Exception {
        Bitmap bitmap = getBitmapFromUrl(URL);
        if (bitmap != null) {
            return saveBitmap(bitmap, downloadPath, true);
        }
        return false;
    }

    private static Bitmap getBitmapFromUrl(String URL) throws Exception {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().get().url(URL).build();
        Response response = okHttpClient.newCall(request).execute();
        if (response.isSuccessful()) {
            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                InputStream inputStream = responseBody.byteStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                response.close();
                return bitmap;
            }
        }
        response.close();
        return null;
    }

    public static boolean saveBitmap(Bitmap bitmap, String path, boolean recycle) throws IOException {
        if (bitmap != null && !bitmap.isRecycled()) {
            File file = new File(path);
            if (file.exists()) {
                //noinspection ResultOfMethodCallIgnored
                file.delete();
            }
            if (IO.createPath(file)) {
                if (file.createNewFile()) {
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream);
                    fileOutputStream.flush();
                    fileOutputStream.close();
                    if (recycle) {
                        bitmap.recycle();
                    }
                    return true;
                }
            }
        }
        return false;
    }

}
