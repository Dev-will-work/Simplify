package com.example.coursework

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.databinding.DataBindingUtil
import androidx.work.Data
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.example.coursework.databinding.ActivityOnboarding1Binding
import kotlinx.datetime.Clock
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime


class OnboardingActivity1 : AppCompatActivity() {
    private lateinit var viewBinding: ActivityOnboarding1Binding

    @OptIn(ExperimentalTime::class)
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_onboarding1)
        val prefix = filesDir

        checkObjectInitialization(SettingsObject, this, "$prefix/settingsdata.json")

        greeting(this)

        val disableOnboarding = SettingsObject.isPropertyToggled("onboarding")
        if (disableOnboarding) {
            startActivity(
                Intent(
                    this@OnboardingActivity1, StartActivity::class.java
                )
            )
            finish()
            return
        }

        with(viewBinding) {
            setClickMoveToActivity(skip, this@OnboardingActivity1, StartActivity::class)
            setClickMoveToActivity(next, this@OnboardingActivity1, OnboardingActivity2::class)
        }
    }
}