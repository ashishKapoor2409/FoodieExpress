package com.example.android.foodieexpress.ui.menu

import android.app.AlertDialog
import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
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
import com.example.android.foodieexpress.Model.CategoryModel
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

        setHasOptionsMenu(true)
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu,menu)
        val menuItem = menu.findItem(R.id.action_search)
        val searchManager = activity!!.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menuItem.actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(activity!!.componentName))

        searchView.setOnQueryTextListener(object :SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                startSearch(p0!!)
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                return false
            }

        })

        val closeButton = searchView.findViewById<View>(R.id.search_close_btn) as ImageView
        closeButton.setOnClickListener {
            val ed = searchView.findViewById<View>(R.id.search_src_text) as EditText
            //Clear Text
            ed.setText("")
            //Clear query
            searchView.setQuery("",false)
            //Collapse the action view
            searchView.onActionViewCollapsed()
            //Collapse the search widget
            menuItem.collapseActionView()
            //Restore result to original
            menuViewModel.loadCategory()


        }
    }

    private fun startSearch(s: String) {
        val resultCategory = ArrayList<CategoryModel>()
        for(i in 0 until adapter!!.getCategoryList().size) {
            val categoryModel = adapter!!.getCategoryList()[i]
            if(categoryModel.name!!.toLowerCase().contains(s))
                resultCategory.add(categoryModel)
        }
        menuViewModel.getCategoryList().value = resultCategory
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onDestroy() {
        EventBus.getDefault().postSticky(MenuItemBack())
        super.onDestroy()
    }
}