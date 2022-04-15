package com.example.android.foodieexpress.Callback

import com.example.android.foodieexpress.Model.Order

interface ILoadOrderCallbackListener {
    fun onLoadOrderSuccess(orderList: List<Order>)
    fun onLoadOrderFailed(message: String)
}