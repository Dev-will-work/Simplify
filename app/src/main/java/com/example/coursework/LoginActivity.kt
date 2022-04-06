package com.example.coursework

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.InputType.*
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.coursework.databinding.ActivityLoginBinding


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        val email = binding.email.textField1
        val password = binding.password.textField1
        val login = binding.login
        val forget = binding.forgotPassword
        val back = binding.back

        var show = true
        var loginState: Boolean

        val prefix = filesDir
        checkObjectInitialization(CachedUser, this, "$prefix/userdata.json")

        binding.password.eye.setOnClickListener {
            if (show) {
                binding.password.textField1.inputType = (TYPE_CLASS_TEXT or
                        TYPE_TEXT_VARIATION_PASSWORD or
                        TYPE_TEXT_FLAG_CAP_SENTENCES or
                        TYPE_TEXT_FLAG_MULTI_LINE)
                show = false
            } else {
                binding.password.textField1.inputType = (TYPE_CLASS_TEXT or
                        TYPE_TEXT_FLAG_CAP_SENTENCES or
                        TYPE_TEXT_FLAG_MULTI_LINE)
                show = true
            }
            binding.password.textField1.maxLines = 4
            binding.password.textField1.clearFocus()
        }

        binding.login.setOnClickListener {
            val inputEmail = binding.email.textField1.text?.toString()
            val inputPassword = binding.password.textField1.text?.toString()
            if (inputEmail != null && inputPassword != null) {
                loginState = CachedUser.login(
                    inputEmail,
                    inputPassword
                )
                if (loginState) {
                    setResult(Activity.RESULT_OK)
                } else {
                    setResult(Activity.RESULT_CANCELED)
                }
                finish()
            }
        }

        email.afterTextChanged {
            email.error = if (!CachedUser.validator.isEmailValid(it)) {
                "Bad email!"
            } else {
                null
            }
        }

        password.apply {
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
                        CachedUser.login(email.text.toString(), password.text.toString())
                }
                false
            }

            login.setOnClickListener {
                CachedUser.login(email.text.toString(), password.text.toString())
            }

            forget.setOnClickListener {
                val retrievedPassword = CachedUser.retrievePassword()
                Toast.makeText(context, retrievedPassword, Toast.LENGTH_SHORT).show()
            }

            back.setOnClickListener {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }
    }
}