package io.deliverysystem.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.transition.TransitionManager
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.iid.FirebaseInstanceId
import io.codelabs.util.bindView
import io.deliverysystem.R
import io.deliverysystem.core.BaseActivity
import io.deliverysystem.data.Customer
import io.deliverysystem.data.LocationAddress
import io.deliverysystem.util.Utils
import io.deliverysystem.util.Utils.showMessage
import org.jetbrains.anko.doAsync
import java.util.*

class CustomerAuthActivity : BaseActivity() {
    private val container: ViewGroup by bindView(R.id.container)
    private val content: ViewGroup by bindView(R.id.content)
    private val loading: ProgressBar by bindView(R.id.loading)
    private val login: Button by bindView(R.id.customer_login)

    private var customer: Customer = Customer()


    override fun getLayoutId(): Int {
        return R.layout.activity_customer_auth
    }

    override fun onViewCreated(instanceState: Bundle?, intent: Intent?) {
        login.setOnClickListener { doLogin() }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), RC_LOCATION
            )
        }
    }

    private fun doLogin() {
        showMessage(this, "Logging in...")

        toggleLoading(true)
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setLogo(R.mipmap.ic_launcher_round)
                .setTosAndPrivacyPolicyUrls(
                    "https://superapp.example.com/terms-of-service.html",
                    "https://superapp.example.com/privacy-policy.html"
                )
                .setIsSmartLockEnabled(false, true)
                .setAvailableProviders(
                    Arrays.asList(
                        AuthUI.IdpConfig.GoogleBuilder().build(),
                        AuthUI.IdpConfig.EmailBuilder().build(),
                        AuthUI.IdpConfig.PhoneBuilder().setDefaultCountryIso("gh").build()
                    )
                )
                .build(),
            RC_SIGN_IN
        )
    }

    // Hide or display the progress bar
    private fun toggleLoading(show: Boolean) {
        TransitionManager.beginDelayedTransition(container)
        if (show) {
            loading.visibility = View.VISIBLE
            content.visibility = View.GONE
        } else {
            loading.visibility = View.GONE
            content.visibility = View.VISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)


            // Successfully signed in
            if (resultCode == Activity.RESULT_OK) {
                // Sign in success, update UI with the signed-in user's information
                Utils.logger("signInWithCredential:success")
                val user = auth.currentUser
                updateUI(user)
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    toggleLoading(false)
                    Utils.showMessage(context, "User cannot be found")
                    return
                }

                //log token to console
                Utils.logger(response.idpToken)

                if (response.error!!.errorCode == ErrorCodes.NO_NETWORK) {
                    toggleLoading(false)
                    Utils.showMessage(context, "Check your internet connection")
                    return
                }

                toggleLoading(false)
                Utils.showMessage(context, "Sign-in error: " + response.error!!)
            }

        }

    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            val uid = user.uid

            //Add additional properties to rider data
            customer.setUid(uid)
            customer.setName(user.displayName)
            customer.setTimestamp(System.currentTimeMillis())
            customer.setEmail(user.email)
            customer.setPhone(user.phoneNumber ?: Utils.DEFAULT_PHONE_NUMBER)
            customer.token = FirebaseInstanceId.getInstance().token
            customer.address = LocationAddress(tracker.latitude, tracker.longitude)

            //Create a document in firestore riders database
            val document = firestore.collection(Utils.CUSTOMERS_REF).document(uid)

            //Extract the document id from the document reference
            document.set(customer).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    toggleLoading(false)
                    doAsync {
                        database.dao().registerCustomer(customer);
                    }

                    val metadata = auth.currentUser!!.metadata
                    if (metadata != null && metadata.creationTimestamp == metadata.lastSignInTimestamp) {
                        // The user is new, show them a fancy intro screen!
                        //Navigate to the home screen when done
                        startActivity(Intent(context, LocationActivity::class.java))
                    } else {
                        // This is an existing user, show them a welcome back screen.
                        //Navigate to the home screen when done
                        startActivity(Intent(context, LocationActivity::class.java))
                    }

                    finish()
                } else {
                    toggleLoading(false)
                    //Show error message to the user
                    Utils.showMessage(context, "Failed to create customer account")
                }
            }.addOnFailureListener { e ->
                toggleLoading(false)
                //Show error message to the user
                Utils.showMessage(context, "Unable to create user account")
            }

        } else {
            toggleLoading(false)
            //Show error message to the user
            Utils.showMessage(context, "Failed to create customer account")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RC_LOCATION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Utils.logger("Location permission")
        }
    }

    companion object {
        private const val RC_LOCATION = 9
        private const val RC_SIGN_IN = 4
    }
}