package com.chatsoone.rechat.ui.explain

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.chatsoone.rechat.databinding.FragmentExplain1Binding

class Explain1Fragment(val imgRes: Int): Fragment() {
    lateinit var binding: FragmentExplain1Binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentExplain1Binding.inflate(layoutInflater)
        binding.explain1Iv.setImageResource(imgRes)
        return binding.root
    }
}