package io.deliverysystem.api;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import io.deliverysystem.util.Utils;

public class DeliveryMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Utils.logger("New token received: " + s);
        Utils.registerToken(FirebaseFirestore.getInstance());
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //Message received
        Utils.logger(remoteMessage.getData());
    }

    @Override
    public void onMessageSent(String s) {
        //Message sent
        Utils.logger("Message sent from device: " + Utils.APP_DEVICE_MODEL + ", message: " + s);
    }
}
