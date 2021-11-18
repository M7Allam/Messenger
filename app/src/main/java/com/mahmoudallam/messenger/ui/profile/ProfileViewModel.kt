package com.mahmoudallam.messenger.ui.profile

import androidx.lifecycle.ViewModel
import com.mahmoudallam.messenger.repo.AuthRepo
import com.mahmoudallam.messenger.repo.ProfileRepo

class ProfileViewModel : ViewModel() {

    //Get User Info
    fun getUserInfo(userUid: String) = ProfileRepo.getUserInfo(userUid)

    //Upload Profile Image
    fun uploadProfileImage(imagePath: String, imageBytes: ByteArray?) = ProfileRepo.uploadProfileImage(imagePath, imageBytes)

    //update Profile Image Ling
    fun updateProfileImageLink(userUid: String, imagePath: String) = ProfileRepo.updateProfileImageLink(userUid, imagePath)

    //GET Current User
    fun currentUser() = AuthRepo.currentUser()

    //SignOut
    fun signOut() = AuthRepo.signOut()

}