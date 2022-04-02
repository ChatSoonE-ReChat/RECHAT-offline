package com.chatsoone.rechat.ui.main.folder

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Base64
import android.view.*
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.PopupWindow
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.chatsoone.rechat.R
import com.chatsoone.rechat.base.BaseFragment
import com.chatsoone.rechat.data.entity.Folder
import com.chatsoone.rechat.data.entity.Icon
import com.chatsoone.rechat.data.local.AppDatabase
import com.chatsoone.rechat.databinding.FragmentMyFolderBinding
import com.chatsoone.rechat.databinding.ItemIconBinding
import com.chatsoone.rechat.databinding.ItemMyFolderBinding
import com.chatsoone.rechat.ui.IconRVAdapter
import com.chatsoone.rechat.ui.chat.FolderContentActivity
import com.google.gson.Gson
import java.io.ByteArrayOutputStream

class MyFolderFragment : BaseFragment<FragmentMyFolderBinding>(FragmentMyFolderBinding::inflate) {
    private lateinit var database: AppDatabase
    private lateinit var folderRVAdapter: MyFolderRVAdapter
    private lateinit var iconRVAdapter: IconRVAdapter
    private lateinit var mPopupWindow: PopupWindow

    private var folderList = ArrayList<Folder>()
    private var iconList = ArrayList<Icon>()
    private val TAG = "FRAG/MYFOLDER"

    // Popupwindow와 RecyclerView 연결을 위해 선언
    private lateinit var itemBinding: ItemMyFolderBinding

    override fun afterOnCreateView() {
        database = AppDatabase.getInstance(requireContext())!!

        initIcon()
        initRecyclerView()
    }

    // 아이콘 받아오기
    private fun initIcon() {
        iconList = database.iconDao().getIconList() as ArrayList
    }

    // recycler view 초기화
    private fun initRecyclerView() {
        folderRVAdapter = MyFolderRVAdapter(this)
        binding.myFolderFolderListRecyclerView.adapter = folderRVAdapter

        // folder list live data (반영)
        database.folderDao().getFolderList(userID).observe(viewLifecycleOwner) {
            folderRVAdapter.addFolderList(it as ArrayList<Folder>)
        }

        // recycler view click listener 초기화
        folderRVAdapter.setMyItemClickListener(object : MyFolderRVAdapter.MyItemClickListener {

            // 폴더 이름 롱클릭 시 폴더 이름 변경
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onFolderNameLongClick(binding: ItemMyFolderBinding, folderIdx: Int) {
                itemBinding = binding
                changeFolderName(itemBinding, folderIdx)
            }

            // 폴더 아이콘 클릭 시 해당 폴더로 이동
            override fun onFolderClick(view: View, position: Int) {
                val selectedFolder = folderRVAdapter.getSelectedFolder(position)

                // folder 삽입 시 status 변경 (null X)
                val gson = Gson()
                val folderJson = gson.toJson(selectedFolder)

                // 폴더 정보 보내기
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
    fun changeFolderName(itemBinding: ItemMyFolderBinding, folderIdx: Int) {

        val size = activity?.windowManager?.currentWindowMetricsPointCompat()
        val width = ((size?.x ?: 0) * 0.8f).toInt()
        val height = ((size?.y ?: 0) * 0.4f).toInt()

        // 이름 바꾸기 팝업 윈도우
        val inflater =
            activity?.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_window_change_name, null)
        mPopupWindow = PopupWindow(popupView, width, WindowManager.LayoutParams.WRAP_CONTENT)

        // 애니메이션 설정 (-1: 설정 안 함, 0: 설정)
        mPopupWindow.animationStyle = 0
        mPopupWindow.animationStyle = R.style.Animation

        // 외부 영역 선택 시 팝업 윈도우 종료
        mPopupWindow.isFocusable = true

        mPopupWindow.isOutsideTouchable = true
        mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
        binding.myFolderBackgroundView.visibility = View.VISIBLE
        mPopupWindow.setOnDismissListener(PopupWindowDismissListener())

        // 기존 폴더 이름을 팝업 윈도우의 EditText에 넘겨준다.
        var text: String = itemBinding.itemMyFolderTv.text.toString()
        mPopupWindow.contentView.findViewById<EditText>(R.id.popup_window_change_name_et)
            .setText(text)

        // RoomDB
        database = AppDatabase.getInstance(requireContext())!!

        // 입력 완료했을 때 누르는 버튼
        mPopupWindow.contentView.findViewById<AppCompatButton>(R.id.popup_window_change_name_button)
            .setOnClickListener {
                text =
                    mPopupWindow.contentView.findViewById<EditText>(R.id.popup_window_change_name_et).text.toString()
                itemBinding.itemMyFolderTv.text = text
                database.folderDao().updateFolderName(folderIdx, text)

                // 팝업 윈도우 종료
                mPopupWindow.dismiss()

                // 뒷배경 원래대로
                binding.myFolderBackgroundView.visibility = View.INVISIBLE
            }
    }

    @SuppressLint("InflateParams", "ClickableViewAccessibility")
    fun changeIcon(itemBinding: ItemMyFolderBinding, position: Int, folderIdx: Int) {
        // 팝업 윈도우 사이즈를 잘못 맞추면 아이템들이 안 뜨므로 하드 코딩으로 사이즈 조정해주기
        // 아이콘 16개 (기본)
        val size = activity?.windowManager?.currentWindowMetricsPointCompat()
        val width = ((size?.x ?: 0) * 0.8f).toInt()
        val height = ((size?.y ?: 0) * 0.6f).toInt()

        // 아이콘 바꾸기 팝업 윈도우
        val inflater =
            activity?.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
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
        iconRVAdapter = IconRVAdapter(iconList)
        popupView.findViewById<RecyclerView>(R.id.popup_window_change_icon_recycler_view).adapter =
            iconRVAdapter

        iconRVAdapter.setMyItemClickListener(object : IconRVAdapter.MyItemClickListener {
            // 아이콘을 하나 선택했을 경우
            override fun onIconClick(
                itemIconBinding: ItemIconBinding,
                iconPosition: Int
            ) {
                // 선택한 아이콘으로 폴더 이미지 변경
                val selectedIcon = iconList[iconPosition]
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

        val inflater =
            activity?.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
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
        mPopupWindow.contentView.findViewById<AppCompatButton>(R.id.popup_window_set_name_button)
            .setOnClickListener {
                // 작성한 폴더 이름을 반영한 새폴더를 만들어준다.
                val name =
                    mPopupWindow.contentView.findViewById<EditText>(R.id.popup_window_set_name_et).text.toString()

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
        val inflater =
            activity?.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
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
        iconRVAdapter = IconRVAdapter(iconList)
        popupView.findViewById<RecyclerView>(R.id.popup_window_change_icon_recycler_view).adapter =
            iconRVAdapter

        iconRVAdapter.setMyItemClickListener(object : IconRVAdapter.MyItemClickListener {
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

    inner class PopupWindowDismissListener() : PopupWindow.OnDismissListener {
        override fun onDismiss() {
            binding.myFolderBackgroundView.visibility = View.INVISIBLE
        }
    }
}