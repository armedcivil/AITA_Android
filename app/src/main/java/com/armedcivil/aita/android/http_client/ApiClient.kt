package com.armedcivil.aita.android.http_client

import android.os.Handler
import android.os.Looper
import com.armedcivil.aita.android.data.Reservation
import com.armedcivil.aita.android.http_client.request.CreateReservationRequest
import com.armedcivil.aita.android.http_client.request.SignInRequest
import com.armedcivil.aita.android.http_client.response.FloorResponse
import com.armedcivil.aita.android.http_client.response.GetReservationResponse
import com.armedcivil.aita.android.services.ApiService
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.Subject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.concurrent.thread

class ApiClient {
    companion object {
        private const val TAG = "ApiClient"
        private const val BASE_URL = "http://192.168.11.3:3001"
        private var accessToken: String? = null
        val instance = ApiClient()
    }

    private val floorResponseSubject: Subject<FloorResponse> = BehaviorSubject.create()
    val floorResponseObservable: Observable<FloorResponse> = floorResponseSubject

    private val retrofit: Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    private val service: ApiService = retrofit.create(ApiService::class.java)

    fun signin(
        email: String,
        password: String,
        callback: (result: Boolean) -> Unit,
    ) {
        // TODO: use kotlin coroutine
        thread {
            runCatching {
                val response = service.signin(SignInRequest(email, password)).execute()
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

    fun fetchFloorData() {
        GlobalScope.launch {
            if (accessToken !== null) {
                val response = service.getFloorData("Bearer $accessToken").execute()
                if (response.body() !== null) {
                    floorResponseSubject.onNext(response.body()!!)
                }
            }
        }
    }

    suspend fun fetchReservations(sheetId: String): GetReservationResponse? {
        val response =
            GlobalScope.async {
                service.getReservations(sheetId).execute().body()
            }
        return response.await()
    }

    suspend fun createReservation(
        sheetId: String,
        startTimestamp: String,
        endTimestamp: String,
    ): Reservation? {
        val response =
            GlobalScope.async {
                service.postReservation(
                    "Bearer $accessToken",
                    CreateReservationRequest(sheetId, startTimestamp, endTimestamp),
                ).execute().body()
            }
        return response.await()
    }

    fun signout() {
        accessToken = null
    }
}
