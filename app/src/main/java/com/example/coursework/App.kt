package com.example.coursework

import android.app.Application
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.os.Build
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.annotation.RequiresApi
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.NetworkInterface


class App : Application() {
    private var retrofit: Retrofit? = null
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate() {
        super.onCreate()

        val ngrokAddress = "http://de36-178-178-85-39.ngrok.io/"

        val gson = GsonBuilder().setLenient().create()
        retrofit = Retrofit.Builder().baseUrl(ngrokAddress)
            .addConverterFactory(GsonConverterFactory.create(gson)).build()
        simplifierApi = retrofit?.create(SimplifierApi::class.java)

//        val prefix = filesDir
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