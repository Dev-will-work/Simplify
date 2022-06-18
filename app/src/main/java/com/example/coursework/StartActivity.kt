package com.example.coursework

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.example.coursework.databinding.ActivityStartBinding

/**
 * This class handles start screen and its actions.
 *
 * @property viewBinding
 * Util object, that simplifies access to activity parts.
 */
class StartActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityStartBinding

    /**
     * Function, executed when the application is opened first time.
     * @receiver
     * This function handles start screen behaviour.
     *
     * @param savedInstanceState
     * Bundle with simple types, can be used for temporal storage
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_start)

        val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                RESULT_OK -> {
                    startActivity(
                        Intent(
                            this@StartActivity, MainActivity::class.java
                        )
                    )
                    finish()
                }
                else -> {}
            }
        }
        viewBinding.logIn.setOnClickListener {
            val i = Intent(this, LoginActivity::class.java)
            launcher.launch(i)
        }

        setClickMoveToActivity(viewBinding.continueGuest, this, MainActivity::class)
        setClickMoveToActivity(viewBinding.signUp, this, RegistrationActivity::class)
    }
}