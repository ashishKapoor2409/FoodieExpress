package com.example.android.foodieexpress.ui.fooddetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.foodieexpress.Common.Common
import com.example.android.foodieexpress.Model.FoodModel

class FoodDetailViewModel : ViewModel() {

    private var mutableLiveDataFood : MutableLiveData<FoodModel>? = null
    fun getMutableLiveDataFood() : MutableLiveData<FoodModel> {
        if(mutableLiveDataFood == null)
            mutableLiveDataFood = MutableLiveData()
        mutableLiveDataFood!!.value = Common.foodSelected
        return mutableLiveDataFood!!
    }


}