package com.toshith.anykboard

import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

object Crypto {
    private val KEY =
        "<KEY>".toByteArray(Charsets.UTF_8)

    fun decrypt(b64: String): String {
        val raw = Base64.getDecoder().decode(b64)
        val iv = raw.copyOfRange(0, 16)
        val msg = raw.copyOfRange(16, raw.size)

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(
            Cipher.DECRYPT_MODE,
            SecretKeySpec(KEY, "AES"),
            IvParameterSpec(iv)
        )

        return String(cipher.doFinal(msg), Charsets.UTF_8)
    }
}
