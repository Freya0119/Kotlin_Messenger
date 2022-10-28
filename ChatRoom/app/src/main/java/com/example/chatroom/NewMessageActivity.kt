package com.example.chatroom

import android.content.Intent
import android.icu.number.NumberFormatter.with
import android.icu.number.NumberRangeFormatter.with
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.common.base.MoreObjects
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_lateest_message.*
import kotlinx.android.synthetic.main.activity_main_action.*
import kotlinx.android.synthetic.main.latest_message_row.*
import kotlinx.android.synthetic.main.latest_message_row.view.*
import kotlinx.android.synthetic.main.select_dialog_item_material.*
import kotlinx.android.synthetic.main.select_item_material.*
import java.sql.Types.NULL
import com.xwray.groupie.GroupieAdapter as XwrayGroupieGroupieAdapter

class NewMessageActivity : AppCompatActivity() {

    companion object {
        //        var currentUser: User? = null
        const val USER_KEY = "USER_KEY"
    }

    private val adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lateest_message)

        latest_message_recyclerView.adapter = adapter
        //添加用戶之間的分隔線
        latest_message_recyclerView.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )
        //click then open 對話紀錄
        adapter.setOnItemClickListener { item, view ->
            val intent = Intent(this, ChatLogActivity::class.java)
            val row = item as LatestMessageRow
            //通過 USER_KEY 連結 row.chatPartnerUser
            intent.putExtra(USER_KEY, row.chatPartnerUser)
            startActivity(intent)
        }
//        fetchCurrentUser()
        listenLatestMessage()
        //TODO 功能???
//        verifyUserIsLoggedIn()
    }

    //TODO hash table
    private val latestMessageMap = HashMap<String, ChatMessage>()

    private fun listenLatestMessage() {
        val fromID = FirebaseAuth.getInstance().uid
        val latestMessageRef =
            FirebaseDatabase.getInstance().getReference("latest-messages/$fromID")
        latestMessageRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, previousChildName: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java) ?: return
//                if (chatMessage.formID == fromID) {
//                    latestMessageMap[p0.key!!] = chatMessage
//                }
                latestMessageMap[p0.key!!] = chatMessage
                refreshRecyclerViewMessage()
            }

            override fun onChildChanged(p0: DataSnapshot, previousChildName: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java) ?: return
//                if (chatMessage.formID == fromID) {
//                    latestMessageMap[p0.key!!] = chatMessage
//                }
                latestMessageMap[p0.key!!] = chatMessage
                refreshRecyclerViewMessage()
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    //refresh not add
    private fun refreshRecyclerViewMessage() {
        adapter.clear()
        latestMessageMap.values.forEach {
            adapter.add(LatestMessageRow(it))
        }
    }

//    //fetch user
//    private fun fetchCurrentUser() {
//        val uid = FirebaseAuth.getInstance().uid
//        val ref = FirebaseDatabase.getInstance().getReference("user/$uid")
//        ref.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(p0: DataSnapshot) {
//                currentUser = p0.getValue(User::class.java)
//            }
//
//            override fun onCancelled(error: DatabaseError) {}
//        })
//    }

//    private fun fetchUserAndText() {
//        val fromID = FirebaseAuth.getInstance().uid
//        val chatMessageRef = FirebaseDatabase.getInstance().getReference("latest-messages/$fromID")
//        chatMessageRef.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onCancelled(error: DatabaseError) {}
//
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val adapter = GroupAdapter<GroupieViewHolder>()
//                snapshot.children.forEach {
//                    val chatItem = it.getValue(ChatMessage::class.java)
//                    adapter.add(LastMessageRow(chatItem!!))
//                }
//                recycle_new_message.adapter = adapter
//
//                adapter.setOnItemClickListener { item, view ->
//                    val userItem = item as LastMessageRow
//                    val intent = Intent(view.context, ChatLogActivity::class.java)
//                    intent.putExtra(MainActionActivity.USER_KEY, userItem.chatUser)
//                    startActivity(intent)
//                    //???
//                    finish()
//                }
//            }
//        })
//    }

    //TODO 檢查是否login並login
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
            R.id.user_list_menu -> {
                val intent = Intent(this, MainActionActivity::class.java)
                startActivity(intent)
            }
//            R.id.new_message_menu -> {
//                val intent = Intent(this, NewMessageActivity::class.java)
//                startActivity(intent)
//            }
            R.id.sigh_out_menu -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}