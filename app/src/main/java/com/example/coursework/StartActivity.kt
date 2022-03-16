package com.example.coursework

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import com.example.coursework.databinding.ActivityStartBinding
import com.example.coursework.ui.login.LoginActivity

class StartActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityStartBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

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

        viewBinding.continueGuest.setOnClickListener {
            startActivity(
                Intent(
                    this@StartActivity, MainActivity::class.java
                )
            )
            finish()
        }

        viewBinding.signUp.setOnClickListener {

        }
    }
}