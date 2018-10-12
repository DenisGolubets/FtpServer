package com.golubets.ftp

import org.slf4j.LoggerFactory
import java.io.*
import java.nio.charset.Charset
import java.nio.file.Paths
import java.util.*
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.HashMap


class Preferences {
    private var properties: Properties = Properties()

    companion object {
        private const val ALGORITHM = "RC2"
        private val LOGGER = LoggerFactory.getLogger(Preferences::class.java)
        private val USER_PATH = System.getProperty("user.home")!!
        private const val FILE_NAME = ".ftp.server.tod"
        //        private val cipher = Cipher.getInstance("Blowfish")
        private val cipher = Cipher.getInstance(ALGORITHM)
        private val k = "2w32eEt9!".toByteArray(Charset.forName("UTF-8"))
        private val key64 = SecretKeySpec(k, ALGORITHM)
        private var propertiesMap: Map<String, String> = HashMap()
    }

    init {
        load()
    }

    operator fun set(key: String, value: String) {
        propertiesMap = propertiesMap.plus(Pair(key, value))
    }

    fun get(key: String, defaultValue: String): String {
        val value = propertiesMap[key]
        if (value.isNullOrBlank()) return defaultValue

        return value!!
    }

    fun save() {
        try {
            if (!propertiesMap.isEmpty()) {
                cipher.init(Cipher.ENCRYPT_MODE, key64)
                ObjectOutputStream(CipherOutputStream(BufferedOutputStream(FileOutputStream(
                        Paths.get(USER_PATH, FILE_NAME).toFile())), cipher)).use { it -> it.writeObject(propertiesMap) }
            }
        } catch (e: FileNotFoundException) {
            LOGGER.error("Can't access file: $USER_PATH$FILE_NAME")
        } catch (e: Exception) {
            LOGGER.error(e.message)
        }
    }

    private fun load() {
        if (Paths.get(USER_PATH, FILE_NAME).toFile().exists()) {
            try {
                cipher.init(Cipher.DECRYPT_MODE, key64)
                ObjectInputStream(CipherInputStream(BufferedInputStream(
                        FileInputStream(Paths.get(USER_PATH, FILE_NAME).toFile())), cipher)).use { it ->
                    propertiesMap = it.readObject() as Map<String, String>
                }
            } catch (e: FileNotFoundException) {
                LOGGER.error("Can't access file: $USER_PATH$FILE_NAME")
            } catch (e: IOException) {
                LOGGER.error(e.message, e.stackTrace.toString())
            }
        }
    }
}
