package com.chatsoone.rechat.ui

import androidx.recyclerview.widget.LinearLayoutManager
import com.chatsoone.rechat.base.BaseActivity
import com.chatsoone.rechat.data.entity.BlockedChatList
import com.chatsoone.rechat.data.local.AppDatabase
import com.chatsoone.rechat.databinding.ActivityBlockListBinding
import com.chatsoone.rechat.ui.main.blocklist.BlockListRVAdapter
import com.chatsoone.rechat.utils.getId

class BlockListActivity: BaseActivity<ActivityBlockListBinding>(ActivityBlockListBinding::inflate) {
    lateinit var blockListRVAdapter: BlockListRVAdapter
    lateinit var database: AppDatabase

    private var blockedList = ArrayList<BlockedChatList>()
    private val userID = getId()

    override fun afterOnCreate() {
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

        blockListRVAdapter= BlockListRVAdapter(this, blockedList, object: BlockListRVAdapter.MyClickListener{
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