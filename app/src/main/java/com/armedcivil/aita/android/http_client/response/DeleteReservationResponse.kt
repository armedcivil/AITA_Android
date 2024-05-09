package com.armedcivil.aita.android.http_client.response

data class DeleteReservationResponse(
    val startTimestamp: String,
    val endTimestamp: String,
    val sheetId: String,
)
