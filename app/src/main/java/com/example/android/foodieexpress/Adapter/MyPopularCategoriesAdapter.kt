package com.example.android.foodieexpress.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
//import butterknife.BindView
//import butterknife.ButterKnife
//import butterknife.Unbinder
import com.bumptech.glide.Glide
import com.example.android.foodieexpress.Model.PopularCategoriesModel
import com.example.android.foodieexpress.R
import de.hdodenhof.circleimageview.CircleImageView

class MyPopularCategoriesAdapter(internal var context: Context,
                                 internal var popularCategoriesModels: List<PopularCategoriesModel>) :
        RecyclerView.Adapter<MyPopularCategoriesAdapter.MyViewHolder>() {
    inner class MyViewHolder(itemView:View) : RecyclerView.ViewHolder(itemView) {

//        @BindView(R.id.txt_category_name)
        var category_name:TextView
//        @BindView(R.id.category_image)
        var category_image: CircleImageView

//        var unbinder:Unbinder
        init {
            category_name = itemView.findViewById(R.id.txt_category_name) as TextView
            category_image = itemView.findViewById(R.id.category_image) as CircleImageView
//            unbinder = ButterKnife.bind(this, itemView)
//                category_name = R.id.txt_category_name as TextView
//                category_image = R.id.category_image as CircleImageView

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
           return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_popular_categories_item, parent, false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(popularCategoriesModels.get(position).image).into(holder.category_image)
        holder.category_name.setText(popularCategoriesModels.get(position).name)
    }

    override fun getItemCount(): Int {
        return popularCategoriesModels.size
    }
}