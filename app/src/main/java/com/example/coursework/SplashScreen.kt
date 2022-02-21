package com.example.coursework

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent

import android.os.Bundle
import android.os.Handler


class SplashScreen : AppCompatActivity() {
    private val SPLASH_TIME_OUT = 3000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)
        Handler().postDelayed(Runnable {
            startActivity(
                Intent(
                    this@SplashScreen, OnboardingActivity1::class.java
                )
            )
            finish()
        }, SPLASH_TIME_OUT.toLong())
    }
}