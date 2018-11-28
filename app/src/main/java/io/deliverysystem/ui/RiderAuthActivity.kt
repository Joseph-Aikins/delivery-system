package io.deliverysystem.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.transition.TransitionManager
import io.codelabs.util.bindView
import io.deliverysystem.R
import io.deliverysystem.core.BaseActivity
import io.deliverysystem.data.Rider
import io.deliverysystem.util.Utils
import io.deliverysystem.util.Utils.logger
import org.jetbrains.anko.doAsync


class RiderAuthActivity : BaseActivity() {
    private val loginBtn: Button by bindView(R.id.rider_login_btn)
    private val container: ViewGroup by bindView(R.id.container)
    private val content: ViewGroup by bindView(R.id.content)
    private val username: EditText by bindView(R.id.username)
    private val password: EditText by bindView(R.id.password)
    private val loading: ProgressBar by bindView(R.id.loading)

    private var rider: Rider = Rider()


    override fun getLayoutId(): Int {
        return R.layout.activity_rider_auth
    }

    override fun onViewCreated(instanceState: Bundle?, intent: Intent?) {
        loginBtn.setOnClickListener { doLoginOrSignUp() }
        username.addTextChangedListener(listener)
        password.addTextChangedListener(listener)
    }

    private val listener: TextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {

        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            setButtonState()
        }
    }

    private fun setButtonState() {
        loginBtn.isEnabled = username.text.toString().isNotEmpty() &&
                password.text.toString().isNotEmpty() &&
                password.text.toString().length == 8
    }

    private fun doLoginOrSignUp() {
        Utils.showMessage(this, "Logging in...")

        //Extract username and password values
        val riderUsername = username.text.toString()
        val riderPwd = password.text.toString()
        toggleLoading(true)

        firestore.collection(Utils.RIDERS_REF).whereEqualTo("name", riderUsername)
            .limit(1).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val result = task.result
                    if (result != null && !result.documents.isEmpty()) {

                        //Obtain rider account details
                        rider = result.documents[0].toObject(Rider::class.java) ?: Rider()

                        doAsync {
                            database.dao().registerRider(rider)
                        }

                        //Sign in rider with the credentials obtained
                        auth.signInWithEmailAndPassword(rider.getName(), rider.pin.toString())
                            .addOnCompleteListener { task1 ->

                                if (task1.isSuccessful) {
                                    toggleLoading(false)

                                    //Navigate to the home screen when done
                                    startActivity(Intent(context, RiderHomeActivity::class.java))
                                    finish()
                                } else {
                                    toggleLoading(false)
                                    //Show error message to the user
                                    Utils.showMessage(context, "Failed to retrieve rider information")
                                }
                            }
                    } else {
                        //No rider exists with the credentials. create a new one
                        createNewRider(riderUsername, riderPwd)
                    }
                }
            }.addOnFailureListener { e ->
                toggleLoading(false)
                //Show error message to the user
                Utils.showMessage(context, e.localizedMessage)
            }
    }

    private fun createNewRider(usr: String, pwd: String) {
        if (!TextUtils.isDigitsOnly(pwd)) {
            toggleLoading(false)
            Utils.showMessage(context, "PIN can only be digits")
            return
        }

        //Create a new user account
        auth.createUserWithEmailAndPassword(usr, pwd).addOnCompleteListener { authTask ->
            if (authTask.isSuccessful) {
                val authResult = authTask.result!!.user
                if (authResult != null) {
                    val uid = authResult.uid

                    //Add additional properties to rider data
                    rider.uid = uid
                    rider.setName(authResult.displayName ?: usr)
                    rider.pin = Integer.parseInt(pwd)
                    rider.timestamp = System.currentTimeMillis()

                    //Create a document in firestore riders database
                    val document = firestore.collection(Utils.RIDERS_REF).document(uid)


                    //Extract the document id from the document reference
                    document.set(rider).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            toggleLoading(false)
                            doAsync {
                                database.dao().registerRider(rider)
                            }

                            //Navigate to the home screen when done
                            startActivity(Intent(context, RiderHomeActivity::class.java))
                            finish()
                        } else {
                            toggleLoading(false)
                            //Show error message to the user
                            Utils.showMessage(context, "Failed to create rider account")
                        }
                    }.addOnFailureListener { e ->
                        toggleLoading(false)
                        //Show error message to the user
                        Utils.showMessage(context, "Unable to create user account")
                    }

                } else {
                    toggleLoading(false)
                    //Show error message to the user
                    Utils.showMessage(context, "Failed to create rider account")
                }
            }
        }.addOnFailureListener { ex ->
            toggleLoading(false)
            //Show error message to the user
            Utils.showMessage(context, ex.localizedMessage)
            logger(ex.cause)
        }

    }

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

}