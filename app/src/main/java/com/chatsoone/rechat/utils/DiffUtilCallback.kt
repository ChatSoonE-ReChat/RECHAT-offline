package com.chatsoone.rechat.utils

import androidx.recyclerview.widget.DiffUtil
import com.chatsoone.rechat.data.entity.ChatList

class DiffUtilCallback(private val oldList: List<ChatList>, private val newList: List<ChatList>) :
    DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldList[oldItemPosition].chatIdx == newList[newItemPosition].chatIdx

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldList[oldItemPosition] == newList[newItemPosition]
}