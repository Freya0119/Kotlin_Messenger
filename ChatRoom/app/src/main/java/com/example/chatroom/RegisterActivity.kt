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
import com.google.firebase.firestore.auth.User
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
            if ((edit_text_username.text.isEmpty()) || (edit_text_password.text.isEmpty())) {
                Toast.makeText(this, "It is empty. Enter something.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //通過firebase驗證，所以不需要自己驗證email and password
            FirebaseAuth.getInstance().signInWithEmailAndPassword(
                edit_text_username.text.toString(),
                edit_text_password.text.toString()
            ).addOnSuccessListener {
                Log.d("LOGIN", "Login Successful, try to into latest message.")
                Toast.makeText(
                    this,
                    "Login Successful, try to into latest message.",
                    Toast.LENGTH_SHORT
                ).show()
            }.addOnFailureListener {
                Log.d("LOGIN", "Login failed.")
                Toast.makeText(this, "Login failed.", Toast.LENGTH_SHORT).show()
            }

            val intent = Intent(this, LatestMessageActivity::class.java)
            startActivity(intent)
        }
        //選擇圖片作為照片button
        bt_selected_photo.setOnClickListener() {
            Log.d("IMAGE", "Click button for choose a picture.")
            Toast.makeText(this, "Select image cannot be used now.", Toast.LENGTH_SHORT).show()
            //跳到選擇本地image的位置
            val intent = Intent(Intent.ACTION_PICK)
            //選取本地image位置?
            intent.type = "image/*"
            //後面的onActivityResult
            startActivityForResult(intent, 0)
        }
    }

    //註冊
    private fun performRegister() {
        //email password字串
        val strUsername = edit_text_username.text.toString()
        val strPassword = edit_text_password.text.toString()

        //如果其中一個為空
        if ((strUsername.isEmpty()) || (strPassword.isEmpty())) {
            Toast.makeText(this, "It is empty. Enter something.", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("SHOW", "Usr name is ${strUsername}, try to register or login.")
        Log.d("SHOW", "Password is ${strPassword}, try to register or login.")

        //發送註冊
        //這裡已經註冊用戶 firebaseAuth有data 所以後面可以取得uid
        FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(strUsername, strPassword)
            .addOnCompleteListener {
                //return@addOnCompleteListener?
                if (!it.isSuccessful) return@addOnCompleteListener
                Log.d(
                    "REGISTER",
                    "Register successful, uid: ${it.result.user?.uid}. Try to upload image to Firebase."
                )
                //儲存到firebase 暫時禁用
                uploadImageToFirebaseStorage()
            }
            .addOnFailureListener {
                Log.d("REGISTER", "Register failed, message: ${it.message}.")
            }
    }

    //被設置的圖片，因為會被改變所以放在外面?
    var selectedPhotoUri: Uri? = null

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
        //save with uid
        val uid = FirebaseAuth.getInstance().uid
        //這裡是儲存到storage 所以位置和user message不一樣
        val ref = FirebaseStorage.getInstance().getReference("image/${uid}")
        //upload image
        ref.putFile(selectedPhotoUri!!).addOnSuccessListener {
            Log.d("IMAGE", "Upload image to: ${it.metadata?.path}.")
        }
        //???downloadUrl onActivityResult的時候改變了image
        //儲存以後打開上傳到firebase 為了把image path傳給saveUserToFirebaseDatabase 再存入user/
        ref.downloadUrl.addOnSuccessListener {
            Log.d(
                "IMAGE", "Image url: ${it.toString()} successful. Try to save user to Firebase."
            )
            //save user to firebaseDatabase with imageUrl
            //TODO move to bt_login?
            saveUserToFirebaseDatabase(it.toString())
        }.addOnFailureListener {
            Log.d("IMAGE", "Image url: ${it.toString()} failed.")
        }
    }

    //儲存到firebase
    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""

        val ref = FirebaseDatabase.getInstance().getReference("user/${uid}")

        val user = User(uid, edit_text_username.text.toString(), profileImageUrl)

        ref.setValue(user).addOnSuccessListener {
            Log.d("SAVE USER TO FIREBASE", "Save user successful. Try into latest message.")
            //註冊完成並打開latest message
            val intent = Intent(this, LatestMessageActivity::class.java)
            //清除所有TASK並且把latestMessage_activity設為新的TASK?
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
}