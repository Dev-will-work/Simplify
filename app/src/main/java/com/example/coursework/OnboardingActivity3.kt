package com.example.coursework

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class OnboardingActivity3 : AppCompatActivity() {
    lateinit var skip: Button
    lateinit var get_started: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding3)

        skip = findViewById(R.id.skip)
        get_started = findViewById(R.id.get_started)

        skip.setOnClickListener {
            startActivity(
                Intent(
                    this@OnboardingActivity3, StartActivity::class.java
                )
            )
            finish()
        }

        get_started.setOnClickListener {
            startActivity(
                Intent(
                    this@OnboardingActivity3, StartActivity::class.java
                )
            )
            finish()
        }
    }


}