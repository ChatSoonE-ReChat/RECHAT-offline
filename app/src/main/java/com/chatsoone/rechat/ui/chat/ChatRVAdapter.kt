package com.chatsoone.rechat.ui.chat

import android.graphics.Point

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import android.util.SparseBooleanArray
import android.view.*
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.chatsoone.rechat.ApplicationClass.Companion.loadBitmap
import com.chatsoone.rechat.data.entity.*
import com.chatsoone.rechat.data.local.AppDatabase
import com.chatsoone.rechat.databinding.ItemChatChooseBinding
import com.chatsoone.rechat.databinding.ItemChatDefaultBinding
import com.chatsoone.rechat.util.DiffUtilCallback
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.collections.ArrayList

class ChatRVAdapter(
    private val mContext: ChatActivity,
    private val size: Point,
    private val mItemClickListener: MyItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var chatList = ArrayList<ChatList>()
    var selectedItemList: SparseBooleanArray = SparseBooleanArray(0)

    // 클릭 인터페이스
    interface MyItemClickListener {
        fun onRemoveChat(chatIdx: Int)
        fun onChooseChatClick(view: View, position: Int)
//        fun onDefaultChatLongClick(itemBinding: ItemChatBinding, chatIdx: Int)
    }

    // 뷰홀더를 생성해줘야 할 때 호출되는 함수로, 아이템 뷰 객체를 만들어서 뷰 홀더에 던져준다.
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ChatViewType.CHOOSE -> {
                ChooseViewHolder(
                    ItemChatChooseBinding.inflate(
                        LayoutInflater.from(viewGroup.context), viewGroup, false
                    ),
                    mItemClickListener = mItemClickListener
                )
            }
            else -> {
                DefaultViewHolder(
                    ItemChatDefaultBinding.inflate(
                        LayoutInflater.from(viewGroup.context), viewGroup, false
                    )
                )
            }
        }
    }

    // 뷰홀더에 데이터 바인딩을 해줘야 할 때마다 호출되는 함수
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (chatList[position].viewType) {
            ChatListViewType.CHOOSE -> {
                (holder as ChooseViewHolder).bind(chatList[position])
                holder.itemView.isSelected = isItemSelected(position)
            }
            else -> {
                (holder as DefaultViewHolder).bind(chatList[position])
                holder.itemView.isSelected = isItemSelected(position)
            }
        }
    }

    override fun getItemCount(): Int = chatList.size

    @SuppressLint("NotifyDataSetChanged")
    fun removeChat() {
        val selectedChatList = ArrayList<ChatList>()
        for (i in 0 until chatList.size) {
            if (selectedItemList[i]) {
                selectedChatList.add(chatList[i])
            }
        }
        updateList(selectedChatList)
    }

    private fun updateList(selectedChatList: List<ChatList>?) {
        selectedChatList?.let {
            val diffCallback = DiffUtilCallback(chatList, selectedChatList)
            val diffResult = DiffUtil.calculateDiff(diffCallback)

            this.chatList.run {
                clear()
                addAll(selectedChatList)
                diffResult.dispatchUpdatesTo(this@ChatRVAdapter)
            }
        }
    }

    // 데이터 추가
    @SuppressLint("NotifyDataSetChanged")
    fun addItem(chat: List<ChatList>) {
        chatList.clear()
        chatList.addAll(chat as ArrayList)
        notifyDataSetChanged()
    }

    // selectedItemList 삭제
    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("NotifyDataSetChanged")
    fun removeSelectedItemList(): ChatList? {
        val database = AppDatabase.getInstance(mContext)!!
        val selectedList = chatList.filter { chatlist -> chatlist.isChecked as Boolean }

        chatList = chatList.filter { chatlist -> !chatlist.isChecked } as ArrayList<ChatList>

        // DB 업데이트
        for (i in selectedList) {
            database.chatDao().deleteByChatIdx(i.chatIdx)
            Log.d("chatDataCheckLiveData", "deletedItem: ${i.chatIdx}")
        }

        notifyDataSetChanged()

        return if (chatList.isNotEmpty()) chatList[0]
        else null
    }

    // will toggle the selection of items
    private fun toggleItemSelected(view: View?, position: Int) {
        if (selectedItemList.get(position, false)) {
            selectedItemList.delete(position)
        } else {
            selectedItemList.put(position, true)
        }
        notifyItemChanged(position)
    }

    // selectedItemList 초기화
    @SuppressLint("NotifyDataSetChanged")
    fun clearSelectedItemList() {
        selectedItemList.clear()
        notifyDataSetChanged()
    }

    // 선택된 chat index 가져오기
    fun getSelectedItemList(): List<Int> {
        val chatIdxList = ArrayList<Int>()
        val selectedList = chatList.filter { chatlist -> chatlist.isChecked as Boolean }

        for (i in selectedList) chatIdxList.add(i.chatIdx)

        return chatIdxList
    }

    // 뷰타입 설정
    @SuppressLint("NotifyDataSetChanged")
    fun setViewType(currentMode: Int) {
        val newChatList = ArrayList<ChatList>()
        for (i in 0 until chatList.size) {
            if (currentMode == 0) {
                // 일반 모드
                chatList[i].viewType = ChatListViewType.DEFAULT
            } else {
                // 선택 모드
                chatList[i].viewType = ChatListViewType.CHOOSE
            }
            newChatList.add(chatList[i])
        }
        this.chatList = newChatList
        notifyDataSetChanged()
    }

    fun setChecked(position: Int) {
        chatList[position].isChecked = !chatList[position].isChecked
        notifyItemChanged(position)
    }

    // 아이템뷰가 선택되었는지를 알려주는 함수
    private fun isItemSelected(position: Int): Boolean {
        return selectedItemList.get(position, false)
    }

    // 직접 설정한 뷰타입으로 설정되게 만든다.
    override fun getItemViewType(position: Int): Int = chatList[position].viewType

    // 디폴트 뷰홀더
    inner class DefaultViewHolder(private val binding: ItemChatDefaultBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SimpleDateFormat")
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(chat: ChatList) {
            binding.itemChatDefaultMessageTv.maxWidth = (size.x * 0.6f).toInt()
            binding.itemChatDefaultMessageTv.minHeight = (size.y * 0.05f).toInt()

            binding.itemChatDefaultNameTv.text = chat.nickName
            binding.itemChatDefaultMessageTv.text = chat.message
            binding.itemChatDefaultDateTimeTv.text = convertDateAtDefault(binding, chat.postTime)
            binding.itemChatDefaultProfileIv.setImageBitmap(loadBitmap(chat.profileImg!!, mContext))

            if (bindingAdapterPosition == chatList.size - 1) {
                binding.itemChatDefaultNewDateTimeLayout.visibility = View.VISIBLE
                binding.itemChatDefaultNewDateTimeTv.text = setNewDate(chat.postTime)
            } else if (bindingAdapterPosition != (chatList.size - 1) && isNextDay(
                    chat.postTime,
                    bindingAdapterPosition
                )
            ) {
                // 다음 날로 날짜가 바뀐 경우
                // 혹은 날짜가 1일 이상 차이날 때
                binding.itemChatDefaultNewDateTimeLayout.visibility = View.VISIBLE
                binding.itemChatDefaultNewDateTimeTv.text = setNewDate(chat.postTime)
            } else {
                // 날짜가 바뀐 게 아닌 경우
                binding.itemChatDefaultNewDateTimeLayout.visibility = View.GONE
            }
        }
    }

    // 선택 모드 뷰홀더
    inner class ChooseViewHolder(
        private val binding: ItemChatChooseBinding,
        private val mItemClickListener: MyItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.itemChatChooseLayout.setOnClickListener {
                toggleItemSelected(itemView, position = bindingAdapterPosition)
                mItemClickListener.onChooseChatClick(itemView, position = bindingAdapterPosition)
            }
        }

        @SuppressLint("SimpleDateFormat")
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(chat: ChatList) {
            binding.itemChatChooseMessageTv.maxWidth = (size.x * 0.6f).toInt()
            binding.itemChatChooseMessageTv.minHeight = (size.y * 0.05f).toInt()

            binding.itemChatChooseNameTv.text = chat.nickName
            binding.itemChatChooseMessageTv.text = chat.message
            binding.itemChatChooseDateTimeTv.text = convertDateAtChoose(binding, chat.postTime)
            binding.itemChatChooseProfileIv.setImageBitmap(loadBitmap(chat.profileImg!!, mContext))

            if (bindingAdapterPosition == chatList.size - 1) {
                binding.itemChatChooseNewDateTimeLayout.visibility = View.VISIBLE
                binding.itemChatChooseNewDateTimeTv.text = setNewDate(chat.postTime)
            } else if (bindingAdapterPosition != (chatList.size - 1) && isNextDay(
                    chat.postTime,
                    bindingAdapterPosition
                )
            ) {
                // 다음 날로 날짜가 바뀐 경우
                // 혹은 날짜가 1일 이상 차이날 때
                binding.itemChatChooseNewDateTimeLayout.visibility = View.VISIBLE
                binding.itemChatChooseNewDateTimeTv.text = setNewDate(chat.postTime)
            } else {
                // 날짜가 바뀐 게 아닌 경우
                binding.itemChatChooseNewDateTimeLayout.visibility = View.GONE
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun convertDateAtDefault(binding: ItemChatDefaultBinding, date: String): String {
        val str: String
        val today = Date()

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val dateAsDate = simpleDateFormat.parse(date)!!
//        val simpleDateFormat2 = SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN)
//        val dateAsString = simpleDateFormat2.format(dateAsDate!!)

        // 오늘이 아니라면 날짜만
        str = if (dateAsDate.year == today.year && dateAsDate.month == today.month && dateAsDate.date == today.date) {
            val time = SimpleDateFormat("a h:mm")
            time.format(dateAsDate).toString()
        } else {
            // simpleDateFormat은 thread에 안전하지 않습니다.
            // DateTimeFormatter을 사용합시다. 아! Date를 LocalDate로도 바꿔야합니다!
            // val time_formatter=DateTimeFormatter.ofPattern("MM월 dd일")
            // date.format(time_formatter)
            val time = SimpleDateFormat("a h:mm")
            time.format(dateAsDate).toString()
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
    @RequiresApi(Build.VERSION_CODES.O)
    private fun convertDateAtChoose(binding: ItemChatChooseBinding, date: String): String {
        val str: String
        val today = Date()

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val dateAsDate = simpleDateFormat.parse(date)!!
//        val simpleDateFormat2 = SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN)
//        val dateAsString = simpleDateFormat2.format(dateAsDate!!)

        // 오늘이 아니라면 날짜만
        str = if (dateAsDate.year == today.year && dateAsDate.month == today.month && dateAsDate.date == today.date) {
            val time = SimpleDateFormat("a h:mm")
            time.format(dateAsDate).toString()
        } else {
            // simpleDateFormat은 thread에 안전하지 않습니다.
            // DateTimeFormatter을 사용합시다. 아! Date를 LocalDate로도 바꿔야합니다!
            // val time_formatter=DateTimeFormatter.ofPattern("MM월 dd일")
            // date.format(time_formatter)
            val time = SimpleDateFormat("a h:mm")
            time.format(dateAsDate).toString()
        }
        return str
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
        val testLocalDate = LocalDate.of(2022, 2, 13)   // 테스트

        val differenceDate = previousLocalDate.until(currentLocalDate, ChronoUnit.DAYS)
        period = differenceDate.toInt()

        return period >= 1
    }
}