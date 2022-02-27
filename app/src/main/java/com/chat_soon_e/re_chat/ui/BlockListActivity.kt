package com.chat_soon_e.re_chat.ui

import androidx.recyclerview.widget.LinearLayoutManager
import com.chat_soon_e.re_chat.data.entities.BlockedChatList
import com.chat_soon_e.re_chat.data.local.AppDatabase
import com.chat_soon_e.re_chat.databinding.ActivityBlockListBinding
import com.chat_soon_e.re_chat.utils.getID

class BlockListActivity:BaseActivity<ActivityBlockListBinding>(ActivityBlockListBinding::inflate) {
    lateinit var blockListRVAdapter: BlockListRVAdapter
    lateinit var database: AppDatabase

    private var blockedList = ArrayList<BlockedChatList>()
    private val userID = getID()

    override fun initAfterBinding() {
        // 초기 설정
        initData()
        initRecyclerView()
        initClickListener()
    }

    private fun initData() {
        // 모든 차단된 목록을 가져온다.
        database = AppDatabase.getInstance(this)!!
        database.chatDao().getBlockedChatList(userID).observe(this) {
            blockedList.clear()
            blockedList.addAll(it)
        }
    }

    private fun initRecyclerView() {
        database = AppDatabase.getInstance(this)!!

        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        linearLayoutManager.stackFromEnd = true
        binding.blockListRecyclerView.layoutManager = linearLayoutManager

        blockListRVAdapter= BlockListRVAdapter(this, blockedList, object:BlockListRVAdapter.MyClickListener{
            override fun onRemoveChat(blockList: BlockedChatList) {
                if(blockList.groupName == null || blockList.groupName == "null")    // 개인톡
                    database.chatDao().unblockOneChat(userID, blockList.blockedName)
                else
                    database.chatDao().unblockOrgChat(userID, blockList.groupName)
            }
        })
        binding.blockListRecyclerView.adapter=blockListRVAdapter
    }

    private fun initClickListener() {
        binding.blockListBackIv.setOnClickListener {
            finish()
        }
    }
}