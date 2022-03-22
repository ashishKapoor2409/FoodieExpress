package com.example.android.foodieexpress.Callback

import android.view.View

interface IRecyclerItemClickListener  {
    fun onItemClick(view: View, pos:Int)
}