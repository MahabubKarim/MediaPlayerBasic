package com.mmk.mediaplayerbasic.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface JamendoApiService {
    @GET("tracks/")
    suspend fun getPopularTracks(
        @Query("client_id") clientId: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 20,
        @Query("order") order: String = "popularity_total"
    ): TrackResponse

    /*companion object {
        private const val BASE_URL = "https://api.jamendo.com/"
+
        fun create(): JamendoApiService {
            // Configure HTTP client
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(JamendoApiService::class.java)
        }
    }*/
}