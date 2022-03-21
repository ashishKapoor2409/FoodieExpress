package com.example.android.foodieexpress.Callback

import com.example.android.foodieexpress.Model.BestDealModel
import com.example.android.foodieexpress.Model.CategoryModel

interface ICategoryCallBackListener {
    fun onCategoryLoadSuccess(categoriesList:List <CategoryModel>)
    fun onCategoryLoadFailed(message:String)
}