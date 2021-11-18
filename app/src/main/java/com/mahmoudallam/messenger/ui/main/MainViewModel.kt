package com.mahmoudallam.messenger.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mahmoudallam.messenger.pojo.UserModel
import com.mahmoudallam.messenger.repo.AuthRepo
import com.mahmoudallam.messenger.repo.ProfileRepo

class MainViewModel : ViewModel() {

    private val currentUserInfo = MutableLiveData<UserModel?>()
    private val allUsersList = MutableLiveData<ArrayList<UserModel?>>()
    private val usersChatsFragmentList = MutableLiveData<HashMap<String, UserModel?>>()

    fun getMutableCurrentUserInfo() = currentUserInfo
    fun setMutableCurrentUserInfo(userModel: UserModel){ currentUserInfo.value = userModel }

    fun getAllUsersList() = allUsersList
    fun setAllUsersList(users: ArrayList<UserModel?>){ allUsersList.value = users }

    fun getUsersChatsFragmentList(): MutableLiveData<HashMap<String, UserModel?>> {
        if (usersChatsFragmentList.value == null) {
            usersChatsFragmentList.value = HashMap()
        }
        return usersChatsFragmentList
    }
    fun setUsersChatsFragmentList(user: HashMap<String, UserModel?>){
        usersChatsFragmentList.value = user }

    //Get User Info
    fun getUserInfo(userUid: String) = ProfileRepo.getUserInfo(userUid)
    //GET Current User
    fun currentUser() = AuthRepo.currentUser()

}