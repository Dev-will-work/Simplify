package com.example.coursework

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class OnboardingActivity1 : AppCompatActivity() {
    lateinit var skip: Button
    lateinit var next: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding1)

        skip = findViewById(R.id.skip)
        next = findViewById(R.id.get_started)

        skip.setOnClickListener {
            startActivity(
                Intent(
                    this@OnboardingActivity1, MainActivity::class.java
                )
            )
            finish()
        }

        next.setOnClickListener {
            startActivity(
                Intent(
                    this@OnboardingActivity1, OnboardingActivity2::class.java
                )
            )
            finish()
        }
    }
}