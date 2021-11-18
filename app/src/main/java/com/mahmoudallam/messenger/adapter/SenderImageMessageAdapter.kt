package com.mahmoudallam.messenger.adapter

import android.content.Context
import android.text.format.DateFormat
import com.google.firebase.storage.FirebaseStorage
import com.mahmoudallam.messenger.R
import com.mahmoudallam.messenger.databinding.ItemImageMessageSenderBinding
import com.mahmoudallam.messenger.pojo.ImageMessage
import com.mahmoudallam.messenger.utilities.GlideApp
import com.xwray.groupie.databinding.BindableItem


class SenderImageMessageAdapter(
    private val imageMessage: ImageMessage,
    private val messageId: String,
    val context: Context
) :
    BindableItem<ItemImageMessageSenderBinding>() {

    override fun getLayout(): Int = R.layout.item_image_message_sender

    override fun bind(binding: ItemImageMessageSenderBinding, position: Int) {
        //Set time
        binding.tvImageMessageTimeSender.text = DateFormat.format("hh:mm a", imageMessage.date).toString()
        //Set Image
        if (imageMessage.imgPath.isNotEmpty()) {
            GlideApp.with(context)
                .load(FirebaseStorage.getInstance().getReference(imageMessage.imgPath))
                .placeholder(R.drawable.ic_blank_image)
                .into(binding.imgImageMessageSender)
        }else{
            binding.imgImageMessageSender.setImageResource(R.drawable.ic_blank_image)
        }
    }


}