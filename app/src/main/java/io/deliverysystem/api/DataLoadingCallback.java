package io.deliverysystem.api;

/**
 * Callback for loading data for data source
 */
public interface DataLoadingCallback {
	void startLoading();
	
	void stopLoading();
}
