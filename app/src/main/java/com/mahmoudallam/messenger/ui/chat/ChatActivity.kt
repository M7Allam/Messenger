package com.mahmoudallam.messenger.ui.chat

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.mahmoudallam.messenger.R
import com.mahmoudallam.messenger.adapter.ReceiverImageMessageAdapter
import com.mahmoudallam.messenger.adapter.ReceiverTextMessagesAdapter
import com.mahmoudallam.messenger.adapter.SenderImageMessageAdapter
import com.mahmoudallam.messenger.adapter.SenderTextMessagesAdapter
import com.mahmoudallam.messenger.databinding.ActivityChatBinding
import com.mahmoudallam.messenger.pojo.ImageMessage
import com.mahmoudallam.messenger.pojo.TextMessage
import com.mahmoudallam.messenger.utilities.GlideApp
import com.mahmoudallam.messenger.utilities.Message
import com.mahmoudallam.messenger.utilities.MessageType
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.contracts.contract

class ChatActivity : AppCompatActivity(), TextWatcher {

    //Binding
    private lateinit var binding: ActivityChatBinding

    //Adapter
    private val messagesAdapter by lazy {
        GroupAdapter<ViewHolder>()
    }

    //Vars
    private val currentUserUid = FirebaseAuth.getInstance().currentUser!!.uid
    private lateinit var currentUsername: String
    private lateinit var selectedUserUid: String
    private lateinit var selectedUsername: String
    private lateinit var selectedPhotoUrl: String
    private lateinit var channelId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat)

        //GET User info from Intent
        getUserInfo()

        //Toolbar
        initToolbar()

        //TextWatcher
        addTextWatcher()

        //Recycler Messages
        initMessagesRecycler()

        //Chat Channel........
        chatChannel { channelId ->
            getMessages(channelId)
            onCLickSend(channelId)
            onClickIcImgSend()
        }

        //onClickListeners
        onClickBack()
    }

    private fun getUserInfo() {
        selectedUserUid = intent.getStringExtra("selectedUserUid").toString()
        selectedUsername = intent.getStringExtra("selectedUsername").toString()
        selectedPhotoUrl = intent.getStringExtra("selectedUserPhotoUrl").toString()
        currentUsername = intent.getStringExtra("currentUsername").toString()
    }

    private fun initToolbar() {
        setSupportActionBar(binding.toolbarChat)
        binding.tvUsernameToolbarChat.text = selectedUsername
        //Set Image profile Toolbar
        if (selectedPhotoUrl.isNotEmpty()) {
            GlideApp.with(this@ChatActivity)
                .load(FirebaseStorage.getInstance().getReference(selectedPhotoUrl))
                .placeholder(R.drawable.ic_account)
                .into(binding.imgProfileToolbarChat)
        } else {
            binding.imgProfileToolbarChat.setImageResource(R.drawable.ic_account)
        }
    }

    private fun addTextWatcher() = binding.etMessage.addTextChangedListener(this)

    private fun initMessagesRecycler() {
        binding.recyclerMessages.apply {
            adapter = messagesAdapter

        }
    }

    private fun chatChannel(onComplete: (channelId: String) -> Unit) {
        //if Chat Channel already exists
        FirebaseFirestore.getInstance()
            .collection("Users")
            .document(currentUserUid)
            .collection("Chats")
            .document(selectedUserUid)
            .get()
            .addOnSuccessListener {
                //tv Loading
                binding.tvLoading.visibility = View.GONE
                binding.recyclerMessages.visibility = View.VISIBLE
                //Chat Channel already exists
                if (it.exists()) {
                    channelId = it["channelId"] as String
                    onComplete(channelId)
                    return@addOnSuccessListener
                }
                //Chat Channel not exists
                channelId = createChatChannel()
                onComplete(channelId)
            }
    }

    private fun createChatChannel(): String {
        val channelId = FirebaseFirestore.getInstance().collection("Users").document()
        //Add Chat Document to Current User
        addChatDocument(channelId.id, currentUserUid, selectedUserUid)
        //Add Chat Document to Selected User
        addChatDocument(channelId.id, selectedUserUid, currentUserUid)

        return channelId.id

    }

    private fun addChatDocument(channelId: String, firstUserUid: String, secondUserUid: String) {
        FirebaseFirestore.getInstance()
            .collection("Users")
            .document(firstUserUid)
            .collection("Chats")
            .document(secondUserUid)
            .set(mapOf("channelId" to channelId))
    }

    private fun onClickBack() {
        binding.imgBackToolbarChat.setOnClickListener {
            onBackPressed()
        }
    }

    private fun onCLickSend(channelId: String) {
        binding.icSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                val messageSend = TextMessage(
                    text,
                    currentUserUid,
                    selectedUserUid,
                    currentUsername,
                    selectedUsername,
                    Calendar.getInstance().time
                )
                sendMessage(channelId, messageSend, text)
                binding.etMessage.setText("")
            }
        }
    }

    private fun sendMessage(channelId: String, message: Message, text: String) {
        //Send Message
        FirebaseFirestore.getInstance()
            .collection("Chat Channels")
            .document(channelId)
            .collection("Messages")
            .add(message)
        //SET Last Message
        lastMessage(message, text)
    }

    private fun lastMessage(message: Message, text: String) {
        val lastMessage = mutableMapOf<String, Any>()
        lastMessage["text"] = text
        lastMessage["date"] = message.date
        lastMessage["type"] = message.type
        lastMessage["senderId"] = message.senderId
        lastMessage["receiverId"] = message.receiverId
        lastMessage["senderName"] = message.senderName
        lastMessage["receiverName"] = message.receiverName

        FirebaseFirestore.getInstance()
            .collection("Users")
            .document(FirebaseAuth.getInstance().currentUser!!.uid)
            .collection("Chats")
            .document(selectedUserUid)
            .update(lastMessage)

        FirebaseFirestore.getInstance()
            .collection("Users")
            .document(selectedUserUid)
            .collection("Chats")
            .document(FirebaseAuth.getInstance().currentUser!!.uid)
            .update(lastMessage)

    }

    private fun getMessages(channelId: String) {
        Log.d("TAG", "getMessages 1: ${Thread.currentThread().name}")
        val query = FirebaseFirestore.getInstance()
            .collection("Chat Channels").document(channelId).collection("Messages")
        //Snapshot Listener.....
        query.orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { querySnapshot, FirebaseException ->
                Log.d("TAG", "getMessages 2: ${Thread.currentThread().name}")
                //FirebaseException
                if (FirebaseException != null) {
                    return@addSnapshotListener
                }
                //Snapshots....
                messagesAdapter.clear()
                querySnapshot!!.documents.forEach { document ->
                    Log.d("TAG", "getMessages: forEach ")
                    if (document["type"] == MessageType.TEXT) {
                        //Text Message
                        Log.d("TAG", "getMessages: MessageType.TEXT ")
                        bindTextMessage(document)
                    } else if (document["type"] == MessageType.IMAGE) {
                        //Image Message
                        Log.d("TAG", "getMessages: MessageType.IMAGE ")
                        bindImageMessage(document)
                    }
                }
            }
    }

    private fun bindTextMessage(document: DocumentSnapshot) {
        val textMessage = document.toObject(TextMessage::class.java)!!
        if (textMessage.senderId == currentUserUid) {
            messagesAdapter.add(
                SenderTextMessagesAdapter(
                    textMessage,
                    document.id,
                    this@ChatActivity
                )
            )
        } else {
            messagesAdapter.add(
                ReceiverTextMessagesAdapter(
                    textMessage,
                    document.id,
                    this@ChatActivity
                )
            )
        }
    }

    private fun bindImageMessage(document: DocumentSnapshot) {
        val imageMessage = document.toObject(ImageMessage::class.java)!!
        if (imageMessage.senderId == currentUserUid) {
            messagesAdapter.add(
                SenderImageMessageAdapter(
                    imageMessage,
                    document.id,
                    this@ChatActivity
                )
            )
        } else {
            messagesAdapter.add(
                ReceiverImageMessageAdapter(
                    imageMessage,
                    document.id,
                    this@ChatActivity
                )
            )
        }
    }

    private fun onClickIcImgSend() {
        binding.icImageSend.setOnClickListener {
            //init Intent
            val intentImg = Intent().apply {
                type = "image/*"
                action = Intent.ACTION_GET_CONTENT
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
            }

            //startActivityForResult
            startActivityForResult(
                Intent.createChooser(intentImg, "Select Image"),
                5
            )
        }
    }


    /** onActivityResult **/
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //Select Image
        if (requestCode == 5 && resultCode == RESULT_OK && data != null && data.data != null) {
            //Upload Image
            uploadImage(prepareImage(data.data)) { imgPath ->
                //Save image in Firebase
                //Image Message Model
                val imageMessage = ImageMessage(
                    imgPath,
                    currentUserUid,
                    selectedUserUid,
                    currentUsername,
                    selectedUsername,
                    Calendar.getInstance().time
                )
                sendMessage(channelId, imageMessage, getString(R.string.sent_photo_message))


            }
        }
    }

    private fun prepareImage(data: Uri?): ByteArray {
        val imageBmp = MediaStore.Images.Media.getBitmap(this.contentResolver, data)
        val outputSystem = ByteArrayOutputStream()
        imageBmp.compress(Bitmap.CompressFormat.JPEG, 30, outputSystem)
        return outputSystem.toByteArray()
    }

    private fun uploadImage(imgBytes: ByteArray, onSuccess: (imgPath: String) -> Unit) {
        val imgRef = FirebaseStorage.getInstance().reference.child(
            "Chats/${channelId}" +
                    "/Images/${FirebaseAuth.getInstance().currentUser!!.uid}/${
                        UUID.nameUUIDFromBytes(
                            imgBytes
                        )
                    }"
        )
        //Upload
        imgRef.putBytes(imgBytes)
            .addOnSuccessListener {
                onSuccess(imgRef.path)
            }.addOnFailureListener {
                Toast.makeText(
                    this@ChatActivity,
                    "Error: something wrong when upload the image", Toast.LENGTH_SHORT
                ).show()
            }
    }


    /** Text Watcher **/
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (s.toString().trim().isNotEmpty()) {
            binding.icSend.background =
                AppCompatResources.getDrawable(this, R.drawable.shape_btn_send_enabled)
        } else {
            binding.icSend.background =
                AppCompatResources.getDrawable(this, R.drawable.shape_btn_send_not_enabled)
        }
    }
    override fun afterTextChanged(s: Editable?) {}
    /**************************************************/
}