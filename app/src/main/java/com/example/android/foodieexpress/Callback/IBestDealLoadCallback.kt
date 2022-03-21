package com.example.android.foodieexpress.Callback

import com.example.android.foodieexpress.Model.BestDealModel


interface IBestDealLoadCallback {
    fun onBestDealLoadSuccess(bestDealList:List <BestDealModel>)
    fun onBestDealLoadFailed(message:String)
}