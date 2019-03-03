package tool.xfy9326.naucourse.Methods;

import java.io.File;
import java.util.ArrayList;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Utils.Course;

public class BackupMethod {
    private static final String BACKUP_PATH = Config.DATA_DIR_PATH + "backup" + File.separator;
    private static final String COURSE_BACKUP_NAME = "Course.nbk";

    private static String getCourseBackupPath() {
        return BACKUP_PATH + COURSE_BACKUP_NAME;
    }

    public static boolean backupCourse(ArrayList<Course> courseArrayList) {
        return DataMethod.saveExtraData(courseArrayList, getCourseBackupPath());
    }

    public static ArrayList<Course> restoreCourse() {
        return restoreCourse(getCourseBackupPath());
    }

    public static ArrayList<Course> restoreCourse(String path) {
        return DataMethod.readExtraTableData(path);
    }
}
