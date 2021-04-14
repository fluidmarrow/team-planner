package com.srivastava.teamplanner.activities

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.srivastava.teamplanner.R
import com.srivastava.teamplanner.firebase.FireStoreClass
import com.srivastava.teamplanner.models.User
import com.srivastava.teamplanner.utils.Constants
import com.srivastava.teamplanner.utils.Constants.getFileExtension
import com.srivastava.teamplanner.utils.Constants.showImageChooser
import kotlinx.android.synthetic.main.activity_my_profile.*
import java.io.IOException


class MyProfileActivity : BaseActivity() {

    private var mSelectedImageURI: Uri? = null
    private var mProfileImageURL: String? = null
    private lateinit var mUserDetails: User

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        setUpActionBar()

        FireStoreClass().loadUserData(this)

        iv_profile_user_image.setOnClickListener{
            if(
                ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED){
                showImageChooser(this)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE)
            }
            btn_update.setOnClickListener{
                if(mSelectedImageURI != null){
                    uploadImageOfUser()
                } else {
                    showProgressDialog(resources.getString(R.string.please_wait))
                    updateUserProfileData()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == Constants.READ_STORAGE_PERMISSION_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                showImageChooser(this)
            } else {
                Toast.makeText(this,"Permission Storage Denied",Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK &&
            requestCode == Constants.PICK_IMAGE_REQUEST_CODE &&
            data != null){
            mSelectedImageURI = data.data

            try{
                Glide
                    .with(this)
                    .load(mSelectedImageURI)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(iv_profile_user_image)
            } catch (e : IOException){
                e.printStackTrace()
            }
        }
    }

    private fun setUpActionBar() {
        setSupportActionBar(toolbar_my_profile_activity)
        val actionBar = supportActionBar
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = "My Profile"
        }
        toolbar_my_profile_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    fun setUserDataInUI(user : User){
        mUserDetails  = user
        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(iv_profile_user_image)

        et_name.setText(user.name)
        et_email.setText((user.email))
        if(user.mobile != 0L){
            et_mobile.setText(user.mobile.toString())
        }
    }

    private fun uploadImageOfUser(){
        showProgressDialog(resources.getString(R.string.please_wait))

        if(mSelectedImageURI != null){
            val sRef : StorageReference = FirebaseStorage.getInstance().reference.child(
                "USER_IMAGE"
                        +System.currentTimeMillis()
                        +"."
                        +getFileExtension(this,mSelectedImageURI))
            sRef.putFile(mSelectedImageURI!!).addOnSuccessListener {
                taskSnapshot -> Log.i("Firebase Image URL",taskSnapshot.metadata!!.reference!!.downloadUrl.toString())
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri -> Log.i("Downloadable Image URL",uri.toString())
                    mProfileImageURL = uri.toString()
                    hideProgressDialog()

                    updateUserProfileData()

                }
            }.addOnFailureListener{
                exception -> Toast.makeText(this@MyProfileActivity,exception.message,Toast.LENGTH_SHORT).show()
                hideProgressDialog()
            }
        }
    }

    fun profileUpdateSuccess(){
        hideProgressDialog()
        onBackPressed()
    }

    private fun updateUserProfileData(){
        val userHashMap = HashMap<String, Any>()
        if(mProfileImageURL!!.isNotEmpty() && mProfileImageURL != mUserDetails.image){
            userHashMap[Constants.IMAGE] = mProfileImageURL!!
        }
        if(et_name.text.toString() != mUserDetails.name){
            userHashMap[Constants.NAME] = et_name.text.toString()
        }
        if(et_mobile.text.toString() != mUserDetails.mobile.toString()){
            userHashMap[Constants.MOBILE] = et_mobile.text.toString()
        }
            FireStoreClass().updateUserProfileData(this,userHashMap)
    }



}