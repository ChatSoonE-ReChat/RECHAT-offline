package com.chatsoone.rechat.ui.main.blocklist

import androidx.recyclerview.widget.LinearLayoutManager
import com.chatsoone.rechat.base.BaseFragment
import com.chatsoone.rechat.data.entity.BlockedChatList
import com.chatsoone.rechat.data.local.AppDatabase
import com.chatsoone.rechat.databinding.FragmentBlockListBinding
import com.chatsoone.rechat.ui.main.MainActivity

class BlockListFragment :
    BaseFragment<FragmentBlockListBinding>(FragmentBlockListBinding::inflate) {
    lateinit var blockListRVAdapter: BlockListRVAdapter
    lateinit var database: AppDatabase

    private var blockedList = ArrayList<BlockedChatList>()

    override fun afterOnCreateView() {
        database = AppDatabase.getInstance(requireContext())!!
//        database = activity?.let { AppDatabase.getInstance(it) }!!

        initBlockList()
        initRecyclerView()
    }

    // 차단된 목록 가져오기
    private fun initBlockList() {
        database.chatDao().getBlockedChatList(userID).observe(this) {
            blockedList.clear()
            blockedList.addAll(it)
        }
    }

    // recycler view 초기화
    private fun initRecyclerView() {
        val linearLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true)
        linearLayoutManager.stackFromEnd = true
        binding.blockListRecyclerView.layoutManager = linearLayoutManager

        blockListRVAdapter =
            BlockListRVAdapter(activity as MainActivity, blockedList, object : BlockListRVAdapter.MyClickListener {
                override fun onRemoveChat(blockList: BlockedChatList) {
                    if (blockList.groupName == null || blockList.groupName == "null")
                    // 개인톡
                        database.chatDao().unblockOneChat(userID, blockList.blockedName)
                    else
                    // 그룹톡
                        database.chatDao().unblockOrgChat(userID, blockList.groupName)
                }
            })
        binding.blockListRecyclerView.adapter = blockListRVAdapter
    }
}