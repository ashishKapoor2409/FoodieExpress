package com.example.android.foodieexpress

import android.accounts.Account
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.android.foodieexpress.Common.Common
import com.example.android.foodieexpress.Model.UserModel
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import dmax.dialog.SpotsDialog
import io.reactivex.disposables.CompositeDisposable
import java.lang.StringBuilder
import java.util.*

class MainActivity : AppCompatActivity() {


    private var placeSelected: Place? = null
    private lateinit var places_fragment: AutocompleteSupportFragment
    private lateinit var placeClient:PlacesClient

    private val placeFields = Arrays.asList(Place.Field.ID,
    Place.Field.NAME,
    Place.Field.LAT_LNG)

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var listener: FirebaseAuth.AuthStateListener
    private lateinit var dialog: AlertDialog
    private val compositeDisposable = CompositeDisposable()

    private lateinit var userRef: DatabaseReference
    private var providers: List<AuthUI.IdpConfig>? = null

        companion object {
            private val APP_REQUEST_CODE = 7171 //Any number
        }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(listener)
    }

    override fun onStop() {
        super.onStop()
        if (listener != null)
            firebaseAuth.removeAuthStateListener { listener }
        compositeDisposable.clear()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    private fun init() {

        Places.initialize(this,getString(R.string.google_maps_key))
        placeClient = Places.createClient(this)

        providers = Arrays.asList<AuthUI.IdpConfig>(AuthUI.IdpConfig.PhoneBuilder().build())
        userRef = FirebaseDatabase.getInstance().getReference(Common.USER_REFERENCE)
        firebaseAuth = FirebaseAuth.getInstance()
        dialog = SpotsDialog.Builder().setContext(this).setCancelable(false).build()
        listener = FirebaseAuth.AuthStateListener { it->

            Dexter.withActivity(this@MainActivity)
                .withPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(object : PermissionListener{
                    override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                        val user = it.currentUser
                        if (user != null) {
                            checkUserFromFirebase(user!!)

                        } else {
                            phoneLogin()
                        }
                    }

                    override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                        Toast.makeText(this@MainActivity,
                            "You must accept the permission to use the app",Toast.LENGTH_SHORT).show()
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: PermissionRequest?,
                        p1: PermissionToken?
                    ) {

                    }

                }).check()
        }
    }

    private fun checkUserFromFirebase(user: FirebaseUser) {
        dialog!!.show()
        userRef.child(user!!.uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val userModel = p0.getValue(UserModel::class.java)
                    goToHomeActivity(userModel)
                } else {
                    showRegisterDialog(user!!)
                }
                dialog!!.dismiss()

            }

            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(this@MainActivity, "" + p0.message, Toast.LENGTH_SHORT).show()
            }

        });
    }

    private fun showRegisterDialog(user: FirebaseUser) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("REGISTER")
        builder.setMessage("Please fill message")

        val itemView = LayoutInflater.from(this@MainActivity)
            .inflate(R.layout.layout_register, null)

        val edit_name = itemView.findViewById<EditText>(R.id.edit_name)
        val edit_phone = itemView.findViewById<EditText>(R.id.edit_phone)
        val txt_address = itemView.findViewById<TextView>(R.id.txt_address_detail)

        places_fragment = supportFragmentManager.findFragmentById(R.id.places_autocomplete_fragment) as AutocompleteSupportFragment

        places_fragment.setPlaceFields(placeFields)
        places_fragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onError(p0: Status) {
                Toast.makeText(this@MainActivity,""+p0.statusMessage, Toast.LENGTH_SHORT).show()
            }

            override fun onPlaceSelected(p0: Place) {
                placeSelected = p0
                txt_address.setText(placeSelected!!.address)
            }

        })

        edit_phone.setText(user!!.phoneNumber)

        builder.setView(itemView)
        builder.setNegativeButton("CANCEL") { dialogInterface, i -> dialogInterface.dismiss() }
        builder.setPositiveButton("REGISTER") { dialogInterface, i ->
            if(placeSelected != null) {
                if (TextUtils.isDigitsOnly(edit_name.text.toString())) {
                    Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val userModel = UserModel()
                userModel.uid = user!!.uid
                userModel.name = edit_name.text.toString()
                userModel.address = txt_address.text.toString()
                userModel.phone = edit_phone.text.toString()
                userModel.lat = placeSelected!!.latLng.latitude
                userModel.lng = placeSelected!!.latLng.longitude

                userRef!!.child(user!!.uid)
                    .setValue(userModel)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            dialogInterface.dismiss()
                            Toast.makeText(
                                this,
                                "Congratulations!Register Success",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            goToHomeActivity(userModel)
                        }
                    }
            }
            else {
                Toast.makeText(this@MainActivity,"Please select address",Toast.LENGTH_SHORT).show()
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

    private fun goToHomeActivity(userModel: UserModel?) {
        FirebaseMessaging.getInstance().token
            .addOnFailureListener{ e-> Toast.makeText(this@MainActivity,""+e.message, Toast.LENGTH_SHORT).show()
                Common.currentUser = userModel!!
                startActivity(Intent(this@MainActivity,HomeActivity::class.java))
                finish()
            }
            .addOnCompleteListener{ task ->
                if(task.isSuccessful) {
                    Common.currentUser = userModel!!
                    Common.updateToken(this@MainActivity, task.result)
                    startActivity(Intent(this@MainActivity,HomeActivity::class.java))
                    finish()
                }
            }


    }

    private fun phoneLogin() {

        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers!!)
                .build(), APP_REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == APP_REQUEST_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                val user = FirebaseAuth.getInstance().currentUser
            } else {
                Toast.makeText(this, "Failed to sign in", Toast.LENGTH_SHORT).show()
            }
        }
    }
}