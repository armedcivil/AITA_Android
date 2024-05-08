package com.armedcivil.aita.android.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.math.BigInteger

@Parcelize
data class Reservation(
    val id: BigInteger,
    val sheetId: String,
    val startTimestamp: String,
    val endTimestamp: String,
    val user: User,
) : Parcelable
