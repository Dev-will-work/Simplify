package com.example.coursework

import android.content.Context
import android.util.Patterns
import android.widget.Toast
import kotlinx.serialization.Serializable
import java.util.*

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
// If user credentials will be cached in local storage, it is recommended it be encrypted
// @see https://developer.android.com/training/articles/keystore
@Serializable
data class LoggedInUser(
    var userId: String,
    var displayName: String,
    var email: String,
    var password: String
)

class Validator {
    fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // A placeholder username validation check
    fun isUserNameValid(username: String): Boolean {
        return username.isNotBlank()
    }

    // A placeholder password validation check
    fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}

 class Cryptor {
    fun customEncryption(password: String): String {
        val bytePassword = password.encodeToByteArray()

        val result = bytePassword.mapIndexed {
                i, b ->
            if (i % 2 == 0) (b + 263).toByte() else (b - 257).toByte()
        }.toByteArray().decodeToString()

        return result
    }

    fun customDecryption(password: String): String {
        val result = password.mapIndexed {
                i, b ->
            if (i % 2 == 0) (b - 263).code.toByte() else (b + 257).code.toByte()
        }.toByteArray().decodeToString()

        return result
    }
}

object CachedUser : SharedObject<LoggedInUser>{
    private lateinit var user: LoggedInUser
    val validator = Validator()
    private val cryptor = Cryptor()

    fun register(ctx: Context, username: String, email: String, password: String, quiet: Boolean = false): Boolean {
        // handle login
        when {
            !validator.isUserNameValid(username) -> {
                Toast.makeText(ctx, "Wrong username!", Toast.LENGTH_SHORT).show()
                return false
            }
            !validator.isEmailValid(email) -> {
                Toast.makeText(ctx, "Wrong email!", Toast.LENGTH_SHORT).show()
                return false
            }
            !validator.isPasswordValid(password) -> {
                Toast.makeText(ctx, "Wrong password!", Toast.LENGTH_SHORT).show()
                return false
            }
            else -> {
                val newUser =
                    LoggedInUser(UUID.randomUUID().toString(), username, email, password)
                setCheckedData(ctx, newUser)
                if (!quiet) Toast.makeText(ctx, "Welcome $username", Toast.LENGTH_LONG).show()
                return true
            }
        }
    }

    fun retrievePassword(): String {
        val retrievedPassword = getPassword()
        return if (retrievedPassword != "") {
            "Your password: $retrievedPassword"
        } else {
            "No cached password found!"
        }
    }

    fun retrieveUsername(): String {
        return user.displayName
    }

    fun retrieveEmail(): String {
        return user.email
    }

    fun retrieveID(): String {
        return user.userId
    }

    fun login(email: String, password: String): Boolean {
        return compare(LoggedInUser("", "", email, password))
    }

    override fun initialized(): Boolean {
        return CachedUser::user.isInitialized
    }

    override fun set(ctx: Context, dummy: LoggedInUser) {
        register(ctx, dummy.displayName, dummy.email, dummy.password, true)
    }

    private fun setCheckedData(ctx: Context, user: LoggedInUser) {
        CachedUser.user = user
        CachedUser.user.password = cryptor.customEncryption(user.password)

        val prefix = ctx.filesDir
        writeFile("$prefix/userData.json", CachedUser.user)
    }

    override fun defaultInitialization(ctx: Context) {
        user = LoggedInUser(UUID.randomUUID().toString(), "guest", "example@email.com", "abcdef")

        val prefix = ctx.filesDir
        writeFile("$prefix/userdata.json", user)
    }

    private fun compare(user: LoggedInUser): Boolean {
        return CachedUser.user.email == user.email && cryptor.customDecryption(CachedUser.user.password) == user.password
    }

    private fun getPassword(): String {
        return cryptor.customDecryption(user.password)
    }
}