package com.chatsoone.rechat.ui.main.blocklist

import android.annotation.SuppressLint
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.chatsoone.rechat.ApplicationClass.Companion.loadBitmap
import com.chatsoone.rechat.data.entity.BlockedChatList
import com.chatsoone.rechat.databinding.ItemBlockListBinding
import com.chatsoone.rechat.ui.main.MainActivity

class BlockListRVAdapter(
    private val mContext: MainActivity,
    private val blockList: ArrayList<BlockedChatList>,
    private val param: MyClickListener
) : RecyclerView.Adapter<BlockListRVAdapter.ViewHolder>() {
    var chatList = ArrayList<BlockedChatList>()
    private val tag = "RV/BLOCK-LIST"

    interface MyClickListener {
        fun onRemoveChat(blockList: BlockedChatList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemBlockListBinding = ItemBlockListBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(blockList[position])
    }

    override fun getItemCount(): Int = blockList.size

    // 차단 목록 업데이트
    @SuppressLint("NotifyDataSetChanged")
    fun addItem(block: List<BlockedChatList>) {
        blockList.clear()
        blockList.addAll(block as ArrayList)
        notifyDataSetChanged()
    }

    // 차단 해제하기 (차단 목록에서 제거)
    private fun removeBlock(position: Int) {
        // roomDB에서 해당 데이터 지우고 UI의 리스트 삭제
        blockList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount);
    }

    inner class ViewHolder(val binding: ItemBlockListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(block: BlockedChatList) {
            binding.itemBlockCancelIv.setOnClickListener {
                param.onRemoveChat(blockList[position])
                removeBlock(position)
            }

            if (block.groupName != null && block.groupName != "null") binding.itemBlockNameTv.text =
                block.groupName
            else binding.itemBlockNameTv.text = block.blockedName

            if (block.blockedProfileImg != null && block.blockedProfileImg != "null") binding.itemBlockProfileIv.setImageBitmap(
                loadBitmap(block.blockedProfileImg, mContext)
            )
        }
    }
}