package io.deliverysystem.injection;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import io.deliverysystem.core.DeliverySystemApplication;

/**
 * {@link Module} for dagger {@link Context}
 */
@Module
public class ContextModule {
	
	private DeliverySystemApplication application;
	
	public ContextModule(DeliverySystemApplication application){
		this.application = application;
	}
	
	/**
	 * Creates a {@link Context} module
	 * @return context of the application
	 */
	@AppScope
	@Provides
	public Context provideContext(){
		return application.getApplicationContext();
	}
}
