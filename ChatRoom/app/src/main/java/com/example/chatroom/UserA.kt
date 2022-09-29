package com.example.chatroom

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class UserA(val uid: String, val username: String, val profileImageUrl: String) : Parcelable {
    constructor() : this("", "", "")
}