package com.example.android.foodieexpress.Common

import com.example.android.foodieexpress.Model.CategoryModel
import com.example.android.foodieexpress.Model.FoodModel
import com.example.android.foodieexpress.Model.UserModel

object Common {
    val COMMON_REF: String = "Comments"
    var foodSelected: FoodModel? = null
    var categorySelected: CategoryModel? = null
    val CATEGORY_REF: String = "Category"
    val FULL_WIDTH_COLUMN: Int = 1
    val DEFAULT_COLUMN_COUNT: Int = 0
    val BEST_DEALS_REF: String = "BestDeals"
    val POPULAR_REF: String = "MostPopular"
    val USER_REFERENCE = "Users"
    var currentUser:UserModel? = null
}