package com.example.coursework.data

import com.example.coursework.data.model.LoggedInUser
import java.util.zip.DataFormatException

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class LoginRepository(val dataSource: LoginDataSource) {

    // in-memory cache of the loggedInUser object
    var user: LoggedInUser? = null
        private set

    val isLoggedIn: Boolean
        get() = user != null

    init {
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
        user = null
    }

    fun logout() {
        user = null
        dataSource.logout()
    }

    fun register(username: String, email: String, password: String): Result<LoggedInUser> {
        // handle login
        val result = dataSource.login(username, email, password)

        if (result is Result.Success) {
            setLoggedInUser(result.data)
        }

        return result
    }

    fun login(email: String, password: String): Result<LoggedInUser>  {
        val decoded_email = this.user?.email!!
        val decoded_password = customDecryption(this.user?.password!!)
        return if (decoded_email == email && decoded_password == password) {
            Result.Success(this.user!!)
        } else {
            Result.Error(DataFormatException("Entered credentials doesn't match saved account data!"))
        }
    }

    private fun setLoggedInUser(loggedInUser: LoggedInUser) {
        this.user = loggedInUser
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }

    private fun customDecryption(password: String): String {
        val result = password.mapIndexed {
                i, b ->
            if (i % 2 == 0) (b - 263).code.toByte() else (b + 257).code.toByte()
        }.toByteArray().decodeToString()

        return result
    }

    fun retrievePassword(): String {
        val retrieved_password = user?.password
        if (retrieved_password != null) {
            return "Your password: " + customDecryption(retrieved_password)
        } else {
            return "No cached user found!"
        }
    }

    fun setUserData(user: LoggedInUser) {
        this.user = user
    }

}