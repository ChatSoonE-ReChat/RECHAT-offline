package com.chatsoone.rechat.ui.explain

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.chatsoone.rechat.R
import com.chatsoone.rechat.databinding.FragmentExplainBinding

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
        explainAdapter.addFragment(Explain1Fragment(R.drawable.expain_01))
        explainAdapter.addFragment(Explain1Fragment(R.drawable.expain_06))
        explainAdapter.addFragment(Explain1Fragment(R.drawable.expain_02))
        explainAdapter.addFragment(Explain1Fragment(R.drawable.expain_03))
        explainAdapter.addFragment(Explain1Fragment(R.drawable.expain_04))
        explainAdapter.addFragment(Explain1Fragment(R.drawable.expain_05))
        explainAdapter.addFragment(Explain1Fragment(R.drawable.expain_07))
        explainAdapter.addFragment(Explain1Fragment(R.drawable.explain_08))
        explainAdapter.addFragment(Explain1Fragment(R.drawable.expain_09))
//        explainAdapter.addFragment(Explain1Fragment(R.drawable.background))
//        explainAdapter.addFragment(Explain1Fragment(R.drawable.background2))
//        explainAdapter.addFragment(Explain1Fragment(R.drawable.chatsoon01))

        binding.explainVp.adapter = explainAdapter
        binding.explainVp.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.explainIndicator.setViewPager2(binding.explainVp)
    }
}