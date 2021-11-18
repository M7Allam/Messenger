package com.mahmoudallam.messenger.ui.auth.signup

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.internal.FirebaseInstanceIdInternal
import com.mahmoudallam.messenger.R
import com.mahmoudallam.messenger.databinding.ActivitySignUpBinding
import com.mahmoudallam.messenger.pojo.UserModel
import com.mahmoudallam.messenger.ui.main.MainActivity
import com.mahmoudallam.messenger.ui.auth.sigin.SignInActivity
import com.mahmoudallam.messenger.utilities.ProgressDialog

class SignUpActivity : AppCompatActivity(), TextWatcher {

    //Binding
    private lateinit var binding: ActivitySignUpBinding

    //ViewModel
    private val viewModel: SignUpViewModel by lazy {
        ViewModelProvider(this@SignUpActivity).get(SignUpViewModel::class.java)
    }

    //ProgressDialog
    private val progressDialog: AlertDialog by lazy {
        ProgressDialog(this, this).build()
    }

    //Variables
    private var defaultInputType: Int? = null
    private var icEyeType: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)

        //get defaultInputType for et_password
        defaultInputType = binding.etPassword.inputType

        //add TextWatcher Listeners
        addTextChangedListeners()

        //onClickListeners
        onClickIcEye()
        onClickSignUp()
        onCLickLogIn()

    }

    private fun addTextChangedListeners() {
        binding.etUsername.addTextChangedListener(this@SignUpActivity)
        binding.etPhoneOrEmail.addTextChangedListener(this@SignUpActivity)
        binding.etPassword.addTextChangedListener(this@SignUpActivity)
    }

    private fun onClickIcEye() {
        binding.icEye.setOnClickListener {
            if (icEyeType == 0) {
                //Enabled false
            } else {
                //Enabled true
                if (binding.etPassword.inputType != defaultInputType) {
                    binding.etPassword.inputType = defaultInputType!!
                } else binding.etPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            }
        }
    }

    private fun getUserToken(userUid: String, token: String){
        FirebaseFirestore.getInstance().collection("Users")
            .document(userUid)
            .update(mapOf("token" to token))
    }

    private fun createUser(username: String, email: String, password: String) {
        //show Loading
        progressDialog.show()
        //Create user
        viewModel.createUserWithEmail(email, password)
            /** onSuccess **/
            .addOnSuccessListener { authResult ->
                //Create user in FireStore
                val userModel = UserModel(
                    authResult.user?.uid,
                    username,
                    email,
                    authResult.user?.phoneNumber,
                    authResult.user?.photoUrl.toString(),
                    ""
                )
                if(userModel.phone == null){
                    userModel.phone = ""
                }
                if(userModel.photoUrl == null){
                    userModel.photoUrl = ""
                }
                //Create user in fireStore
                viewModel.createUserInFireStore(userModel).addOnSuccessListener {
                    authResult.user!!.getIdToken(true).addOnSuccessListener { tokenResult ->
                        getUserToken(authResult.user!!.uid, tokenResult.token!!)
                    }
                }
                //dismiss Loading
                progressDialog.dismiss()

                //Goto MainActivity
                goToMainActivity()
            }
            /** onFailure **/
            .addOnFailureListener {
                //dismiss Loading
                progressDialog.dismiss()
                Toast.makeText(this@SignUpActivity, it.message, Toast.LENGTH_LONG).show()
            }
    }

    private fun onClickSignUp() {
        binding.btnSignUp.setOnClickListener {

            //GetTexts
            val username = binding.etUsername.text.trim().toString()
            val email = binding.etPhoneOrEmail.text.trim().toString()
            val password = binding.etPassword.text.trim().toString()

            //validationOfTexts
            if (username.isEmpty()) {
                binding.etUsername.error = "Username required"
                binding.etUsername.requestFocus()
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                binding.etPhoneOrEmail.error = "Email required"
                binding.etPhoneOrEmail.requestFocus()
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.etPhoneOrEmail.error = "Invalid Email"
                binding.etPhoneOrEmail.requestFocus()
                return@setOnClickListener
            }
            if (password.length < 6) {
                binding.etPassword.error = "Password at least 6 characters"
                binding.etPassword.requestFocus()
                return@setOnClickListener
            }


            //Firebase Auth
            createUser(username, email, password)

        }
    }

    private fun onCLickLogIn() {
        binding.btnLogIn.setOnClickListener {
            val signInIntent = Intent(this@SignUpActivity, SignInActivity::class.java)
            startActivity(signInIntent)
        }
    }

    private fun goToMainActivity(){
        val mainActivityIntent = Intent(this@SignUpActivity, MainActivity::class.java)
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(mainActivityIntent)
    }

    /** Text Watcher implement methods **/
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        //ic_eye
        if (binding.etPassword.text.trim().isNotEmpty()) {
            //Enabled true
            icEyeType = 1
            binding.icEye.setImageResource(R.drawable.ic_eye_blue)
        } else {
            //Enabled false
            icEyeType = 0
            binding.icEye.setImageResource(R.drawable.ic_eye_grey)
            binding.etPassword.inputType = defaultInputType!!
        }


        //
        binding.btnSignUp.isEnabled =
            binding.etUsername.text.trim().isNotEmpty() && binding.etPhoneOrEmail.text.trim()
                .isNotEmpty() &&
                    binding.etPassword.text.trim().isNotEmpty()

    }

    override fun afterTextChanged(s: Editable?) {}
    /**************************************/

}
