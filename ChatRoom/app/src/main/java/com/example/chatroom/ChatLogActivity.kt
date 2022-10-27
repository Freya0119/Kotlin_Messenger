package com.example.chatroom

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*
import kotlinx.android.synthetic.main.new_message_row.*

class ChatLogActivity : AppCompatActivity() {
    companion object {
        const val TAG = "CHAT LOG"
    }

    //adapter
    var adapter = GroupAdapter<GroupieViewHolder>()
    var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)
        //getParcelableExtra 接收putExtra傳過來的userItem???傳過來的是被選擇交談的user
        toUser = intent.getParcelableExtra<User>(MainActionActivity.USER_KEY)

//        val testChatItem=intent.getParcelableExtra<ChatMessage>(NewMessageActivity.CHAT_USER_KEY)
//        Toast.makeText(this,testChatItem?.text,Toast.LENGTH_SHORT).show()

        //設置菜單欄
        supportActionBar?.title = toUser?.username
        //監聽adapter?
        recycle_chat_log.adapter = adapter

        button_send_chat_log.setOnClickListener() {
            Log.d(TAG, "this is send button")
            performSendMessage()
        }

        listenForMessage()
    }

    //檢查聊天訊息更新
    private fun listenForMessage() {
        val fromID = FirebaseAuth.getInstance().uid
        val toID = toUser?.uid
        val currentUser = MainActionActivity.currentUser
        val ref = FirebaseDatabase.getInstance().getReference("message/$fromID/$toID")
        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)
                if (chatMessage != null) {
//                    Log.d(TAG, chatMessage.text)
                    //判斷id為me or you
                    if (chatMessage.formID == FirebaseAuth.getInstance().uid) {
//                        val currentUser = LatestMessageActivity.currentUser
                        adapter.add(ChatFromItem("${chatMessage.text}", currentUser!!))
                        recycle_chat_log.adapter = adapter
                    } else {
                        adapter.add(ChatToItem("${chatMessage.text}", toUser!!))
                    }
                }
                //拉消息到最下面
                recycle_chat_log.scrollToPosition(adapter.itemCount - 1)
            }

            override fun onCancelled(error: DatabaseError) {}
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
        })
    }

    //生成發送對話框
    private fun performSendMessage() {
        val fromID = FirebaseAuth.getInstance().uid.toString()
        val toID = toUser?.uid.toString()
        val text = edit_text_chat_log.text.toString()
        //push chat message
        val reference =
            FirebaseDatabase.getInstance().getReference("message/$fromID/$toID").push()
        val toReference =
            FirebaseDatabase.getInstance().getReference("message/$toID/$fromID").push()
        //chatMessage object 包括key text fromUid toUid 時間
        val chatMessage = ChatMessage(
            reference.key!!, text, fromID, toID, System.currentTimeMillis() / 1000
        )
        //set chat message
        reference.setValue(chatMessage).addOnSuccessListener {
            recycle_chat_log.scrollToPosition(adapter.itemCount - 1)
            edit_text_chat_log.text.clear()
        }
        toReference.setValue(chatMessage)

        val latestMessageRef =
            FirebaseDatabase.getInstance().getReference("latest-messages/$fromID/$toID")
        latestMessageRef.setValue(chatMessage)
        val toLatestMessageRef =
            FirebaseDatabase.getInstance().getReference("latest-messages/$toID/$fromID")
        toLatestMessageRef.setValue(chatMessage)
    }
}

class ChatFromItem(val text: String, private val user: User) : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.chat_from_row_username_textView.text = user.username
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
        viewHolder.itemView.chat_to_row_username_textView.text = user.username
        viewHolder.itemView.chat_to_row_text_textView.text = text
        val uri = user.profileImageUrl
        Picasso.get().load(uri).into(viewHolder.itemView.image_view_chat_to)
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}