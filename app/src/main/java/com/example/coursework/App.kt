package com.example.coursework

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.os.Build
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.annotation.RequiresApi
import androidx.work.Data
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.NetworkInterface
import java.util.concurrent.TimeUnit


class App : Application() {
    private var retrofit: Retrofit? = null

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

    private fun createInputData(): Data {
        return Data.Builder()
            .putString("last_timestamp", Counters.old_timestamp.toString())
            .putBoolean("disable_notifications", SettingsObject.isPropertyToggled("notifications"))
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate() {
        super.onCreate()

        val ngrokAddress = "http://de36-178-178-85-39.ngrok.io/"
        val channelId = "another"
        val prefix = filesDir

        val gson = GsonBuilder().setLenient().create()
        retrofit = Retrofit.Builder().baseUrl(ngrokAddress)
            .addConverterFactory(GsonConverterFactory.create(gson)).build()
        simplifierApi = retrofit?.create(SimplifierApi::class.java)

        checkObjectInitialization(SettingsObject, this, "$prefix/settingsdata.json")
        checkObjectInitialization(Counters, this, "$prefix/statistics.json")

        createNotificationChannel(
            "Urgent",
            "Channel for urgent messages",
            NotificationManager.IMPORTANCE_HIGH,
            channelId
        )

        val uploadWorkRequest: WorkRequest =
            PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.DAYS)
                .setInputData(createInputData())
                .build()

        WorkManager
            .getInstance(this.applicationContext).cancelAllWorkByTag("com.example.coursework.NotificationWorker")

        WorkManager
            .getInstance(this.applicationContext)
            .enqueue(uploadWorkRequest)

//        clearFile("$prefix/languagedata.json")

    }

    fun connectLocallyWithDHCPAssignedIP() {
        var pcIP = ""

        // temporary workaround to get assigned ip and automatically connect to shared wifi gateway
        val connectionInterface = NetworkInterface.getByName("ap0")
        if (connectionInterface == null) {
            pcIP = "127.0.0.1"
            Toast.makeText(this, "No connection from other device found, simplification is not available!", LENGTH_LONG).show()
        } else {
            val fullAddress =
                connectionInterface.interfaceAddresses[1].address.toString().trim('/')
            val subnet = fullAddress.substringBeforeLast('.')
            val lastbyte = fullAddress.substringAfterLast('.').toInt()

            Thread {
                val runtime = Runtime.getRuntime()
                for (i in 1..255) {
                    Thread {
                        val proc1 = runtime.exec("ping -w 1 $subnet.$i")
                        val stdInput = BufferedReader(InputStreamReader(proc1.inputStream))

                        val v = "[0-9](?= rec)".toRegex().find(stdInput.readText())?.value

                        if (v == "1" && i != lastbyte) {
                            pcIP = "$subnet.$i"
                        }
                    }.start()
                }
            }.start()
        }

        val gson = GsonBuilder().setLenient().create()
        Thread {
            while (true) {
                if (pcIP != "") {
                    retrofit = Retrofit.Builder().baseUrl("http://$pcIP/")
                        .addConverterFactory(GsonConverterFactory.create(gson)).build()
                    simplifierApi = retrofit?.create(SimplifierApi::class.java)
                    return@Thread
                }
            }
        }.start()
    }

    companion object {
        private var simplifierApi: SimplifierApi? = null
        val api: SimplifierApi?
            get() = simplifierApi
    }
}