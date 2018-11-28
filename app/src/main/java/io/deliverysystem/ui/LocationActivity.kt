package io.deliverysystem.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.text.format.DateUtils
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.codelabs.util.bindView
import io.deliverysystem.R
import io.deliverysystem.api.ContentCallback
import io.deliverysystem.api.DataLoadingCallback
import io.deliverysystem.api.OrdersRepository
import io.deliverysystem.core.BaseActivity
import io.deliverysystem.data.Customer
import io.deliverysystem.data.Item
import io.deliverysystem.data.LocationAddress
import io.deliverysystem.data.Order
import io.deliverysystem.util.Utils
import java.util.*
import kotlin.collections.ArrayList

class LocationActivity : BaseActivity(), OnMapReadyCallback, ContentCallback<MutableList<Customer>> {
    private val container: ViewGroup by bindView(R.id.container)
    private val toolbar: Toolbar by bindView(R.id.toolbar)
    private val grid: RecyclerView by bindView(R.id.grid)
    private val fab: FloatingActionButton by bindView(R.id.fab)

    private var mMap: GoogleMap? = null
    private var hasLocationPermission: Boolean = false
    private lateinit var repository: OrdersRepository


    private lateinit var adapter: OrdersAdapter


    override fun getLayoutId(): Int {
        return R.layout.activity_location
    }

    @SuppressLint("NewApi")
    override fun onViewCreated(instanceState: Bundle?, intent: Intent?) {
        setSupportActionBar(toolbar)

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

        fab.setOnClickListener { addOrder() }
        setupGrid()

    }

    private fun setupGrid() {
        //Setup grid
        val layoutManager = LinearLayoutManager(context)
        grid.layoutManager = layoutManager
        adapter = OrdersAdapter(this)
        grid.adapter = adapter
        grid.itemAnimator = DefaultItemAnimator()
        grid.addItemDecoration(DividerItemDecoration(context, layoutManager.orientation))
        grid.setHasFixedSize(true)

        //Load data form database server
        repository = OrdersRepository.getInstance(this)
        repository.getOrders(Objects.requireNonNull(auth.uid!!), this, adapter)
    }

    override fun onMapReady(p0: GoogleMap?) {
        mMap = p0

        if (hasLocationPermission) getDeviceLocation()
        else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) getDeviceLocation()

        mMap?.setOnMarkerClickListener { marker ->
            Utils.showMessage(context, marker.snippet)
            return@setOnMarkerClickListener true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RC_LOC && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            hasLocationPermission = true
            getDeviceLocation()
        }
    }

    private fun addOrder() {
        Utils.showMessage(context, "Adding new order")
        //Get the current user
        val user = auth.currentUser

        //Create a list of order items
        val items: List<Item> = ArrayList(0)

        //Create an order containing the items created above
        val order =
            Order(/*user == null || user.getDisplayName() == null ? user.getEmail() : user.getDisplayName()*/ "New shopping item",
                user?.uid
            )

        //Set location for each order
        order.address = LocationAddress(tracker.latitude, tracker.longitude)

        //Add item to order
        order.items = items

        //Set order timestamp
        order.timestamp = System.currentTimeMillis()

        //Log order to console
        Utils.logger(order)

        repository.addOrder(order, auth.uid!!, object : ContentCallback<Void?> {
            override fun onSuccess(results: Void?) {
                Utils.showMessage(context, "Order saved successfully")
            }

            override fun onError(message: String?) {
                Utils.showMessage(context, message)
            }
        })
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

    override fun onError(message: String?) {
        if (message == null) return
        Utils.showMessage(context, message)
    }

    override fun onSuccess(results: MutableList<Customer>?) {
        if (results == null) return

        //Add content to adapter and update UI
        adapter.addAndResort(results)

        mMap?.clear()

        for (item in results) {
            val address = item.address
            if (address != null) {
                val options = MarkerOptions()
                    .position(LatLng(address.lat, address.lng))
                    .title(item.getName())
                    .snippet(item.getUid())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))

                mMap?.addMarker(options)?.showInfoWindow()
            }

            var items: MutableList<Order> = ArrayList(0)
            firestore.collection(
                String.format(
                    Locale.getDefault(),
                    "%s/%s/%s",
                    Utils.ORDERS_REF,
                    item.uid,
                    Utils.USER_ORDERS_REF
                )
            ).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (task.result != null) {
                            items = task.result?.toObjects(Order::class.java)?.toMutableList() ?: ArrayList(0)
                        }

                    }
                }.addOnFailureListener { Utils.logger(it.localizedMessage) }

            mMap?.setOnMarkerClickListener {
                val intent = Intent(context, TrackingActivity::class.java)
                intent.putExtra(TrackingActivity.EXTRA_CUSTOMER, item)
                intent.putParcelableArrayListExtra(
                    TrackingActivity.EXTRA_CUSTOMER_ORDER,
                    items as ArrayList<out Parcelable>
                )
                startActivity(intent)
                return@setOnMarkerClickListener true
            }
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

    class OrdersAdapter(private val host: LocationActivity) : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
        DataLoadingCallback {
        val orders = ArrayList<Customer>(0)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {

                TYPE_EMPTY_LIST -> EmptyViewHolder(
                    LayoutInflater.from(host).inflate(
                        R.layout.item_order_empty,
                        parent,
                        false
                    )
                )
                TYPE_ORDER_ITEM -> OrderItemViewHolder(
                    LayoutInflater.from(host).inflate(
                        R.layout.item_order,
                        parent,
                        false
                    )
                )

                else -> throw IllegalArgumentException("No viewholder found")
            }
        }

        override fun getItemCount(): Int {
            var count = 0
            //For empty views
            if (orders.isEmpty()) count++
            //For all order items
            else count += orders.size
            //Return count
            return count
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (getItemViewType(position)) {
                TYPE_EMPTY_LIST -> bindEmptyItemViewHolder(holder as EmptyViewHolder, position)
                TYPE_ORDER_ITEM -> bindOrderItemViewHolder(holder as OrderItemViewHolder, position)
            }
        }

        companion object {
            private const val TYPE_EMPTY_LIST = -1
            private const val TYPE_ORDER_ITEM = 0

        }

        override fun getItemViewType(position: Int): Int {
            if (orders.isEmpty()) return TYPE_EMPTY_LIST
            return TYPE_ORDER_ITEM
        }

        //Bind empty view
        private fun bindEmptyItemViewHolder(holder: EmptyViewHolder, position: Int) {
        }

        //Bind order items
        private fun bindOrderItemViewHolder(holder: OrderItemViewHolder, position: Int) {
            val order = orders[position]
            Utils.logger(order)

            holder.delete.setOnClickListener { Utils.showMessage(host, "Not available") }
            holder.edit.setOnClickListener { Utils.showMessage(host, "Not available") }

            //Get properties of order
            holder.name.text = order.name
            holder.timestamp.text = if (order.timestamp != -1L) DateUtils.getRelativeTimeSpanString(
                order.timestamp,
                System.currentTimeMillis(),
                DateUtils.SECOND_IN_MILLIS
            ) else "unavailable"

            val _orders: MutableList<Order> = ArrayList<Order>(0)

            host.firestore.collection(
                String.format(
                    Locale.getDefault(),
                    "%s/%s/%s",
                    Utils.ORDERS_REF,
                    order.uid,
                    Utils.USER_ORDERS_REF
                )
            ).get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        if (it.result != null) {
                            val items = it.result!!.toObjects(Order::class.java)
                            _orders.addAll(items)
                            if (orders.isEmpty()) return@addOnCompleteListener

                            if (items.isEmpty())
                                holder.items.text = String.format(Locale.getDefault(), "%d items found", items.size)
                            else
                                holder.items.text = String.format(
                                    Locale.getDefault(),
                                    "%s and %d others",
                                    items[0].name,
                                    items.size - 1
                                )
                        }

                    }
                }.addOnFailureListener { Utils.logger(it.localizedMessage) }

            holder.itemView.setOnClickListener {
                val intent = Intent(host, TrackingActivity::class.java)
                intent.putExtra(TrackingActivity.EXTRA_CUSTOMER, order)
                intent.putParcelableArrayListExtra(
                    TrackingActivity.EXTRA_CUSTOMER_ORDER,
                    _orders as ArrayList<out Parcelable>
                )
                host.startActivity(intent)
            }

        }


        //Entry point for new orders
        fun addAndResort(onlineOrders: MutableList<Customer>) {
            if (onlineOrders.isEmpty()) return

            for (item in onlineOrders) {
                var add = true
                for (i in 0 until orders.size) {
                    add = item.getUid() != orders[i].uid
                }

                if (add) {
                    orders.add(item)
                    notifyItemRangeChanged(0, onlineOrders.size)
                }
            }

        }

        override fun startLoading() {
            Utils.logger("Loading data from database server")
        }

        override fun stopLoading() {
            Utils.logger("Data loaded from database server")
        }

        class EmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

        class OrderItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val delete by bindView<ImageView>(R.id.order_delete)
            val edit by bindView<ImageView>(R.id.order_edit)
            val items by bindView<TextView>(R.id.order_items)
            val name by bindView<TextView>(R.id.order_name)
            val timestamp by bindView<TextView>(R.id.order_timestamp)

        }
    }

    companion object {
        private const val RC_LOC = 8
    }
}