package io.deliverysystem.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.lifecycle.Observer
import io.deliverysystem.R
import io.deliverysystem.core.BaseActivity
import io.deliverysystem.util.Utils.logger

class MainActivity : BaseActivity() {
    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun onViewCreated(instanceState: Bundle?, intent: Intent?) {
        //Hold the screen for a few seconds and then navigate the user to the next screen
        Handler().postDelayed({
            database.dao().getCustomer(auth.uid).observe(this@MainActivity, Observer {
                // Logger for utils
                logger(it)

                // Check for the type of user logged into the system
                if (it == null || it.isEmpty()) {
                    startActivity(
                        Intent(
                            this@MainActivity,
                            if (auth.currentUser == null) UserSelectionActivity::class.java else RiderHomeActivity::class.java
                        )
                    )
                    finishAfterTransition()
                } else {
                    if (auth.currentUser == null) {
                        startActivity(
                            Intent(
                                this@MainActivity,
                                UserSelectionActivity::class.java
                            )
                        )
                        finishAfterTransition()
                        return@Observer
                    } else {
                        var add = false
                        for (i in 0 until it.size) {
                            add = auth.uid == it[i].uid
                        }

                        if (add) {
                            startActivity(
                                Intent(
                                    this@MainActivity,
                                    LocationActivity::class.java
                                )
                            )
                            finishAfterTransition()
                            return@Observer
                        }
                    }
                }
            })
            finishAfterTransition()
        }, 1500)

    }

}
