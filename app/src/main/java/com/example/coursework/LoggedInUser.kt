package com.example.coursework

import android.content.Context
import android.util.Patterns
import android.widget.Toast
import com.example.coursework.ImageStore.image_uri
import kotlinx.serialization.Serializable
import java.util.*

/**
 * Util class for serialization of user personal data.
 *
 * @property userId
 * Internal user ID.
 * @property displayName
 * User nickname.
 * @property email
 * User email.
 * @property password
 * User password.
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

/**
 * Special class for check if all of entered user information is correct.
 *
 */
class Validator {
    /**
     * Function, that checks if the entered email is valid.
     *
     * @param email
     * input email.
     * @return true if email is valid, false otherwise.
     */
    fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Function, that checks if the entered username is valid.
     *
     * @param username
     * input username.
     * @return true if username is valid, false otherwise.
     */
    fun isUserNameValid(username: String): Boolean {
        return username.isNotBlank()
    }

    /**
     * Function, that checks if the entered password is valid.
     *
     * @param password
     * input password.
     * @return true if password is valid, false otherwise.
     */
    fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}

/**
 * Special class which can encrypt and decrypt sensitive data by internal encryption methods.
 *
 */
 class Cryptor {
    /**
     * Function, which encrypts data by internal encryption methods.
     *
     * @param password
     * password or any other sensitive data in text form.
     * @return encrypted input data
     */
    fun customEncryption(password: String): String {
        val bytePassword = password.encodeToByteArray()

        val result = bytePassword.mapIndexed {
                i, b ->
            if (i % 2 == 0) (b + 263).toByte() else (b - 257).toByte()
        }.toByteArray().decodeToString()

        return result
    }

    /**
     * Function, which decrypts data by internal encryption methods.
     *
     * @param password
     * encrypted password or any other sensitive data in text form.
     * @return decrypted input data
     */
    fun customDecryption(password: String): String {
        val result = password.mapIndexed {
                i, b ->
            if (i % 2 == 0) (b - 263).code.toByte() else (b + 257).code.toByte()
        }.toByteArray().decodeToString()

        return result
    }
}

/**
 * Object that provides user personal data to another components.
 * Compliant to SharedObject interface.
 * @property user
 * Internal class with user personal data.
 * @property validator
 * Internal class for data checks.
 * @property cryptor
 * Internal class for sensitive data encryption.
 */
object CachedUser : SharedObject<LoggedInUser>{
    private lateinit var user: LoggedInUser
    val validator = Validator()
    private val cryptor = Cryptor()

    /**
     * Function which handles user registration and caching all the needed data.
     *
     * @param ctx
     * Context of the application.
     * @param username
     * Input username.
     * @param email
     * Input email.
     * @param password
     * Input password.
     * @param quiet
     * Bool, representing if the registration should be done automatically in quiet mode.
     * @return Boolean, representing success state of the registration.
     */
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

    /**
     * Function, which allows safely get decrypted password to remember it.
     *
     * @return Cached password or no password found alert.
     */
    fun retrievePassword(): String {
        val retrievedPassword = getPassword()
        return if (retrievedPassword != "") {
            "Your password: $retrievedPassword"
        } else {
            "No cached password found!"
        }
    }

    /**
     * Function, which allows to get user nickname.
     *
     * @return user nickname.
     */
    fun retrieveUsername(): String {
        return user.displayName
    }

    /**
     * Function, which allows to get user email.
     *
     * @return user email.
     */
    fun retrieveEmail(): String {
        return user.email
    }

    /**
     * Function, which allows to get user internal ID.
     *
     * @return user internal ID.
     */
    fun retrieveID(): String {
        return user.userId
    }

    /**
     * Function, which internally executes authentification steps.
     *
     * @param email
     * entered email.
     * @param password
     * entered password.
     * @return authentication success state.
     */
    fun login(email: String, password: String): Boolean {
        return compare(LoggedInUser("", "", email, password))
    }

    /**
     * Function which tells if this object is ready to use.
     * @receiver
     * returns true if user personal data is initialized.
     *
     * @return Bool, that determines if this object properties are fully initialized and ready.
     *
     */
    override fun initialized(): Boolean {
        return CachedUser::user.isInitialized
    }

    /**
     * This function sets deserialized data to object for further use.
     * @receiver
     * Sets user personal data from dummy to provider object.
     *
     * @param ctx
     * Context of the application.
     * @param dummy
     * Class of the similar structure, needed for serialization.
     */
    override fun set(ctx: Context, dummy: LoggedInUser) {
        register(ctx, dummy.displayName, dummy.email, dummy.password, true)
    }

    /**
     * Internal set function, executed after all the checks under the data are performed.
     *
     * @param ctx
     * Context of the application.
     * @param user
     * Serialization class with appropriate user data.
     */
    private fun setCheckedData(ctx: Context, user: LoggedInUser) {
        CachedUser.user = user
        CachedUser.user.password = cryptor.customEncryption(user.password)

        val prefix = ctx.filesDir
        writeFile("$prefix/userData.json", CachedUser.user)
    }

    /**
     * Function for setting default values if the last state is missing.
     * @receiver
     * sets user personal data to guest account synthetic data.
     *
     * @param ctx
     * Context of the application.
     */
    override fun defaultInitialization(ctx: Context) {
        user = LoggedInUser(UUID.randomUUID().toString(), "guest", "example@email.com", "abcdef")

        val prefix = ctx.filesDir
        writeFile("$prefix/userdata.json", user)
    }

    /**
     * Function for internal user personal data checks.
     *
     * @param user
     * Class with all data we need to check.
     * @return data validation success state.
     */
    private fun compare(user: LoggedInUser): Boolean {
        return CachedUser.user.email == user.email && cryptor.customDecryption(CachedUser.user.password) == user.password
    }

    /**
     * Internal function which decrypts user password for limited uses.
     *
     * @return decrypted user password.
     */
    private fun getPassword(): String {
        return cryptor.customDecryption(user.password)
    }
}