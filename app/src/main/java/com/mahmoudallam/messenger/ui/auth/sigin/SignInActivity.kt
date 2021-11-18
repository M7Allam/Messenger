package com.mahmoudallam.messenger.ui.auth.sigin

import android.content.Intent
import android.graphics.drawable.DrawableContainer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceIdReceiver
import com.google.firebase.iid.internal.FirebaseInstanceIdInternal
import com.mahmoudallam.messenger.R
import com.mahmoudallam.messenger.ui.auth.signup.SignUpActivity
import com.mahmoudallam.messenger.databinding.ActivitySignInBinding
import com.mahmoudallam.messenger.ui.main.MainActivity
import com.mahmoudallam.messenger.utilities.ProgressDialog

class SignInActivity : AppCompatActivity(), TextWatcher {

    //Binding
    private lateinit var binding: ActivitySignInBinding

    //ViewModel
    private val viewModel: SignInViewModel by lazy {
        ViewModelProvider(this).get(SignInViewModel::class.java)
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
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_in)

        //get defaultInputType for et_password
        defaultInputType = binding.etPassword.inputType

        //Add TextWatcher Listeners
        addTextWatcherListeners();

        //onClick Listeners
        onClickIcEye()
        onClickSignIn()
        onClickCreateNewAccount()


    }

    private fun addTextWatcherListeners() {
        binding.etPhoneOrEmail.addTextChangedListener(this@SignInActivity)
        binding.etPassword.addTextChangedListener(this@SignInActivity)

    }

    private fun getUserToken(userUid: String, token: String){
        FirebaseFirestore.getInstance().collection("Users")
            .document(userUid)
            .update(mapOf("token" to token))
    }

    private fun signInWithEmail(email: String, password: String) {
        //show Loading
        progressDialog.show()
        //SignIn
        viewModel.signInWithEmail(email, password)
            /** onSuccess **/
            .addOnSuccessListener { authResult ->
                authResult.user!!.getIdToken(true).addOnSuccessListener {
                    getUserToken(authResult.user!!.uid, it.token!!)
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
                Toast.makeText(this@SignInActivity, it.message, Toast.LENGTH_LONG).show()
            }
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

    private fun onClickSignIn() {
        binding.btnLogIn.setOnClickListener {
            //GetTexts
            val email = binding.etPhoneOrEmail.text.trim().toString()
            val password = binding.etPassword.text.trim().toString()

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

            //SignIn Auth
            signInWithEmail(email, password)
        }
    }

    private fun onClickCreateNewAccount() {
        binding.btnNewAccount.setOnClickListener {
            val createNewAccountIntent = Intent(this@SignInActivity, SignUpActivity::class.java)
            startActivity(createNewAccountIntent)
        }
    }

    private fun goToMainActivity() {
        val mainActivityIntent = Intent(this@SignInActivity, MainActivity::class.java)
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


        //btn_logIn
        binding.btnLogIn.isEnabled =
            binding.etPhoneOrEmail.text.trim().isNotEmpty() && binding.etPassword.text.trim()
                .isNotEmpty()
    }

    override fun afterTextChanged(s: Editable?) {}

    override fun onStart() {
        super.onStart()

        //Keep User signIn
        if (viewModel.getCurrentUser() != null) {
            goToMainActivity()
        }
    }

}