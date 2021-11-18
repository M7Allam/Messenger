package com.mahmoudallam.messenger.ui.profile

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.storage.FirebaseStorage
import com.mahmoudallam.messenger.R
import com.mahmoudallam.messenger.databinding.ActivityProfileBinding
import com.mahmoudallam.messenger.pojo.UserModel
import com.mahmoudallam.messenger.ui.auth.sigin.SignInActivity
import com.mahmoudallam.messenger.utilities.GlideApp
import com.mahmoudallam.messenger.utilities.ProgressDialog
import java.io.ByteArrayOutputStream
import java.util.*

class ProfileActivity : AppCompatActivity() {

    //Static Variables
    companion object {
        const val RC_SELECT_IMAGE = 2
    }

    //Binding
    private lateinit var binding: ActivityProfileBinding

    //ViewModel
    private val viewModel: ProfileViewModel by lazy {
        ViewModelProvider(this).get(ProfileViewModel::class.java)
    }

    //ProgressDialog
    private val progressDialog: AlertDialog by lazy {
        ProgressDialog(this, this).build()
    }

    /** onCreate **/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile)

        //Toolbar
        initToolbar()

        //Get User Info
        getUserInfo()

        //onCLickListeners
        onCLickImgProfile()
        onClickSignOut()

    }

    private fun initToolbar() {
        setSupportActionBar(binding.toolbarProfile)
        supportActionBar?.title = getString(R.string.me)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun getUserInfo() {
        val username : String = intent.getStringExtra("username").toString()
        val photoUrl : String = intent.getStringExtra("photoUrl").toString()
        //SET Ui
        setUserInfoToUi(username, photoUrl)
    }

    private fun setUserInfoToUi(username : String, photoUrl : String){
        //Set username
        binding.username = username
        //Set Image profile
        if (photoUrl.isNotEmpty()) {
            GlideApp.with(this@ProfileActivity)
                .load(FirebaseStorage.getInstance().getReference(photoUrl))
                .placeholder(R.drawable.ic_account)
                .into(binding.imgProfile)
        }else{
            binding.imgProfile.setImageResource(R.drawable.ic_account)
        }
    }

    private fun onCLickImgProfile() {
        binding.imgProfile.setOnClickListener {
            //init Intent
            val intentImg = Intent().apply {
                type = "image/*"
                action = Intent.ACTION_GET_CONTENT
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
            }

            //startActivityForResult
            startActivityForResult(Intent.createChooser(intentImg, "Select Image"), RC_SELECT_IMAGE)
        }
    }

    private fun prepareImage(data: Uri?): ByteArray {
        val imageBmp = MediaStore.Images.Media.getBitmap(this.contentResolver, data)
        val outputSystem = ByteArrayOutputStream()
        imageBmp.compress(Bitmap.CompressFormat.JPEG, 20, outputSystem)
        return outputSystem.toByteArray()
    }

    private fun uploadProfileImage(
        data: Uri?,
        imageBytes: ByteArray?,
        onSuccess: (imagePath: String) -> Unit
    ) {
        val imagePath =
            "Profile Pictures/${viewModel.currentUser()?.uid}/${UUID.nameUUIDFromBytes(imageBytes)}"
        viewModel.uploadProfileImage(imagePath, imageBytes)
            .addOnSuccessListener {
                //Save image link to fireStore
                onSuccess(it.storage.path)
            }
            //Failure upload Image
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
            }
    }

    private fun saveImageInFirebase(data: Uri?) {
        //Prepare Image to ByteArray
        val imageBytes = prepareImage(data)
        //Upload Image to FirebaseStorage
        uploadProfileImage(data, imageBytes) { path ->
            //Save image Url to firestore
            viewModel.updateProfileImageLink(viewModel.currentUser()?.uid!!, path)
                .addOnSuccessListener {
                    //SET image to UI
                    binding.imgProfile.setImageURI(data)
                    progressDialog.dismiss()
                }.addOnFailureListener {
                    progressDialog.dismiss()
                    Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun goToSignInActivity() {
        val mainActivityIntent = Intent(this@ProfileActivity, SignInActivity::class.java)
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(mainActivityIntent)
    }

    private fun onClickSignOut() {
        binding.btnSignOut.setOnClickListener {
            viewModel.signOut()
            goToSignInActivity()
        }
    }


    /** onActivityResult **/
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //Select Image
        if (requestCode == RC_SELECT_IMAGE && resultCode == RESULT_OK && data != null && data.data != null) {
            progressDialog.show()
            //Save image in Firebase
            saveImageInFirebase(data.data)
        }
    }

    /** onOptionsItemSelected **/
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            //Toolbar backBtn
            android.R.id.home -> {
                finish()
                return true
            }
        }

        return false
    }
}