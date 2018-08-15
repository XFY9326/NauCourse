package lib.xfy9326.net.api;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

class AES {

    static String Encrypt(String str, String key, String iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        int blockSize = cipher.getBlockSize();

        byte[] dataBytes = str.getBytes();
        int plaintextLength = dataBytes.length;
        if (plaintextLength % blockSize != 0) {
            plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
        }

        byte[] plaintext = new byte[plaintextLength];
        System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);

        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes());

        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        byte[] encrypted = cipher.doFinal(plaintext);

        return byte2hex(encrypted).toLowerCase();
    }

    static String Decrypt(String str, String key, String iv) throws Exception {
        byte[] encrypted1 = hex2byte(str);

        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes());

        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

        byte[] original = cipher.doFinal(encrypted1);
        return new String(original);
    }

    private static byte[] hex2byte(String srtHex) {
        if (srtHex == null) {
            return null;
        }
        int l = srtHex.length();
        if (l % 2 == 1) {
            return null;
        }
        byte[] b = new byte[l / 2];
        for (int i = 0; i != l / 2; i++) {
            b[i] = (byte) Integer.parseInt(srtHex.substring(i * 2, i * 2 + 2), 16);
        }
        return b;
    }

    private static String byte2hex(byte[] b) {
        StringBuilder hs = new StringBuilder();
        String sTmp;
        for (byte aB : b) {
            sTmp = (Integer.toHexString(aB & 0XFF));
            if (sTmp.length() == 1) {
                hs.append("0").append(sTmp);
            } else {
                hs.append(sTmp);
            }
        }
        return hs.toString().toUpperCase();
    }
}
