package com.mahmoudallam.messenger.repo

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mahmoudallam.messenger.pojo.UserModel

//Singleton -> convert class to object
object AuthRepo {

    //Auth instance
    private val mAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    //FireStore instance
    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    //Create User with Email
    fun createUserWithEmail(email: String, password: String) = mAuth.createUserWithEmailAndPassword(email, password)

    //Create User in FireStore
    fun createUserInFireStore(user: UserModel) = firestore.collection("Users").document(user.uid!!).set(user)

    //SignIn with Email
    fun signInWithEmail(email: String, password: String) = mAuth.signInWithEmailAndPassword(email, password)

    fun currentUser() = mAuth.currentUser

    fun signOut() = mAuth.signOut()




}