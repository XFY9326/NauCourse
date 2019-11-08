package tool.xfy9326.naucourse.methods;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Environment;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import lib.xfy9326.nausso.NauSSOClient;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.tools.FileUtils;

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

    public static Bitmap getStuPhoto(Context context) {
        if (context != null && new File(getStuPhotoPath(context)).exists()) {
            return BitmapFactory.decodeFile(getStuPhotoPath(context));
        }
        return null;
    }

    public static String getSchoolCalendarImagePath(Context context) {
        return context.getFilesDir() + File.separator + "SchoolCalendarImage";
    }

    public static String getStuPhotoPath(Context context) {
        return context.getFilesDir() + File.separator + "StuPhoto";
    }

    public static boolean downloadImage(Context context, String URL, String downloadPath, boolean needLogin) throws Exception {
        NauSSOClient client = BaseMethod.getApp(context).getClient();
        if (client != null) {
            Bitmap bitmap;
            if (needLogin) {
                bitmap = client.getBitmapWithLogin(URL);
            } else {
                bitmap = client.getBitmap(URL);
            }
            if (bitmap != null) {
                return saveBitmap(bitmap, downloadPath, true);
            }
        }
        return false;
    }

    @Nullable
    public static Bitmap getViewBitmap(Context context, View view) {
        return getViewsBitmap(context, new View[]{view}, true, Color.TRANSPARENT);
    }

    @Nullable
    public static Bitmap getViewsBitmap(Context context, View[] views, boolean isVertical, int backgroundColor) {
        if (views.length > 0) {
            int widthSum = 0;
            int heightSum = 0;
            int maxWidth = 0;
            int maxHeight = 0;

            for (View v : views) {
                int w = v.getWidth();
                int h = v.getHeight();
                if (maxWidth < w) {
                    maxWidth = w;
                }
                if (maxHeight < h) {
                    maxHeight = h;
                }
                widthSum += w;
                heightSum += h;
            }
            if (widthSum > 0 && heightSum > 0) {
                Bitmap bitmap;
                if (isVertical) {
                    bitmap = Bitmap.createBitmap(maxWidth, heightSum, Bitmap.Config.RGB_565);
                } else {
                    bitmap = Bitmap.createBitmap(widthSum, maxHeight, Bitmap.Config.RGB_565);
                }

                Canvas canvas = new Canvas(bitmap);
                canvas.drawColor(backgroundColor);

                int heightCount = 0;
                int widthCount = 0;
                for (View v : views) {
                    int w = v.getWidth();
                    int h = v.getHeight();

                    Bitmap bitmapTemp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                    Canvas canvasTemp = new Canvas(bitmapTemp);
                    v.draw(canvasTemp);

                    if (isVertical) {
                        float left = 0;
                        if (w < maxWidth) {
                            left = (maxWidth - w) / 2f;
                        }
                        canvas.drawBitmap(bitmapTemp, left, heightCount, null);
                    } else {
                        float top = 0;
                        if (h < maxHeight) {
                            top = (maxHeight - h) / 2f;
                        }
                        canvas.drawBitmap(bitmapTemp, widthCount, top, null);
                    }
                    heightCount += h;
                    widthCount += w;

                    bitmapTemp.recycle();
                }

                drawWaterPrint(context, canvas, "@" + context.getString(R.string.app_name));

                return bitmap;
            }
        }
        return null;
    }

    private static void drawWaterPrint(Context context, Canvas canvas, String text) {
        Paint paint = new Paint();
        paint.setColor(ResourcesCompat.getColor(context.getResources(), R.color.course_snapshot_water_print, context.getTheme()));
        paint.setAlpha(80);
        paint.setAntiAlias(true);
        paint.setTextSize((float) dip2px(context, Config.WATER_PRINT_TEXT_SIZE));
        paint.setFakeBoldText(true);
        canvas.save();

        float textWidth = paint.measureText(text);

        canvas.drawText(text, canvas.getWidth() - textWidth - dip2px(context, 4), canvas.getHeight() - 15 - dip2px(context, 4), paint);

        canvas.restore();
    }

    public static boolean saveBitmap(Bitmap bitmap, String path, boolean recycle) throws IOException {
        if (bitmap != null && !bitmap.isRecycled()) {
            File file = new File(path);
            if (file.exists()) {
                //noinspection ResultOfMethodCallIgnored
                file.delete();
            }
            if (FileUtils.createPath(file)) {
                if (file.createNewFile()) {
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream);
                    fileOutputStream.flush();
                    fileOutputStream.close();
                    if (recycle && !bitmap.isRecycled()) {
                        bitmap.recycle();
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
