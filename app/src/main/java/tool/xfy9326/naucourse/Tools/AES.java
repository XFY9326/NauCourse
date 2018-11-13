package tool.xfy9326.naucourse.Tools;

import android.annotation.SuppressLint;

import java.io.UnsupportedEncodingException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by xfy9326 on 18-2-20.
 * AES加密解密
 */

public class AES {
    private static final String CipherMode = "AES";

    private static SecretKeySpec createKey(@Nullable String password) {
        byte[] data = null;
        if (password == null) {
            password = "";
        }
        StringBuilder sb = new StringBuilder(32);
        sb.append(password);
        while (sb.length() < 32) {
            sb.append("0");
        }
        if (sb.length() > 32) {
            sb.setLength(32);
        }

        try {
            data = sb.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return new SecretKeySpec(data, "AES");
    }

    private static byte[] encrypt(byte[] content, String password) {
        try {
            SecretKeySpec key = createKey(password);
            @SuppressLint("GetInstance") Cipher cipher = Cipher.getInstance(CipherMode);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * AES加密
     *
     * @param content  需要加密的内容
     * @param password 加密的密码
     * @return 加密后的字符
     */

    @Nullable
    public static String encrypt(@Nullable String content, String password) {
        if (content != null) {
            byte[] data = null;
            try {
                data = content.getBytes("UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            data = encrypt(data, password);
            if (data != null) {
                return byte2hex(data);
            }
        }
        return null;
    }

    private static byte[] decrypt(byte[] content, String password) {
        try {
            SecretKeySpec key = createKey(password);
            @SuppressLint("GetInstance") Cipher cipher = Cipher.getInstance(CipherMode);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * AES解密
     *
     * @param content  需要解密的内容
     * @param password 解密的密码
     * @return 解密完成的内容
     */
    @Nullable
    public static String decrypt(@Nullable String content, String password) {
        if (content != null) {
            byte[] data = null;
            try {
                data = hex2byte(content);
            } catch (Exception e) {
                e.printStackTrace();
            }
            data = decrypt(data, password);
            if (data == null)
                return null;
            String result = null;
            try {
                result = new String(data, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return result;
        } else {
            return null;
        }
    }

    private static String byte2hex(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        String tmp;
        for (byte aB : b) {
            tmp = (Integer.toHexString(aB & 0XFF));
            if (tmp.length() == 1) {
                sb.append("0");
            }
            sb.append(tmp);
        }
        return sb.toString().toUpperCase();
    }

    @NonNull
    private static byte[] hex2byte(@Nullable String inputString) {
        if (inputString == null || inputString.length() < 2) {
            return new byte[0];
        }
        inputString = inputString.toLowerCase();
        int l = inputString.length() / 2;
        byte[] result = new byte[l];
        for (int i = 0; i < l; ++i) {
            String tmp = inputString.substring(2 * i, 2 * i + 2);
            result[i] = (byte) (Integer.parseInt(tmp, 16) & 0xFF);
        }
        return result;
    }
}
