package com.example.android.foodieexpress.Callback

import com.example.android.foodieexpress.Model.CategoryModel
import com.example.android.foodieexpress.Model.CommentModel

interface ICommentCallBack {
    fun onCommentLoadSuccess(commentList:List <CommentModel>)
    fun onCommentLoadFailed(message:String)
}