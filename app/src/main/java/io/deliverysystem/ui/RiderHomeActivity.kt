package io.deliverysystem.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import io.codelabs.util.bindView
import io.deliverysystem.R
import io.deliverysystem.core.BaseActivity
import io.deliverysystem.data.Item
import io.deliverysystem.data.Order
import io.deliverysystem.data.Rider
import io.deliverysystem.util.Utils
import io.deliverysystem.util.Utils.logger
import io.deliverysystem.util.Utils.showMessage
import java.util.*

class RiderHomeActivity : BaseActivity(), OnMapReadyCallback {
    private val toolbar: Toolbar by bindView(R.id.toolbar)
    private val container: ViewGroup by bindView(R.id.container)

    private var mMap: GoogleMap? = null
    private var hasLocationPermission: Boolean = false
    private var rider: Rider? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_rider_home
    }

    @SuppressLint("NewApi")
    override fun onViewCreated(instanceState: Bundle?, intent: Intent?) {
        setSupportActionBar(toolbar)

        //Get logged in user
        if (auth.uid != null) {
            database.dao().getRider(auth.uid).observeForever {

                val filter = it.filter { rider -> rider.uid == auth.uid }
                if (filter.isNotEmpty()) {
                    rider = filter[0]
                    Utils.logger("Rider account: $rider")
                    toolbar.title = String.format("ID: ${rider?.uid}")
                }
            }
        }

        //Get location permission
        hasLocationPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasLocationPermission)
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), RC_LOC
            )

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment?
        mapFragment?.getMapAsync(this) ?: Utils.showMessage(context, "Unable to show map")
    }

    override fun onMapReady(p0: GoogleMap?) {
        mMap = p0

        if (hasLocationPermission) getDeviceLocation()
        else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) getDeviceLocation()

        mMap?.setOnMarkerClickListener { marker ->
            Utils.showMessage(context, marker.snippet)
            return@setOnMarkerClickListener true
        }

        val orders: MutableList<Order> = mutableListOf()
        firestore.collection(Utils.ORDERS_REF).addSnapshotListener(this@RiderHomeActivity) { snapshot, exception ->
            if (exception != null) {
                Utils.showMessage(this@RiderHomeActivity, "Cannot retrieve customers' orders")
                return@addSnapshotListener
            }

            val objects = snapshot?.toObjects(Order::class.java)

            logger(objects)
            objects?.forEach {
                orders.add(it)
                if (mMap != null) {
                    val address = it.address
                    mMap!!.addMarker(
                        MarkerOptions()
                            .position(LatLng(address.lat, address.lng))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
                            .title(it.name)
                            .snippet(it.key)
                    ).showInfoWindow()
                }
            }
        }

        mMap?.setOnMarkerClickListener {
            orders.forEach { item: Order? ->
                if (item != null && item.key == it.snippet) {
                    val intent = Intent(context, TrackingActivity::class.java)
                    intent.putExtra(TrackingActivity.EXTRA_ORDER, item)
                    intent.putParcelableArrayListExtra(
                        TrackingActivity.EXTRA_CUSTOMER_ORDER,
                        item.items as? ArrayList<out Parcelable> ?: mutableListOf<Item>() as ArrayList<out Parcelable>
                    )
                    startActivity(intent)
                } else {
                    showMessage(applicationContext, "Unable to get order")
                }
            }
            return@setOnMarkerClickListener true
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_logout -> {
                Utils.logout(this)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        if (hasLocationPermission) mMap?.isMyLocationEnabled = true

        if (tracker.canGetLocation()) {
            Utils.logger("Can get user's location")
            Utils.logger(
                String.format(
                    Locale.getDefault(), "Location: %.2f, %.2f.  Address: %s",
                    tracker.latitude, tracker.longitude, tracker.location.toString()
                )
            )
            val sydney = LatLng(tracker.latitude, tracker.longitude)
//            val geocoder = Geocoder(this, Locale.getDefault())
            mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 14.0f))

            mMap?.setOnMyLocationButtonClickListener {
                mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 16.0f))
                return@setOnMyLocationButtonClickListener true
            }
        } else tracker.showSettingsAlert()

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RC_LOC && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            hasLocationPermission = true
            getDeviceLocation()
        }
    }

    companion object {
        private const val RC_LOC = 8
    }
}