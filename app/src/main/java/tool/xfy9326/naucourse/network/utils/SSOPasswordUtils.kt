package tool.xfy9326.naucourse.network.utils

import tool.xfy9326.naucourse.kt.toHex
import java.math.BigInteger
import java.security.KeyFactory
import java.security.interfaces.RSAPublicKey
import java.security.spec.RSAPublicKeySpec
import javax.crypto.Cipher


object SSOPasswordUtils {
    private const val ALGORITHM_NAME = "RSA"
    private const val EXPONENT = "010001"

    @Suppress("SpellCheckingInspection")
    private const val MODULUS =
        "008aed7e057fe8f14c73550b0e6467b023616ddc8fa91846d2613cdb7f7621e3cada4cd5d812d627af6b87727ade4e26d26208b7326815941492b2204c3167ab2d53df1e3a2c9153bdb7c8c2e968df97a5e7e01cc410f92c4c2c2fba529b3ee988ebc1fca99ff5119e036d732c368acf8beba01aa2fdafa45b21e4de4928d0d403"

    fun encrypt(password: String): String {
        val modulus = BigInteger(MODULUS, 16)
        val exponent = BigInteger(EXPONENT, 16)

        val keyFactory = KeyFactory.getInstance(ALGORITHM_NAME)
        val publicKey = keyFactory.generatePublic(RSAPublicKeySpec(modulus, exponent)) as RSAPublicKey

        val cipher = Cipher.getInstance(ALGORITHM_NAME)
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)

        return cipher.doFinal(password.reversed().toByteArray()).toHex()
    }
}