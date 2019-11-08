package tool.xfy9326.naucourse.methods.io;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.util.Base64;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.beans.course.Course;
import tool.xfy9326.naucourse.methods.BaseMethod;
import tool.xfy9326.naucourse.methods.compute.CourseMethod;
import tool.xfy9326.naucourse.tools.TextCompress;

public class ShareMethod {
    public static void shareCourse(@NonNull Activity activity, @NonNull Course course) {
        String json = CourseMethod.getCourseJson(course);
        if (json != null && !json.isEmpty()) {
            json = json.replaceAll("\n", "").replace(" ", "");
            try {
                byte[] compressStr = TextCompress.compress(json);
                if (compressStr != null) {
                    String shareCode = Config.SHARE_COURSE_PREFIX + Base64.encodeToString(compressStr, Base64.NO_PADDING | Base64.NO_WRAP | Base64.URL_SAFE);
                    shareText(activity, activity.getString(R.string.share_course), shareCode);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(activity, R.string.course_share_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static void shareText(@NonNull Activity activity, String title, String content) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, title);
        intent.putExtra(Intent.EXTRA_TEXT, content);
        BaseMethod.runIntent(activity, Intent.createChooser(intent, activity.getString(R.string.share)));
    }

    @Nullable
    public static Course getShareCourse(String shareStr) {
        if (shareStr != null && !shareStr.isEmpty()) {
            shareStr = shareStr.trim();
            if (shareStr.startsWith(Config.SHARE_COURSE_PREFIX) && shareStr.length() > Config.SHARE_COURSE_PREFIX.length()) {
                String dataStr = shareStr.substring(Config.SHARE_COURSE_PREFIX.length());
                try {
                    String json = TextCompress.uncompress(Base64.decode(dataStr, Base64.NO_PADDING | Base64.NO_WRAP | Base64.URL_SAFE));
                    if (json != null) {
                        return CourseMethod.getCourseByJson(json);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Nullable
    public static String getStringFromClipBoard(Context context) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager != null) {
            ClipData data = clipboardManager.getPrimaryClip();
            if (data != null && data.getItemCount() > 0) {
                ClipData.Item item = data.getItemAt(0);
                CharSequence charSequence = item.getText();
                if (charSequence != null) {
                    return charSequence.toString();
                }
            }
        }
        return null;
    }
}
