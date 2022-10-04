package com.example.chatroom

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.latest_message_row.view.*

//被加載的用戶物件viewHolder
class LatestMessageRow(private val chatMessage: ChatMessage) : Item<GroupieViewHolder>() {
    var chatPartnerUser: UserA? = null

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textView_latest_message.text = chatMessage.text
        //顯示聊天對象ID and image
        val chatPartnerID: String
        if (chatMessage.formID == FirebaseAuth.getInstance().uid) {
            chatPartnerID = chatMessage.toID
        } else {
            chatPartnerID = chatMessage.formID
        }

        val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerID")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                //set username once
                val chatPartnerUser = p0.getValue(UserA::class.java)
                viewHolder.itemView.textView_username_latest_message.text =
                    chatPartnerUser?.username
                //set image once
                val targetImageView = viewHolder.itemView.imageView_latest_message
                Picasso.get().load("${chatPartnerUser?.profileImageUrl}").into(targetImageView)
            }
        })
    }

    override fun getLayout(): Int {
        return R.layout.latest_message_row
    }

}