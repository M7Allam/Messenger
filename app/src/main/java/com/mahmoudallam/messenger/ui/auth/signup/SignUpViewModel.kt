package com.mahmoudallam.messenger.ui.auth.signup

import androidx.lifecycle.ViewModel
import com.mahmoudallam.messenger.pojo.UserModel
import com.mahmoudallam.messenger.repo.AuthRepo

class SignUpViewModel : ViewModel() {

    //Create User with Email
    fun createUserWithEmail(email: String, password: String) = AuthRepo.createUserWithEmail(email, password)

    //Create User In FireStore
    fun createUserInFireStore(userModel: UserModel) = AuthRepo.createUserInFireStore(userModel)
}