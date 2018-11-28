package io.deliverysystem.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.transition.TransitionManager
import io.codelabs.util.bindView
import io.deliverysystem.R
import io.deliverysystem.core.BaseActivity
import io.deliverysystem.data.Customer
import io.deliverysystem.util.Utils


class UserSelectionActivity : BaseActivity() {
    private val container: ViewGroup by bindView(R.id.container)
    private val content: ViewGroup by bindView(R.id.content)
    private val loading: ProgressBar by bindView(R.id.loading)

    private lateinit var customer: Customer

    override fun getLayoutId(): Int {
        return R.layout.activity_user_selection
    }

    override fun onViewCreated(instanceState: Bundle?, intent: Intent?) {
        customer = Customer()
        Utils.logger(customer)
        toggleLoading(false)
    }

    fun signUp(view: View) {
        startActivity(Intent(this@UserSelectionActivity, CustomerAuthActivity::class.java))
        finishAfterTransition()
    }

    fun login(view: View) {
        startActivity(Intent(this@UserSelectionActivity, CustomerAuthActivity::class.java))
        finishAfterTransition()
    }

    fun riderLogin(view: View) {
        startActivity(Intent(this, RiderAuthActivity::class.java))
        finish()
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