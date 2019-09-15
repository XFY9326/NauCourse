package lib.xfy9326.nausso;

import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

@SuppressWarnings("SameParameterValue")
class AES {
    private static String textRightAppend(String text, String mode) {
        int segmentByteSize = "utf8".equals(mode) ? 16 : 32;
        if (text.length() % segmentByteSize == 0) {
            return text;
        }
        int appendLength = segmentByteSize - text.length() % segmentByteSize;
        int i = 0;
        StringBuilder textBuilder = new StringBuilder(text);
        while (i++ < appendLength) {
            textBuilder.append("0");
        }
        text = textBuilder.toString();
        return text;
    }

    static String encrypt(String str, String key, String iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");

        String text = textRightAppend(str, "utf8");
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] ivBytes = iv.getBytes(StandardCharsets.UTF_8);
        byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);

        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        byte[] encrypted = cipher.doFinal(textBytes);

        return byte2hex(ivBytes) + byte2hex(encrypted).substring(0, str.length() * 2);
    }

    private static String byte2hex(byte[] b) {
        StringBuilder hs = new StringBuilder();
        String sTmp;
        for (byte ab : b) {
            sTmp = (Integer.toHexString(ab & 0XFF));
            if (sTmp.length() == 1) {
                hs.append("0").append(sTmp);
            } else {
                hs.append(sTmp);
            }
        }
        return hs.toString().toLowerCase();
    }
}

