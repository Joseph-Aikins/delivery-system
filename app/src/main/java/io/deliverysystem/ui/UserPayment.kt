package io.deliverysystem.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import io.codelabs.util.bindView
import io.deliverysystem.R
import io.deliverysystem.core.BaseActivity
import io.deliverysystem.data.Authentication
import io.deliverysystem.data.Order
import io.deliverysystem.util.Utils
import io.deliverysystem.util.Utils.logger

class UserPayment : BaseActivity() {
    private val code: EditText by bindView(R.id.code_field)
    private var authentication: Authentication? = null
    private var order: Order? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_user_payment
    }

    override fun onViewCreated(instanceState: Bundle?, intent: Intent?) {
        if (intent != null && intent.hasExtra(EXTRA_AUTH_MODEL)) {
            authentication = intent.getParcelableExtra<Authentication>(EXTRA_AUTH_MODEL)
            order = intent.getParcelableExtra(EXTRA_ORDER_MODEL)
            logger(authentication)
        }
    }

    fun doAuthentication(v: View) {
        if (authentication == null) {
            val shortCode = code.text.toString()

            if (shortCode.isEmpty()) {
                Utils.showMessage(this@UserPayment, "Please enter your short code")
            } else if (shortCode == authentication!!.code) {
                Utils.showMessage(this@UserPayment, "Credentials approved")
                firestore.collection(Utils.ORDER_AUTH_REF)
                    .document(authentication!!.key)
                    .update(
                        mapOf(
                            "status" to true
                        )
                    ).addOnCompleteListener { task ->
                        logger("Authentication updated successfully")
                    }

                firestore.collection(Utils.ORDERS_REF)
                    .document(order!!.key)
                    .update(
                        mapOf(
                            "status" to true
                        )
                    ).addOnCompleteListener { task ->
                        logger("Order updated successfully")
                    }
                finishAfterTransition()
            }
        } else {
            Utils.showMessage(this@UserPayment, "You have no authentication to verify")
            code.requestFocus()
        }
    }

    companion object {
        const val EXTRA_AUTH_MODEL = "EXTRA_AUTH_MODEL"
        const val EXTRA_ORDER_MODEL = "EXTRA_ORDER_MODEL"
    }
}