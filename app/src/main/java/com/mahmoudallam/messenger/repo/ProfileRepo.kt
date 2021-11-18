package com.mahmoudallam.messenger.repo

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

object ProfileRepo {

    private val storage : FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    private val firestore : FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    fun getUserInfo(userUid: String) = firestore.collection("Users").document(userUid).get()

    fun uploadProfileImage(imagePath: String, imageBytes: ByteArray?) = storage.reference.child(imagePath).putBytes(imageBytes!!)

    fun updateProfileImageLink(userUid: String, imagePath: String) = firestore.collection("Users")
        .document(userUid).update("photoUrl", imagePath)
}