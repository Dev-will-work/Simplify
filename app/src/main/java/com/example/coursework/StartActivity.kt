package com.example.coursework

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.startActivity
import com.example.coursework.databinding.ActivityStartBinding
import com.example.coursework.ui.login.LoginActivity
import kotlin.reflect.KClass

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

        setClickMoveToActivity(viewBinding.continueGuest, this@StartActivity, MainActivity::class)
        setClickMoveToActivity(viewBinding.signUp, this@StartActivity, RegistrationActivity::class)
    }
}

fun setClickMoveToActivity(view: View, ctx: Context, dest: KClass<*>) {
    view.setOnClickListener {
        startActivity(ctx,
            Intent(
                ctx, dest.java
            ), Bundle()
        )
        (ctx as Activity).finish()
    }
}