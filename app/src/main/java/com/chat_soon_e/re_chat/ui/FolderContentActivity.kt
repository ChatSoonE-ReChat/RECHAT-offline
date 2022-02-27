package com.chat_soon_e.re_chat.ui

import android.graphics.Insets
import android.graphics.Point
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import com.chat_soon_e.re_chat.data.entities.Folder
import com.chat_soon_e.re_chat.data.local.AppDatabase
import com.chat_soon_e.re_chat.databinding.ActivityFolderContentBinding
import com.chat_soon_e.re_chat.utils.getID
import com.google.gson.Gson

class FolderContentActivity: BaseActivity<ActivityFolderContentBinding>(ActivityFolderContentBinding::inflate) {
    private lateinit var database: AppDatabase
    private lateinit var folderContentRVAdapter: FolderContentRVAdapter

    lateinit var folderInfo: Folder
    private val userID = getID()
    private val tag = "ACT/FOLDER-CONTENT"

    override fun initAfterBinding() {
        Log.d(tag, "initAfterBinding()/userID: $userID")

        initData()
        initRecyclerView()
        initClickListener()
    }

    // FolderContent 데이터 초기화
    private fun initData(){
        database = AppDatabase.getInstance(this)!!

        // 전 페이지에서 데이터 가져오는 부분
        if(intent.hasExtra("folderData")) {
            val folderJson = intent.getStringExtra("folderData")
            folderInfo = Gson().fromJson(folderJson, Folder::class.java)
            binding.folderContentNameTv.text = folderInfo.folderName
            Log.d(tag, "data: $folderInfo")
        }
    }

    // RecyclerView 초기화
    private fun initRecyclerView() {
        // 휴대폰 윈도우 사이즈를 가져온다.
        val size = windowManager.currentWindowMetricsPointCompat()
        database = AppDatabase.getInstance(this)!!

        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        linearLayoutManager.stackFromEnd = true
        binding.folderContentRecyclerView.layoutManager = linearLayoutManager

        // FolderContent 데이터를 RecyclerView 어댑터와 연결
        // userID: kakaoUserIdx, folderInfo.idx: folder index
        database.folderContentDao().getFolderChat(userID, folderInfo.idx).observe(this) {
            folderContentRVAdapter.addItem(it)
            Log.d("folderDatacheck: ", it.toString())
        }

        // RecyclerView click listener 초기화
        folderContentRVAdapter = FolderContentRVAdapter(this, size, object: FolderContentRVAdapter.MyClickListener {
            // 채팅 삭제
            override fun onRemoveChat(chatIdx: Int) {
                database.folderContentDao().deleteChat(folderInfo.idx, chatIdx)
            }

            // 채팅 롱클릭 시 팝업 메뉴
            override fun onChatLongClick(popupMenu: PopupMenu) {
                popupMenu.show()
            }
        })
        binding.folderContentRecyclerView.adapter = folderContentRVAdapter
    }

    // 디바이스 크기에 사이즈를 맞추기 위한 함수
    private fun WindowManager.currentWindowMetricsPointCompat(): Point {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val windowInsets = currentWindowMetrics.windowInsets
            var insets: Insets = windowInsets.getInsets(WindowInsets.Type.navigationBars())
            windowInsets.displayCutout?.run {
                insets = Insets.max(insets, Insets.of(safeInsetLeft, safeInsetTop, safeInsetRight, safeInsetBottom))
            }
            val insetsWidth = insets.right + insets.left
            val insetsHeight = insets.top + insets.bottom
            Point(currentWindowMetrics.bounds.width() - insetsWidth, currentWindowMetrics.bounds.height() - insetsHeight)
        } else{
            Point().apply {
                defaultDisplay.getSize(this)
            }
        }
    }

    private fun initClickListener() {
        // 뒤로 가기 버튼 눌렀을 때
        binding.folderContentBackIv.setOnClickListener {
            finish()
        }
    }
}