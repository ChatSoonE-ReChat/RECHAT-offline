package com.chat_soon_e.re_chat.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Insets
import android.graphics.Point
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.PopupWindow
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.chat_soon_e.re_chat.R
import com.chat_soon_e.re_chat.data.entities.Folder
import com.chat_soon_e.re_chat.data.entities.Icon
import com.chat_soon_e.re_chat.data.local.AppDatabase
import com.chat_soon_e.re_chat.databinding.ActivityHiddenFolderBinding
import com.chat_soon_e.re_chat.databinding.ItemHiddenFolderBinding
import com.chat_soon_e.re_chat.databinding.ItemIconBinding
import com.chat_soon_e.re_chat.utils.getID
import com.google.gson.Gson

class HiddenFolderActivity: BaseActivity<ActivityHiddenFolderBinding>(ActivityHiddenFolderBinding::inflate) {
    private lateinit var database: AppDatabase
    private lateinit var hiddenFolderRVAdapter: HiddenFolderRVAdapter
    private lateinit var iconRVAdapter: ChangeIconRVAdapter
    private lateinit var mPopupWindow: PopupWindow

    private var iconList = ArrayList<Icon>()
    private var hiddenFolderList = ArrayList<Folder>()
    private val tag = "ACT/HIDDEN-FOLDER"
    private val userID=getID()

    override fun initAfterBinding() {
        Log.d(tag, "initAfterBinding()/userID: $userID")
        initFolder()
    }

    // 폴더 리스트 초기화
    private fun initFolder() {
        database = AppDatabase.getInstance(this)!!

        // RecyclerView 초기화
        hiddenFolderRVAdapter = HiddenFolderRVAdapter(this)
        binding.hiddenFolderListRecyclerView.adapter = hiddenFolderRVAdapter

        database.folderDao().getHiddenFolder(userID).observe(this){
            hiddenFolderRVAdapter.addFolderList(it as ArrayList<Folder>)
        }

        hiddenFolderRVAdapter.setMyItemClickListener(object: HiddenFolderRVAdapter.MyItemClickListener {
            // 보관함으로 보내기
            // 폴더 상태 HIDDEN -> ACTIVE
            override fun onShowFolder(folderIdx: Int) {
                database.folderDao().updateFolderUnHide(folderIdx)
            }

            // 폴더 삭제
            // 폴더 상태를 HIDDEN -> DELETED로 바꿔준다.
            override fun onRemoveFolder(folderIdx: Int) {
                database.folderDao().deleteFolder(folderIdx)
            }

            // 폴더 클릭 시 이동
            override fun onFolderClick(view: View, position: Int) {
                val selectedFolder = hiddenFolderRVAdapter.getSelectedFolder(position)
                val selectedFolderJson = Gson().toJson(selectedFolder)

                // 폴더 정보 보내기
                val intent = Intent(this@HiddenFolderActivity, FolderContentActivity::class.java)
                intent.putExtra("folderData", selectedFolderJson)
                startActivity(intent)
            }

            // 폴더 롱클릭 시 팝업 메뉴
            override fun onFolderLongClick(popupMenu: PopupMenu) {
                popupMenu.show()
            }

            // 폴더 이름 롱클릭 시 이름 변경
            override fun onFolderNameLongClick(itemHiddenFolderBinding: ItemHiddenFolderBinding, folderIdx: Int) {
                changeFolderName(itemHiddenFolderBinding, folderIdx)
            }
        })
    }

    // 이름 바꾸기 팝업 윈도우
    @SuppressLint("InflateParams")
    fun changeFolderName(itemHiddenFolderBinding: ItemHiddenFolderBinding, folderIdx:Int) {
        val size = windowManager.currentWindowMetricsPointCompat()
        val width = (size.x * 0.8f).toInt()
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_window_change_name, null)

        mPopupWindow = PopupWindow(popupView, width, WindowManager.LayoutParams.WRAP_CONTENT)
        mPopupWindow.animationStyle = 0
        mPopupWindow.isFocusable = true
        mPopupWindow.isOutsideTouchable = true
        mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
        mPopupWindow.setOnDismissListener(PopupWindowDismissListener())
        binding.hiddenFolderBackgroundView.visibility = View.VISIBLE

        // 기존 폴더 이름을 팝업 윈도우의 EditText에 넘겨준다.
        var text: String = itemHiddenFolderBinding.itemHiddenFolderTv.text.toString()
        mPopupWindow.contentView.findViewById<EditText>(R.id.popup_window_change_name_et).setText(text)

        database = AppDatabase.getInstance(this@HiddenFolderActivity)!!

        // 입력 완료했을 때 누르는 버튼
        mPopupWindow.contentView.findViewById<AppCompatButton>(R.id.popup_window_change_name_button).setOnClickListener {
            // 바뀐 폴더 이름을 뷰와 RoomDB에 각각 적용해준다.
            text = mPopupWindow.contentView.findViewById<EditText>(R.id.popup_window_change_name_et).text.toString()
            itemHiddenFolderBinding.itemHiddenFolderTv.text = text
            database.folderDao().updateFolderName(folderIdx, text)
            mPopupWindow.dismiss()
        }
    }

    // 아이콘 바꾸기 팝업 윈도우
    @SuppressLint("InflateParams")
    fun changeIcon(itemHiddenFolderBinding: ItemHiddenFolderBinding, position: Int, folderListFromAdapter: ArrayList<Folder>) {
        val size = windowManager.currentWindowMetricsPointCompat()
        val width = (size.x * 0.8f).toInt()
        val height = (size.y * 0.6f).toInt()
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_window_change_icon, null)

        mPopupWindow = PopupWindow(popupView, width, height)
        mPopupWindow.animationStyle = 0
        mPopupWindow.isFocusable = true
        mPopupWindow.isOutsideTouchable = true
        mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
        mPopupWindow.setOnDismissListener(PopupWindowDismissListener())
        binding.hiddenFolderBackgroundView.visibility = View.VISIBLE

        // 데이터베이스로부터 아이콘 리스트 불러와 연결해주기
        database = AppDatabase.getInstance(this@HiddenFolderActivity)!!
        iconList = database.iconDao().getIconList() as ArrayList
        iconRVAdapter = ChangeIconRVAdapter(iconList)
        popupView.findViewById<RecyclerView>(R.id.popup_window_change_icon_recycler_view).adapter = iconRVAdapter

        iconRVAdapter.setMyItemClickListener(object: ChangeIconRVAdapter.MyItemClickListener {
            // 아이콘을 선택했을 경우
            override fun onIconClick(itemIconBinding: ItemIconBinding, iconPosition: Int) {//icon 포지션
                // 선택한 아이콘으로 폴더 이미지 변경
                val selectedIcon = iconList[iconPosition]
                itemHiddenFolderBinding.itemHiddenFolderIv.setImageResource(selectedIcon.iconImage)
                database.folderDao().updateFolderIcon(folderListFromAdapter[position].idx, selectedIcon.iconImage)
                mPopupWindow.dismiss()
            }
        })
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

    inner class PopupWindowDismissListener(): PopupWindow.OnDismissListener {
        override fun onDismiss() {
            binding.hiddenFolderBackgroundView.visibility = View.INVISIBLE
        }
    }

    override fun onBackPressed() {
        finish()
    }
}