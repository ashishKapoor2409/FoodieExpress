package com.example.android.foodieexpress.ui.view_orders

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.foodieexpress.Model.OrderModel

class ViewOrderModel: ViewModel() {
    val mutableLiveDataOrderModelList: MutableLiveData<List<OrderModel>>
    init {
        mutableLiveDataOrderModelList = MutableLiveData()
    }

    fun setMutableLiveDataOrderList(orderModelList:List<OrderModel>)
    {
        mutableLiveDataOrderModelList.value = orderModelList
    }

}