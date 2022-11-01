package com.example.chatroom

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_main_action.*
import kotlinx.android.synthetic.main.new_message_row.view.*

class MainActionActivity : AppCompatActivity() {
    companion object {
        const val USER_KEY = "USER_KEY"

        //哪裡抓當前user合適
        var currentUser: User? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_action)
        // get USER_KEY 前面要加入USER_KEY為暗號，這裡要設定USER_KEY為cons，暗號對上才能設置
//        //沒用到
//        val user = intent.getParcelableExtra<User>(MainActionActivity.USER_KEY)
//        supportActionBar?.title = user?.username

        fetchCurrentUser()
        fetchUsers()
    }

    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("user/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                currentUser = snapshot.getValue(User::class.java)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    //抓所有user
    private fun fetchUsers() {
        val ref = FirebaseDatabase.getInstance().getReference("user")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val adapter = GroupAdapter<GroupieViewHolder>()
                //load each user under "/user"
                p0.children.forEach {
                    //別的*.kt的User Class
                    val user = it.getValue(User::class.java)
                    if (user != null) {
                        if (user.uid != FirebaseAuth.getInstance().uid) {
                            //add Item<GroupViewHolder> object
                            adapter.add(UserItem(user))
                            recycle_new_message.adapter = adapter
                        }
                    }
                }
                //點擊進入對應聊天activity
                adapter.setOnItemClickListener { item, view ->
                    val userItem = item as UserItem
                    //???view.context
                    val intent = Intent(view.context, ChatLogActivity::class.java)
                    intent.putExtra(USER_KEY, userItem.user)
                    startActivity(intent)
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
//            R.id.user_list_menu -> {
//                val intent = Intent(this, MainActionActivity::class.java)
//                startActivity(intent)
//            }
            R.id.new_message_menu -> {
                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }
            R.id.sigh_out_menu -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
            android.R.id.home -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

class UserItem(val user: User) : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.new_message_row_username_textView.text = user.username
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.new_message_row_image)
    }

    override fun getLayout(): Int {
        return R.layout.new_message_row
    }
}