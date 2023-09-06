package com.nilhansuer.kafeinproject

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val signupButton = findViewById<Button>(R.id.signupButton)

        signupButton.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
            finish()
        }

        auth = FirebaseAuth.getInstance()
        val loginButton = findViewById<Button>(R.id.registerButton)

        val emailText = findViewById<EditText>(R.id.emailText)
        val passwordText = findViewById<EditText>(R.id.passwordText)

        loginButton.setOnClickListener {
            val email = emailText.text.toString()
            val password = passwordText.text.toString()

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val intent = Intent(this, ProfilePageActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        val builder = AlertDialog.Builder(this)
                        val customView = LayoutInflater.from(this).inflate(R.layout.custom_layout_dialog, null)
                        builder.setView(customView)

                        val dialog = builder.create()
                        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        val buttonOK = customView.findViewById<Button>(R.id.buttonOK)

                        buttonOK.setOnClickListener {
                            dialog.dismiss()
                        }
                        dialog.show()
                    }
                }
        }
    }
}