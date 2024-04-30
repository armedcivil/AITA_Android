package com.armedcivil.aita.android.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.math.BigInteger

@Parcelize
data class User(
    val id: BigInteger,
    val name: String,
    val email: String,
    val iconImagePath: String?,
) : Parcelable
