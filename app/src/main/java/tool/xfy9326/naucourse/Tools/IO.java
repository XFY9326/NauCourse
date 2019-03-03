package tool.xfy9326.naucourse.Tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import androidx.annotation.NonNull;

/**
 * Created by 10696 on 2018/2/27.
 * 文件读取与写入
 */

public class IO {

    /**
     * 写入文字到文件
     *
     * @param content 需要写入的内容
     * @param path    文件路径
     * @return 是否写入成功
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean writeFile(@NonNull String content, @NonNull String path) {
        File file = new File(path);
        try {
            if (CheckFile(file, true)) {
                return false;
            }
            OutputStream writer = new FileOutputStream(file);
            byte[] Bytes = content.getBytes();
            writer.write(Bytes);
            writer.flush();
            writer.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 从文件中读取文字
     *
     * @param path 文件路径
     * @return 读取的文字
     */
    public static String readFile(@NonNull String path) {
        File file = new File(path);
        try {
            if (CheckFile(file, false)) {
                return null;
            }
            InputStream file_stream = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(file_stream));
            String line;
            StringBuilder result = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }
            reader.close();
            file_stream.close();
            return result.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean createPath(File file) {
        if (file.getParent().trim().length() != 1) {
            File filepath = file.getParentFile();
            if (!filepath.exists()) {
                return filepath.mkdirs();
            }
        }
        return true;
    }

    private static boolean CheckFile(File file, boolean delete) throws IOException {
        if (file.exists()) {
            if (file.isFile()) {
                if (delete && file.canWrite() && file.canRead()) {
                    //noinspection ResultOfMethodCallIgnored
                    file.delete();
                }
                return false;
            }
        } else {
            return !createPath(file) || !file.createNewFile();
        }
        return true;
    }

    public static boolean copyFile(String oldPath, String newPath, boolean deleteOldFile) {
        try {
            File oldFile = new File(oldPath);

            if (!oldFile.exists() || !oldFile.isFile() || !oldFile.canRead()) {
                return false;
            }

            FileInputStream fileInputStream = new FileInputStream(oldPath);
            FileOutputStream fileOutputStream = new FileOutputStream(newPath);
            byte[] buffer = new byte[1024];
            int byteRead;
            while (-1 != (byteRead = fileInputStream.read(buffer))) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
            fileInputStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();
            if (deleteOldFile) {
                return oldFile.delete();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
