package com.mahmoudallam.messenger.utilities

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import com.mahmoudallam.messenger.databinding.LayoutDialogProgressBinding

class ProgressDialog(var activity: FragmentActivity, var context: Context){

    fun build(): AlertDialog{
        val builder = AlertDialog.Builder(context)
        val binding = LayoutDialogProgressBinding.inflate(activity.layoutInflater)
        builder.setCancelable(false)
        builder.setView(binding.root)

        return builder.create()
    }

}