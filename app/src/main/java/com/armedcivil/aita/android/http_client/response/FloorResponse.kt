package com.armedcivil.aita.android.http_client.response

import android.os.Parcelable
import com.armedcivil.aita.android.data.Floor
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FloorResponse(val viewerKey: String, val floors: Array<Floor>) : Parcelable
