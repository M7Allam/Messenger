package com.mahmoudallam.messenger.ui.auth.sigin

import androidx.lifecycle.ViewModel
import com.mahmoudallam.messenger.repo.AuthRepo

class SignInViewModel : ViewModel() {

    //SignIn with Email
    fun signInWithEmail(email: String, password: String) = AuthRepo.signInWithEmail(email, password)

    fun getCurrentUser() = AuthRepo.currentUser()
}