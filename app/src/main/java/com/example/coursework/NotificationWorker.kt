package com.example.coursework

import android.app.Activity
import android.content.Context
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import android.widget.Toast.LENGTH_SHORT
import androidx.work.Worker
import androidx.work.WorkerParameters

class UploadWorker(appContext: Context, workerParams: WorkerParameters):
    Worker(appContext, workerParams) {
    override fun doWork(): Result {

        // Do the work here--in this case, upload the images.
        OnboardingActivity1.runOnUiThread(Runnable { Toast.makeText(activity, "Hello", LENGTH_SHORT).show() })
        Toast.makeText(this.applicationContext, "", LENGTH_LONG).show()

        // Indicate whether the work finished successfully with the Result
        return Result.success()
    }
}