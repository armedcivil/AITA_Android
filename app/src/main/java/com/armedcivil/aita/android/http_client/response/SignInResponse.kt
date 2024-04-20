package com.armedcivil.aita.android.http_client.response

import com.google.gson.annotations.SerializedName

data class SignInResponse(
    @SerializedName("accessToken")
    val accessToken: String
)
