package com.example.chatroom

//要發送的object
class ChatMessage(
    val id: String, val text: String, val formID: String, val toID: String, val timeStamp: Long
) {
    constructor() : this("", "", "", "", -1)
}