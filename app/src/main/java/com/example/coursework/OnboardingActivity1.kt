package com.example.coursework

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.databinding.DataBindingUtil
import com.example.coursework.databinding.ActivityOnboarding1Binding
import kotlinx.datetime.Clock
import kotlin.time.ExperimentalTime

class OnboardingActivity1 : AppCompatActivity() {
    private lateinit var viewBinding: ActivityOnboarding1Binding

    private fun createNotificationChannel(name: String, description: String, importance: Int, channel_id: String) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channel_id, name, importance).apply {
                this.description = description
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @ExperimentalTime
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_onboarding1)
        val prefix = filesDir
        val channelId = "another"
        val notificationId = 1

        checkObjectInitialization(SettingsObject, this, "$prefix/settingsdata.json")
        checkObjectInitialization(Counters, this, "$prefix/statistics.json")

        createNotificationChannel(
            "Urgent",
            "Channel for urgent messages",
            NotificationManager.IMPORTANCE_HIGH,
            channelId
        )
        val diff = Counters.current_timestamp - Clock.System.now()
        val disableNotifications = SettingsObject.isPropertyToggled("notifications")
        if (diff.inWholeDays > 0 && disableNotifications) {
            val builder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_account)
                .setContentTitle("Come here!")
                .setContentText("You don't use app for one day, visit us please!")
                .setPriority(NotificationCompat.PRIORITY_MAX)

            with(NotificationManagerCompat.from(this)) {
                // notificationId is a unique int for each notification that you must define
                notify(notificationId, builder.build())
            }
        }

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