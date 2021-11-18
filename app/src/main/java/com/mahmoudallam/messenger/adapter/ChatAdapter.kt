package com.mahmoudallam.messenger.adapter

import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.mahmoudallam.messenger.R
import com.mahmoudallam.messenger.databinding.ItemChatBinding
import com.mahmoudallam.messenger.pojo.TextMessage
import com.mahmoudallam.messenger.pojo.UserModel
import com.mahmoudallam.messenger.ui.main.MainViewModel
import com.mahmoudallam.messenger.utilities.GlideApp
import com.xwray.groupie.GroupDataObserver
import com.xwray.groupie.databinding.BindableItem
import com.xwray.groupie.databinding.ViewHolder

class ChatAdapter(
    val uid: String,
    var user: UserModel,
    val lastMessage: TextMessage,
    val context: Context,
    val activity: FragmentActivity,
    val viewModel: MainViewModel
) : BindableItem<ItemChatBinding>() {

    var mutableMap : MutableLiveData<HashMap<String, UserModel?>> = viewModel.getUsersChatsFragmentList()

    override fun getLayout(): Int = R.layout.item_chat

    override fun bind(binding: ItemChatBinding, position: Int) {
        getUser {
            binding.tvItemUsername.text = user.name
            binding.tvItemTime.text = DateFormat.format("hh:mm a", lastMessage.date).toString()
            if(lastMessage.senderId == FirebaseAuth.getInstance().currentUser?.uid){
                val text = context.getString(R.string.you) + " ${lastMessage.text}"
                binding.tvItemLastMessage.text = text
            }else{
                binding.tvItemLastMessage.text =  lastMessage.text
            }
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

    override fun unbind(holder: ViewHolder<ItemChatBinding>) {
        mutableMap.removeObservers(activity)
        Log.i("TAG", "ChatAdapter unbind: removeObservers")
        super.unbind(holder)
    }

    private fun getUser(onSuccess: (UserModel) -> Unit) {

        //Get User from ViewModel
        mutableMap.observe(activity, { usersMap ->
            if (usersMap[uid]?.name.isNullOrEmpty()) {
                Log.i("TAG", "ChatAdapter getUser: From Firebase")
                //Get User from Firebase
                FirebaseFirestore.getInstance().collection("Users").document(uid).get()
                    .addOnSuccessListener {
                        user = it.toObject(UserModel::class.java)!!
                        Log.i("TAG", "ChatAdapter getUser: ${user.name}")
                        usersMap[uid] = user
                        viewModel.setUsersChatsFragmentList(usersMap)
                    }

            } else {
                Log.i("TAG", "ChatAdapter getUser: From Map")
                user = usersMap[uid]!!
                Log.i("TAG", "ChatAdapter getUser: ${user.name}")
                onSuccess(user)
            }

        })

    }


}

