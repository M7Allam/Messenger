package com.mahmoudallam.messenger.adapter

import android.content.Context
import android.text.format.DateFormat
import com.mahmoudallam.messenger.R
import com.mahmoudallam.messenger.databinding.ItemTextMessageSenderBinding
import com.mahmoudallam.messenger.pojo.TextMessage
import com.xwray.groupie.databinding.BindableItem

class SenderTextMessagesAdapter(
    private val textMessage: TextMessage,
    private val messageId: String,
    val context: Context
) :
    BindableItem<ItemTextMessageSenderBinding>() {

    override fun getLayout(): Int = R.layout.item_text_message_sender

    override fun bind(binding: ItemTextMessageSenderBinding, position: Int) {
        binding.tvMessageSender.text = textMessage.text
        binding.tvMessageTimeSender.text = DateFormat.format("hh:mm a", textMessage.date).toString()
    }


}