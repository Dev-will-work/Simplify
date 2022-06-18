package com.example.coursework

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent

import android.os.Bundle
import android.os.Handler
import android.os.Looper

/**
 * This class handles splash screen and its actions.
 *
 * @property splashTimeOut
 * Util object, that simplifies access to activity parts.
 */
class SplashScreen : AppCompatActivity() {
    private val splashTimeOut = 2000

    /**
     * Function, executed when the application is opened first time.
     * @receiver
     * This function handles splash screen behaviour.
     *
     * @param savedInstanceState
     * Bundle with simple types, can be used for temporal storage
     */
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