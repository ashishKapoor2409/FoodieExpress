package com.example.android.foodieexpress.Callback

import com.example.android.foodieexpress.Model.OrderModel

interface ILoadOrderCallbackListener {
    fun onLoadOrderSuccess(orderModelList: List<OrderModel>)
    fun onLoadOrderFailed(message: String)
}