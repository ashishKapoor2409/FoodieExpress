package com.example.android.foodieexpress.ui.comment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.foodieexpress.Model.CommentModel

class CommentViewModel: ViewModel() {

    val mutableLiveDataCommentList : MutableLiveData<List<CommentModel>>

    init {
        mutableLiveDataCommentList = MutableLiveData()
    }

    fun setCommentList(commentList:List<CommentModel>) {
       mutableLiveDataCommentList.value = commentList
    }
}