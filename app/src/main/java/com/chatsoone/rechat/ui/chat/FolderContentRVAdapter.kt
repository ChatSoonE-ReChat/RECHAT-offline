package com.chatsoone.rechat.ui.chat

import android.annotation.SuppressLint
import android.graphics.Insets
import android.graphics.Point
import android.os.Build
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.chatsoone.rechat.ApplicationClass.Companion.loadBitmap
import com.chatsoone.rechat.R
import com.chatsoone.rechat.data.remote.FolderContent
import com.chatsoone.rechat.databinding.ItemFolderContentBinding
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.collections.ArrayList

class FolderContentRVAdapter(
    private val mContext: FolderContentActivity,
    private val size: Point,
    private val mItemClickListener: MyClickListener
) : RecyclerView.Adapter<FolderContentRVAdapter.ViewHolder>() {
    private lateinit var popupMenu: PopupMenu

    var chatList = ArrayList<FolderContent>()
    private val tag = "RV/FOLDER_CONTENT"

    // 클릭 인터페이스
    interface MyClickListener {
        fun onRemoveChat(chatIdx: Int)
        fun onChatLongClick(popupMenu: PopupMenu)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemFolderContentBinding = ItemFolderContentBinding.inflate(
            LayoutInflater.from(viewGroup.context), viewGroup, false
        )
        return ViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(chatList[position])
    }

    override fun getItemCount(): Int = chatList.size

    private fun removeChat(position: Int) {
        chatList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount);
    }

    //AddData
    @SuppressLint("NotifyDataSetChanged")
    fun addItem(chat: List<FolderContent>) {
        chatList.clear()
        chatList.addAll(chat as ArrayList)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemFolderContentBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.itemFolderContentLayout.setOnLongClickListener {
                popupMenu = PopupMenu(
                    mContext,
                    binding.itemFolderContentMessageTv,
                    Gravity.START,
                    0,
                    R.style.MyFolderOptionPopupMenuTheme
                )
                popupMenu.menuInflater.inflate(R.menu.menu_chat_option, popupMenu.menu)
                popupMenu.setOnMenuItemClickListener { item ->
                    when (item?.itemId) {
                        R.id.popup_chat_option_menu_delete -> {
                            mItemClickListener.onRemoveChat(chatList[bindingAdapterPosition].chatIdx)
                            removeChat(bindingAdapterPosition)
                        }
                    }
                    false
                }
                mItemClickListener.onChatLongClick(popupMenu)
                return@setOnLongClickListener false
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(chat: FolderContent) {
            Log.d(tag, "chat.nickName: ${chat.nickname}")
            Log.d(tag, "chat.message: ${chat.message}")
            Log.d(tag, "chat.postTime: ${chat.postTime}")

//            val chatList = AppDatabase.getInstance(mContext)!!.chatListDao().getOneChatList(chat.chatIdx)

            binding.itemFolderContentMessageTv.maxWidth = (size.x * 0.6f).toInt()
            binding.itemFolderContentMessageTv.minHeight = (size.y * 0.05f).toInt()

            binding.itemFolderContentNameTv.text = chat.nickname
            binding.itemFolderContentMessageTv.text = chat.message
            if (chat.profileImgUrl == null || chat.profileImgUrl == "null") binding.itemFolderContentProfileIv.setImageResource(
                R.drawable.ic_profile_default
            )
            else binding.itemFolderContentProfileIv.setImageBitmap(
                loadBitmap(
                    chat.profileImgUrl!!,
                    mContext
                )
            )
            binding.itemFolderContentDateTimeTv.text = convertDate(binding, chat.postTime)

            if (bindingAdapterPosition == chatList.size - 1) {
                binding.itemFolderContentNewDateTimeLayout.visibility = View.VISIBLE
                binding.itemFolderContentNewDateTimeTv.text = setNewDate(chat.postTime)
            } else if (bindingAdapterPosition != (chatList.size - 1) && isNextDay(
                    chat.postTime,
                    bindingAdapterPosition
                )
            ) {
                // 다음 날로 날짜가 바뀐 경우
                // 혹은 날짜가 1일 이상 차이날 때
                binding.itemFolderContentNewDateTimeLayout.visibility = View.VISIBLE
                binding.itemFolderContentNewDateTimeTv.text = setNewDate(chat.postTime)
            } else {
                // 날짜가 바뀐 게 아닌 경우
                binding.itemFolderContentNewDateTimeLayout.visibility = View.GONE
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun convertDate(binding: ItemFolderContentBinding, date: String): String {
        val str: String
        val today = Date()

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val dateAsDate = simpleDateFormat.parse(date)!!
//        val simpleDateFormat2 = SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN)
//        val dateAsString = simpleDateFormat2.format(dateAsDate!!)

        // 오늘이 아니라면 날짜만
        if (dateAsDate.year == today.year && dateAsDate.month == today.month && dateAsDate.date == today.date) {
            val time = SimpleDateFormat("a h:mm")
            str = time.format(dateAsDate).toString()
        } else {
            // simpleDateFormat은 thread에 안전하지 않습니다.
            // DateTimeFormatter을 사용합시다. 아! Date를 LocalDate로도 바꿔야합니다!
            // val time_formatter=DateTimeFormatter.ofPattern("MM월 dd일")
            // date.format(time_formatter)
            val time = SimpleDateFormat("a h:mm")
            str = time.format(dateAsDate).toString()
        }
        return str
    }

    @SuppressLint("SimpleDateFormat")
    private fun setNewDate(date: String): String {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val dateAsDate = simpleDateFormat.parse(date)!!
        val newDate = SimpleDateFormat("yyyy년 M월 d일 EE")
        return newDate.format(dateAsDate).toString()
    }

    @SuppressLint("SimpleDateFormat")
    private fun isNextDay(date: String, position: Int): Boolean {
        val period: Int

        val simpleDateFormat1 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val currentDateAsDate = simpleDateFormat1.parse(date)

        val previousDateAsDate = simpleDateFormat1.parse(chatList[position + 1].postTime)

        val previousLocalDate =
            previousDateAsDate!!.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val currentLocalDate =
            currentDateAsDate!!.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val testLocalDate = LocalDate.of(2022, 2, 13)

        Log.d(tag, "isNextDay()/previousDate: $previousLocalDate")
        Log.d(tag, "isNextDay()/currentDate: $currentLocalDate")

        val differenceDate = previousLocalDate.until(currentLocalDate, ChronoUnit.DAYS)
        period = differenceDate.toInt()

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val previousLocalDate = chatList[position - 1].postTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
//            val currentLocalDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
//
//            Log.d(tag, "isNextDay()/previousDate: $previousLocalDate")
//            Log.d(tag, "isNextDay()/currentDate: $currentLocalDate")
//
//            val differenceDate = previousLocalDate.until(currentLocalDate, ChronoUnit.DAYS) + 1
//            period = differenceDate.toInt()
//        } else {
//            val previousLocalDate = org.joda.time.LocalDate.
//            val currentLocalDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
//        }

        Log.d(tag, "isNextDay()/period: $period")
        return period >= 1
    }
}