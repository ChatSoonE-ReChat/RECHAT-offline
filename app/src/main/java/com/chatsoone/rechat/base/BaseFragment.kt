package com.chatsoone.rechat.base

import android.graphics.Insets
import android.graphics.Point
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewbinding.ViewBinding
import com.chatsoone.rechat.ui.ChatViewModel

abstract class BaseFragment<VB : ViewBinding>(private val inflate: (LayoutInflater) -> VB) :
    Fragment() {
    private var _binding: VB? = null
    val binding get() = _binding!!
    var userId = com.chatsoone.rechat.utils.getId()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflate.invoke(inflater)
        afterOnCreateView()
        return binding.root
    }

    protected abstract fun afterOnCreateView()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Toast message
    fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    // 디바이스 크기에 따라 사이즈 변경
    fun WindowManager.currentWindowMetricsPointCompat(): Point {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val windowInsets = currentWindowMetrics.windowInsets
            var insets: Insets = windowInsets.getInsets(WindowInsets.Type.navigationBars())

            windowInsets.displayCutout?.run {
                insets = Insets.max(insets, Insets.of(safeInsetLeft, safeInsetTop, safeInsetRight, safeInsetBottom))
            }

            val insetsWidth = insets.right + insets.left
            val insetsHeight = insets.top + insets.bottom
            Point(currentWindowMetrics.bounds.width() - insetsWidth, currentWindowMetrics.bounds.height() - insetsHeight)
        } else {
            Point().apply {
                defaultDisplay.getSize(this)
            }
        }
    }
}