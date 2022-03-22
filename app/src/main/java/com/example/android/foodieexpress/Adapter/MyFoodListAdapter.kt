package com.example.android.foodieexpress.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android.foodieexpress.Callback.IRecyclerItemClickListener
import com.example.android.foodieexpress.Common.Common
import com.example.android.foodieexpress.EventBus.CategoryClick
import com.example.android.foodieexpress.EventBus.FoodItemClick
import com.example.android.foodieexpress.Model.CategoryModel
import com.example.android.foodieexpress.Model.FoodModel
import com.example.android.foodieexpress.R
import org.greenrobot.eventbus.EventBus

class MyFoodListAdapter(internal var context: Context,
                          internal var foodList : List<FoodModel>):
    RecyclerView.Adapter<MyFoodListAdapter.MyViewHolder>(){



    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        var txt_food_name: TextView? =null
        var txt_food_price: TextView?= null
        var img_food_image:ImageView? = null
        var img_fav:ImageView? = null
        var img_cart:ImageView? = null
        var category_name: TextView? = null
        var category_image: ImageView? = null
        internal var listener:IRecyclerItemClickListener?=null

        fun setListener(listener: IRecyclerItemClickListener) {
            this.listener = listener
        }

        init {
            txt_food_name = itemView.findViewById(R.id.txt_food_name) as TextView
            txt_food_price = itemView.findViewById(R.id.txt_food_price) as TextView
            img_food_image = itemView.findViewById(R.id.img_food_image) as ImageView
            img_fav = itemView.findViewById(R.id.img_fav) as ImageView
            img_cart = itemView.findViewById(R.id.img_quick_cart) as ImageView

            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            listener!!.onItemClick(view!!,adapterPosition)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyFoodListAdapter.MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_food_item,parent,false))
    }


    override fun getItemCount(): Int {
        return foodList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(foodList.get(position).image).into(holder.img_food_image!!)
        holder.txt_food_name!!.setText(foodList.get(position).name)
        holder.txt_food_price!!.setText(foodList.get(position).price.toString())
        holder.setListener(object :IRecyclerItemClickListener {
            override fun onItemClick(view: View, pos: Int) {
                Common.foodSelected = foodList.get(pos)
                EventBus.getDefault().postSticky(FoodItemClick(true, foodList.get(pos)))
            }
        })

    }
}