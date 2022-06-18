package com.example.coursework

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for connections with text simplifier on Flask backend.
 *
 */
interface SimplifierApi {
    /**
     * Function, which sends text [inputText] and user ID [id] to Flask backend.
     *
     * @param inputText
     * Text, which needs to be simplified.
     * @param id
     * Unique user ID.
     * @return abstract Call with simplified text.
     */
    @GET("ai_request/")
    fun getSimplifiedText(
        @Query("q") inputText: String?,
        @Query("id") id: String?
    ): Call<SimplifierData?>?
}