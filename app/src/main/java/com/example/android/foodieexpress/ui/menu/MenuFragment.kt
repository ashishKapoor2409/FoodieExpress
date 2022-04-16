package com.example.android.foodieexpress.ui.menu

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.foodieexpress.Adapter.MyBestDealsAdapter
import com.example.android.foodieexpress.Adapter.MyCategoriesAdapter
import com.example.android.foodieexpress.Common.Common
import com.example.android.foodieexpress.Common.SpacesItemDecoration
import com.example.android.foodieexpress.EventBus.MenuItemBack
import com.example.android.foodieexpress.R
import dmax.dialog.SpotsDialog
import org.greenrobot.eventbus.EventBus

class MenuFragment : Fragment() {

    private lateinit var menuViewModel: MenuViewModel
    private lateinit var dialog: AlertDialog
    private lateinit var layoutAnimationController: LayoutAnimationController
    private var adapter:MyCategoriesAdapter? = null
    var recyclerView:RecyclerView? =null

    // This property is only valid between onCreateView and
    // onDestroyView.

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        menuViewModel =
            ViewModelProvider(this).get(MenuViewModel::class.java)

        val root: View = inflater.inflate(R.layout.fragment_category,container, false)
        initViews(root)
        recyclerView = root.findViewById(R.id.recycler_menu)
        menuViewModel.getMessageError().observe(this, Observer {
            Toast.makeText(context,it,Toast.LENGTH_SHORT).show()
        })
        menuViewModel.getCategoryList().observe(this, Observer {
            dialog.dismiss()
            adapter = MyCategoriesAdapter(context!!, it)
            recyclerView!!.adapter = adapter
            recyclerView!!.layoutAnimation = layoutAnimationController
        })
        return root
    }

    private fun initViews(root:View) {
        dialog = SpotsDialog.Builder().setContext(context)
            .setCancelable(false).build()
        dialog.show()
        recyclerView = root.findViewById(R.id.recycler_menu)
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_item_from_left)
        recyclerView!!.setHasFixedSize(true)
        val layoutManager = GridLayoutManager(context, 2)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.spanSizeLookup = object:GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if(adapter != null) {
                    when(adapter!!.getItemViewType(position)) {
                        Common.DEFAULT_COLUMN_COUNT->1
                        Common.FULL_WIDTH_COLUMN -> 2
                        else -> -1
                    }
                } else {
                    -1
                }
            }

        }
        recyclerView!!.layoutManager = layoutManager
        recyclerView!!.addItemDecoration(SpacesItemDecoration(0))
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onDestroy() {
        EventBus.getDefault().postSticky(MenuItemBack())
        super.onDestroy()
    }
}