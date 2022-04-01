package com.chatsoone.rechat.ui.main.hiddenfolder

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Insets
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.chatsoone.rechat.R
import com.chatsoone.rechat.base.BaseFragment
import com.chatsoone.rechat.data.entity.Folder
import com.chatsoone.rechat.data.entity.Icon
import com.chatsoone.rechat.data.local.AppDatabase
import com.chatsoone.rechat.databinding.FragmentMyHiddenFolderBinding
import com.chatsoone.rechat.databinding.ItemIconBinding
import com.chatsoone.rechat.databinding.ItemMyHiddenFolderBinding
import com.chatsoone.rechat.ui.IconRVAdapter
import com.chatsoone.rechat.ui.chat.FolderContentActivity
import com.chatsoone.rechat.ui.main.MainActivity
import com.chatsoone.rechat.utils.getID
import com.google.gson.Gson

class MyHiddenFolderFragment :
    BaseFragment<FragmentMyHiddenFolderBinding>(FragmentMyHiddenFolderBinding::inflate) {
    private lateinit var database: AppDatabase
    private lateinit var mPopupWindow: PopupWindow
    private lateinit var hiddenFolderRVAdapter: HiddenFolderRVAdapter
    private lateinit var iconRVAdapter: IconRVAdapter

    private val TAG = "FRAG/HIDDEN-FOLDER"
    private var mContext: MainActivity? = null
    private var iconList = ArrayList<Icon>()
    private var hiddenFolderList = ArrayList<Folder>()

    override fun afterOnCreateView() {
        // 패턴이 맞지 않는다면 MyFolderFragment로 변환
//        val spf= activity?.getSharedPreferences("lock_correct", 0)
//        if (spf != null) {
//            if(spf.getInt("correct", 0)==-1){
//                activity_main?.replaceFragment(MyFolderFragment())
//            }
//        }

        database = AppDatabase.getInstance(requireContext())!!
        Log.d(TAG, "afterOnCreateView/userID: $userID")
        initHiddenFolder()
    }

    // 숨긴 폴더 리스트 초기화
    private fun initHiddenFolder() {
        // RecyclerView 초기화
        hiddenFolderRVAdapter = HiddenFolderRVAdapter(this)
        binding.myHiddenFolderRecyclerView.adapter = hiddenFolderRVAdapter

        database.folderDao().getHiddenFolder(userID).observe(viewLifecycleOwner) {
            hiddenFolderRVAdapter.addFolderList(it as ArrayList<Folder>)
        }

        hiddenFolderRVAdapter.setMyItemClickListener(object :
            HiddenFolderRVAdapter.MyItemClickListener {
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
                val intent = Intent(activity, FolderContentActivity::class.java)
                intent.putExtra("folderData", selectedFolderJson)
                startActivity(intent)
            }

            // 폴더 롱클릭 시 팝업 메뉴
            override fun onFolderLongClick(popupMenu: PopupMenu) {
                popupMenu.show()
            }

            // 폴더 이름 롱클릭 시 이름 변경
            override fun onFolderNameLongClick(
                itemHiddenFolderBinding: ItemMyHiddenFolderBinding,
                folderIdx: Int
            ) {
                changeFolderName(itemHiddenFolderBinding, folderIdx)
            }
        })
    }

    // 이름 바꾸기 팝업 윈도우
    @SuppressLint("InflateParams")
    fun changeFolderName(itemHiddenFolderBinding: ItemMyHiddenFolderBinding, folderIdx: Int) {
        val size = activity?.windowManager?.currentWindowMetricsPointCompat()
        val width = ((size?.x ?: 0) * 0.8f).toInt()
        val inflater =
            activity?.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_window_change_name, null)

        mPopupWindow = PopupWindow(popupView, width, WindowManager.LayoutParams.WRAP_CONTENT)
        mPopupWindow.animationStyle = 0
        mPopupWindow.isFocusable = true
        mPopupWindow.isOutsideTouchable = true
        mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
        mPopupWindow.setOnDismissListener(PopupWindowDismissListener())
        binding.myHiddenFolderBackgroundView.visibility = View.VISIBLE

        // 기존 폴더 이름을 팝업 윈도우의 EditText에 넘겨준다.
        var text: String = itemHiddenFolderBinding.itemHiddenFolderTv.text.toString()
        mPopupWindow.contentView.findViewById<EditText>(R.id.popup_window_change_name_et)
            .setText(text)

        database = AppDatabase.getInstance(requireContext())!!

        // 입력 완료했을 때 누르는 버튼
        mPopupWindow.contentView.findViewById<AppCompatButton>(R.id.popup_window_change_name_button)
            .setOnClickListener {
                // 바뀐 폴더 이름을 뷰와 RoomDB에 각각 적용해준다.
                text =
                    mPopupWindow.contentView.findViewById<EditText>(R.id.popup_window_change_name_et).text.toString()
                itemHiddenFolderBinding.itemHiddenFolderTv.text = text
                database.folderDao().updateFolderName(folderIdx, text)
                mPopupWindow.dismiss()
            }
    }

    // 아이콘 바꾸기 팝업 윈도우
    @SuppressLint("InflateParams")
    fun changeIcon(
        itemHiddenFolderBinding: ItemMyHiddenFolderBinding,
        position: Int,
        folderListFromAdapter: ArrayList<Folder>
    ) {
        val size = activity?.windowManager?.currentWindowMetricsPointCompat()
        val width = ((size?.x ?: 0) * 0.8f).toInt()
        val height = ((size?.y ?: 0) * 0.6f).toInt()
        val inflater =
            activity?.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_window_change_icon, null)

        mPopupWindow = PopupWindow(popupView, width, height)
        mPopupWindow.animationStyle = 0
        mPopupWindow.isFocusable = true
        mPopupWindow.isOutsideTouchable = true
        mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
        mPopupWindow.setOnDismissListener(PopupWindowDismissListener())
        binding.myHiddenFolderBackgroundView.visibility = View.VISIBLE

        // 데이터베이스로부터 아이콘 리스트 불러와 연결해주기
        database = AppDatabase.getInstance(requireContext())!!
        iconList = database.iconDao().getIconList() as ArrayList
        iconRVAdapter = IconRVAdapter(iconList)
        popupView.findViewById<RecyclerView>(R.id.popup_window_change_icon_recycler_view).adapter =
            iconRVAdapter

        iconRVAdapter.setMyItemClickListener(object : IconRVAdapter.MyItemClickListener {
            // 아이콘을 선택했을 경우
            override fun onIconClick(
                itemIconBinding: ItemIconBinding,
                iconPosition: Int
            ) {
                // 선택한 아이콘으로 폴더 이미지 변경
                val selectedIcon = iconList[iconPosition]
                itemHiddenFolderBinding.itemHiddenFolderIv.setImageResource(selectedIcon.iconImage)
                database.folderDao()
                    .updateFolderIcon(folderListFromAdapter[position].idx, selectedIcon.iconImage)
                mPopupWindow.dismiss()
            }
        })
    }

    inner class PopupWindowDismissListener() : PopupWindow.OnDismissListener {
        override fun onDismiss() {
            binding.myHiddenFolderBackgroundView.visibility = View.INVISIBLE
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = this.activity as MainActivity
    }

    override fun onDetach() {
        super.onDetach()
        mContext = null
    }
}