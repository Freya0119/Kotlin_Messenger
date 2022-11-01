package com.example.chatroom

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.chatroom.MainActionActivity.Companion.USER_KEY
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.view.*

class LoginActivity : AppCompatActivity() {
    companion object {
        const val USER_KEY = "USER_KEY"
        val currentUser: User? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        checkLogin()

        login_button.setOnClickListener() {
            //1.Check email and password is not empty
            if (email_login_edit_text.text.isEmpty() || password_login_edit_text.text.isEmpty()) {
                Toast.makeText(this, "Input it.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            //2.Login with Firebase
            FirebaseAuth.getInstance().signInWithEmailAndPassword(
                email_login_edit_text.text.toString(),
                password_login_edit_text.text.toString()
            ).addOnSuccessListener {
                Toast.makeText(this, "Login successful.", Toast.LENGTH_SHORT).show()
                //3.Start MainActionActivity
                val intent = Intent(this, MainActionActivity::class.java)
                startActivity(intent)
            }
        }
        back_register_button.setOnClickListener() {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun checkLogin() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid != null) {
            startActivity(Intent(this, MainActionActivity::class.java))
        } else {
            Toast.makeText(this, "Check login. UID is empty.", Toast.LENGTH_SHORT).show()
        }
    }
}