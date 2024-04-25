package com.armedcivil.aita.android.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Floor(val label: String, val objects: Array<SceneObject>) : Parcelable

@Parcelize
data class SceneObject(
    val modelPath: String,
    val matrix: Matrix4,
    val isChair: Boolean,
    val id: String,
    val topImagePath: String,
) : Parcelable

@Parcelize
data class Matrix4(val elements: Array<Double>) : Parcelable
