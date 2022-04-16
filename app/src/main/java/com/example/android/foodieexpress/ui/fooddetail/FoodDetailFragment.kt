package com.example.android.foodieexpress.ui.fooddetail

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.andremion.counterfab.CounterFab
import com.bumptech.glide.Glide
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton
import com.example.android.foodieexpress.Common.Common
import com.example.android.foodieexpress.Database.CartDataSource
import com.example.android.foodieexpress.Database.CartDatabase
import com.example.android.foodieexpress.Database.CartItem
import com.example.android.foodieexpress.Database.LocalCartDataSource
import com.example.android.foodieexpress.EventBus.CountCartEvent
import com.example.android.foodieexpress.EventBus.MenuItemBack
import com.example.android.foodieexpress.Model.CommentModel
import com.example.android.foodieexpress.Model.FoodModel
import com.example.android.foodieexpress.R
import com.example.android.foodieexpress.ui.comment.CommentFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import dmax.dialog.SpotsDialog
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import java.lang.StringBuilder

class FoodDetailFragment : Fragment(), TextWatcher {

    private lateinit var foodDetailViewModel: FoodDetailViewModel

    private lateinit var  addonBottomSheetDialog: BottomSheetDialog

    private val compositeDisposable = CompositeDisposable()
    private lateinit var cartDataSource:CartDataSource

    private var img_food:ImageView? = null
    private var btnCart:CounterFab? = null
    private var btnRating: FloatingActionButton? = null
    private var food_name:TextView?=null
    private var food_description:TextView?=null
    private var food_price:TextView?=null
    private var number_button:ElegantNumberButton?=null
    private var ratingBar:RatingBar?=null
    private var btnShowComment:Button?=null
    private var rdi_group_size: RadioGroup?=null
    private var img_add_on : ImageView? = null
    private var chip_group_user_selected_addon:ChipGroup? = null

    private var chip_group_addon: ChipGroup? = null
    private var edt_search_addon:EditText?=null


    private var waitingDialog:AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        foodDetailViewModel =
            ViewModelProvider(this).get(FoodDetailViewModel::class.java)
        val root: View = inflater.inflate(R.layout.fragment_food_detail, container, false)
        initViews(root)
        foodDetailViewModel.getMutableLiveDataFood().observe(this, Observer {
            displayInfo(it)
        })

        foodDetailViewModel.getMutableLiveDataComment().observe(this, Observer {
            submitRatingToFirebase(it)
        })
        return root
    }

    private fun submitRatingToFirebase(commentModel: CommentModel?) {
        waitingDialog!!.show()

        FirebaseDatabase.getInstance()
            .getReference(Common.COMMON_REF)
            .child(Common.foodSelected!!.id!!)
            .push()
            .setValue(commentModel)
            .addOnCompleteListener{
                task-> if(task.isSuccessful) {
                    addRatingToFood(commentModel!!.ratingValue.toDouble())
            }
                waitingDialog!!.dismiss()
            }
    }

    private fun addRatingToFood(ratingValue: Double) {
            FirebaseDatabase.getInstance()
                .getReference(Common.CATEGORY_REF)
                .child(Common.categorySelected!!.menu_id!!)
                .child("foods")
                .child(Common.foodSelected!!.key!!)
                .addListenerForSingleValueEvent(object :ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists()) {
                            val foodModel = snapshot.getValue(FoodModel::class.java)
                            foodModel!!.key = Common.foodSelected!!.key
                            val sumRating = foodModel.ratingValue!!.toDouble() + ratingValue
                            val ratingCount = foodModel.ratingCount+1

                            val updateData= HashMap<String, Any>()
                            updateData["ratingValue"] = sumRating
                            updateData["ratingCount"] = ratingCount

                            foodModel.ratingCount = ratingCount
                            foodModel.ratingValue = sumRating

                            snapshot.ref
                                .updateChildren(updateData)
                                .addOnCompleteListener{task ->
                                    waitingDialog!!.dismiss()
                                    if(task.isSuccessful){
                                        Common.foodSelected = foodModel
                                        foodDetailViewModel!!.setFoodModel(foodModel)
                                        Toast.makeText(context!!, "Thank you!!!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    }

                    override fun onCancelled(p0: DatabaseError) {
                        waitingDialog!!.dismiss()
                        Toast.makeText(context!!, ""+p0.message, Toast.LENGTH_SHORT).show()
                    }

                })
    }

    private fun displayInfo(it: FoodModel?) {
        Glide.with(context!!).load(it!!.image).into(img_food!!)
        food_name!!.text = StringBuilder(it!!.name!!)
        food_description!!.text = StringBuilder(it!!.description!!)
        food_price!!.text = StringBuilder(it!!.price.toString()!!)

        ratingBar!!.rating = it!!.ratingValue.toFloat() / it!!.ratingCount

        for(sizeModel in it!!.size) {
            val radioButton = RadioButton(context)
            radioButton.setOnCheckedChangeListener { compoundButton, b ->
                if(b)
                    Common.foodSelected!!.userSelectedSize = sizeModel
                calculateTotalPrice()
            }
            val params = LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1.0f)
            radioButton.layoutParams = params
            radioButton.text = sizeModel.name
            radioButton.tag = sizeModel.price

            rdi_group_size!!.addView(radioButton)
        }

        if(rdi_group_size!!.childCount>0) {
            val radioButton = rdi_group_size!!.getChildAt(0) as RadioButton
            radioButton.isChecked = true

        }
    }

    private fun calculateTotalPrice() {
        var totalPrice = Common.foodSelected!!.price.toDouble()
        var displayPrice = 0.0

        if (Common.foodSelected!!.userSelectedAddOn != null && Common.foodSelected!!.userSelectedAddOn!!.size>0) {
            for(addOnModel in Common.foodSelected!!.userSelectedAddOn!!) {
                totalPrice+= addOnModel.price!!.toDouble()
            }
        }
        totalPrice+= Common.foodSelected!!.userSelectedSize!!.price!!.toDouble()
        displayPrice = totalPrice * number_button!!.number.toInt()
        displayPrice = Math.round(displayPrice *100.0) /100.0
        food_price!!.text = StringBuilder("").append(Common.formatPrice(displayPrice)).toString()

    }

    private fun initViews(root: View) {

        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(context!!).cartDAO())

        addonBottomSheetDialog = BottomSheetDialog(context!!,R.style.DialogStyle)
        val layout_user_selected_addon = layoutInflater.inflate(R.layout.layout_addon_display,null)
        chip_group_addon = layout_user_selected_addon.findViewById(R.id.chip_group_addon) as ChipGroup
        edt_search_addon = layout_user_selected_addon.findViewById(R.id.edt_search) as EditText
        addonBottomSheetDialog.setContentView(layout_user_selected_addon)
        addonBottomSheetDialog.setOnDismissListener { dialogInterface ->
            displayUserSelectedAddon()
            calculateTotalPrice()
        }

        waitingDialog = SpotsDialog.Builder().setContext(context!!).setCancelable(false).build()
        btnCart = root!!.findViewById(R.id.btnCart) as CounterFab
        img_food = root!!.findViewById(R.id.img_food) as ImageView
        btnRating = root!!.findViewById(R.id.btn_rating) as FloatingActionButton
        food_name = root!!.findViewById(R.id.food_name) as TextView
        food_description = root!!.findViewById(R.id.food_description) as TextView
        food_price = root!!.findViewById(R.id.food_price) as TextView
        number_button = root!!.findViewById(R.id.number_button) as ElegantNumberButton
        ratingBar = root!!.findViewById(R.id.ratingBar) as RatingBar
        btnShowComment = root!!.findViewById(R.id.btnShowComment) as Button
        rdi_group_size = root!!.findViewById(R.id.rdi_group_size) as RadioGroup
        img_add_on = root!!.findViewById(R.id.img_add_addon) as ImageView
        chip_group_user_selected_addon = root!!.findViewById(R.id.chip_group_user_selected_addon) as ChipGroup


        img_add_on!!.setOnClickListener {
            if(Common.foodSelected!!.addon!=null) {
                displayAllAddOn()
                addonBottomSheetDialog.show()
            }
        }

        btnRating!!.setOnClickListener() {
            showDialogRating()
        }
        btnShowComment!!.setOnClickListener() {
            val commentFragment = CommentFragment.getInstance()
            commentFragment.show(activity!!.supportFragmentManager,"Comment Fragment")
        }

        btnCart!!.setOnClickListener() {
            val cartItem = CartItem()
            cartItem.uid = Common.currentUser!!.uid!!
            cartItem.userPhone = Common.currentUser!!.phone
            cartItem.foodId = Common.foodSelected!!.id!!
            cartItem.foodName = Common.foodSelected!!.name!!
            cartItem.foodImage = Common.foodSelected!!.image!!
            cartItem.foodQuantity = number_button!!.number.toInt()
            cartItem.foodExtraPrice = Common.calculateExtraPrice(Common.foodSelected!!.userSelectedSize,
            Common.foodSelected!!.userSelectedAddOn)

            if(Common.foodSelected!!.userSelectedAddOn !=null)
                cartItem.foodAddOn = Gson().toJson(Common.foodSelected!!.userSelectedAddOn)
            else
                cartItem.foodAddOn = "Default"
            if(Common.foodSelected!!.userSelectedSize !=null)
                cartItem.foodSize = Gson().toJson(Common.foodSelected!!.userSelectedSize)
            else
                cartItem.foodSize = "Default"

            cartDataSource.getItemWithAllOptionsInCart(Common.currentUser!!.uid!!,
                cartItem.foodId,
                cartItem.foodSize!!,
                cartItem.foodAddOn!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object: SingleObserver<CartItem> {
                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onSuccess(cartItemFromDB : CartItem) {
                        if(cartItemFromDB.equals(cartItem)) {
                            cartItemFromDB.foodExtraPrice = cartItem.foodExtraPrice
                            cartItemFromDB.foodAddOn = cartItem.foodAddOn
                            cartItemFromDB.foodSize = cartItem.foodSize
                            cartItemFromDB.foodQuantity = cartItemFromDB.foodQuantity!!.plus( cartItem.foodQuantity!!)

                            cartDataSource.updateCart(cartItemFromDB)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(object: SingleObserver<Int> {
                                    override fun onSubscribe(d: Disposable) {

                                    }

                                    override fun onSuccess(t: Int) {
                                        Toast.makeText(context,"Update Cart Success",Toast.LENGTH_SHORT).show()
                                        EventBus.getDefault().postSticky(CountCartEvent(true))
                                    }

                                    override fun onError(e: Throwable) {
                                        Toast.makeText(context,"[UPDATE CART]"+ e.message,Toast.LENGTH_SHORT).show()
                                    }

                                })
                        } else {
                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({
                                    Toast.makeText(context, "Add to cart success", Toast.LENGTH_SHORT).show()

                                    EventBus.getDefault().postSticky(CountCartEvent(true))
                                }, { t: Throwable ->
                                    Toast.makeText(context, "{INSERT CART}" + t!!.message, Toast.LENGTH_SHORT)
                                        .show()
                                }))
                        }
                    }

                    override fun onError(e: Throwable) {
                        if(e.message!!.contains("empty"))
                        {
                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({
                                    Toast.makeText(context, "Add to cart success", Toast.LENGTH_SHORT).show()

                                    EventBus.getDefault().postSticky(CountCartEvent(true))
                                }, { t: Throwable ->
                                    Toast.makeText(context, "{INSERT CART}" + t!!.message, Toast.LENGTH_SHORT)
                                        .show()
                                }))
                        } else {
                            Toast.makeText(context, "{CART ERROR}" + e!!.message, Toast.LENGTH_SHORT).show()
                        }
                    }

                })

        }



    }

    private fun displayAllAddOn() {
        if(Common.foodSelected!!.addon!!.size>0) {
            chip_group_addon!!.clearCheck()
            chip_group_addon!!.removeAllViews()
            edt_search_addon!!.addTextChangedListener(this)

            for(addOnModel in Common.foodSelected!!.addon!!) {
                    val chip = layoutInflater.inflate(R.layout.layout_chip, null, false) as Chip
                    chip.text = StringBuilder(addOnModel.name!!).append("(+$").append(addOnModel.price).append(")")
                        .toString()
                    chip.setOnCheckedChangeListener { compoundButton, b ->
                        if(b) {
                            if(Common.foodSelected!!.userSelectedAddOn == null)
                                Common.foodSelected!!.userSelectedAddOn = ArrayList()
                            Common.foodSelected!!.userSelectedAddOn!!.add(addOnModel)
                        }
                    }
                    chip_group_addon!!.addView(chip)
            }
        }

    }

    private fun displayUserSelectedAddon() {
        if (Common.foodSelected!!.userSelectedAddOn != null && Common.foodSelected!!.userSelectedAddOn!!.size > 0) {
            chip_group_user_selected_addon!!.removeAllViews()
            for(addOnModel in Common.foodSelected!!.userSelectedAddOn!!) {
                val chip = layoutInflater.inflate(R.layout.layout_chip_with_delete,null,false) as Chip
                chip.text = StringBuilder(addOnModel.name).append("(+$").append(addOnModel.price).append(")").toString()
                chip.isClickable = false
                chip.setOnCloseIconClickListener { view->
                    chip_group_user_selected_addon!!.removeView(view)
                    Common.foodSelected!!.userSelectedAddOn!!.remove(addOnModel)
                    calculateTotalPrice()
                }
            chip_group_user_selected_addon!!.addView(chip)

            }
        } else
            chip_group_user_selected_addon!!.removeAllViews()
    }

    private fun showDialogRating() {
       var builder = AlertDialog.Builder(context!!)
        builder.setTitle("Rating food")
        builder.setMessage("Please fill information")

        val itemView = LayoutInflater.from(context).inflate(R.layout.layout_rating_comment,null)
        val ratingBar = itemView.findViewById<RatingBar>(R.id.rating_bar)
        val edt_comment = itemView.findViewById<EditText>(R.id.edit_comment)

        builder.setView(itemView)
        builder.setNegativeButton("CANCEL") {dialogInterface, i->
            dialogInterface.dismiss()
        }

        builder.setPositiveButton("OK") {dialogInterface, i->
            val commentModel = CommentModel()
            commentModel.name = Common.currentUser!!.name
            commentModel.uid = Common.currentUser!!.uid
            commentModel.comment = edt_comment.text.toString()
            commentModel.ratingValue = ratingBar.rating
            val serverTimeStamp = HashMap<String, Any>()
            serverTimeStamp["timeStamp"] = ServerValue.TIMESTAMP
            commentModel.commentTimeStamp =(serverTimeStamp)

            foodDetailViewModel!!.setCommentModel(commentModel)
        }

        val dialog = builder.create()
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun beforeTextChanged(charSequence: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun onTextChanged(charSequence: CharSequence?, p1: Int, p2: Int, p3: Int) {
        chip_group_addon!!.clearCheck()
        chip_group_addon!!.removeAllViews()
        for(addOnModel in Common.foodSelected!!.addon!!) {
            if(addOnModel.name!!.toLowerCase().contains(charSequence.toString().toLowerCase())) {
                val chip = layoutInflater.inflate(R.layout.layout_chip, null, false) as Chip
                chip.text = StringBuilder(addOnModel.name!!).append("(+$").append(addOnModel.price).append(")")
                    .toString()
                chip.setOnCheckedChangeListener { compoundButton, b ->
                    if(b) {
                        if(Common.foodSelected!!.userSelectedAddOn == null)
                            Common.foodSelected!!.userSelectedAddOn = ArrayList()
                        Common.foodSelected!!.userSelectedAddOn!!.add(addOnModel)
                    }
                }
                chip_group_addon!!.addView(chip)

            }
        }
    }

    override fun afterTextChanged(p0: Editable?) {
        TODO("Not yet implemented")
    }

    override fun onDestroy() {
        EventBus.getDefault().postSticky(MenuItemBack())
        super.onDestroy()
    }
}