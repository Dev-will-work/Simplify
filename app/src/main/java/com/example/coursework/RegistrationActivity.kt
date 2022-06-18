package com.example.coursework

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.inputmethod.EditorInfo
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.example.coursework.databinding.ActivityRegistrationBinding

/**
 * This class handles registration screen and its actions.
 *
 * @property viewBinding
 * Util object, that simplifies access to activity parts.
 */
class RegistrationActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityRegistrationBinding

    /**
     * Function, executed when the application is opened first time.
     * @receiver
     * This function handles all registration process and other registration screen behaviour.
     *
     * @param savedInstanceState
     * Bundle with simple types, can be used for temporal storage
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_registration)

        val username = viewBinding.username.textField1
        val email = viewBinding.email.textField1
        val password = viewBinding.password.textField1
        var registrationState: Boolean
        var show = true

        val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                RESULT_OK -> {
                    startActivity(
                        Intent(
                            this@RegistrationActivity, MainActivity::class.java
                        )
                    )
                    finish()
                }
                else -> {}
            }
        }

        viewBinding.back.setOnClickListener {
            startActivity(
                Intent(
                    this@RegistrationActivity, StartActivity::class.java
                )
            )
            finish()
        }

        viewBinding.login.setOnClickListener {
            startActivity(
                Intent(
                    this@RegistrationActivity, LoginActivity::class.java
                )
            )
        }

        viewBinding.password.eye.setOnClickListener {
            if (show) {
                viewBinding.password.textField1.inputType = (InputType.TYPE_CLASS_TEXT or
                        InputType.TYPE_TEXT_VARIATION_PASSWORD or
                        InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or
                        InputType.TYPE_TEXT_FLAG_MULTI_LINE)
                show = false
            } else {
                viewBinding.password.textField1.inputType = (InputType.TYPE_CLASS_TEXT or
                        InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or
                        InputType.TYPE_TEXT_FLAG_MULTI_LINE)
                show = true
            }
            viewBinding.password.textField1.maxLines = 4
            viewBinding.password.textField1.clearFocus()
        }

        viewBinding.signUp.setOnClickListener {
            val inputUsername = viewBinding.username.textField1.text?.toString()
            val inputEmail = viewBinding.email.textField1.text?.toString()
            val inputPassword = viewBinding.password.textField1.text?.toString()
            if (inputEmail != null && inputPassword != null && inputUsername != null) {
                registrationState = CachedUser.register(
                    this,
                    inputUsername,
                    inputEmail,
                    inputPassword
                )
                if (registrationState) {
                    startActivity(
                        Intent(
                            this@RegistrationActivity, MainActivity::class.java
                        )
                    )
                }
                finish()
            }
        }

        viewBinding.username.textField1.afterTextChanged {
            email.error = if (!CachedUser.validator.isUserNameValid(it)) {
                "Bad username!"
            } else {
                null
            }
        }

        email.afterTextChanged {
            email.error = if (!CachedUser.validator.isEmailValid(it)) {
                "Bad email!"
            } else {
                null
            }
        }

        viewBinding.password.textField1.apply {
            afterTextChanged {
                password.error = if (!CachedUser.validator.isPasswordValid(it)) {
                    "Bad password!"
                } else {
                    null
                }
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        CachedUser.register(
                            this@RegistrationActivity,
                            username.text.toString(),
                            email.text.toString(),
                            password.text.toString()
                        )
                }
                false
            }

            viewBinding.login.setOnClickListener {
                val i = Intent(this@RegistrationActivity, LoginActivity::class.java)
                launcher.launch(i)
            }

        }
    }
}