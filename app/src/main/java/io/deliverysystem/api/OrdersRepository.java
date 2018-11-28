package io.deliverysystem.api;

import com.google.firebase.firestore.DocumentReference;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import androidx.annotation.NonNull;
import io.deliverysystem.core.BaseActivity;
import io.deliverysystem.data.Customer;
import io.deliverysystem.data.Order;
import io.deliverysystem.util.Utils;

/**
 * {@link io.deliverysystem.data.Order} repository class
 */
public class OrdersRepository {
	
	private BaseActivity host;
	private static final Object LOCK = new Object();
	private static OrdersRepository instance = null;
	
	private OrdersRepository(BaseActivity activity) {
		this.host = activity;
	}
	
	public void getOrders(@NonNull @NotNull String uid, ContentCallback<List<Customer>> callback, DataLoadingCallback dataLoadingCallback) {
		dataLoadingCallback.startLoading();
		host.firestore.collection(Utils.CUSTOMERS_REF)/*.document(uid).collection(Utils.USER_ORDERS_REF)*/
				.addSnapshotListener((queryDocumentSnapshots, e) -> {
					dataLoadingCallback.stopLoading();
					
					if (e != null) {
						dataLoadingCallback.stopLoading();
						callback.onSuccess(null);
						callback.onError(e.getLocalizedMessage());
						return;
					}
					
					if (queryDocumentSnapshots != null) {
						dataLoadingCallback.stopLoading();
						callback.onSuccess(queryDocumentSnapshots.toObjects(Customer.class));
					}
					
				});
	}
	
	public void addOrder(Order order, @NonNull @NotNull String uid, ContentCallback<Void> callback) {
		DocumentReference document = host.firestore.collection(Utils.ORDERS_REF).document(uid).collection(Utils.USER_ORDERS_REF).document();
		order.setKey(document.getId());
		document.set(order).addOnCompleteListener(task -> callback.onSuccess(null))
				.addOnFailureListener(e -> callback.onError(e.getLocalizedMessage()));
	}
	
	public static OrdersRepository getInstance(BaseActivity activity) {
		if (instance == null) {
			synchronized (LOCK) {
				instance = new OrdersRepository(activity);
			}
		}
		return instance;
	}
}
