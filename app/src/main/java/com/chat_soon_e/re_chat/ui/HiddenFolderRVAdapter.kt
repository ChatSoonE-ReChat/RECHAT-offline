package com.chat_soon_e.re_chat.ui

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.chat_soon_e.re_chat.R
import com.chat_soon_e.re_chat.data.entities.Folder
import com.chat_soon_e.re_chat.databinding.ItemHiddenFolderBinding

class HiddenFolderRVAdapter(private val fragment: MyHiddenFolderFragment): RecyclerView.Adapter<HiddenFolderRVAdapter.ViewHolder>() {
    private val hiddenFolderList = ArrayList<Folder>()
    private val tag = "RV/HIDDEN-FOLDER"

    private lateinit var popupMenu: PopupMenu
    private lateinit var itemHiddenFolderBinding: ItemHiddenFolderBinding
    private lateinit var mItemClickListener: MyItemClickListener

    // 클릭 인터페이스
    interface MyItemClickListener {
        fun onShowFolder(folderIdx: Int)
        fun onRemoveFolder(folderIdx: Int)
        fun onFolderClick(view: View, position: Int)
        fun onFolderLongClick(popupMenu: PopupMenu)
        fun onFolderNameLongClick(itemHiddenFolderBinding: ItemHiddenFolderBinding, folderIdx: Int)
    }

    // 리스너 객체를 외부에서 전달받는 함수
    fun setMyItemClickListener(itemClickListener: MyItemClickListener) {
        mItemClickListener = itemClickListener
    }

    // 뷰홀더를 생성해줘야 할 때 호출되는 함수
    // 아이템 뷰 객체를 만들어서 뷰홀더에 던져준다.
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): HiddenFolderRVAdapter.ViewHolder {
        val itemHiddenFolderBinding: ItemHiddenFolderBinding =
            ItemHiddenFolderBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(itemHiddenFolderBinding)
    }

    // 뷰홀더에 데이터 바인딩을 해줘야 할 때마다 호출되는 함수
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(hiddenFolderList[position])
        itemHiddenFolderBinding = holder.itemHiddenFolderBinding

        // 폴더 이름 롱클릭 시 이름 변경할 수 있도록
        itemHiddenFolderBinding.itemHiddenFolderTv.setOnLongClickListener {
            mItemClickListener.onFolderNameLongClick(itemHiddenFolderBinding, position)
            return@setOnLongClickListener false
        }

        // 폴더 클릭 시 해당 폴더로 이동할 수 있도록
        itemHiddenFolderBinding.itemHiddenFolderIv.setOnClickListener {
            mItemClickListener.onFolderClick(itemHiddenFolderBinding.itemHiddenFolderIv, position)
        }

        // 폴더 아이템 롱클릭 시 팝업 메뉴 뜨도록
        itemHiddenFolderBinding.itemHiddenFolderIv.setOnLongClickListener {
            // 팝업 메뉴: 이름 바꾸기, 아이콘 바꾸기, 삭제하기, 숨기기
            popupMenu = PopupMenu(fragment.requireContext(), holder.itemView, Gravity.START, 0, R.style.MyFolderOptionPopupMenuTheme)
            popupMenu.menuInflater.inflate(R.menu.popup_hidden_folder_option_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item?.itemId) {
                    R.id.popup_folder_edit_menu_1 -> {
                        // 이름 바꾸기
                        fragment.changeFolderName(itemHiddenFolderBinding, hiddenFolderList[position].idx)
                    }

                    R.id.popup_folder_edit_menu_2 -> {
                        // 아이콘 바꾸기
                        fragment.changeIcon(itemHiddenFolderBinding, position, hiddenFolderList)
                    }

                    R.id.popup_folder_edit_menu_3 -> {
                        // 삭제하기
                        mItemClickListener.onRemoveFolder(hiddenFolderList[position].idx)
                        removeFolder(position)
                    }

                    R.id.popup_folder_edit_menu_4 -> {
                        // 내폴더로 보내기 (숨김 해제)
                        mItemClickListener.onShowFolder(hiddenFolderList[position].idx)
                        removeFolder(position)
                    }
                }
                false
            }

            mItemClickListener.onFolderLongClick(popupMenu)
            return@setOnLongClickListener false
        }
    }

    // 데이터셋의 크기 반환
    override fun getItemCount(): Int = hiddenFolderList.size

    // folder list 추가 및 연결
    @SuppressLint("NotifyDataSetChanged")
    fun addFolderList(folderList: ArrayList<Folder>) {
        this.hiddenFolderList.clear()
        this.hiddenFolderList.addAll(folderList)
        notifyDataSetChanged()
    }

    // 폴더 삭제
    private fun removeFolder(position: Int) {
        hiddenFolderList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount)
    }

    // 선택된 폴더 객체 반환
    fun getSelectedFolder(position: Int): Folder {
        return hiddenFolderList[position]
    }

    // 뷰홀더
    inner class ViewHolder(val itemHiddenFolderBinding: ItemHiddenFolderBinding): RecyclerView.ViewHolder(itemHiddenFolderBinding.root) {
        fun bind(folder: Folder) {
            itemHiddenFolderBinding.itemHiddenFolderTv.text = folder.folderName
            itemHiddenFolderBinding.itemHiddenFolderIv.setImageResource(folder.folderImg!!)
        }
    }
}