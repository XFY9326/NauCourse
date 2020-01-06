package tool.xfy9326.naucourse.methods.io;

import android.content.Context;
import android.os.Environment;

import androidx.core.content.ContextCompat;

import java.io.File;

public class DataPath {
    private static DataPath dataPath;
    private static String dataDirPath;
    private final Context context;

    private DataPath(Context context) {
        this.context = context.getApplicationContext();
    }

    public static DataPath getInstance(Context context) {
        if (dataPath == null) {
            dataPath = new DataPath(context);
        }
        return dataPath;
    }

    public String getDataDirPath() {
        if (dataDirPath == null) {
            dataDirPath = ContextCompat.getExternalFilesDirs(context, Environment.DIRECTORY_DOCUMENTS)[0].getAbsolutePath() + File.separator + "NauCourse" + File.separator;
        }
        return dataDirPath;
    }
}