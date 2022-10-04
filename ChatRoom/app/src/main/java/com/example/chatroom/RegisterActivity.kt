package com.example.chatroom

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        //註冊button
        bt_register.setOnClickListener() {
            performRegister()
        }
        //登陸button
        bt_login.setOnClickListener() {
            //change tag
            Log.d("login", "try to login new activity")

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            //TODO email password
            FirebaseAuth.getInstance().signInWithEmailAndPassword("test@gmail.com", "123123")
        }
        //選擇圖片作為照片button
        bt_selected_photo.setOnClickListener() {
            //change tag
            Log.d("select", "choose one picture")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            //???
            startActivityForResult(intent, 0)
        }
    }

    //註冊
    private fun performRegister() {
        //email password字串
        val strUsername = edit_text_username.text.toString()
        val strPassword = edit_text_password.text.toString()
        //為空
        if ((strUsername.isEmpty()) || (strPassword.isEmpty())) {
            Toast.makeText(this, "enter something", Toast.LENGTH_SHORT).show()
            return
        }
        //change tag
        Log.d("show username", strUsername)
        Log.d("show password", strPassword)
        //發送註冊
        FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(strUsername, strPassword)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener
                Log.d("register", "successful, uid: ${it.result.user?.uid}")

                uploadImageToFirebaseStorage()
            }
            .addOnFailureListener {
                Log.d("fail", "failed message: ${it.message}")
                Toast.makeText(this, "enter something", Toast.LENGTH_SHORT).show()
            }
    }

    //被選擇的圖片?
    var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && data != null && resultCode == Activity.RESULT_OK) {
            //change tag
            Log.d("selected", "photo can be choose")
        }

        selectedPhotoUri = data?.data

        //設置photo和邊框
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

        selected_photo_imageview_register.setImageBitmap(bitmap)
        selected_photo_imageview_register.alpha = 0f
    }

    //upload照片
    private fun uploadImageToFirebaseStorage() {
        if (selectedPhotoUri == null) return

        val fileName = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("image/${fileName}")

        ref.putFile(selectedPhotoUri!!).addOnSuccessListener {
            //change tag
            Log.d("register", "upload photo to: ${it.metadata?.path}")
        }
        //download link
        ref.downloadUrl.addOnSuccessListener {
            //change tag
            Log.d("register", "photo url: ${it.toString()}")

            saveUserToFirebaseDatabase(it.toString())
        }.addOnFailureListener {
            //TODO ?
        }
    }
    //儲存到firebase
    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/user/${uid}")

        val user = User(uid, edit_text_username.text.toString(), profileImageUrl)

        ref.setValue(user).addOnSuccessListener {
            //change tag
            Log.d("upload", "up photo success")

            val intent = Intent(this, LatestMessageActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
}

class User(val uid: String, val username: String, val profileImageUrl: String) {
    constructor() : this("", "", "")
}