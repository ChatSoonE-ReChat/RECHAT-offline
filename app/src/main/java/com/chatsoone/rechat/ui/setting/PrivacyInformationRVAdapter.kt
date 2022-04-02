package com.chatsoone.rechat.ui.setting

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chatsoone.rechat.data.entity.PrivacyInformation
import com.chatsoone.rechat.databinding.ItemPrivacyInformationBinding

class PrivacyInformationRVAdapter(private val privacyInformationList: ArrayList<PrivacyInformation>): RecyclerView.Adapter<PrivacyInformationRVAdapter.ViewHolder>() {
    private lateinit var binding: ItemPrivacyInformationBinding

    private val tag = "RV/PRIVACY-INFORMATION"

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemPrivacyInformationBinding = ItemPrivacyInformationBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(privacyInformationList[position])
        binding = holder.binding
    }

    override fun getItemCount(): Int = privacyInformationList.size

    inner class ViewHolder(val binding: ItemPrivacyInformationBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(privacyInformation: PrivacyInformation) {
            Log.d(tag, "ViewHolder/bind()/title: ${binding.itemPrivacyInformationTitleTv.text}")

            if(privacyInformation.title != null) binding.itemPrivacyInformationTitleTv.text = privacyInformation.title
            else binding.itemPrivacyInformationTitleTv.text = ""
            binding.itemPrivacyInformationContentTv.text = privacyInformation.content
        }
    }
}