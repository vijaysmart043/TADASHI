package com.vijay.tadashi.core.ai.gemini.network

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API for the official Gemini REST endpoint.
 */
interface GeminiApi {
    /**
     * Calls the "generateContent" endpoint for a specific model.
     *
     * The API key must be passed via query parameter `key` (per official REST API).
     */
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body body: RequestBody
    ): Response<ResponseBody>
}

