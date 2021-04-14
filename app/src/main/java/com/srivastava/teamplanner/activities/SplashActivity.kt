package com.srivastava.teamplanner.activities

import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import com.srivastava.teamplanner.R
import com.srivastava.teamplanner.firebase.FireStoreClass
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : BaseActivity() {
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        val typeFace : Typeface = Typeface.createFromAsset(assets,"Montserrat-Regular.ttf")
        tv_app_name.typeface = typeFace

        Handler().postDelayed({
            val currentUserId = FireStoreClass().getCurrentUserId()

            if(currentUserId.isNotEmpty()){
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                startActivity(Intent(this, IntroActivity::class.java))
                finish()
            }
        },2500)
    }
}