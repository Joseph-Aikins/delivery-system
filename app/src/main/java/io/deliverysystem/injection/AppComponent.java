package io.deliverysystem.injection;

import dagger.Component;
import io.deliverysystem.core.BaseActivity;
import io.deliverysystem.core.DeliverySystemApplication;
import io.deliverysystem.injection.firebase.FirebaseModule;

/**
 * Dagger Application component
 */
@AppScope
@Component(modules = {ContextModule.class, FirebaseModule.class})
public interface AppComponent {
	
	//Application level injection
	void inject(DeliverySystemApplication application);
	
	void inject(BaseActivity activity);
}
