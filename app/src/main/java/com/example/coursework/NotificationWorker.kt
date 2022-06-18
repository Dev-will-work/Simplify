package com.example.coursework

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.datetime.Clock
import kotlinx.datetime.toInstant

/**
 * Class, which handles app push notifications and possibly any other periodical work.
 *
 * @constructor
 * Throws context and specific worker parameters to worker and its base class.
 *
 * @param appContext
 * Context of the application.
 * @param workerParams
 * Specific worker parameters which need to be applied to worker.
 */
class NotificationWorker(appContext: Context, workerParams: WorkerParameters):
    Worker(appContext, workerParams) {

    /**
     * Function which executes all stored work when the time is up.
     *
     * @return Result object with state of the work.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    override fun doWork(): Result {

        val notificationId = 1

        val lastTimestamp = inputData.getString("last_timestamp")
        val disableNotifications = inputData.getBoolean("disable_notifications", true)

            if (lastTimestamp != null) {
                val diff = (Clock.System.now() - lastTimestamp.toInstant()).inWholeDays
                    if (diff > 0 && !disableNotifications) {

                    val intent = Intent(applicationContext, OnboardingActivity1::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    val pendingIntent: PendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)

                    val builder = NotificationCompat.Builder(this.applicationContext, "another")
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentTitle("Come here!")
                        .setContentText("You have not used our app for $diff days, visit us please!")
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)

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