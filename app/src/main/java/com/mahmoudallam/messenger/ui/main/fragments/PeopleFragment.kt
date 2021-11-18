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
import com.google.firebase.firestore.FirebaseFirestore
import com.mahmoudallam.messenger.R
import com.mahmoudallam.messenger.adapter.ChatAdapter
import com.mahmoudallam.messenger.adapter.PeopleAdapter
import com.mahmoudallam.messenger.databinding.FragmentPeopleBinding
import com.mahmoudallam.messenger.databinding.ItemChatBinding
import com.mahmoudallam.messenger.databinding.ItemPeopleBinding
import com.mahmoudallam.messenger.pojo.TextMessage
import com.mahmoudallam.messenger.pojo.UserModel
import com.mahmoudallam.messenger.ui.chat.ChatActivity
import com.mahmoudallam.messenger.ui.main.MainViewModel
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.Section
import com.xwray.groupie.databinding.BindableItem
import com.xwray.groupie.kotlinandroidextensions.ViewHolder

class PeopleFragment : Fragment() {

    //Binding
    private val binding: FragmentPeopleBinding by lazy {
        FragmentPeopleBinding.inflate(LayoutInflater.from(requireActivity()))
    }

    //ViewModel
    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
    }

    //Vars
    private lateinit var peopleSection: Section
    private lateinit var currentUser: UserModel

    /** onCreateView **/
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        //Main Activity Toolbar
        //toolbarTitle()

        //Get Current user
        getCurrentUserInfo()

        //All Users.....
        getAllUsers(::initRecycler)


        // Inflate the layout for this fragment
        return binding.root
    }

    private fun toolbarTitle() {
        val toolbarTitle = activity?.findViewById(R.id.toolbar_title) as TextView
        toolbarTitle.text = getString(R.string.people)
    }

    private fun getCurrentUserInfo(){
        viewModel.getMutableCurrentUserInfo().observe(requireActivity(), {
            currentUser = it!!
            Log.i("TAG", "getCurrentUserInfo: currentUser Observer ${currentUser.name}")
        })
    }

    private fun initRecycler(items: List<BindableItem<ItemPeopleBinding>>){
        //Adapter
        val myAdapter = GroupAdapter<ViewHolder>().apply {
            peopleSection = Section(items)
            add(peopleSection)
            setOnItemClickListener(onItemClick)
        }
        //Recycler
        binding.recyclerPeople.apply {
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = myAdapter
        }
    }

    //onItemClick Recycler
    private val onItemClick = OnItemClickListener{ item, view ->
        if(item is PeopleAdapter){
            val intent = Intent(requireContext(), ChatActivity::class.java)
            intent.putExtra("selectedUserUid", item.user.uid)
            intent.putExtra("selectedUsername", item.user.name)
            intent.putExtra("selectedUserPhotoUrl", item.user.photoUrl)
            intent.putExtra("currentUsername", currentUser.name)
            requireActivity().startActivity(intent)
        }
    }

    private fun getAllUsers(onListen: (List<BindableItem<ItemPeopleBinding>>) -> Unit){
        viewModel.getAllUsersList().observe(requireActivity(),  { usersList ->

            //Fetch Data
            val items = mutableListOf<BindableItem<ItemPeopleBinding>>()
            usersList.forEach {
                items.add(PeopleAdapter(it?.uid!!, it, requireActivity()))
            }

            onListen(items)
        })
    }


}