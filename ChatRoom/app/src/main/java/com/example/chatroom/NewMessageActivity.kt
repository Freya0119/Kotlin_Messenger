package com.example.chatroom

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.new_message_row.*
import kotlinx.android.synthetic.main.new_message_row.view.*

class NewMessageActivity : AppCompatActivity() {
    companion object {
        const val USER_KEY = "USER_KEY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        //通過key取得username?
        // get USER_KEY 前面要加入USER_KEY為暗號，這裡要設定USER_KEY為cons，暗號對上才能設置
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title = user?.username
        //抓user
        fetchUsers()
    }

    //抓相關用戶
    private fun fetchUsers() {
        val ref = FirebaseDatabase.getInstance().getReference("/user")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val adapter = GroupAdapter<GroupieViewHolder>()

                //load each user under "/user"
                p0.children.forEach {
                    Log.d("FOREACH", it.toString())

                    //別的*.kt的User Class
                    val user = it.getValue(User::class.java)
                    if (user != null) {
                        //add Item<GroupViewHolder> object
                        adapter.add(UserItem(user))
                        recycle_new_message.adapter = adapter
                    }
                }

                //點擊進入對應聊天activity
                adapter.setOnItemClickListener { item, view ->
                    val userItem = item as UserItem
                    //只會load chat log room?
                    //???view.context
                    val intent = Intent(view.context, ChatLogActivity::class.java)
                    intent.putExtra(USER_KEY, item.user)
                    startActivity(intent)

                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}

class UserItem(val user: User) : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.username_textview_new_message.text = user.username
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.imageview_new_message)
    }

    override fun getLayout(): Int {
        return R.layout.new_message_row
    }
}