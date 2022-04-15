package com.example.android.foodieexpress.ui.view_orders

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.foodieexpress.Callback.ILoadOrderCallbackListener
import com.example.android.foodieexpress.Model.Order
import butterknife.Unbinder
import com.example.android.foodieexpress.Adapter.MyOrderAdapter
import com.example.android.foodieexpress.Common.Common
import com.example.android.foodieexpress.R
import com.example.android.foodieexpress.ui.menu.MenuViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dmax.dialog.SpotsDialog
import java.util.*
import kotlin.collections.ArrayList


class ViewOrderFragment: Fragment(), ILoadOrderCallbackListener {

    private var viewOrderModel: ViewOrderModel? = null

    internal lateinit var dialog: AlertDialog

    internal lateinit var recycler_order: RecyclerView

    internal lateinit var listener:ILoadOrderCallbackListener


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        viewOrderModel =
            ViewModelProvider(this).get(ViewOrderModel::class.java)

        val root = inflater.inflate(R.layout.fragment_view_orders,container,false)
        initViews(root)
        loadOrderFromFirebase()
        viewOrderModel!!.mutableLiveDataOrderList.observe(this, Observer {
            Collections.reverse(it!!)
            val adapter = MyOrderAdapter(context!!,it!!)
            recycler_order!!.adapter = adapter
        })
        return root
    }

    private fun loadOrderFromFirebase() {
        dialog.show()
        val orderList = ArrayList<Order>()

        FirebaseDatabase.getInstance().getReference(Common.ORDER_REF)
            .orderByChild("userId")
            .equalTo(Common.currentUser!!.uid!!)
            .limitToLast(100)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(orderSnapShot in snapshot.children)
                    {
                        val order = orderSnapShot.getValue(Order::class.java)
                        order!!.orderNumber = orderSnapShot.key
                        orderList.add(order!!)
                    }
                    listener.onLoadOrderSuccess(orderList)
                }

                override fun onCancelled(error: DatabaseError) {
                    listener.onLoadOrderFailed(error.message!!)
                }

            })
    }

    private fun initViews(root: View?) {
        listener = this
        dialog = SpotsDialog.Builder().setContext(context!!).setCancelable(false).build()

        recycler_order = root!!.findViewById(R.id.recycler_order) as RecyclerView
        recycler_order.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context!!)
        recycler_order.layoutManager = layoutManager
        recycler_order.addItemDecoration(DividerItemDecoration(context!!,layoutManager.orientation))



    }

    override fun onLoadOrderSuccess(orderList: List<Order>) {
        dialog.dismiss()
        viewOrderModel!!.setMutableLiveDataOrderList(orderList)
    }

    override fun onLoadOrderFailed(message: String) {
        dialog.dismiss()
        Toast.makeText(context!!, message,Toast.LENGTH_SHORT).show()
    }

}