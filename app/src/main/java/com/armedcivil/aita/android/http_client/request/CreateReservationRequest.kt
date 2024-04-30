package com.armedcivil.aita.android.http_client.request

data class CreateReservationRequest(
    val sheetId: String,
    val startTimestamp: String,
    val endTimestamp: String,
)
