package com.example.android.foodieexpress.Database

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Cart")
class CartItem {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name="foodId")
    var foodId:String?=""

    @ColumnInfo(name="Name")
    var foodName: String? = null

    @ColumnInfo(name = "foodPrice")
    var foodPrice: Double? = 0.0

    @ColumnInfo(name = "foodQuantity")
    var foodQuantity: Int? = 0

    @ColumnInfo(name = "foodAddOn")
    var foodAddOn: String? = ""

    @ColumnInfo(name = "foodExtraPrice")
    var foodExtraPrice: Double = 0.0

    @ColumnInfo(name = "uid")
    var uid: String? = ""


}