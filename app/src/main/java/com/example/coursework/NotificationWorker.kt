package com.example.coursework

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import android.widget.Toast.LENGTH_SHORT
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.datetime.Clock
import kotlinx.datetime.toInstant

class NotificationWorker(appContext: Context, workerParams: WorkerParameters):
    Worker(appContext, workerParams) {
    override fun doWork(): Result {

        val notificationId = 1

        val lastTimestamp = inputData.getString("last_timestamp")
        val disableNotifications = inputData.getBoolean("disable_notifications", true)

            if (lastTimestamp != null) {
                val diff = (Clock.System.now() - lastTimestamp.toInstant()).inWholeDays
                        if (diff > 0 && !disableNotifications) {
                    val builder = NotificationCompat.Builder(this.applicationContext, "another")
                        .setSmallIcon(R.drawable.ic_simplify)
                        .setContentTitle("Come here!")
                        .setContentText("You have not used our app for $diff days, visit us please!")
                        .setPriority(NotificationCompat.PRIORITY_MAX)

                    with(NotificationManagerCompat.from(this.applicationContext)) {
                        // notificationId is a unique int for each notification that you must define
                        notify(notificationId, builder.build())
                    }
                }

            // Do the work here--in this case, upload the images.
//            Handler(Looper.getMainLooper()).post {
//                //code that runs in main
//
//            }
        }

        // Indicate whether the work finished successfully with the Result
        return Result.success()
    }
}