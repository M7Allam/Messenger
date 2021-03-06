package com.mahmoudallam.messenger.utilities

import java.util.*

interface Message {

    val senderId: String
    val receiverId: String
    val senderName: String
    val receiverName: String
    val date: Date
    val type: String

}