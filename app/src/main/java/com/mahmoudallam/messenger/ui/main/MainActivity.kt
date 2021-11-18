package com.mahmoudallam.messenger.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.storage.FirebaseStorage
import com.mahmoudallam.messenger.R
import com.mahmoudallam.messenger.databinding.ActivityMainBinding
import com.mahmoudallam.messenger.pojo.UserModel
import com.mahmoudallam.messenger.ui.main.fragments.ChatFragment
import com.mahmoudallam.messenger.ui.main.fragments.PeopleFragment
import com.mahmoudallam.messenger.ui.profile.ProfileActivity
import com.mahmoudallam.messenger.utilities.GlideApp
import com.mahmoudallam.messenger.utilities.ProgressDialog

class MainActivity : AppCompatActivity() {

    //Binding
    private lateinit var binding: ActivityMainBinding

    //ViewModel
    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }

    //ProgressDialog
    private val progressDialog: AlertDialog by lazy {
        ProgressDialog(this, this).build()
    }

    //Model
    private lateinit var userModel: UserModel


    /** onCreate **/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        //Toolbar
        initToolbar()



        //BottomNav
        initBottomNav()

        //Get User Info
        getUserInfo()

        //getAllUsers for People Fragment
        getAllUsers()

        //onCLickListeners
        onCLickImgProfile()

    }

    private fun initToolbar() {
        setSupportActionBar(binding.toolbarMain)
        supportActionBar?.title = ""
    }

    private fun getUserInfo() {
        progressDialog.show()
        viewModel.getUserInfo(viewModel.currentUser()?.uid!!)
            .addOnSuccessListener {
                userModel = it?.toObject(UserModel::class.java)!!
                viewModel.setMutableCurrentUserInfo(userModel)
                //Set Image profile
                if (userModel.photoUrl!!.isNotEmpty()) {
                    GlideApp.with(this@MainActivity)
                        .load(FirebaseStorage.getInstance().getReference(userModel.photoUrl!!))
                        .placeholder(R.drawable.ic_account)
                        .into(binding.imgProfileMain)
                } else {
                    binding.imgProfileMain.setImageResource(R.drawable.ic_account)
                }
                progressDialog.dismiss()
            }
            .addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                progressDialog.dismiss()
            }
    }

    private fun onCLickImgProfile() {
        binding.imgProfileMain.setOnClickListener {
            val intent = Intent(this@MainActivity, ProfileActivity::class.java)
            intent.putExtra("username", userModel.name)
            intent.putExtra("photoUrl", userModel.photoUrl)
            startActivity(intent)
        }
    }

    private fun initBottomNav() {
        val navController = findNavController(R.id.nav_controller)
        binding.bottomNav.setupWithNavController(navController)
        //Toolbar title
        val chatString = getString(R.string.chat)
        val peopleString = getString(R.string.people)
        //Fragment destination changed
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            when(destination.id){
                R.id.chatFragment -> binding.setToolbarTitle(chatString)
                R.id.peopleFragment -> binding.setToolbarTitle(peopleString)
            }
        }
    }

    private fun getAllUsers(){
        FirebaseFirestore.getInstance().collection("Users").get()
            .addOnSuccessListener { documents ->
                val users : ArrayList<UserModel?> = ArrayList()
                documents.forEach { document ->
                    val userModel = document.toObject(UserModel::class.java)
                    userModel.uid = document.id
                    users.add(userModel)
                }
                viewModel.setAllUsersList(users)
            }
    }

}