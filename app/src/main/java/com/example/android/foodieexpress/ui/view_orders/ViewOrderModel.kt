package com.example.android.foodieexpress.ui.view_orders

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.foodieexpress.Model.Order

class ViewOrderModel: ViewModel() {
    val mutableLiveDataOrderList: MutableLiveData<List<Order>>
    init {
        mutableLiveDataOrderList = MutableLiveData()
    }

    fun setMutableLiveDataOrderList(orderList:List<Order>)
    {
        mutableLiveDataOrderList.value = orderList
    }

}