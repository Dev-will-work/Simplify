package com.example.coursework

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent

import android.os.Bundle
import android.os.Handler
import android.os.Looper


class SplashScreen : AppCompatActivity() {
    private val splashTimeOut = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(
                Intent(
                    this@SplashScreen, OnboardingActivity1::class.java
                )
            )
            finish()
        }, splashTimeOut.toLong())
    }
}