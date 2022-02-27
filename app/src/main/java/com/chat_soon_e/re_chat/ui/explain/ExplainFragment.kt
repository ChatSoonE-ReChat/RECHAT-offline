package com.chat_soon_e.re_chat.ui.explain

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.chat_soon_e.re_chat.R
import com.chat_soon_e.re_chat.databinding.FragmentExplainBinding

class ExplainFragment: Fragment(){
    lateinit var binding: FragmentExplainBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentExplainBinding.inflate(inflater, container, false)
        initViewPager()
        return binding.root
    }

    private fun initViewPager() {
        val explainAdapter=ExplainViewpagerAdapter(this)

        // addFragment. 설명 이미지 추가하기
        explainAdapter.addFragment(Explain1Fragment(R.drawable.expain_one))
        explainAdapter.addFragment(Explain1Fragment(R.drawable.expain_six))
        explainAdapter.addFragment(Explain1Fragment(R.drawable.expain_two))
        explainAdapter.addFragment(Explain1Fragment(R.drawable.expain_three))
        explainAdapter.addFragment(Explain1Fragment(R.drawable.expain_four))
        explainAdapter.addFragment(Explain1Fragment(R.drawable.expain_five))
        explainAdapter.addFragment(Explain1Fragment(R.drawable.expain_seven))
        explainAdapter.addFragment(Explain1Fragment(R.drawable.expain_eight))
        explainAdapter.addFragment(Explain1Fragment(R.drawable.expain_nine))
//        explainAdapter.addFragment(Explain1Fragment(R.drawable.background))
//        explainAdapter.addFragment(Explain1Fragment(R.drawable.background2))
//        explainAdapter.addFragment(Explain1Fragment(R.drawable.chatsoon01))

        binding.explainVp.adapter = explainAdapter
        binding.explainVp.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.explainIndicator.setViewPager2(binding.explainVp)
    }
}