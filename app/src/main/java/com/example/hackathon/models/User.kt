package com.example.hackathon.models


import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
        val correo: String,
        var password: String
) : Parcelable