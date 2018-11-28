package io.deliverysystem.core;

import android.app.Application;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import io.deliverysystem.injection.AppComponent;
import io.deliverysystem.injection.ContextModule;
import io.deliverysystem.injection.DaggerAppComponent;
import io.deliverysystem.injection.firebase.FirebaseModule;
import io.deliverysystem.util.Utils;

import javax.inject.Inject;

/**
 * {@link Application} class for the Delivery System
 */
public class DeliverySystemApplication extends Application {
	private static final String TAG = "DeliverySystem";
	
	private AppComponent appComponent;
	@Inject
	FirebaseFirestore firestore;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		//Init Dagger here
		appComponent = DaggerAppComponent.builder()
				.contextModule(new ContextModule(this))
				.firebaseModule(new FirebaseModule())
				.build();
		
		//Inject component
		appComponent.inject(this);
		
		//Firebase Instance ID init
		Utils.logger(FirebaseInstanceId.getInstance().getId());
		
		Utils.registerToken(firestore);
	}
	
	// Getter for AppComponent
	public AppComponent getAppComponent() {
		return appComponent;
	}
}
