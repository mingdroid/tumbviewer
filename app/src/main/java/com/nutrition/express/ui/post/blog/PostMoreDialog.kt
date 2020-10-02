package com.nutrition.express.ui.post.blog

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nutrition.express.databinding.BottomSheetVideoListBinding
import com.nutrition.express.util.copy2Clipboard

class PostMoreDialog : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = BottomSheetVideoListBinding.inflate(inflater, container, false)
        val url = arguments?.getString("video_url") ?: ""
        binding.itemLink.text = url
        binding.itemLink.setOnClickListener {
            if (url.isNotEmpty()) {
                context?.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
            dismiss()
        }
        binding.itemCopy.setOnClickListener {
            context?.let { copy2Clipboard(it, url) }
            dismiss()
        }
        return binding.root
    }
}