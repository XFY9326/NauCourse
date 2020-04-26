package tool.xfy9326.naucourse.network.tools

import okhttp3.HttpUrl
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.network.clients.VPNClient
import tool.xfy9326.naucourse.utils.BaseUtils.toHex
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

// VPN的url加密
object VPNTools {
    private const val VPN_URL_ENCRYPT_KEY = "wrdvpnisthebest!"
    private const val VPN_URL_ENCRYPT_IV = "wrdvpnisthebest!"
    private const val TEXT_ENCODE_MODE = "utf8"
    private const val CIPHER_INSTANCE_MODE = "AES/CFB/NoPadding"
    private const val CIPHER_KEY_MODE = "AES"
    private const val FILL_ZERO = "0"
    private const val BYTE_SIZE_16 = 16
    private const val BYTE_SIZE_32 = 32
    private const val VPN_URL_SCHEME_JOIN_SYMBOL = "-"

    private fun encryptHost(host: String): String = encrypt(
        host,
        VPN_URL_ENCRYPT_KEY,
        VPN_URL_ENCRYPT_IV
    )

    fun buildVPNUrl(url: HttpUrl): HttpUrl {
        var vpnScheme = url.scheme
        if (url.port != Constants.Network.HTTP_PORT && url.port != Constants.Network.HTTPS_PORT) {
            vpnScheme += VPN_URL_SCHEME_JOIN_SYMBOL + url.port
        }
        val vpnPaths = url.encodedPathSegments
        return HttpUrl.Builder().apply {
            scheme(Constants.Network.HTTP)
            host(VPNClient.VPN_HOST)
            addPathSegment(vpnScheme)
            addPathSegment(encryptHost(url.host))
            vpnPaths.forEach {
                addEncodedPathSegment(it)
            }
            query(url.query)
        }.build()
    }

    @Suppress("SameParameterValue")
    private fun textRightAppend(text: String, mode: String): String {
        val segmentByteSize = if (TEXT_ENCODE_MODE == mode) BYTE_SIZE_16 else BYTE_SIZE_32
        if (text.length % segmentByteSize == 0) return text
        val appendLength = segmentByteSize - text.length % segmentByteSize
        val textBuilder = StringBuilder(text)
        for (i in 0 until appendLength) textBuilder.append(FILL_ZERO)
        return textBuilder.toString()
    }

    @Suppress("SameParameterValue")
    private fun encrypt(str: String, key: String, iv: String): String {
        val cipher: Cipher = Cipher.getInstance(CIPHER_INSTANCE_MODE)
        val text = textRightAppend(
            str,
            TEXT_ENCODE_MODE
        )
        val keyBytes: ByteArray = key.toByteArray(StandardCharsets.UTF_8)
        val ivBytes: ByteArray = iv.toByteArray(StandardCharsets.UTF_8)
        val textBytes: ByteArray = text.toByteArray(StandardCharsets.UTF_8)
        val keySpec = SecretKeySpec(keyBytes, CIPHER_KEY_MODE)
        val ivSpec = IvParameterSpec(ivBytes)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        val encrypted: ByteArray = cipher.doFinal(textBytes)
        return ivBytes.toHex().toLowerCase(Locale.CHINA) + encrypted.toHex().toLowerCase(Locale.CHINA).substring(0, str.length * 2)
    }
}