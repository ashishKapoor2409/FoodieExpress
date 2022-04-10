package com.example.android.foodieexpress

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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
import com.example.android.foodieexpress.EventBus.CategoryClick
import com.example.android.foodieexpress.EventBus.CountCartEvent
import com.example.android.foodieexpress.EventBus.FoodItemClick
import com.example.android.foodieexpress.EventBus.HideFABCart
import com.example.android.foodieexpress.databinding.ActivityHomeBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HomeActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var cartDataSource: CartDataSource
    private lateinit var fab:CounterFab
    private lateinit var binding: ActivityHomeBinding
    private lateinit var navController:NavController
    private var drawer: DrawerLayout? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                    navController.navigate(R.id.nav_home)
                }
                else if(item.itemId == R.id.nav_cart) {
                    navController.navigate(R.id.nav_cart)
                }
                else if(item.itemId == R.id.nav_menu) {
                    navController.navigate(R.id.nav_menu)
                }
                return true
            }

        })
        countCartItem()
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
    fun onHideFABEvent(event: HideFABCart) {
        if(event.isHide) {
            fab.hide()
        } else {
            fab.show()
        }
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