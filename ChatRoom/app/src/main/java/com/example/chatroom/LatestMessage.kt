package com.example.chatroom

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.latest_message_row.view.*
import kotlinx.android.synthetic.main.select_dialog_item_material.*
import com.xwray.groupie.GroupieAdapter as XwrayGroupieGroupieAdapter

class LatestMessage : AppCompatActivity() {

    companion object {
        var currentUser: UserA? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lateest_message)

        //setupDummyRows()
        listenLatestMessage()

        fetchCurrentUser()

        verifyUserIsLoggedIn()
    }

    //被加載的用戶物件viewHolder
    class LatestMessageRow(val chatMessage: ChatMessage) : Item<GroupieViewHolder>() {
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.textView_latest_message.text = chatMessage.text
        }

        override fun getLayout(): Int {
            return R.layout.latest_message_row
        }

    }

    private val adapter = GroupAdapter<GroupieViewHolder>()

    //加載user和最後消息
    private fun setupDummyRows() {
//        adapter.add(LatestMessageRow())
//
//        recycle_latest_messages.adapter = adapter
    }

    //監聽latest message
    private fun listenLatestMessage() {
        val fromID = FirebaseAuth.getInstance().uid
        val latestMessageRef = FirebaseDatabase.getInstance().getReference("latest-message/$fromID")
        latestMessageRef.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onChildAdded(p0: DataSnapshot, previousChildName: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java) ?: return
                adapter.add(LatestMessageRow(chatMessage))
            }

            override fun onChildChanged(p0: DataSnapshot, previousChildName: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java) ?: return
                adapter.add(LatestMessageRow(chatMessage))
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }
        })
    }

    //fetch user
    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val currentUser = p0.getValue(UserA::class.java)
                Log.d("Latest Message", "Current user: ${currentUser?.profileImageUrl}")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    //檢查是否login並login
    private fun verifyUserIsLoggedIn() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_sigh_out -> {
                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_new_message -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}