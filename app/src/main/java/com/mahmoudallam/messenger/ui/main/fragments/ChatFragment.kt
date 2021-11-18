package com.mahmoudallam.messenger.ui.main.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.mahmoudallam.messenger.R
import com.mahmoudallam.messenger.adapter.ChatAdapter
import com.mahmoudallam.messenger.databinding.FragmentChatBinding
import com.mahmoudallam.messenger.databinding.ItemChatBinding
import com.mahmoudallam.messenger.pojo.TextMessage
import com.mahmoudallam.messenger.pojo.UserModel
import com.mahmoudallam.messenger.ui.chat.ChatActivity
import com.mahmoudallam.messenger.ui.main.MainViewModel
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.Section
import com.xwray.groupie.databinding.BindableItem
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder

class ChatFragment : Fragment() {

    //Binding
    private val binding: FragmentChatBinding by lazy {
        FragmentChatBinding.inflate(LayoutInflater.from(requireActivity()))
    }

    //ViewModel
    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
    }

    //Vars
    private lateinit var chatSection: Section
    private lateinit var currentUser: UserModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        //Toolbar
        //toolbarTitle()


        //GET Current User
        getCurrentUserInfo()

        //Chats Listener.....
        addChatListener(::initRecycler)

        // Inflate the layout for this fragment
        //inflater.inflate(R.layout.fragment_chat, container, false)
        return binding.root
    }

    private fun toolbarTitle() {
        val toolbarTitle = activity?.findViewById(R.id.toolbar_title) as TextView
        toolbarTitle.text = getString(R.string.chat)
    }

    private fun getCurrentUserInfo(){
        viewModel.getMutableCurrentUserInfo().observe(requireActivity(), Observer {
            currentUser = it!!
            Log.i("TAG", "getCurrentUserInfo: currentUser Observer ${currentUser.name}")
        })
    }

    private fun initRecycler(items: List<BindableItem<ItemChatBinding>>){

        //Adapter
        val myAdapter = GroupAdapter<ViewHolder>().apply {
            chatSection = Section(items)
            add(chatSection)
            setOnItemClickListener(onItemClick)
        }

        //Recycler
        binding.recyclerChat.apply {
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = myAdapter
        }
    }

    //onItemClick Recycler
    private val onItemClick = OnItemClickListener{ item, view ->
        if(item is ChatAdapter){
            val intent = Intent(activity, ChatActivity::class.java)
            intent.putExtra("selectedUserUid", item.user.uid)
            intent.putExtra("selectedUsername", item.user.name)
            intent.putExtra("selectedUserPhotoUrl", item.user.photoUrl)
            intent.putExtra("currentUsername", currentUser.name)
            requireActivity().startActivity(intent)
        }
    }

    private fun addChatListener(onListen: (List<BindableItem<ItemChatBinding>>) -> Unit): ListenerRegistration {
        return FirebaseFirestore.getInstance().collection("Users")
            .document(FirebaseAuth.getInstance().currentUser!!.uid)
            .collection("Chats")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { querySnapshot, firebaseFireStoreException ->
                //Exception
                if (firebaseFireStoreException != null) {
                    return@addSnapshotListener
                }

                //Fetch Data
                val items = mutableListOf<BindableItem<ItemChatBinding>>()
                querySnapshot!!.documents.forEach {
                    if(it.exists()){
                        items.add(ChatAdapter(
                            it.id,
                            UserModel(),
                            it.toObject(TextMessage::class.java)!!,
                            requireContext(),
                            requireActivity(),
                            viewModel))
                        Log.i("TAG", "addChatListener: user " + it.get("name"))
                    }

                }

                //onListen Adapter
                onListen(items)
            }
    }




}