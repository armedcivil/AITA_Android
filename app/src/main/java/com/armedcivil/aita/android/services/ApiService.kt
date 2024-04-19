package com.armedcivil.aita.android.services

import com.armedcivil.aita.android.http_client.Credential
import com.armedcivil.aita.android.http_client.SignInResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("auth/user/login")
    fun signin(@Body() credential: Credential): Call<SignInResponse>
}