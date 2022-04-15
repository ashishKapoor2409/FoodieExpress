package com.example.android.foodieexpress.Common

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.widget.TextView
import com.example.android.foodieexpress.Model.*
import java.lang.StringBuilder
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*

object Common {
    fun formatPrice(price: Double): String {
        if(price != 0.toDouble()) {
            val df = DecimalFormat("#,##0.00")
            df.roundingMode = RoundingMode.HALF_UP
            val finalPrice = StringBuilder(df.format(price)).toString()
            return finalPrice.replace(".",",")
        } else
            return "0,00"

    }

    fun calculateExtraPrice(userSelectedSize: SizeModel?,
                            userSelectedAddOn: MutableList<AddonModel>?
    ): Double {

        var result:Double = 0.0
        if(userSelectedSize == null && userSelectedAddOn == null)
            return 0.0
        else if(userSelectedSize == null) {
            for(addOnModel in userSelectedAddOn!!)
                result+= addOnModel.price!!.toDouble()
            return result
        }
        else if(userSelectedAddOn == null) {
            result = userSelectedSize!!.price.toDouble()
            return result
        } else
        {
            result = userSelectedSize!!.price.toDouble()
            for(addOnModel in userSelectedAddOn!!)
                result+= addOnModel.price!!.toDouble()
            return result
        }

    }

    fun setSpanString(welcome: String, name: String?, txtUser: TextView?) {
        val builder = SpannableStringBuilder()
        builder.append(welcome)
        val txtSpannable = SpannableString(name)
        val boldSpan = StyleSpan(Typeface.BOLD)

        txtSpannable.setSpan(boldSpan,0,name!!.length,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.append(txtSpannable)
        txtUser!!.setText(builder,TextView.BufferType.SPANNABLE)
    }

    fun createOrderNumber(): String {
        return StringBuilder().append(System.currentTimeMillis())
            .append(Math.abs(Random().nextInt()))
            .toString()
    }

    val ORDER_REF: String = "Order"
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