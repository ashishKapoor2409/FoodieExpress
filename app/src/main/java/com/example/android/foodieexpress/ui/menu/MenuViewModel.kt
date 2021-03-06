package com.example.android.foodieexpress.ui.menu

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.foodieexpress.Callback.ICategoryCallBackListener
import com.example.android.foodieexpress.Common.Common
import com.example.android.foodieexpress.Model.BestDealModel
import com.example.android.foodieexpress.Model.CategoryModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MenuViewModel : ViewModel(), ICategoryCallBackListener {

    private var categoriesListMutable : MutableLiveData<List<CategoryModel>>? = null
    private var messageError : MutableLiveData<String> = MutableLiveData()
    private val categoryCallBackListener : ICategoryCallBackListener

    init {
        categoryCallBackListener = this
    }

    override fun onCategoryLoadSuccess(categoriesList: List<CategoryModel>) {
        categoriesListMutable!!.value = categoriesList
    }

    override fun onCategoryLoadFailed(message: String) {
        messageError.value = message
    }

    fun getCategoryList() : MutableLiveData<List<CategoryModel>> {
        if(categoriesListMutable == null) {
            categoriesListMutable = MutableLiveData()
            loadCategory()
        }
        return categoriesListMutable!!

    }

    fun getMessageError(): MutableLiveData<String>{
        return messageError
    }

    fun loadCategory() {
        val tempList = ArrayList<CategoryModel>()
        val categoryRef = FirebaseDatabase.getInstance().getReference(Common.CATEGORY_REF)
        categoryRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                for(itemSnapshot in p0!!.children) {
                    val model = itemSnapshot.getValue<CategoryModel>(CategoryModel::class.java)
                    model!!.menu_id = itemSnapshot.key
                    tempList.add(model!!)
                }
                categoryCallBackListener.onCategoryLoadSuccess(tempList)
            }

            override fun onCancelled(p0: DatabaseError) {
                categoryCallBackListener.onCategoryLoadFailed(p0.message)
            }

        })
    }
//    private val _text = MutableLiveData<String>().apply {
//        value = "This is CATEGORIES Fragment"
//    }
//    val text: LiveData<String> = _text
}