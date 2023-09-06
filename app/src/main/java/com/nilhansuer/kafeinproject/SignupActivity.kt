package com.nilhansuer.kafeinproject

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.res.AssetManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


class SignupActivity : AppCompatActivity(){

    private lateinit var firebaseAuth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val countrySpinner: Spinner = findViewById(R.id.countrySpinner)
        val countryList: ArrayList<String> = ArrayList()

        try {
            val assetManager: AssetManager = assets
            val inputStream = assetManager.open("country.txt")
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))

            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                countryList.add(line!!)
            }

            bufferedReader.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val adapter: ArrayAdapter<String> =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, countryList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        countrySpinner.adapter = adapter

        val defaultItemPosition = adapter.getPosition("Turkey")
        countrySpinner.setSelection(defaultItemPosition)

        firebaseAuth = FirebaseAuth.getInstance()

        val registerButton = findViewById<Button>(R.id.registerButton)
        val editName = findViewById<EditText>(R.id.editName)
        val editEmail = findViewById<EditText>(R.id.editEmail)
        val editPassword = findViewById<EditText>(R.id.editPassword)
        val editPasswordConfir = findViewById<EditText>(R.id.editPasswordConfir)
        val editSurname = findViewById<EditText>(R.id.editSurname)


        val passwordWarning = findViewById<TextView>(R.id.passwordWarning)
        val passwordConfirWarning = findViewById<TextView>(R.id.passwordConfirWarning)

        editPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val password = s.toString()
                if (password.length < 6) {
                    passwordWarning.visibility = View.VISIBLE
                } else {
                    passwordWarning.visibility = View.INVISIBLE
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        editPasswordConfir.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(editPassword.text.toString() == editPasswordConfir.text.toString()){
                    passwordConfirWarning.visibility = View.VISIBLE
                }
                else{
                    passwordConfirWarning.visibility = View.INVISIBLE
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        registerButton.setOnClickListener {
            val name = editName.text.toString()
            val surname = editSurname.text.toString()
            val email = editEmail.text.toString()
            val password = editPassword.text.toString()
            val country = countrySpinner.selectedItem.toString()


            if (email.isNotEmpty() && password.isNotEmpty()) {
                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) {task ->
                    if (task.isSuccessful) {
                        val userMap = hashMapOf(
                            "name" to name,
                            "email" to email,
                            "surname" to surname,
                            "country" to country
                        )
                        val userId = FirebaseAuth.getInstance().currentUser!!.uid
                        db.collection("users").document(userId).set(userMap)
                            .addOnSuccessListener {
                                Log.d(TAG, "createUserWithEmail:success")
                                val intent = Intent(this, LoginActivity::class.java)
                                startActivity(intent)
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Error adding user", e)
                            }
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure")
                    }
                }
            }
            else {
                Log.w(TAG, "Empty fields are not allowed")
            }
        }

        val exitButton: ImageView = findViewById(R.id.buttonExit)
        exitButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

}
