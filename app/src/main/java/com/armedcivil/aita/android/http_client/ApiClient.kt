package com.armedcivil.aita.android.http_client

import android.os.Handler
import android.os.Looper
import com.armedcivil.aita.android.services.ApiService
import okhttp3.Callback
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.concurrent.thread

class ApiClient {
    companion object {
        private const val TAG = "ApiClient"
        private const val BASE_URL = "http://192.168.11.3:3001"
        private var accessToken: String? = null
        val instance = ApiClient();
    }

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service: ApiService = retrofit.create(ApiService::class.java)

    fun signin(email: String, password: String, callback: (result: Boolean) -> Unit) {
        thread {
            runCatching {
                val response = service.signin(Credential(email, password)).execute()
                if (response.body() !== null) {
                    accessToken = response.body()!!.accessToken
                    Handler(Looper.getMainLooper()).post {
                        callback(true)
                    }
                } else {
                    Handler(Looper.getMainLooper()).post {
                        callback(false)
                    }
                }
            }
        }
    }

    fun signout() {
        accessToken = null
    }
}