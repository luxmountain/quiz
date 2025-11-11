package com.uilover.project247.data.api

import com.uilover.project247.data.models.DictionaryEntry
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Free Dictionary API Service
 * API Documentation: https://dictionaryapi.dev/
 */
interface DictionaryApiService {
    
    @GET("api/v2/entries/en/{word}")
    suspend fun searchWord(@Path("word") word: String): Response<List<DictionaryEntry>>
    
    companion object {
        private const val BASE_URL = "https://api.dictionaryapi.dev/"
        
        fun create(): DictionaryApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(DictionaryApiService::class.java)
        }
    }
}
