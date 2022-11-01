package com.example.chatroom

import android.accessibilityservice.AccessibilityService
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.view.ViewCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        //註冊button
        register_button.setOnClickListener() {
            performRegister()
        }
        //選擇圖片作為照片button
        select_image_button.setOnClickListener() {
            Log.d("IMAGE", "Click button for choose a picture.")
            //跳到選擇本地image的位置
            val intent = Intent(Intent.ACTION_PICK)
            //選取本地image/位置?
            intent.type = "image/*"
            //後面的onActivityResult
            startActivityForResult(intent, 0)
        }
        //登陸button
        turn_login_button.setOnClickListener() {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    //註冊
    private fun performRegister() {
        val strUsername = username_edit_text.text.toString()
        val strEmail = email_edit_text.text.toString()
        val strPassword = password_edit_text.text.toString()

        if ((strUsername.isEmpty()) || (strEmail.isEmpty()) || (strPassword.isEmpty())) {
            Toast.makeText(this, "It is empty. Enter something.", Toast.LENGTH_SHORT).show()
            return
        }

        //發送註冊，這裡已經註冊用戶 firebaseAuth有data 所以後面可以取得uid
        FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(strEmail, strPassword)
            .addOnCompleteListener {
                //如果不成功?
                if (!it.isSuccessful) return@addOnCompleteListener
//                //test
//                Toast.makeText(this, FirebaseAuth.getInstance().uid, Toast.LENGTH_SHORT).show()

                //儲存到firebase
                uploadImageToFirebaseStorage()
            }
    }

    //被設置的圖片，因為會被改變所以放在外面?
    private var selectedPhotoUri: Uri? = null

    //從選擇照片返回register介面?onActivityResult有獲取返回值的功能
    //和bt_select_image的startActivityForResult回應
    //requestCode-> 功能Int
    //resultCode-> RESULT-OK
    //data-> 傳回來的data，例如:getExtra(USERNAME_KEY, username),getExtra(PASSWORD_KEY,password)，須為intent
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //選擇成功
        if (requestCode == 0 && data != null && resultCode == Activity.RESULT_OK) {
            Log.d("IMAGE", "Select photo successful.")

            selectedPhotoUri = data.data
            //設置photo和邊框
            select_image_button.alpha = 0f
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
            select_image_imageView.setImageBitmap(bitmap)
        }
    }

    //upload照片
    private fun uploadImageToFirebaseStorage() {
        if (selectedPhotoUri == null) {
            Toast.makeText(this, "Image is empty", Toast.LENGTH_SHORT).show()
            return
        }

        val uid = FirebaseAuth.getInstance().uid
        //這裡是儲存到storage 所以位置和user message不一樣
        val ref = FirebaseStorage.getInstance().getReference("image/$uid")

//        if (ref != null) {
//            Toast.makeText(this, ref.path, Toast.LENGTH_SHORT).show()
//            return
//        }

        //upload image and create user to FirebaseDatabase
        ref.putFile(selectedPhotoUri!!).addOnSuccessListener {
            Toast.makeText(this, "Upload image to: ${it.metadata?.path}.", Toast.LENGTH_SHORT)
                .show()
            //downloadUrl需要配合putFile 作用是下載url???
            //儲存以後打開上傳到firebase 為了把image path傳給saveUserToFirebaseDatabase 再存入user/
            ref.downloadUrl.addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Image url: ${it.toString()} successful. Try to save user to FirebaseDatabase.",
                    Toast.LENGTH_LONG
                ).show()
                //save user to firebaseDatabase with imageUrl
                saveUserToFirebaseDatabase(it.toString())
            }
        }.addOnFailureListener() {
            Toast.makeText(this, "Upload failed.", Toast.LENGTH_SHORT).show()
        }
    }

    //儲存到firebase
    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance()
            .getReference("user/${uid}")

        val user = User(uid, username_edit_text.text.toString(), profileImageUrl)

        ref.setValue(user).addOnSuccessListener {
            Toast.makeText(this, "Save into FirebaseDatabase successful.", Toast.LENGTH_LONG).show()
            //註冊完成並打開latest message
            val intent = Intent(this, NewMessageActivity::class.java)
            //清除所有TASK並且把latestMessage_activity設為新的TASK?
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
}