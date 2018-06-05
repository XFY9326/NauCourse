package tool.xfy9326.naucourse.Methods;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

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
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().get().url(URL).build();
        Response response = okHttpClient.newCall(request).execute();
        if (response.isSuccessful()) {
            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                InputStream inputStream = responseBody.byteStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                File file = new File(downloadPath);
                if (file.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    file.delete();
                }
                if (file.createNewFile()) {
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                    fileOutputStream.flush();
                    fileOutputStream.close();
                    bitmap.recycle();
                    return true;
                }
            }
        }
        return false;
    }

}
