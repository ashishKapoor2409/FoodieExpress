package com.example.android.foodieexpress.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.foodieexpress.Callback.IBestDealLoadCallback
import com.example.android.foodieexpress.Callback.IPopularLoadCallback
import com.example.android.foodieexpress.Common.Common
import com.example.android.foodieexpress.Model.BestDealModel
import com.example.android.foodieexpress.Model.PopularCategoriesModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeViewModel : ViewModel(), IPopularLoadCallback, IBestDealLoadCallback {

    private var popularListMutableLiveData:MutableLiveData<List<PopularCategoriesModel>>? = null
    private var bestDealListMutableLiveData:MutableLiveData<List<BestDealModel>>? = null
    private lateinit var messageError : MutableLiveData<String>
    private var popularLoadCallbackListener: IPopularLoadCallback
    private var bestDealLoadCallbackListener: IBestDealLoadCallback

    val bestDealList : LiveData<List<BestDealModel>>
        get() {
            if(bestDealListMutableLiveData == null) {
                bestDealListMutableLiveData = MutableLiveData()
                messageError = MutableLiveData()
                loadBestDealList()
            }
            return bestDealListMutableLiveData!!
        }

    private fun loadBestDealList() {
        val tempList = ArrayList<BestDealModel>()
        val bestDealRef = FirebaseDatabase.getInstance().getReference(Common.BEST_DEALS_REF)
        bestDealRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                for(itemSnapshot in p0!!.children) {
                    val model = itemSnapshot.getValue<BestDealModel>(BestDealModel::class.java)
                    tempList.add(model!!)
                }
                bestDealLoadCallbackListener.onBestDealLoadSuccess(tempList)
            }

            override fun onCancelled(p0: DatabaseError) {
                bestDealLoadCallbackListener.onBestDealLoadFailed(p0.message)
            }

        })
    }

    val popularList: LiveData<List<PopularCategoriesModel>>
        get() {
            if(popularListMutableLiveData == null) {
                popularListMutableLiveData = MutableLiveData()
                messageError = MutableLiveData()
                loadPopularList()
            }
            return popularListMutableLiveData!!
        }

    private fun loadPopularList() {
        val tempList = ArrayList<PopularCategoriesModel>()
        val popularRef = FirebaseDatabase.getInstance().getReference(Common.POPULAR_REF)
        popularRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                for(itemSnapshot in p0!!.children) {
                    val model = itemSnapshot.getValue<PopularCategoriesModel>(PopularCategoriesModel::class.java)
                    tempList.add(model!!)
                }
                popularLoadCallbackListener.onPopularLoadSuccess(tempList)
            }

            override fun onCancelled(p0: DatabaseError) {
                popularLoadCallbackListener.onPopularLoadFailed(p0.message)
            }

        })
    }

    init {
        popularLoadCallbackListener = this
        bestDealLoadCallbackListener = this
    }

    override fun onPopularLoadSuccess(popularModelList: List<PopularCategoriesModel>) {
        popularListMutableLiveData!!.value = popularModelList
    }

    override fun onPopularLoadFailed(message: String) {
        messageError.value = message
    }

    override fun onBestDealLoadSuccess(bestDealList: List<BestDealModel>) {
        bestDealListMutableLiveData!!.value = bestDealList
    }

    override fun onBestDealLoadFailed(message: String) {
        messageError.value = message
    }

//    private val _text = MutableLiveData<String>().apply {
//        value = "This is home Fragment"
//    }
//    val text: LiveData<String> = _text
}