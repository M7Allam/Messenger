package com.mahmoudallam.messenger.pojo

data class UserModel(
    var uid: String?,
    var name: String?,
    var email: String?,
    var phone: String?,
    var photoUrl: String?,
    var token: String
){
    constructor(): this("", "", "", "", "", "")
}
