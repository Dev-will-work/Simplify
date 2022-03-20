package com.example.coursework.ui.login

import android.app.Activity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.InputType.*
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.coursework.databinding.ActivityLoginBinding

import com.example.coursework.R
import com.example.coursework.data.model.LoggedInUser
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.io.File
import kotlin.random.Random


class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        // Save the user's current game state
        savedInstanceState.putParcelable("User", loginViewModel.getUserData())
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState)

        // Restore state members from saved instance
        val userData = savedInstanceState.getParcelable<LoggedInUser>("User")
        if (userData != null) {
            loginViewModel.setUserData(userData)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        //binding = DataBindingUtil.inflate(layoutInflater, R.layout.activity_login, ViewGroup,true)
//        binding = ActivityLoginBinding.inflate(layoutInflater)
        //setContentView(binding.root)

        var show = true

        binding.password.eye.setOnClickListener {
            if (show) {
                binding.password.textField1.inputType = (TYPE_CLASS_TEXT or
                        TYPE_TEXT_VARIATION_PASSWORD or
                        TYPE_TEXT_FLAG_CAP_SENTENCES or
                        TYPE_TEXT_FLAG_MULTI_LINE)
                binding.password.textField1.clearFocus()
                show = false
            } else {
                binding.password.textField1.inputType = (TYPE_CLASS_TEXT or
                        TYPE_TEXT_FLAG_CAP_SENTENCES or
                        TYPE_TEXT_FLAG_MULTI_LINE)
                binding.password.textField1.clearFocus()
                show = true
            }
        }

        val username = binding.username.textField1
        val password = binding.password.textField1
        val login = binding.login
        val loading = binding.loading
        val forget = binding.forgotPassword
        val back = binding.back

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            login.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                username.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
        })

        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer

            loading.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                updateUiWithUser(loginResult.success)
            }
            setResult(Activity.RESULT_OK)

            //Complete and destroy login activity once successful
            finish()
        })

        val prefix = getFilesDir()
        if (File("$prefix/userdata.json").exists()) {
            val userData = Json.decodeFromString<LoggedInUser>(File("$prefix/userdata.json").readText())
            loginViewModel.setUserData(userData)
        }

        username.afterTextChanged {
            loginViewModel.loginDataChanged(
                username.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    username.text.toString(),
                    password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                            username.text.toString(),
                            password.text.toString()
                        )
                }
                false
            }

            login.setOnClickListener {
                loading.visibility = View.VISIBLE
                loginViewModel.login(username.text.toString(), password.text.toString())
            }

            forget.setOnClickListener {
                val retrieved_password = loginViewModel.retrievePassword()
                Toast.makeText(context, retrieved_password, Toast.LENGTH_SHORT).show()
            }

            back.setOnClickListener {
                setResult(Activity.RESULT_CANCELED)

                //Complete and destroy login activity if cancelled
                finish()
            }


        }
    }

    override fun onStop() {
        super.onStop()
        val jsonData = Json.encodeToString(loginViewModel.getUserData())
        val prefix = getFilesDir()
        File("$prefix/userdata.json").writeText(jsonData)
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome)
        val displayName = model.displayName
        // TODO : initiate successful logged in experience
        Toast.makeText(
            applicationContext,
            "$welcome $displayName",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}