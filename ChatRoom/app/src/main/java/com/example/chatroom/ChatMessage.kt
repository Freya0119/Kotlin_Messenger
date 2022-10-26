package com.example.chatroom

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

//要發送的object
@Parcelize
class ChatMessage(
    val id: String, val text: String, val formID: String, val toID: String, val timeStamp: Long
) : Parcelable {
    constructor() : this("", "", "", "", -1)
}