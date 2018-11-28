package io.deliverysystem.injection.firebase;

import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import javax.inject.Inject;

import dagger.Module;
import dagger.Provides;
import io.deliverysystem.injection.AppScope;
import io.deliverysystem.util.Utils;

@Module
public class FirebaseModule {
	
	@Inject
	@AppScope
	@Provides
	FirebaseApp provideFirebaseApp(Context context) {
		return FirebaseApp.initializeApp(context);
	}
	
	/**
	 * {@link FirebaseFirestoreSettings} config
	 *
	 * @return settings
	 */
	@AppScope
	@Provides
	FirebaseFirestoreSettings provideFirestoreSettings() {
		return new FirebaseFirestoreSettings.Builder().setPersistenceEnabled(!Utils.APP_IS_DEBUG).setTimestampsInSnapshotsEnabled(true).build();
	}
	
	/**
	 * {@link FirebaseFirestore} config
	 *
	 * @return firestore
	 */
	@AppScope
	@Provides
	FirebaseFirestore provideFirestore(FirebaseFirestoreSettings settings, FirebaseApp app) {
		FirebaseFirestore instance = FirebaseFirestore.getInstance(app);
		instance.setFirestoreSettings(settings);
		return instance;
	}
	
	/**
	 * {@link StorageReference} config
	 *
	 * @return storage reference
	 */
	@AppScope
	@Provides
	StorageReference provideStorageReference(FirebaseApp app) {
		FirebaseStorage storage = FirebaseStorage.getInstance(app);
		return storage.getReference();
	}
	
	/**
	 * {@link FirebaseMessaging} config
	 *
	 * @return messaging service
	 */
	@AppScope
	@Provides
	FirebaseMessaging provideMessaging() {
		return FirebaseMessaging.getInstance();
	}
	
	/**
	 * {@link FirebaseAuth} config
	 *
	 * @return user authentication service
	 */
	@AppScope
	@Provides
	FirebaseAuth provideAuth() {
		return FirebaseAuth.getInstance();
	}
	
}
