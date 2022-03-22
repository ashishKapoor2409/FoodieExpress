package com.example.android.foodieexpress.ui.slideshow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.android.foodieexpress.R
import com.example.android.foodieexpress.databinding.FragmentSlideshowBinding

class SlideshowFragment : Fragment() {

    private lateinit var slideshowViewModel: SlideshowViewModel


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        slideshowViewModel =
            ViewModelProvider(this).get(SlideshowViewModel::class.java)
        val root: View = inflater.inflate(R.layout.fragment_home, container, false)
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}