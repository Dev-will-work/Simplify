package com.example.coursework

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface SimplifierApi {
    @GET("ai_request/")
    fun getSimplifiedText(
        @Query("q") inputText: String?
    ): Call<SimplifierData?>?
}