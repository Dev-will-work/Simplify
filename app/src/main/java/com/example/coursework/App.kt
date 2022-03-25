package com.example.coursework

import android.app.Application
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import android.os.Build
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.annotation.RequiresApi
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.NetworkInterface
import kotlinx.coroutines.*


class App : Application() {
    private var retrofit: Retrofit? = null
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate() {
        super.onCreate()

        val ngrok_address = "http://839c-83-149-47-18.ngrok.io/"

        val gson = GsonBuilder().setLenient().create()
        retrofit = Retrofit.Builder().baseUrl(ngrok_address)
            .addConverterFactory(GsonConverterFactory.create(gson)).build()
        simplifierApi = retrofit?.create(SimplifierApi::class.java)

    }

    fun conectLocallyWithDHCPAssignedIP() {
        var pc_ip = ""

        // temporary workaround to get assigned ip and automatically connect to shared wifi gateway
        val connection_interface = NetworkInterface.getByName("ap0")
        if (connection_interface == null) {
            pc_ip = "127.0.0.1"
            Toast.makeText(this, "No connection from other device found, simplification is not available!", LENGTH_LONG).show()
        } else {
            val full_address =
                connection_interface.interfaceAddresses[1].address.toString().trim('/')
            val subnet = full_address.substringBeforeLast('.')
            val lastbyte = full_address.substringAfterLast('.').toInt()

            Thread {
                val runtime = Runtime.getRuntime()
                for (i in 1..255) {
                    Thread {
                        val proc1 = runtime.exec("ping -w 1 $subnet.$i")
                        val stdInput = BufferedReader(InputStreamReader(proc1.inputStream))

                        val v = "[0-9](?= rec)".toRegex().find(stdInput.readText())?.value

                        if (v == "1" && i != lastbyte) {
                            pc_ip = "$subnet.$i"
                        }
                    }.start()
                }
            }.start()
        }

        val gson = GsonBuilder().setLenient().create()
        Thread {
            while (true) {
                if (pc_ip != "") {
                    retrofit = Retrofit.Builder().baseUrl("http://$pc_ip/")
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