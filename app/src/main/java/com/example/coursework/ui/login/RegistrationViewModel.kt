package com.example.coursework.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import com.example.coursework.data.LoginRepository
import com.example.coursework.data.Result

import com.example.coursework.R
import com.example.coursework.data.model.LoggedInUser

class RegistrationViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<RegistrationFormState>()
    val registrationFormState: LiveData<RegistrationFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(username: String, email: String, password: String) {
        // can be launched in a separate asynchronous job
        val result = loginRepository.register(username, email, password)

        if (result is Result.Success) {
            val displayName = result.data.displayName
            if (displayName != null) {
                _loginResult.value =
                    LoginResult(success = LoggedInUserView(displayName = displayName))
            }
        } else {
            _loginResult.value = LoginResult(error = R.string.login_failed)
        }
    }

    fun loginDataChanged(username: String, email: String, password: String) {
        if (!isUserNameValid(username)) {
            _loginForm.value = RegistrationFormState(usernameError = R.string.invalid_username)
        } else if (!isEmailValid(email)) {
            _loginForm.value = RegistrationFormState(emailError = R.string.invalid_email)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = RegistrationFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = RegistrationFormState(isDataValid = true)
        }
    }

    private fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return username.isNotBlank()
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }

    fun retrievePassword(): String {
        return loginRepository.retrievePassword()
    }

    fun getUserData(): LoggedInUser? {
        return loginRepository.user
    }

    fun setUserData(user: LoggedInUser) {
        loginRepository.setUserData(user)
    }
}