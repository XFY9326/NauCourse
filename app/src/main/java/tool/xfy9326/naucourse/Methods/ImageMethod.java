package tool.xfy9326.naucourse.Methods;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;

public class ImageMethod {

    /**
     * 获取背景图片
     *
     * @param context Context
     * @return Bitmap
     */
    public static Bitmap getBitmap(Context context) {
        if (context != null && new File(getCourseTableBackgroundImagePath(context)).exists()) {
            return BitmapFactory.decodeFile(getCourseTableBackgroundImagePath(context));
        }
        return null;
    }

    public static String getCourseTableBackgroundImagePath(Context context) {
        return context.getFilesDir() + File.separator + "CourseTableBackgroundImage";
    }

}
