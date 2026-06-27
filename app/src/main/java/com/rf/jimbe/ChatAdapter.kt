package com.rf.jimbe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private val list: List<MessageModel>, private val currentUserId: String) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SEND = 1
    private val TYPE_RECEIVE = 2

    class SendViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMessage: TextView = view.findViewById(R.id.tvMessage)
    }

    class ReceiveViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMessage: TextView = view.findViewById(R.id.tvMessage)
    }

    // LOGIKA PENENTU BUBBLE: Dinamis cek "Apakah pembuat pesan sama dengan UID Akun yg sedang login?"
    override fun getItemViewType(position: Int): Int {
        return if (list[position].senderId == currentUserId) TYPE_SEND else TYPE_RECEIVE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SEND) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_send, parent, false)
            SendViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_receive, parent, false)
            ReceiveViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = list[position]
        if (holder is SendViewHolder) {
            holder.tvMessage.text = message.message
        } else if (holder is ReceiveViewHolder) {
            holder.tvMessage.text = message.message
        }
    }

    override fun getItemCount(): Int = list.size
}
