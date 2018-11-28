package io.deliverysystem.api;

import javax.annotation.Nullable;

/**
 * Callback class for loading data from the database
 *
 * @param <T> callback content
 */
public interface ContentCallback<T> {
	
	void onError(@Nullable String message);
	
	void onSuccess(T results);
	
}
