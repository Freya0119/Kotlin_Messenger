package com.example.chatroom

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*

class ChatLogActivity : AppCompatActivity() {
    companion object {
        val TAG = "Chat Log"
    }

    //adapter
    val adapter = GroupAdapter<GroupieViewHolder>()
    var toUser: UserA? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        toUser = intent.getParcelableExtra<UserA>(NewMessageActivity.USER_KEY)

        supportActionBar?.title = toUser?.username

        recycle_chat_log.adapter = adapter

        send_button_chat_log.setOnClickListener() {
            Log.d(TAG, "this is send button")
        }
        listenForMessage()
    }

    //生成對話框
    private fun performSendMessage() {
        val reference = FirebaseDatabase.getInstance().getReference("/message").push()

        val fromID = FirebaseAuth.getInstance().uid.toString()
        //need delete?
        val user = intent.getParcelableExtra<UserA>(NewMessageActivity.USER_KEY)
        val toID = toUser?.uid.toString()

        val text = edit_text_chat_log.text.toString()
        val chatMessage = ChatMessage(
            reference.key!!,
            text,
            fromID,
            toID,
            System.currentTimeMillis() / 1000
        )
        //set 傳edit內容給firebase
        reference.setValue(chatMessage).addOnSuccessListener {
            Log.d(TAG, "send success id:${reference.key}")
        }
    }

    //new接收firebase資料
    private fun listenForMessage() {
        val ref = FirebaseDatabase.getInstance().getReference("/message")
        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)
                if (chatMessage != null) {
                    Log.d(TAG, chatMessage.text)
                    //判斷id為me or you
                    if (chatMessage.formID == FirebaseAuth.getInstance().uid) {
                        adapter.add(ChatFromItem(chatMessage.text))
                    } else {
                        adapter.add(ChatToItem(chatMessage.text, toUser!!))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }
        })
    }
}

class ChatFromItem(val text: String) : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textView_from_row.text = text
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}

class ChatToItem(val text: String, private val user: UserA) : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textView_to_row.text = text
        //add image
        val uri = user.profileImageUrl
        Picasso.get().load(uri).into(viewHolder.itemView.image_view_chat_from)
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}