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
    var chatPartnerUser: User? = null

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.latest_message_row_message_textView.text = chatMessage.text
        
        val chatPartnerID: String = if (chatMessage.formID == FirebaseAuth.getInstance().uid) {
            chatMessage.toID
        } else {
            chatMessage.formID
        }

        val ref = FirebaseDatabase.getInstance().getReference("users/$chatPartnerID")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val chatPartnerUser = p0.getValue(User::class.java)
                viewHolder.itemView.latest_message_row_username_textView.text =
                    chatPartnerUser?.username
                val targetImageView = viewHolder.itemView.latest_message_row_imageView
                Picasso.get().load("${chatPartnerUser?.profileImageUrl}").into(targetImageView)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun getLayout(): Int {
        return R.layout.latest_message_row
    }

}