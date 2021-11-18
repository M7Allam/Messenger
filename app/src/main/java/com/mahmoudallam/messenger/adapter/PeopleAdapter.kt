package com.mahmoudallam.messenger.adapter

import android.content.Context
import android.text.format.DateFormat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.mahmoudallam.messenger.R
import com.mahmoudallam.messenger.databinding.ItemChatBinding
import com.mahmoudallam.messenger.databinding.ItemPeopleBinding
import com.mahmoudallam.messenger.pojo.TextMessage
import com.mahmoudallam.messenger.pojo.UserModel
import com.mahmoudallam.messenger.utilities.GlideApp
import com.xwray.groupie.databinding.BindableItem

class PeopleAdapter(
    val uid: String,
    var user: UserModel,
    val context: Context
) : BindableItem<ItemPeopleBinding>() {

    override fun getLayout(): Int = R.layout.item_people

    override fun bind(binding: ItemPeopleBinding, position: Int) {
        //Set username
        binding.tvItemUsername.text = user.name

        //Set Image profile
        if (user.photoUrl!!.isNotEmpty()) {
            GlideApp.with(context)
                .load(FirebaseStorage.getInstance().getReference(user.photoUrl!!))
                .placeholder(R.drawable.ic_account)
                .into(binding.imgItemUserProfile)
        } else {
            binding.imgItemUserProfile.setImageResource(R.drawable.ic_account)
        }
    }


}

