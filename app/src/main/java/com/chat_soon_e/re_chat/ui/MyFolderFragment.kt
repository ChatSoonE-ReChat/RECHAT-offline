package com.chat_soon_e.re_chat.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Insets
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.PopupWindow
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.chat_soon_e.re_chat.R
import com.chat_soon_e.re_chat.data.entities.Folder
import com.chat_soon_e.re_chat.data.entities.Icon
import com.chat_soon_e.re_chat.data.local.AppDatabase
import com.chat_soon_e.re_chat.databinding.FragmentMyfolderBinding
import com.chat_soon_e.re_chat.databinding.ItemIconBinding
import com.chat_soon_e.re_chat.databinding.ItemMyFolderBinding
import com.chat_soon_e.re_chat.utils.getID
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.gson.Gson
import java.io.ByteArrayOutputStream

class MyFolderFragment:Fragment() {
    private lateinit var binding:FragmentMyfolderBinding

    private lateinit var database: AppDatabase
    private lateinit var folderRVAdapter: MyFolderRVAdapter
    private lateinit var iconRVAdapter: ChangeIconRVAdapter
    private lateinit var mPopupWindow: PopupWindow

    private var folderList = ArrayList<Folder>()
    private var iconList = ArrayList<Icon>()
    private val userID = getID()
    private val TAG = "FRAG/MYFOLDER"

    // Popupwindow와 RecyclerView 연결을 위해 선언
    private lateinit var itemBinding: ItemMyFolderBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentMyfolderBinding.inflate(inflater, container, false)

        database = AppDatabase.getInstance(requireContext())!!
        iconList = database.iconDao().getIconList() as ArrayList   // 아이콘 받아오기
        initRecyclerView()          // 폴더 초기화

        return binding.root
    }

    // RecyclerView 초기화
    private fun initRecyclerView() {
        // RecyclerView 초기화
        folderRVAdapter = MyFolderRVAdapter(this)
        binding.myFolderFolderListRecyclerView.adapter = folderRVAdapter

        // LiveData
        database.folderDao().getFolderList(userID).observe(viewLifecycleOwner){
            folderRVAdapter.addFolderList(it as ArrayList<Folder>)
        }

        // click listener
        folderRVAdapter.setMyItemClickListener(object: MyFolderRVAdapter.MyItemClickListener {
            // 폴더 이름 롱클릭 시 폴더 이름 변경
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onFolderNameLongClick(binding: ItemMyFolderBinding, folderIdx: Int) {
                itemBinding = binding
                changeFolderName(itemBinding, folderIdx)
            }

            // 폴더 아이콘 클릭 시 해당 폴더로 이동
            override fun onFolderClick(view: View, position: Int) {
                val selectedFolder = folderRVAdapter.getSelectedFolder(position)

                // folder 삽입 시 status 변경! null X
                val gson = Gson()
                val folderJson = gson.toJson(selectedFolder)

                // 폴더 정보를 보내기
                val intent = Intent(activity, FolderContentActivity::class.java)
                intent.putExtra("folderData", folderJson)
                startActivity(intent)
            }

            // 폴더 아이콘 롱클릭 시 팝업 메뉴 뜨도록
            override fun onFolderLongClick(popupMenu: PopupMenu) {
                popupMenu.show()
            }

            // 폴더 삭제하기
            override fun onRemoveFolder(folderIdx: Int) {
                database.folderDao().deleteFolder(folderIdx)
            }

            // 폴더 숨기기
            @SuppressLint("NotifyDataSetChanged")
            override fun onHideFolder(folderIdx: Int) {
                // 숨김 폴더 인덱스를 맨 뒤로 넣는 식으로 해서 폴더 리스트 순서를 바꿔줘야 한다. (데이터베이스 안에)
                database.folderDao().updateFolderHide(folderIdx)
                // 폴더를 숨긴다. 데이터가 바뀔 것
                folderRVAdapter.notifyDataSetChanged()
            }
        })
    }

    // 이름 바꾸기 팝업 윈도우를 띄워서 폴더 이름을 변경할 수 있도록 해준다.
    @SuppressLint("InflateParams", "ClickableViewAccessibility")
    fun changeFolderName(itemBinding: ItemMyFolderBinding, folderIdx:Int) {

        val size = activity?.windowManager?.currentWindowMetricsPointCompat()
        val width = ((size?.x ?: 0) * 0.8f).toInt()
        val height = ((size?.y ?: 0) * 0.4f).toInt()

        // 이름 바꾸기 팝업 윈도우
        val inflater = activity?.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_window_change_name, null)
        mPopupWindow = PopupWindow(popupView, width, WindowManager.LayoutParams.WRAP_CONTENT)

        mPopupWindow.animationStyle = 0        // 애니메이션 설정 (-1: 설정 안 함, 0: 설정)
        mPopupWindow.animationStyle = R.style.Animation
        mPopupWindow.isFocusable = true         // 외부 영역 선택 시 팝업 윈도우 종료
        mPopupWindow.isOutsideTouchable = true
        mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
        binding.myFolderBackgroundView.visibility = View.VISIBLE    // 뒷배경 흐려지게
        mPopupWindow.setOnDismissListener(PopupWindowDismissListener())

        // 기존 폴더 이름을 팝업 윈도우의 EditText에 넘겨준다.
        var text: String = itemBinding.itemMyFolderTv.text.toString()
        mPopupWindow.contentView.findViewById<EditText>(R.id.popup_window_change_name_et).setText(text)

        // RoomDB
        database = AppDatabase.getInstance(requireContext())!!

        // 입력 완료했을 때 누르는 버튼
        mPopupWindow.contentView.findViewById<AppCompatButton>(R.id.popup_window_change_name_button).setOnClickListener {
            text = mPopupWindow.contentView.findViewById<EditText>(R.id.popup_window_change_name_et).text.toString()
            itemBinding.itemMyFolderTv.text = text
            database.folderDao().updateFolderName(folderIdx, text)

            // 팝업 윈도우 종료
            mPopupWindow.dismiss()

            // 뒷배경 원래대로
            binding.myFolderBackgroundView.visibility = View.INVISIBLE
        }

        // LiveData 부분이 있었던 부분인데, 윗부분과 중복되어 임시로 삭제
//        database.folderDao().getFolderList(userID).observe(this){
//            folderRVAdapter.addFolderList(it as ArrayList<Folder>)
//        }
    }

    @SuppressLint("InflateParams", "ClickableViewAccessibility")
    fun changeIcon(itemBinding: ItemMyFolderBinding, position: Int, folderIdx: Int) {
        // 팝업 윈도우 사이즈를 잘못 맞추면 아이템들이 안 뜨므로 하드 코딩으로 사이즈 조정해주기
        // 아이콘 16개 (기본)
        val size = activity?.windowManager?.currentWindowMetricsPointCompat()
        val width = ((size?.x ?: 0) * 0.8f).toInt()
        val height = ((size?.y ?: 0) * 0.6f).toInt()

        // 아이콘 바꾸기 팝업 윈도우
        val inflater = activity?.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_window_change_icon, null)
        mPopupWindow = PopupWindow(popupView, width, height)

        mPopupWindow.animationStyle = 0        // 애니메이션 설정 (-1: 설정 안 함, 0: 설정)
        mPopupWindow.animationStyle = R.style.Animation
        mPopupWindow.isFocusable = true
        mPopupWindow.isOutsideTouchable = true

        mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
        binding.myFolderBackgroundView.visibility = View.VISIBLE    // 뒷배경 흐려지게
        mPopupWindow.setOnDismissListener(PopupWindowDismissListener())

        // RecyclerView 초기화
        iconRVAdapter = ChangeIconRVAdapter(iconList)
        popupView.findViewById<RecyclerView>(R.id.popup_window_change_icon_recycler_view).adapter = iconRVAdapter

        iconRVAdapter.setMyItemClickListener(object: ChangeIconRVAdapter.MyItemClickListener {
            // 아이콘을 하나 선택했을 경우
            override fun onIconClick(itemIconBinding: ItemIconBinding, itemPosition: Int) {//해당 파라미터는 아이콘 DB!
                // 선택한 아이콘으로 폴더 이미지 변경
                val selectedIcon = iconList[itemPosition]
                itemBinding.itemMyFolderIv.setImageResource(selectedIcon.iconImage)

                val iconBitmap = BitmapFactory.decodeResource(resources, selectedIcon.iconImage)
                val baos = ByteArrayOutputStream()
                iconBitmap.compress(Bitmap.CompressFormat.PNG, 70, baos)

                val iconBitmapAsByte = baos.toByteArray()
                val iconBitmapAsString = Base64.encodeToString(iconBitmapAsByte, Base64.DEFAULT)

                database = AppDatabase.getInstance(requireContext())!!

                // RoomDB 적용
                database.folderDao().updateFolderIcon(folderIdx, selectedIcon.iconImage)

                // 팝업 윈도우 종료
                mPopupWindow.dismiss()
            }
        })
    }

    // 새폴더 이름 설정
    @SuppressLint("InflateParams")
    private fun setFolderName() {
        val size = activity?.windowManager?.currentWindowMetricsPointCompat()
        val width = ((size?.x ?: 0) * 0.8f).toInt()
        val height = ((size?.y ?: 0) * 0.4f).toInt()

        val inflater = activity?.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_window_set_folder_name, null)
        mPopupWindow = PopupWindow(popupView, width, WindowManager.LayoutParams.WRAP_CONTENT)

        mPopupWindow.animationStyle = 0        // 애니메이션 설정 (-1: 설정 안 함, 0: 설정)
        mPopupWindow.animationStyle = R.style.Animation
        mPopupWindow.isFocusable = true         // 외부 영역 선택 시 팝업 윈도우 종료
        mPopupWindow.isOutsideTouchable = true
        mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
        binding.myFolderBackgroundView.visibility = View.VISIBLE    // 뒷배경 흐려지게
        mPopupWindow.setOnDismissListener(PopupWindowDismissListener())

        // 입력 완료했을 때 누르는 버튼
        mPopupWindow.contentView.findViewById<AppCompatButton>(R.id.popup_window_set_name_button).setOnClickListener {
            // 작성한 폴더 이름을 반영한 새폴더를 만들어준다.
            val name = mPopupWindow.contentView.findViewById<EditText>(R.id.popup_window_set_name_et).text.toString()

            // 팝업 윈도우 종료
            mPopupWindow.dismiss()

            // 작성한 폴더 이름을 setFolderIcon 함수로 넘겨준다.
            setFolderIcon(name)
        }
    }

    // 새폴더 아이콘 설정
    @SuppressLint("InflateParams")
    private fun setFolderIcon(name: String) {
        // 팝업 윈도우 사이즈를 잘못 맞추면 아이템들이 안 뜨므로 하드 코딩으로 사이즈 조정해주기
        // 아이콘 16개 (기본)
        val size = activity?.windowManager?.currentWindowMetricsPointCompat()
        val width = ((size?.x ?: 0) * 0.8f).toInt()
        val height = ((size?.y ?: 0) * 0.6f).toInt()

        // 아이콘 바꾸기 팝업 윈도우
        val inflater = activity?.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_window_change_icon, null)
        mPopupWindow = PopupWindow(popupView, width, height)

        mPopupWindow.animationStyle = 0        // 애니메이션 설정 (-1: 설정 안 함, 0: 설정)
        mPopupWindow.animationStyle = R.style.Animation
        mPopupWindow.isFocusable = true
        mPopupWindow.isOutsideTouchable = true
        mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
        binding.myFolderBackgroundView.visibility = View.VISIBLE    // 뒷배경 흐려지게
        mPopupWindow.setOnDismissListener(PopupWindowDismissListener())

        // RecyclerView 초기화
        iconRVAdapter = ChangeIconRVAdapter(iconList)
        popupView.findViewById<RecyclerView>(R.id.popup_window_change_icon_recycler_view).adapter = iconRVAdapter

        iconRVAdapter.setMyItemClickListener(object: ChangeIconRVAdapter.MyItemClickListener {
            // 아이콘을 하나 선택했을 경우
            override fun onIconClick(itemBinding: ItemIconBinding, itemPosition: Int) {
                val selectedIcon = iconList[itemPosition]
//                val lastIdx = folderList.size

                val iconBitmap = BitmapFactory.decodeResource(resources, selectedIcon.iconImage)
                val baos = ByteArrayOutputStream()
                iconBitmap.compress(Bitmap.CompressFormat.PNG, 70, baos)

                val iconBitmapAsByte = baos.toByteArray()
                val iconBitmapAsString = Base64.encodeToString(iconBitmapAsByte, Base64.DEFAULT)

                // Bitmap bigPictureBitmap  = BitmapFactory.decodeResource(context.getResources(), R.drawable.i_hero);
                // 선택한 아이콘과 전달받은 폴더 이름으로 폴더 하나 생성한 후 RoomDB에 적용
                val newFolder = Folder(userID, name, selectedIcon.iconImage)
                database = AppDatabase.getInstance(requireContext())!!
                database.folderDao().insert(newFolder)

                // 팝업 윈도우 종료
                mPopupWindow.dismiss()
            }
        })
    }

    // 디바이스 크기에 사이즈를 맞추기 위한 함수
    private fun WindowManager.currentWindowMetricsPointCompat(): Point {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
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
            binding.myFolderBackgroundView.visibility = View.INVISIBLE
        }
    }

}