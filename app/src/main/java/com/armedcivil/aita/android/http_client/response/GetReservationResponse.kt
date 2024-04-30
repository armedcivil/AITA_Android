package com.armedcivil.aita.android.http_client.response

import android.os.Parcelable
import com.armedcivil.aita.android.data.Reservation
import kotlinx.parcelize.Parcelize

@Parcelize
data class GetReservationResponse(val reservations: Array<Reservation>) : Parcelable
