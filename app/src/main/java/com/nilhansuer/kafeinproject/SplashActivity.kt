package com.nilhansuer.kafeinproject

import android.content.Intent
import android.os.Bundle
import android.os.Handler

import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = FirebaseAuth.getInstance()
        postDelayed()
    }

    private fun postDelayed() {
        Handler().postDelayed({
            loginCheck()
        },3000)
    }

    private fun loginCheck() {
        if(auth.currentUser != null){
            val intent = Intent(this, ProfilePageActivity::class.java)
            startActivity(intent)
        } else navigateToLoginActivity()
        finish()
    }


    private fun navigateToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

}
