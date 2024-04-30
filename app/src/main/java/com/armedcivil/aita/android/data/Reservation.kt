package com.armedcivil.aita.android.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.math.BigInteger
import java.util.Date

@Parcelize
data class Reservation(
    val id: BigInteger,
    val sheetId: String,
    val startTimestamp: Date,
    val endTimestamp: Date,
    val user: User,
) : Parcelable
