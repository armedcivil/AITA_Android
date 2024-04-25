package com.armedcivil.aita.android.services

import com.armedcivil.aita.android.http_client.request.SignInRequest
import com.armedcivil.aita.android.http_client.response.FloorResponse
import com.armedcivil.aita.android.http_client.response.SignInResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("auth/user/login")
    fun signin(
        @Body() credential: SignInRequest,
    ): Call<SignInResponse>

    @GET("company/floor")
    fun getFloorData(
        @Header("Authorization") accessToken: String,
    ): Call<FloorResponse>
}
