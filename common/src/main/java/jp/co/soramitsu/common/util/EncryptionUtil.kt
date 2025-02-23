/*
This file is part of the SORA network and Polkaswap app.

Copyright (c) 2020, 2021, Polka Biome Ltd. All rights reserved.
SPDX-License-Identifier: BSD-4-Clause

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or other
materials provided with the distribution.

All advertising materials mentioning features or use of this software must display
the following acknowledgement: This product includes software developed by Polka Biome
Ltd., SORA, and Polkaswap.

Neither the name of the Polka Biome Ltd. nor the names of its contributors may be used
to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY Polka Biome Ltd. AS IS AND ANY EXPRESS OR IMPLIED WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Polka Biome Ltd. BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package jp.co.soramitsu.common.util

import android.content.Context
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.math.BigInteger
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.Key
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.spec.AlgorithmParameterSpec
import java.util.Calendar
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.security.auth.x500.X500Principal
import org.spongycastle.util.Arrays
import org.spongycastle.util.encoders.Base64

class EncryptionUtil(
    private val context: Context
) {

    companion object {
        private const val RSA = "RSA"
        private const val AES = "AES"
        private const val KEY_STORE_PROVIDER = "AndroidKeyStore"
        private const val TRANSFORMATION = "RSA/ECB/PKCS1Padding"
        private const val KEY_ALIAS = "key_alias"
        private const val BLOCK_SIZE = 16
        private const val AES_KEY_LENGTH = 256
        private var second = false

        private var privateKey: PrivateKey? = null
        private var publicKey: PublicKey? = null

        private const val SECRET_KEY = "secret_key"
        private val secureRandom = SecureRandom()
        private var keyStore: KeyStore? = null
    }

    init {
        initKeystore()
    }

    private fun getPrerenceAesKey(): Key {
        val secretKey: SecretKey
        val encryptedKey =
            context.getSharedPreferences(KEY_ALIAS, Context.MODE_PRIVATE).getString(SECRET_KEY, "")
        if (encryptedKey!!.isEmpty()) {
            val keyGenerator = KeyGenerator.getInstance(AES)
            keyGenerator.init(AES_KEY_LENGTH, secureRandom)
            secretKey = keyGenerator.generateKey()
            context.getSharedPreferences(KEY_ALIAS, Context.MODE_PRIVATE).edit()
                .putString(SECRET_KEY, encryptRsa(secretKey.encoded)).apply()
        } else {
            val key = decryptRsa(encryptedKey)
            secretKey = SecretKeySpec(key, 0, key!!.size, AES)
        }
        return secretKey
    }

    private fun initKeystore() {
        try {
            keyStore = KeyStore.getInstance(KEY_STORE_PROVIDER)
            keyStore!!.load(null)

            if (keyStore!!.getKey(KEY_ALIAS, null) == null) {
                createKeys()
            }

            privateKey = keyStore!!.getKey(KEY_ALIAS, null) as PrivateKey
            publicKey = keyStore!!.getCertificate(KEY_ALIAS).publicKey
        } catch (e: Exception) {
            if (!second) {
                second = true
                initKeystore()
            }
            e.printStackTrace()
        }
    }

    private fun createKeys() {
        val startDate = Calendar.getInstance()
        val endDate = Calendar.getInstance()
        endDate.add(Calendar.YEAR, 25)

        val spec: AlgorithmParameterSpec

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            spec = KeyPairGeneratorSpec.Builder(context)
                .setAlias(KEY_ALIAS)
                .setSubject(X500Principal("CN=Sora"))
                .setSerialNumber(BigInteger.ONE)
                .setStartDate(startDate.time)
                .setEndDate(endDate.time)
                .build()
        } else {
            spec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setCertificateSubject(X500Principal("CN=Sora"))
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                .setCertificateSerialNumber(BigInteger.ONE)
                .setCertificateNotBefore(startDate.time)
                .setCertificateNotAfter(endDate.time)
                .build()
        }
        val keyPairGenerator = KeyPairGenerator.getInstance(RSA, KEY_STORE_PROVIDER)
        keyPairGenerator.initialize(spec)
        keyPairGenerator.generateKeyPair()
    }

    fun encrypt(cleartext: String?): String {
        if (cleartext != null && cleartext.isNotEmpty()) {
            try {
                return encrypt(getPrerenceAesKey().encoded, cleartext)
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            }
        }
        return ""
    }

    fun encrypt(key: ByteArray, cleartext: String): String {
        try {
            val result = encrypt(key, cleartext.toByteArray())
            return Base64.toBase64String(result)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    fun decrypt(encryptedBase64: String): String {
        try {
            return decrypt(getPrerenceAesKey().encoded, encryptedBase64)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }

        return ""
    }

    fun decrypt(key: ByteArray, encryptedBase64: String): String {
        try {
            val encrypted = Base64.decode(encryptedBase64)
            val result = decrypt(key, encrypted)
            return String(result)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ""
    }

    @Throws(Exception::class)
    private fun encrypt(key: ByteArray, clear: ByteArray): ByteArray {
        val skeySpec = SecretKeySpec(key, "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, IvParameterSpec(generateIVBytes()), secureRandom)
        return Arrays.concatenate(cipher.iv, cipher.doFinal(clear))
    }

    @Throws(
        NoSuchPaddingException::class,
        NoSuchAlgorithmException::class,
        InvalidAlgorithmParameterException::class,
        InvalidKeyException::class,
        BadPaddingException::class,
        IllegalBlockSizeException::class
    )
    private fun decrypt(key: ByteArray, encrypted: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(
            Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"),
            IvParameterSpec(Arrays.copyOfRange(encrypted, 0, BLOCK_SIZE)), secureRandom
        )
        return cipher.doFinal(Arrays.copyOfRange(encrypted, BLOCK_SIZE, encrypted.size))
    }

    private fun encryptRsa(input: ByteArray): String {
        val cipher: Cipher

        try {
            cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, publicKey)
            return Base64.toBase64String(cipher.doFinal(input))
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: NoSuchPaddingException) {
            e.printStackTrace()
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        } catch (e: BadPaddingException) {
            e.printStackTrace()
        } catch (e: IllegalBlockSizeException) {
            e.printStackTrace()
        }

        return ""
    }

    private fun decryptRsa(encrypted: String): ByteArray? {
        val cipher: Cipher

        try {
            cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, privateKey)
            return cipher.doFinal(Base64.decode(encrypted))
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: NoSuchPaddingException) {
            e.printStackTrace()
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        } catch (e: BadPaddingException) {
            e.printStackTrace()
        } catch (e: IllegalBlockSizeException) {
            e.printStackTrace()
        }

        return null
    }

    private fun generateIVBytes(): ByteArray {
        val ivBytes = ByteArray(BLOCK_SIZE)
        secureRandom.nextBytes(ivBytes)
        return ivBytes
    }
}
