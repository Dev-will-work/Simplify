package com.example.coursework

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.coursework.data.model.LoggedInUser
import com.example.coursework.databinding.ActivityRegistrationBinding
import com.example.coursework.ui.login.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class RegistrationActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityRegistrationBinding
    lateinit var registrationViewModel: RegistrationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_registration)

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

        viewBinding.back1.setOnClickListener {
            startActivity(
                Intent(
                    this@RegistrationActivity, StartActivity::class.java
                )
            )
            finish()
        }

        viewBinding.login1.setOnClickListener {
            startActivity(
                Intent(
                    this@RegistrationActivity, LoginActivity::class.java
                )
            )
        }

        var show = true

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

        registrationViewModel = ViewModelProvider(this, RegistrationViewModelFactory())
            .get(RegistrationViewModel::class.java)

        registrationViewModel.registrationFormState.observe(this@RegistrationActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            viewBinding.signUp1.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                viewBinding.username.textField1.error = getString(loginState.usernameError)
            }
            if (loginState.emailError != null) {
                viewBinding.email.textField1.error = getString(loginState.emailError)
            }
            if (loginState.passwordError != null) {
                viewBinding.password.textField1.error = getString(loginState.passwordError)
            }
        })

        registrationViewModel.loginResult.observe(this@RegistrationActivity, Observer {
            val loginResult = it ?: return@Observer

            //viewBinding.loading.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                updateUiWithUser(loginResult.success)
                val jsonData = Json.encodeToString(registrationViewModel.getUserData())
                val prefix = getFilesDir()
                File("$prefix/userdata.json").writeText(jsonData)
            }

            startActivity(
                Intent(
                    this@RegistrationActivity, MainActivity::class.java
                )
            )

            //Complete and destroy login activity once successful
            finish()
        })

        val prefix = getFilesDir()
        if (File("$prefix/userdata.json").exists()) {
            var userData: LoggedInUser? = null
            try {
                userData =
                    Json.decodeFromString<LoggedInUser>(File("$prefix/userdata.json").readText())
            } catch (e: Exception) {
                Toast.makeText(this, "Can't decode user data", LENGTH_SHORT).show()
            }
            userData?.let {
                registrationViewModel.setUserData(it)
            }
        }

        viewBinding.username.textField1.afterTextChanged {
            registrationViewModel.loginDataChanged(
                viewBinding.username.textField1.text.toString(),
                viewBinding.email.textField1.text.toString(),
                viewBinding.password.textField1.text.toString()
            )
        }

        viewBinding.password.textField1.apply {
            afterTextChanged {
                registrationViewModel.loginDataChanged(
                    viewBinding.username.textField1.text.toString(),
                    viewBinding.email.textField1.text.toString(),
                    viewBinding.password.textField1.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        registrationViewModel.login(
                            viewBinding.username.textField1.text.toString(),
                            viewBinding.email.textField1.text.toString(),
                            viewBinding.password.textField1.text.toString()
                        )
                }
                false
            }

            viewBinding.login1.setOnClickListener {
                //loading.visibility = View.VISIBLE
                val i = Intent(this@RegistrationActivity, LoginActivity::class.java)
                launcher.launch(i)
            }

        }
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