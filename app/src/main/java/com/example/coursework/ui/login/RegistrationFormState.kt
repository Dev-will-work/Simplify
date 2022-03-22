package com.example.coursework.ui.login

/**
 * Data validation state of the registration form.
 */
data class RegistrationFormState(
    val usernameError: Int? = null,
    val emailError: Int? = null,
    val passwordError: Int? = null,
    val isDataValid: Boolean = false
)