package com.mahmoudallam.messenger.pojo

import com.mahmoudallam.messenger.utilities.Message
import com.mahmoudallam.messenger.utilities.MessageType
import java.util.*

data class TextMessage (
    val text: String,
    override val senderId: String,
    override val receiverId: String,
    override val senderName: String,
    override val receiverName: String,
    override val date: Date,
    override val type: String = MessageType.TEXT

) : Message{

    constructor() : this("", "", "", "", "", Date(0))
}