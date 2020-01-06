package tool.xfy9326.naucourse.methods.io;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.util.ArrayList;

import tool.xfy9326.naucourse.beans.course.Course;

public class BackupMethod {
    private static final String COURSE_BACKUP_PREFIX = ".nbk";
    private static final String COURSE_BACKUP_NAME = "Course" + COURSE_BACKUP_PREFIX;

    private static String getCourseBackupPath(Context context) {
        return DataPath.getInstance(context).getDataDirPath() + "backup" + File.separator + COURSE_BACKUP_NAME;
    }

    public static boolean backupCourse(Context context, ArrayList<Course> courseArrayList) {
        return DataMethod.saveExtraData(courseArrayList, getCourseBackupPath(context));
    }

    public static ArrayList<Course> restoreCourse(Context context) {
        return restoreCourse(getCourseBackupPath(context));
    }

    public static ArrayList<Course> restoreCourse(Context context, Uri uri) {
        return DataMethod.readExtraTableData(context, uri);
    }

    private static ArrayList<Course> restoreCourse(String path) {
        return DataMethod.readExtraTableData(path);
    }
}
