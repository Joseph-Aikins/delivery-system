package io.deliverysystem.core;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.StorageReference;
import io.deliverysystem.api.location.GPSTracker;
import io.deliverysystem.data.room.Database;
import io.deliverysystem.util.Utils;

import javax.inject.Inject;

/**
 * Base {@link AppCompatActivity}
 */
public abstract class BaseActivity extends AppCompatActivity {
	
	@Inject
	public FirebaseFirestore firestore;
	@Inject
	public StorageReference reference;
	@Inject
	public FirebaseMessaging messaging;
	@Inject
	public FirebaseAuth auth;
	public Database database;
	
	private FirebaseAuth.AuthStateListener listener;
	@Inject
	public Context context;
	public GPSTracker tracker;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Set layout view for child class
		setContentView(getLayoutId());



		//Get default room database instance
		database = Database.getInstance(this);
		
		//Init dagger
		((DeliverySystemApplication) getApplication()).getAppComponent().inject(this);
		
		listener = firebaseAuth -> {
			FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
			if (firebaseUser == null) {
				//startActivity(new Intent(getApplicationContext(), UserSelectionActivity.class));
				Utils.logger(firebaseUser);
			}
		};
		
		tracker = new GPSTracker(this);
		
		// Callback for child class
		onViewCreated(savedInstanceState, getIntent());
	}
	
	/**
	 * Get active network state
	 *
	 * @return state
	 */
	public boolean isNetworkAvailable() {
		ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if (manager == null) {
			return false;
		}
		NetworkInfo networkInfo = manager.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnected();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if (listener != null) {
			auth.addAuthStateListener(listener);
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if (listener != null) {
			auth.removeAuthStateListener(listener);
		}
	}
	
	protected abstract int getLayoutId();
	
	protected abstract void onViewCreated(Bundle instanceState, Intent intent);
	
}
