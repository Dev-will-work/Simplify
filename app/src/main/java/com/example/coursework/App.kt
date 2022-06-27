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
import androidx.work.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.NetworkInterface
import java.util.concurrent.TimeUnit

/**
 * class App is the main class of the Simplify application where
 * all the preparations before the rendering are done.
 * @property retrofit
 * Main instance of retrofit which responsible for connection with the Flask server backend.
 */
class App : Application() {
    private var retrofit: Retrofit? = null

    /**
     * Function which creates new notification channels.
     *
     * @param name
     * Name of new notification channel
     * @param description
     * Extended description of new notification channel
     * @param importance
     * Importance of new notification channel. @see [NotificationManager] IMPORTANCE constants.
     * @param channel_id
     * Unique id for the new channel.
     */
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

    /**
     * Function which used for throwing necessary data into the notification worker.
     *
     * @return Data class with all needed data of simple types.
     */
    private fun createInputData(): Data {
        return Data.Builder()
            .putString("last_timestamp", Counters.old_timestamp.toString())
            .putBoolean("disable_notifications", SettingsObject.isPropertyToggled(getString(R.string.notification_request_check_part)))
            .build()
    }

    /**
     * Function, executed when the application is opened first time.
     * @receiver
     * This function initializes retrofit interface and setups periodical app notifications.
     */
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

        val uploadWorkRequest: PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.DAYS)
                .setInputData(createInputData())
                .setInitialDelay(6, TimeUnit.HOURS)
                .build()

//        WorkManager
//            .getInstance(this.applicationContext).cancelAllWorkByTag("com.example.coursework.NotificationWorker")

        WorkManager
            .getInstance(this.applicationContext)
            .enqueueUniquePeriodicWork("Notification work", ExistingPeriodicWorkPolicy.KEEP, uploadWorkRequest)

//        clearFile("$prefix/userdata.json")
//        clearFile("$prefix/languagedata.json")
//        clearFile("$prefix/settingsdata.json")

    }

    /**
     * Old utility function responsible for finding the ip of server backend,
     * hosted on the connected by wifi PC
     */
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

    /**
     * Anonymous object with link on retrofit API interface.
     * @property simplifierApi
     * getter for retrofit API interface.
     * @property api
     * Shortened getter for retrofit API interface.
     */
    companion object {
        private var simplifierApi: SimplifierApi? = null
        val api: SimplifierApi?
            get() = simplifierApi
    }
}