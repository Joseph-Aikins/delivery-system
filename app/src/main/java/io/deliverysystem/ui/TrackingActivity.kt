package io.deliverysystem.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.iid.FirebaseInstanceId
import io.codelabs.util.bindView
import io.deliverysystem.R
import io.deliverysystem.core.BaseActivity
import io.deliverysystem.data.Authentication
import io.deliverysystem.data.Customer
import io.deliverysystem.data.Model
import io.deliverysystem.data.Order
import io.deliverysystem.util.GetPathFromLocation
import io.deliverysystem.util.Utils
import io.deliverysystem.util.Utils.logger
import java.util.*
import kotlin.random.Random


class TrackingActivity : BaseActivity(), OnMapReadyCallback {
    private val toolbar: Toolbar by bindView(R.id.toolbar)
    private val eSignBtn: TextView by bindView(R.id.e_sign_btn)
    private val container: ViewGroup by bindView(R.id.container)
    private var mMap: GoogleMap? = null
    private var userLocation: String? = null

    private var model: Model? = null
    private lateinit var smsManager: SmsManager
    private lateinit var randNum: Random

    private var hasLocationPermission: Boolean = false
    private var hasSMSPermission: Boolean = false
    override fun getLayoutId(): Int {
        return R.layout.activity_tracking
    }

    @SuppressLint("NewApi")
    override fun onViewCreated(instanceState: Bundle?, intent: Intent) {
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finishAfterTransition() }

        smsManager = SmsManager.getDefault()
        randNum = Random(4)

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

        hasSMSPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasSMSPermission)
            requestPermissions(
                arrayOf(
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.RECEIVE_SMS
                ), RC_SMS
            )

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment?
        mapFragment?.getMapAsync(this) ?: Utils.showMessage(context, "Unable to show map")

        if (intent.hasExtra(EXTRA_CUSTOMER)) {
            model = intent.getParcelableExtra<Customer>(EXTRA_CUSTOMER)
            bindCustomer()
        } else if (intent.hasExtra(EXTRA_ORDER)) {
            model = intent.getParcelableExtra<Order>(EXTRA_ORDER)
            bindOrder()
        }
        eSignBtn.setOnClickListener { doSigning() }
    }

    private fun bindOrder() {
        if (model is Order) {
            logger(model.toString())

            val geocoder = Geocoder(context, Locale.getDefault())
            Utils.logger(geocoder)
            try {
                val location =
                    geocoder.getFromLocation((model as? Order)?.address!!.lng, (model as? Order)?.address!!.lng, 3)
                if (location != null && !location.isEmpty()) {
                    userLocation = location[0].getAddressLine(0)
                    Utils.logger("User location: $userLocation")
                }
            } catch (e: Exception) {
                Utils.logger("Unable to find user's location")
            }
        }
    }

    private fun doSigning() {
        val shortCode = randNum.nextInt(111111, 999999)
        Utils.logger(shortCode)
        val builder = AlertDialog.Builder(this@TrackingActivity)
        builder.setTitle("Authenticate transaction")
            .setMessage("Send authentication code to \"${model?.name}\"?")
            .setPositiveButton("Send") { dialog, _ ->
                dialog.dismiss()
                Utils.showMessage(context, "Sending code to \"${model?.name}\". Please wait...")
                val riderName =
                    if (auth.currentUser?.displayName.isNullOrEmpty()) "Sam Tugah" else auth.currentUser?.displayName
                val userToken = (model as? Customer)?.token ?: FirebaseInstanceId.getInstance().token
                val document = firestore.collection(Utils.ORDER_AUTH_REF).document()
                val authentication = Authentication(
                    model?.name,
                    model?.uid,
                    auth.uid,
                    riderName,
                    if (model is Customer) (model as? Customer)?.email else Utils.DEFAULT_EMAIL,
                    userToken,
                    shortCode.toString(),
                    document.id
                )

                document.set(
                    authentication
                ).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Utils.showMessage(
                            applicationContext,
                            "Authentication request sent successfully to \"${model?.name}\""
                        )
                        val intent = Intent(applicationContext, UserPayment::class.java)
                        intent.putExtra(UserPayment.EXTRA_AUTH_MODEL, authentication)
                        startActivity(intent)
                        sendSms(shortCode)
                        finishAfterTransition()
                    } else {
                        Utils.showMessage(
                            applicationContext,
                            "Unable to send authentication request to \"${model?.name}\""
                        )
                    }
                }.addOnFailureListener {
                    Utils.showMessage(
                        applicationContext,
                        "Unable to send authentication request to \"${model?.name}\""
                    )
                }
            }.setNegativeButton("Dismiss") { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }

    private fun sendSms(shortCode: Int) {
        if (model is Customer || model is Order) {

            when {
                if (model is Customer) !(model as? Customer)?.phone.isNullOrEmpty() else !(model as? Order)?.phone.isNullOrEmpty() -> {

                    smsManager.sendTextMessage(
                        (model as? Customer)?.phone ?: Utils.DEFAULT_PHONE_NUMBER, null,
                        "Hello, ${model?.name}! Your order authentication short code is: $shortCode", null, null
                    )
                }

                if (model is Customer) !(model as? Customer)?.email.isNullOrEmpty() else !(model as? Order)?.rider.isNullOrEmpty() -> {
                    Utils.showMessage(applicationContext, "Email will be sent to this address in future updates")
                }
                else -> {
                    Utils.showMessage(applicationContext, "Cannot send message to customer")
                }
            }
        } else {
            Utils.showMessage(applicationContext, "Unknown data type")
        }
    }

    private fun bindCustomer() {
        val geocoder = Geocoder(context, Locale.getDefault())
        Utils.logger(geocoder)
        try {
            val location =
                geocoder.getFromLocation((model as? Customer)?.address!!.lng, (model as? Customer)?.address!!.lng, 3)
            if (location != null && !location.isEmpty()) {
                userLocation = location[0].getAddressLine(0)
                Utils.logger("User location: $userLocation")
            }
        } catch (e: Exception) {
            Utils.logger("Unable to find user's location")
        }

    }

    override fun onMapReady(p0: GoogleMap?) {
        mMap = p0

        if (hasLocationPermission) getDeviceLocation()
        else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) getDeviceLocation()

        if ((model as? Customer)?.address != null) {
            val sydney = LatLng((model as? Customer)?.address!!.lat, (model as? Customer)?.address!!.lng)
//			Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 19.0f))

            //Draw polygon here
            val path = GetPathFromLocation(LatLng(tracker.latitude, tracker.longitude), sydney) { polyLine ->
                Utils.logger("Polyline: $polyLine")

                mMap?.addPolyline(polyLine)

                //Customer's location
                mMap?.addMarker(
                    MarkerOptions().position(sydney)
                        .title(model?.name)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                )?.showInfoWindow()

                //Rider's location
                mMap?.addMarker(
                    MarkerOptions().position(LatLng(tracker.latitude, tracker.longitude))
                        .title("My location")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                )?.showInfoWindow()

            }
            path.execute()

            //For debugging purposes only
            val url = path.getUrl(LatLng(tracker.latitude, tracker.longitude), sydney)
            Utils.logger("URL: $url")
        }

        mMap?.setOnMarkerClickListener { marker ->
            Utils.showMessage(context, marker.snippet)
            return@setOnMarkerClickListener true
        }
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
        if (requestCode == RC_LOC) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hasLocationPermission = true
                getDeviceLocation()
            }
        } else if (requestCode == RC_SMS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hasSMSPermission = true
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.tracker_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_maps_intent ->
                if (model != null) {
                    val s = userLocation?.replace(' ', '+')
                    Utils.logger(s)
                    val gmmIntentUri = Uri.parse(
                        String.format(
                            Locale.getDefault(),
                            "google.navigation:q=%f,%f",
                            (model as? Customer)?.address?.lat,
                            (model as? Customer)?.address?.lng
                        )
                    )
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    startActivity(mapIntent)
                } else {
                    Utils.showMessage(context, "User cannot be found")
                }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val EXTRA_CUSTOMER_ORDER = "EXTRA_CUSTOMER_ORDER"
        const val EXTRA_CUSTOMER = "EXTRA_CUSTOMER"
        const val EXTRA_ORDER = "EXTRA_ORDER"
        private const val RC_LOC = 3
        private const val RC_SMS = 4
    }
}