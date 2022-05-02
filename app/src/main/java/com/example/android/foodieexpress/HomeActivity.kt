package com.example.android.foodieexpress

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import com.andremion.counterfab.CounterFab
import com.example.android.foodieexpress.Common.Common
import com.example.android.foodieexpress.Database.CartDataSource
import com.example.android.foodieexpress.Database.CartDatabase
import com.example.android.foodieexpress.Database.LocalCartDataSource
import com.example.android.foodieexpress.EventBus.*
import com.example.android.foodieexpress.Model.CategoryModel
import com.example.android.foodieexpress.Model.FoodModel
import com.example.android.foodieexpress.Model.PopularCategoriesModel
import com.example.android.foodieexpress.Model.UserModel
import com.example.android.foodieexpress.databinding.ActivityHomeBinding
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dmax.dialog.SpotsDialog
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import kotlin.collections.HashMap

class HomeActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var cartDataSource: CartDataSource
    private lateinit var fab:CounterFab
    private lateinit var binding: ActivityHomeBinding
    private lateinit var navController:NavController
    private var drawer: DrawerLayout? =null
    private var dialog: AlertDialog? = null

    private var placeSelected: Place? = null
    private lateinit var places_fragment: AutocompleteSupportFragment
    private lateinit var placeClient: PlacesClient

    private val placeFields = Arrays.asList(Place.Field.ID,
        Place.Field.NAME,
        Place.Field.LAT_LNG)

    private var menuItemClick = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dialog = SpotsDialog.Builder().setContext(this).setCancelable(false).build()
        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(this).cartDAO())


        setSupportActionBar(binding.appBarHome.toolbar)

        binding.appBarHome.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        fab = findViewById(R.id.fab)
        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener{ view ->
            navController.navigate(R.id.nav_cart)
        }
        drawer = binding.drawerLayout
        val navView: NavigationView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_content_home)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_menu, R.id.nav_food_detail,R.id.nav_cart
            ), drawer
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        var headerView = navView.getHeaderView(0)
        var txt_user = headerView.findViewById<TextView>(R.id.txt_user)
        Common.setSpanString("Hey, ",Common.currentUser!!.uid,txt_user)

        navView.setNavigationItemSelectedListener(object : NavigationView.OnNavigationItemSelectedListener{
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                item.isChecked = true
                drawer!!.closeDrawers()
                if(item.itemId == R.id.nav_sign_out){
                    signOut()
                }
                else if(item.itemId == R.id.nav_home) {
                    if(menuItemClick != item.itemId)
                        navController.navigate(R.id.nav_home)
                }
                else if(item.itemId == R.id.nav_cart) {
                    if(menuItemClick != item.itemId)
                        navController.navigate(R.id.nav_cart)
                }
                else if(item.itemId == R.id.nav_menu) {
                    if(menuItemClick != item.itemId)
                        navController.navigate(R.id.nav_menu)
                }
                else if(item.itemId == R.id.nav_view_order) {
                    if(menuItemClick != item.itemId)
                        navController.navigate(R.id.nav_view_order)
                }
                else if(item.itemId == R.id.nav_update_info) {
                    showUpdateInfoDialog()

                }

                menuItemClick = item!!.itemId

                return true
            }

        })
        initPlacesClient()
        countCartItem()
    }

    private fun initPlacesClient() {
        Places.initialize(this,getString(R.string.google_maps_key))
        placeClient = Places.createClient(this)
    }

    private fun showUpdateInfoDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("REGISTER")
        builder.setMessage("Please fill message")

        val itemView = LayoutInflater.from(this@HomeActivity)
            .inflate(R.layout.layout_register, null)

        val edit_name = itemView.findViewById<EditText>(R.id.edit_name)
        val edit_phone = itemView.findViewById<EditText>(R.id.edit_phone)
        val txt_address = itemView.findViewById<TextView>(R.id.txt_address_detail)

        places_fragment = supportFragmentManager.findFragmentById(R.id.places_autocomplete_fragment) as AutocompleteSupportFragment

        places_fragment.setPlaceFields(placeFields)
        places_fragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onError(p0: Status) {
                Toast.makeText(this@HomeActivity,""+p0.statusMessage, Toast.LENGTH_SHORT).show()
            }

            override fun onPlaceSelected(p0: Place) {
                placeSelected = p0
                txt_address.setText(placeSelected!!.address)
            }

        })

        edit_phone.setText(Common.currentUser!!.phone)
        txt_address.setText(Common.currentUser!!.address)
            edit_name.setText(Common.currentUser!!.name)

        builder.setView(itemView)
        builder.setNegativeButton("CANCEL") { dialogInterface, i -> dialogInterface.dismiss() }
        builder.setPositiveButton("UPDATE") { dialogInterface, i ->
            if(placeSelected != null) {
                if (TextUtils.isDigitsOnly(edit_name.text.toString())) {
                    Toast.makeText(this@HomeActivity, "Please enter your name", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val update_data = HashMap<String,Any>()
                update_data.put("name",edit_name.text.toString())
                update_data.put("address",txt_address.text.toString())
                update_data.put("lat",placeSelected!!.latLng.latitude)
                update_data.put("lng",placeSelected!!.latLng.longitude)

                FirebaseDatabase.getInstance()
                    .getReference(Common.USER_REFERENCE)
                    .child(Common.currentUser!!.uid!!)
                    .updateChildren(update_data)
                    .addOnFailureListener {
                        Toast.makeText(this@HomeActivity,it.message,Toast.LENGTH_SHORT).show()
                    }
                    .addOnSuccessListener {
                        Common.currentUser!!.name = update_data["name"].toString()
                        Common.currentUser!!.address = update_data["address"].toString()
                        Common.currentUser!!.lat = update_data["lat"].toString().toDouble()
                        Common.currentUser!!.lng = update_data["lng"].toString().toDouble()

                        Toast.makeText(this@HomeActivity,"Update  Info Success",Toast.LENGTH_SHORT).show()
                    }
            }
            else {
                Toast.makeText(this@HomeActivity,"Please select address",Toast.LENGTH_SHORT).show()
            }
        }

        val dialog = builder.create()
        dialog.setOnDismissListener {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.remove(places_fragment!!)
            fragmentTransaction.commit()
        }
        dialog.show()
    }

    private fun signOut() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Sign Out")
            .setMessage("Do you really want to exit")
            .setNegativeButton("CANCEL",{dialogInterfce,_ ->
                dialogInterfce.dismiss()
            })
            .setPositiveButton("OK"){dialogInterface,_ ->
                Common.foodSelected = null
                Common.categorySelected = null
                Common.currentUser = null
                FirebaseAuth.getInstance().signOut()

                val intent = Intent(this@HomeActivity,MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        val dialog = builder.create()
        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        countCartItem()
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_home)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onCategorySelected(event: CategoryClick) {
        if(event.isSuccess) {
            findNavController(R.id.nav_host_fragment_content_home).navigate(R.id.nav_food_list)
            //Toast.makeText(this,"Click to"+event.category.name,Toast.LENGTH_SHORT).show()
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onFoodSelected(event: FoodItemClick) {
        if(event.isSuccess) {
            findNavController(R.id.nav_host_fragment_content_home).navigate(R.id.nav_food_detail)
            //Toast.makeText(this,"Click to"+event.category.name,Toast.LENGTH_SHORT).show()
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onCountCartEvent(event: CountCartEvent) {
        if(event.isSuccess) {
            countCartItem()
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onPopularFoodItemClick(event: PopularFoodItemClick) {
        if(event.popularCategoriesModel!= null) {
            dialog!!.show()
            FirebaseDatabase.getInstance()
                .getReference("Category")
                .child(event.popularCategoriesModel!!.menu_id!!)
                .addListenerForSingleValueEvent(object:ValueEventListener{
                    override fun onCancelled(error: DatabaseError) {
                        dialog!!.dismiss()
                        Toast.makeText(this@HomeActivity,""+error.message, Toast.LENGTH_SHORT).show()
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if(p0.exists()) {
                            Common.categorySelected = p0.getValue(CategoryModel::class.java)
                            Common.categorySelected!!.menu_id = p0.key

                            FirebaseDatabase.getInstance()
                                .getReference("Category")
                                .child(event.popularCategoriesModel!!.menu_id!!)
                                .child("foods")
                                .orderByChild("id")
                                .equalTo(event.popularCategoriesModel.food_id)
                                .limitToLast(1)
                                .addListenerForSingleValueEvent(object : ValueEventListener{
                                    override fun onDataChange(p0: DataSnapshot) {
                                        if(p0.exists()) {
                                            for(foodSnapShot in p0.children) {
                                                Common.foodSelected = foodSnapShot.getValue(FoodModel::class.java)
                                                Common.foodSelected!!.key = foodSnapShot.key
                                            }
                                            navController!!.navigate(R.id.nav_food_detail)
                                        } else {
                                            Toast.makeText(this@HomeActivity,"Item doesn't exists", Toast.LENGTH_SHORT).show()
                                        }
                                        dialog!!.dismiss()
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        dialog!!.dismiss()
                                        Toast.makeText(this@HomeActivity,""+error.message, Toast.LENGTH_SHORT).show()
                                    }

                                })
                        } else {
                            dialog!!.dismiss()
                            Toast.makeText(this@HomeActivity,"Item doesn't exists", Toast.LENGTH_SHORT).show()
                        }
                    }
                })
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onBestDealFoodItemClick(event: BestDealItemClick) {
        if(event.model!= null) {
            dialog!!.show()
            FirebaseDatabase.getInstance()
                .getReference("Category")
                .child(event.model!!.menu_id!!)
                .addListenerForSingleValueEvent(object:ValueEventListener{
                    override fun onCancelled(error: DatabaseError) {
                        dialog!!.dismiss()
                        Toast.makeText(this@HomeActivity,""+error.message, Toast.LENGTH_SHORT).show()
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if(p0.exists()) {
                            Common.categorySelected = p0.getValue(CategoryModel::class.java)
                            Common.categorySelected!!.menu_id = p0.key

                            FirebaseDatabase.getInstance()
                                .getReference("Category")
                                .child(event.model!!.menu_id!!)
                                .child("foods")
                                .orderByChild("id")
                                .equalTo(event.model.food_id)
                                .limitToLast(1)
                                .addListenerForSingleValueEvent(object : ValueEventListener{
                                    override fun onDataChange(p0: DataSnapshot) {
                                        if(p0.exists()) {
                                            for(foodSnapShot in p0.children) {
                                                Common.foodSelected =
                                                    foodSnapShot.getValue(FoodModel::class.java)
                                                Common.foodSelected!!.key = foodSnapShot.key
                                            }
                                            navController!!.navigate(R.id.nav_food_detail)
                                        } else {
                                            Toast.makeText(this@HomeActivity,"Item doesn't exists", Toast.LENGTH_SHORT).show()
                                        }
                                        dialog!!.dismiss()
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        dialog!!.dismiss()
                                        Toast.makeText(this@HomeActivity,""+error.message, Toast.LENGTH_SHORT).show()
                                    }

                                })
                        } else {
                            dialog!!.dismiss()
                            Toast.makeText(this@HomeActivity,"Item doesn't exists", Toast.LENGTH_SHORT).show()
                        }
                    }
                })
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onHideFABEvent(event: HideFABCart) {
        if(event.isHide) {
            fab.hide()
        } else {
            fab.show()
        }
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public fun onMenuItemBack(event: MenuItemBack) {
        menuItemClick = -1
        if(supportFragmentManager.backStackEntryCount > 0)
            supportFragmentManager.popBackStack();
    }

    private fun countCartItem() {
        cartDataSource.countItemInCart(Common.currentUser!!.uid!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<Int>{
                override fun onSubscribe(d: Disposable) {

                }

                override fun onSuccess(t: Int) {
                    fab.count = t
                }

                override fun onError(e: Throwable) {
                    if(!e.message!!.contains("Query returned empty"))
                        Toast.makeText(this@HomeActivity,"[COUNT CART]"+e.message,Toast.LENGTH_SHORT).show()
                    else
                        fab.count = 0
                }

            })
    }
}