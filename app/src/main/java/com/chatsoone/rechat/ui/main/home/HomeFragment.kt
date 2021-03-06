package com.chatsoone.rechat.ui.main.home

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.util.SparseBooleanArray
import android.view.*
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chatsoone.rechat.ApplicationClass
import com.chatsoone.rechat.ApplicationClass.Companion.FRAG
import com.chatsoone.rechat.ApplicationClass.Companion.currentWindowMetricsPointCompat
import com.chatsoone.rechat.R
import com.chatsoone.rechat.data.entity.ChatList
import com.chatsoone.rechat.data.entity.Folder
import com.chatsoone.rechat.data.entity.Icon
import com.chatsoone.rechat.data.local.AppDatabase
import com.chatsoone.rechat.databinding.*
import com.chatsoone.rechat.ui.ChatViewModel
import com.chatsoone.rechat.ui.FolderListRVAdapter
import com.chatsoone.rechat.ui.ItemViewModel
import com.chatsoone.rechat.ui.chat.ChatActivity
import com.chatsoone.rechat.ui.main.MainActivity
import com.chatsoone.rechat.ui.pattern.CreatePatternActivity
import com.chatsoone.rechat.ui.pattern.InputPatternActivity
import com.chatsoone.rechat.util.getID
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.zip.Inflater

class HomeFragment : Fragment(), LifecycleObserver {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var database: AppDatabase
    private lateinit var homeRVAdapter: HomeRVAdapter
    private lateinit var folderListRVAdapter: FolderListRVAdapter
    private lateinit var mPopupWindow: PopupWindow

    private val userID = getID()
    private var iconList = ArrayList<Icon>()
    private var folderList = ArrayList<Folder>()
    private var chatList = ArrayList<ChatList>()
    private val chatViewModel by activityViewModels<ChatViewModel>()
    private val selectedItemViewModel by activityViewModels<ItemViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        database = AppDatabase.getInstance(requireContext())!!
        folderListRVAdapter = FolderListRVAdapter(requireContext())

        initRecyclerView()
        initClickListener()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatViewModel.setMode(mode = 0)

        // observe mode
        chatViewModel.mode.observe(viewLifecycleOwner, Observer {
            // ?????? ???????????? ??? ?????? ??????
            homeRVAdapter.setAllViewType(it)

            if (it == 0) {
                setDefaultMode()
            }
            else setChooseMode()

            Log.d(FRAG, "HOME/mode: $it")
        })

        // observe chat
        database.chatDao().getRecentChat(userID).observe(viewLifecycleOwner, Observer {
            Log.d(ApplicationClass.FRAG, "HOME/getRecentChat: $it")
            homeRVAdapter.addItem(it)
            chatList.clear()
            chatList.addAll(it)
            binding.homeRecyclerView.scrollToPosition(homeRVAdapter.itemCount - 1)
        })

        // live data ?????? (??????/????????? ??????)
        database.folderDao().getFolderList(userID).observe(viewLifecycleOwner) {
            folderList.addAll(it)
            folderListRVAdapter.addFolderList(folderList)
        }
    }

    // recycler view ?????????
    private fun initRecyclerView() {
        val linearLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true)
        linearLayoutManager.stackFromEnd = true
        binding.homeRecyclerView.layoutManager = linearLayoutManager

        homeRVAdapter = HomeRVAdapter(
            this.activity as MainActivity,
            object : HomeRVAdapter.MyItemClickListener {
                // ?????? ?????? (?????? ??? ChatActivity??? ??????)
                override fun onDefaultChatClick(view: View, position: Int, chat: ChatList) {
                    checkNewChat(position)

                    val spf =
                        requireContext().getSharedPreferences("all_chat", Context.MODE_PRIVATE)
                    val editor = spf.edit()
                    editor.putInt("all_chat", 1)
                    editor.apply()

                    // ChatActivity??? ????????? ??????
                    val intent = Intent(activity as MainActivity, ChatActivity::class.java)
                    intent.putExtra("chatListJson", chat)
                    startActivity(intent)
                }

                // ?????? ????????? ??????????????? (default?????? ????????? ?????? ??? ?????? ????????? ??????)
                override fun onProfileClick(
                    itemBinding: ItemChatListDefaultBinding,
                    position: Int
                ) {
                    chatViewModel.setMode(1)
//                    itemBinding.itemChatListProfileIv.setImageResource(R.drawable.ic_check_circle)
//                    homeRVAdapter.setChecked(position)
//                    homeRVAdapter.setDefaultChecked(itemBinding, position)
                    selectedItemViewModel.setSelectedItemList(homeRVAdapter.getSelectedItem())
                }

                // ?????? ?????? (?????? ??? ????????? ?????? & ????????? ??? ???????????? ????????????)
                override fun onChooseChatClick(
                    itemBinding: ItemChatListChooseBinding,
                    position: Int
                ) {
//                    itemBinding.itemChatListProfileIv.setImageResource(R.drawable.ic_check_circle)
//                    homeRVAdapter.setChecked(position)
//                    homeRVAdapter.setChooseChecked(itemBinding, position)
                    selectedItemViewModel.setSelectedItemList(homeRVAdapter.getSelectedItem())
                    if(homeRVAdapter.getSelectedItem().size==0)
                        chatViewModel.setMode(0)

                }
            })

        binding.homeRecyclerView.adapter = homeRVAdapter
    }

    // ?????? ??? ????????? ???????????? ???
    private fun checkNewChat(position: Int) {
        database.chatDao().updateIsNew(chatList[position].chatIdx, 0)
        database.chatListDao().updateIsNew(chatList[position].chatIdx, 0)
    }

    // ?????? ?????? ??????
    private fun setDefaultMode() {

        homeRVAdapter.clearSelectedItemList()
        selectedItemViewModel.setSelectedItemList(homeRVAdapter.getSelectedItem())

        binding.homeTitleTv.visibility=View.VISIBLE
        binding.homeSettingIv.visibility=View.GONE
        binding.homeLayout.setBackgroundColor(Color.parseColor("#B9E3FB"))
    }

    // ?????? ?????? ??????
    private fun setChooseMode() {
        selectedItemViewModel.setSelectedItemList(homeRVAdapter.getSelectedItem())

        binding.homeTitleTv.visibility = View.VISIBLE // ???????????? ?????? ??????
        binding.homeSettingIv.visibility = View.VISIBLE
        binding.homeLayout.setBackgroundColor(Color.parseColor("#B9E3FB"))
    }

    // click listener ?????????
    private fun initClickListener() {
        Log.d(FRAG, "HOME/initClickListener")

        // ?????? ?????? ?????? ?????? ???????????? ??? ?????? ????????? ??????
        binding.homeSettingIv.setOnClickListener {
            showDialog()
        }
    }

    // Bottom dialog ????????????
    private fun showDialog(){
        val dialog=Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.fragment_bottom_dialog)

        // ?????? ?????????
        dialog.findViewById<TextView>(R.id.bottom_dialog_delete_tv).setOnClickListener {
            homeRVAdapter.removeSelectedItemList()
            chatViewModel.setMode(0)    // ?????? ?????? setDefaultMode() ??????
            Toast.makeText(requireContext(), "?????????????????????.", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        // ?????? ?????????
        dialog.findViewById<TextView>(R.id.bottom_dialog_block_tv).setOnClickListener {
            val selectedChatList = homeRVAdapter.getSelectedItem()
            for (i in selectedChatList) {
                if (i.groupName != "null") i.groupName?.let { it1 ->
                    database.chatDao().blockOrgChat(userID, it1)
                }
                else database.chatDao().blockOneChat(userID, i.groupName!!)
            }

            homeRVAdapter.blockSelectedItemList()
            chatViewModel.setMode(0)    // ?????? ?????? setDefaultMode() ??????
            Toast.makeText(requireContext(), "?????????????????????.", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        // ?????? ?????????
        dialog.findViewById<TextView>(R.id.bottom_dialog_cancel_tv).setOnClickListener {
            chatViewModel.setMode(0)    // ?????? ?????? setDefaultMode() ??????
            dialog.dismiss()
        }
        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations=R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)

    }
}