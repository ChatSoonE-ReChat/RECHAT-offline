package com.chat_soon_e.re_chat.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.chat_soon_e.re_chat.data.entities.BlockedChatList
import com.chat_soon_e.re_chat.data.local.AppDatabase
import com.chat_soon_e.re_chat.databinding.FragmentBlockListBinding
import com.chat_soon_e.re_chat.utils.getID

class BlockListFragment: Fragment() {
    lateinit var binding:FragmentBlockListBinding
    lateinit var blockListRVAdapter: BlockListRVAdapter
    lateinit var database: AppDatabase
    private var blockedList = ArrayList<BlockedChatList>()
    private val userID = getID()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentBlockListBinding.inflate(layoutInflater, container, false)
        return binding.root
    }
    private fun initData() {
        // 모든 차단된 목록을 가져온다.
        database = activity?.let { AppDatabase.getInstance(it) }!!
        database.chatDao().getBlockedChatList(userID).observe(this) {
            blockedList.clear()
            blockedList.addAll(it)
        }
    }
    private fun initRecyclerView() {
        database = AppDatabase.getInstance(requireContext())!!

        val linearLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true)
        linearLayoutManager.stackFromEnd = true
        binding.blockListRecyclerView.layoutManager = linearLayoutManager

        blockListRVAdapter= BlockListRVAdapter(this.activity as MainActivity, blockedList, object:BlockListRVAdapter.MyClickListener{
            override fun onRemoveChat(blockList: BlockedChatList) {
                if(blockList.groupName == null || blockList.groupName == "null")    // 개인톡
                    database.chatDao().unblockOneChat(userID, blockList.blockedName)
                else
                    database.chatDao().unblockOrgChat(userID, blockList.groupName)
            }
        })
        binding.blockListRecyclerView.adapter=blockListRVAdapter
    }


}