package com.srivastava.teamplanner.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.srivastava.teamplanner.R
import com.srivastava.teamplanner.models.User
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.activity_sign_in.et_email
import kotlinx.android.synthetic.main.activity_sign_in.et_password
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignInActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth
// ...
// Initialize Firebase Auth


    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        setUpActionBar()
        auth = Firebase.auth
        btn_sign_in.setOnClickListener{
            val email: String = et_email.text.toString().trim{it<=' '}
            val password: String = et_password.text.toString()
            if (validateForm(email, password)){
                signInWithEmailAndPassword(email,password)
            } else {
                Toast.makeText(
                        this,
                        "Signing In Failed",
                        Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /*public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if(currentUser != null){
            reload()
        }
    }*/

    private fun setUpActionBar() {
        setSupportActionBar(toolbar_sign_in_activity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }
        toolbar_sign_in_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun signInWithEmailAndPassword(email: String, password: String){
        showProgressDialog(resources.getString(R.string.please_wait))
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            hideProgressDialog()
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                    Log.i("signInWithEmail", "success")
                Toast.makeText(baseContext, "Authentication success.",
                        Toast.LENGTH_SHORT).show()
                val user = auth.currentUser
                startActivity(Intent(this,MainActivity::class.java))
                finish()
            //updateUI(user)
            } else {
                // If sign in fails, display a message to the user.
                    Log.i("signInWithEmail", "failure", task.exception)
                Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
            //updateUI(null)
            // ...
            }
        // ...
        }
    }

    /*private fun reload() {
        auth.currentUser!!.reload().addOnCompleteListener { task ->

            if (task.isSuccessful) {
                //updateUI(auth.currentUser)
                Toast.makeText(this@SignInActivity,
                        "Reload successful!",
                        Toast.LENGTH_SHORT).show()
            } else {
                Log.e("TAG", "reload", task.exception)
                Toast.makeText(this@SignInActivity,
                        "Failed to reload user.",
                        Toast.LENGTH_SHORT).show()
            }
        }
    }*/

    private fun validateForm(email: String, password: String): Boolean{
        return when{

            TextUtils.isEmpty(email)-> {
                showErrorSnackBar("Please Enter email")
                false
            } TextUtils.isEmpty(password)-> {
                showErrorSnackBar("Please Enter password")
                false
            } else -> true
        }
    }

    fun logInSuccess(loggedInUser: User?) {
        hideProgressDialog()
        startActivity(Intent(this,MainActivity::class.java))
        finish()
    }

}