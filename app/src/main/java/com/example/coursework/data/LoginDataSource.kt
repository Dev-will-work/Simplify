package com.example.coursework.data

import com.example.coursework.data.model.LoggedInUser
import java.io.IOException
import kotlin.random.Random
import kotlin.random.nextUBytes

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
//class LoginDataSource {
//
////    fun login(username: String, email: String, password: String): Result<LoggedInUser> {
////        try {
////            // TODO: handle loggedInUser authentication
////            val encrypted_password = customEncryption(password)
////            val customID = java.util.UUID.randomUUID().toString()
////            val User = LoggedInUser(customID, username, email, encrypted_password)
////            return Result.Success(User)
////        } catch (e: Throwable) {
////            return Result.Error(IOException("Error logging in", e))
////        }
////    }
//
//}