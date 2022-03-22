package com.example.coursework.data

import com.example.coursework.data.model.LoggedInUser
import java.io.IOException
import kotlin.random.Random
import kotlin.random.nextUBytes

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    fun getASCIIString(): String {
        return listOf(Random.Default.nextInt(0,128).toByte(),
            Random.Default.nextInt(0,128).toByte(),
            Random.Default.nextInt(0,128).toByte(),
            Random.Default.nextInt(0,128).toByte()).toByteArray().decodeToString()
    }

    fun customEncryption(password: String): String {
        val bytePassword = password.encodeToByteArray()

        val result = bytePassword.mapIndexed {
            i, b ->
            if (i % 2 == 0) (b + 263).toByte() else (b - 257).toByte()
        }.toByteArray().decodeToString()

//        for (byte in bytePassword) {
//            if (bytePassword.indexOf(byte) % 2 == 0) {
//                bytePassword[bytePassword.indexOf(byte)] = (byte + 197).toByte()
//            } else {
//                bytePassword[bytePassword.indexOf(byte)] = (byte - 61).toByte()
//            }
//        }
        return result
    }

    fun login(username: String, email: String, password: String): Result<LoggedInUser> {
        try {
            // TODO: handle loggedInUser authentication
            val encrypted_password = customEncryption(password)
            val customID = java.util.UUID.randomUUID().toString()
            val User = LoggedInUser(customID, username, email, encrypted_password)
            return Result.Success(User)
        } catch (e: Throwable) {
            return Result.Error(IOException("Error logging in", e))
        }
    }

    fun logout() {
        // TODO: revoke authentication
    }
}