package com.example.android.foodieexpress.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton
import com.example.android.foodieexpress.Callback.IRecyclerItemClickListener
import com.example.android.foodieexpress.Database.CartDataSource
import com.example.android.foodieexpress.Database.CartDatabase
import com.example.android.foodieexpress.Database.CartItem
import com.example.android.foodieexpress.Database.LocalCartDataSource
import com.example.android.foodieexpress.EventBus.UpdateItemInCart
import com.example.android.foodieexpress.Model.FoodModel
import com.example.android.foodieexpress.R
import io.reactivex.disposables.CompositeDisposable
import org.greenrobot.eventbus.EventBus
import java.lang.StringBuilder

class MyCartAdapter(internal var context: Context,
                    internal var cartItems : List<CartItem>):
    RecyclerView.Adapter<MyCartAdapter.MyViewHolder>(){

    internal var compositeDisposable: CompositeDisposable
    internal var cartDataSource: CartDataSource

    init {
        compositeDisposable = CompositeDisposable()
        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(context).cartDAO())
    }
        inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
            View.OnClickListener {



        lateinit var img_cart: ImageView
        lateinit var txt_food_name: TextView
        lateinit var txt_food_price: TextView
        lateinit var number_button: ElegantNumberButton

        init {
            txt_food_name = itemView.findViewById(R.id.txt_food_name) as TextView
            txt_food_price = itemView.findViewById(R.id.txt_food_price) as TextView
            number_button = itemView.findViewById(R.id.number_button) as ElegantNumberButton
            img_cart = itemView.findViewById(R.id.img_cart) as ImageView
        }

        override fun onClick(p0: View?) {

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context)
            .inflate(R.layout.layout_cart_item,parent,false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(cartItems[position].foodImage)
            .into(holder.img_cart)
        holder.txt_food_name.text = StringBuilder(cartItems[position].foodName)
        holder.txt_food_price.text = StringBuilder("").append(cartItems[position].foodPrice!!.plus( cartItems[position].foodExtraPrice))
        holder.number_button.number = cartItems[position].foodQuantity.toString()

        holder.number_button.setOnValueChangeListener{view,oldValue,newValue ->
            cartItems[position].foodQuantity = newValue
            EventBus.getDefault().postSticky(UpdateItemInCart(cartItems[position]))
        }
    }

    override fun getItemCount(): Int {
        return cartItems.size
    }
}