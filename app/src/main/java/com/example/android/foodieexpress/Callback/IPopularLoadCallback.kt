package com.example.android.foodieexpress.Callback

import com.example.android.foodieexpress.Model.PopularCategoriesModel

interface IPopularLoadCallback {
    fun onPopularLoadSuccess(popularModelList:List <PopularCategoriesModel>)
    fun onPopularLoadFailed(message:String)
}