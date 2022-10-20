package com.example.chatroom

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.chatroom.LatestMessageActivity.Companion.currentUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*
import kotlinx.android.synthetic.main.new_message_row.*
import java.util.*

class ChatLogActivity : AppCompatActivity() {
    companion object {
        const val TAG = "CHAT LOG"
    }

    //adapter
    val adapter = GroupAdapter<GroupieViewHolder>()
    var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        //getParcelableExtra 接收putExtra傳過來的userItem???傳過來的是被選擇交談的user
        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)

        //設置菜單欄
        supportActionBar?.title = toUser?.username

        recycle_chat_log.adapter = adapter

        //發送聊天訊息
        button_send_chat_log.setOnClickListener() {
            Log.d(TAG, "this is send button")
            performSendMessage()
        }
        fetchCurrentUser()
        //loadMessageBefore()
        //listenForMessage()
    }

    val currentUser: User? = null
    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid.toString()
        val ref = FirebaseDatabase.getInstance().getReference("user/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
                currentUser = snapshot.getValue(User::class.java)
                if (currentUser != null) {
                    Toast.makeText(this, currentUser.username, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    //生成發送對話框
    private fun performSendMessage() {
        //val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        //from me
        val fromID = FirebaseAuth.getInstance().uid.toString()
        //to who be choose
        val toID = toUser?.uid.toString()
        val text = edit_text_chat_log.text.toString()
        //push message裡面的
        val reference = FirebaseDatabase.getInstance().getReference("/message/$fromID/$toID")
        val toReference = FirebaseDatabase.getInstance().getReference("/message/$toID/$fromID")
        //chatMessage object 包括fromID toID UserA text 時間
        val chatMessage = ChatMessage(
            reference.key!!,
            text,
            fromID,
            toID,
            System.currentTimeMillis() / 1000
        )
        //set 傳edit內容給firebase
        reference.setValue(chatMessage).addOnSuccessListener {
            Log.d(TAG, "Saved our chat message: ${reference.key}")
            //拉到最後一條消息
            recycle_chat_log.scrollToPosition(adapter.itemCount - 1)
            //clear edit text的內容
            edit_text_chat_log.text.clear()
        }
        toReference.setValue(chatMessage)

        //latest message perform???紀錄最後一條顯示在聊天室
        val latestMessageRef =
            FirebaseDatabase.getInstance().getReference("latest-messages/$fromID/$toID")
        latestMessageRef.setValue(chatMessage)
        val toLatestMessageRef =
            FirebaseDatabase.getInstance().getReference("latest-messages/$toID/$fromID")
        toLatestMessageRef.setValue(chatMessage)
    }

    //load聊天紀錄
    private fun loadMessageBefore() {
        val fromID = FirebaseAuth.getInstance().uid?.toString()
        val toID = toUser?.uid.toString()
        val logRef = FirebaseDatabase.getInstance().getReference("/message/$fromID/$toID")
        logRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                TODO("Not yet implemented")
                p0.children.forEach {
                    val chatItem = it.getValue(ChatMessage::class.java)
                    if (chatItem != null) {
                        adapter.add(ChatFromItem("test", currentUser!!))
                    }
                }
                recycle_chat_log.adapter = adapter
            }
        })
    }

    //檢查聊天訊息更新
    private fun listenForMessage() {
        val fromID = FirebaseAuth.getInstance().uid.toString()
        val toID = toUser?.uid.toString()

        val ref = FirebaseDatabase.getInstance().getReference("/message/$fromID/$toID")
        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)
                if (chatMessage != null) {
                    Log.d(TAG, chatMessage.text)
                    //判斷id為me or you
                    if (chatMessage.formID == FirebaseAuth.getInstance().uid) {
                        val currentUser = LatestMessageActivity.currentUser
                        adapter.add(ChatFromItem(chatMessage.text, currentUser!!))
                    } else {
                        adapter.add(ChatToItem(chatMessage.text, toUser!!))
                    }
                }
                //拉消息到最下面
                recycle_chat_log.scrollToPosition(adapter.itemCount - 1)
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

class ChatFromItem(val text: String, private val user: User) : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textView_from_row.text = text
        val uri = user.profileImageUrl
        Picasso.get().load(uri).into(viewHolder.itemView.image_view_chat_from)
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}

class ChatToItem(val text: String, private val user: User) : Item<GroupieViewHolder>() {
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