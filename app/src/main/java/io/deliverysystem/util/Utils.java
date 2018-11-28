package io.deliverysystem.util;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import io.deliverysystem.BuildConfig;
import io.deliverysystem.core.BaseActivity;
import io.deliverysystem.data.AccessToken;
import io.deliverysystem.ui.UserSelectionActivity;

import javax.annotation.Nullable;

/**
 * Utility class
 */
public final class Utils {
    public static final String DATABASE_NAME = "delivery_system.db";
    public static final int DATABASE_VERSION = 1;
    private static final String TAG = "Utils";

    public static final String APP_ID = BuildConfig.APPLICATION_ID;
    public static final int APP_VERSION_CODE = BuildConfig.VERSION_CODE;
    public static final String APP_VERIONS_NAME = BuildConfig.VERSION_NAME;
    public static final String APP_DEVICE_MODEL = Build.MODEL;
    private static final String APP_DEVICE = Build.DEVICE;
    public static final boolean APP_IS_DEBUG = BuildConfig.DEBUG;
    private static final String TOKEN_REF = "tokens";
    public static final String RIDERS_REF = "riders";
    public static final String CUSTOMERS_REF = "customers";
    public static final String ORDERS_REF = "orders";
    public static final String ORDER_AUTH_REF = "orders_authentication";
    public static final String USER_ORDERS_REF = "customer_order_log";
    public static final String DEFAULT_PHONE_NUMBER = "+233554022344"/*"+233500139807"*/;
    public static final String DEFAULT_EMAIL = "quabynahdennis@gmail.com"/*"+233500139807"*/;

    //Method for debugging
    public static void logger(Object o) {
        Log.d(TAG, "logger: " + String.valueOf(o));
    }

    //Show a quick message on the screen
    public static void showMessage(Context ctx, @Nullable Object o) {
        Toast.makeText(ctx, String.valueOf(o), Toast.LENGTH_SHORT).show();
    }

    public static void registerToken(FirebaseFirestore firestore) {
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                AccessToken accessToken = new AccessToken(APP_DEVICE, APP_DEVICE_MODEL, task1.getResult().getToken());
                firestore.collection(TOKEN_REF).document(FirebaseApp.getInstance().getPersistenceKey())
                        .set(accessToken)
                        .addOnCompleteListener(task -> logger("Access token stored successfully."))
                        .addOnFailureListener(e -> logger(e.getLocalizedMessage()));
            }
        });
    }

    public static void logout(BaseActivity context) {
        AuthUI.getInstance()
                .signOut(context)
                .addOnCompleteListener(task -> {
                    // user is now signed out
                    context.startActivity(new Intent(context, UserSelectionActivity.class));
                    context.finish();
                });
    }
}
