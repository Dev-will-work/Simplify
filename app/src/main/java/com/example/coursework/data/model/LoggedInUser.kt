package com.example.coursework.data.model

import android.content.Context
import android.util.Patterns
import android.widget.Toast
import com.example.coursework.SharedObject
import com.example.coursework.writeFile
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

class UserController {
    lateinit var user: LoggedInUser

    private fun customEncryption(password: String): String {
        val bytePassword = password.encodeToByteArray()

        val result = bytePassword.mapIndexed {
                i, b ->
            if (i % 2 == 0) (b + 263).toByte() else (b - 257).toByte()
        }.toByteArray().decodeToString()

        return result
    }

    private fun customDecryption(password: String): String {
        val result = password.mapIndexed {
                i, b ->
            if (i % 2 == 0) (b - 263).code.toByte() else (b + 257).code.toByte()
        }.toByteArray().decodeToString()

        return result
    }

    fun set(ctx: Context, user: LoggedInUser) {
        this.user = user
        this.user.password = customEncryption(user.password)

        val prefix = ctx.filesDir
        writeFile("$prefix/userData.json", this.user)
    }

    fun defaultInitialization(ctx: Context) {
        this.user = LoggedInUser(UUID.randomUUID().toString(), "guest", "example@email.com", "abcdef")

        val prefix = ctx.filesDir
        writeFile("$prefix/userdata.json", this.user)
    }

    fun compare(user: LoggedInUser): Boolean {
        return this.user.email == user.email && customDecryption(this.user.password) == user.password
    }

    fun getPassword(): String {
        return customDecryption(this.user.password)
    }

    fun getID(): String {
        return this.user.userId
    }

    fun userInitialized(): Boolean {
        return this::user.isInitialized
    }
}

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

object CachedUser : SharedObject<LoggedInUser>{
    private var userController = UserController()
    val validator = Validator()

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
                this.userController.set(ctx, newUser)
                if (!quiet) Toast.makeText(ctx, "Welcome $username", Toast.LENGTH_LONG).show()
                return true
            }
        }
    }

    fun retrievePassword(): String {
        val retrievedPassword = this.userController.getPassword()
        return if (retrievedPassword != "") {
            "Your password: $retrievedPassword"
        } else {
            "No cached password found!"
        }
    }

    fun retrieveUsername(): String {
        return userController.user.displayName
    }

    fun retrieveEmail(): String {
        return userController.user.email
    }

    fun retrieveID(): String {
        return userController.getID()
    }

    fun login(email: String, password: String): Boolean {
        return userController.compare(LoggedInUser("", "", email, password))
    }

    override fun defaultInitialization(ctx: Context) {
        userController.defaultInitialization(ctx)
    }

    override fun initialized(): Boolean {
        return userController.userInitialized()
    }

    override fun set(ctx: Context, dummy: LoggedInUser) {
        register(ctx, dummy.displayName, dummy.email, dummy.password, true)
    }
}