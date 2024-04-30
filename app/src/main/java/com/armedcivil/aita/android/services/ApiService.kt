package com.armedcivil.aita.android.services

import com.armedcivil.aita.android.data.Reservation
import com.armedcivil.aita.android.http_client.request.CreateReservationRequest
import com.armedcivil.aita.android.http_client.request.SignInRequest
import com.armedcivil.aita.android.http_client.response.FloorResponse
import com.armedcivil.aita.android.http_client.response.GetReservationResponse
import com.armedcivil.aita.android.http_client.response.SignInResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("auth/user/login")
    fun signin(
        @Body() credential: SignInRequest,
    ): Call<SignInResponse>

    @GET("company/floor")
    fun getFloorData(
        @Header("Authorization") accessToken: String,
    ): Call<FloorResponse>

    @GET("reservation")
    fun getReservations(
        @Query("sheet_id") sheetId: String,
    ): Call<GetReservationResponse>

    @POST("reservation")
    fun postReservation(
        @Header("Authorization") accessToken: String,
        @Body() request: CreateReservationRequest,
    ): Call<Reservation>
}
