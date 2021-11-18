package com.mahmoudallam.messenger.adapter

import android.content.Context
import android.text.format.DateFormat
import com.mahmoudallam.messenger.R
import com.mahmoudallam.messenger.databinding.ItemTextMessageReceiverBinding
import com.mahmoudallam.messenger.pojo.TextMessage
import com.xwray.groupie.databinding.BindableItem

class ReceiverTextMessagesAdapter(
    private val textMessage: TextMessage,
    private val messageId: String,
    val context: Context
) :
    BindableItem<ItemTextMessageReceiverBinding>() {

    override fun getLayout(): Int = R.layout.item_text_message_receiver

    override fun bind(binding: ItemTextMessageReceiverBinding, position: Int) {
        binding.tvMessageReceive.text = textMessage.text
        binding.tvMessageTimeReceive.text = DateFormat.format("hh:mm a", textMessage.date).toString()
    }


}