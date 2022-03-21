package com.example.android.foodieexpress.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asksira.loopingviewpager.LoopingViewPager
import com.example.android.foodieexpress.Adapter.MyBestDealsAdapter
//import butterknife.BindView
//import butterknife.ButterKnife
//import butterknife.Unbinder
import com.example.android.foodieexpress.Adapter.MyPopularCategoriesAdapter
import com.example.android.foodieexpress.R
import com.example.android.foodieexpress.databinding.FragmentHomeBinding

class  HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    var recyclerView:RecyclerView? =null
    var viewPager:LoopingViewPager? = null

    var layoutAnimationController : LayoutAnimationController? = null

//    var unbinder:Unbinder? = null



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root = inflater.inflate(R.layout.fragment_home, container, false)


//        unbinder = ButterKnife.bind(this, root)
        initView(root)
        //Bind data

        homeViewModel.popularList.observe(viewLifecycleOwner, Observer {
            val listData = it
            val adapter = MyPopularCategoriesAdapter(context!!,listData)
            recyclerView!!.adapter = adapter
            recyclerView!!.layoutAnimation = layoutAnimationController
        })

        homeViewModel.bestDealList.observe(this, Observer {
            val adapter = MyBestDealsAdapter(context!!, it, false)
            viewPager!!.adapter = adapter
        })
        return root
    }

    private fun initView(root:View) {
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_item_from_left)
        viewPager = root.findViewById(R.id.viewpager) as LoopingViewPager
        recyclerView = root.findViewById(R.id.recycler_popular) as RecyclerView
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
    }

    override fun onResume() {
        super.onResume()
        viewPager!!.resumeAutoScroll()
    }

    override fun onPause() {
        viewPager!!.pauseAutoScroll()
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}