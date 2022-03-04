package com.example.coursework

import android.app.Application
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class App : Application() {
    private var retrofit: Retrofit? = null
    override fun onCreate() {
        super.onCreate()
        val gson = GsonBuilder().setLenient().create()
        retrofit = Retrofit.Builder().baseUrl("http://192.168.120.204/")
            .addConverterFactory(GsonConverterFactory.create(gson)).build()
        simplifierApi = retrofit?.create(SimplifierApi::class.java)
    }

    companion object {
        private var simplifierApi: SimplifierApi? = null
        val api: SimplifierApi?
            get() = simplifierApi
    }
}