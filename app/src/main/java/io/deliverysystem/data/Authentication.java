package io.deliverysystem.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link Customer} authentication data model
 */
public class Authentication implements Parcelable {
    public String customer;
    public String customerId;
    public String riderId;
    public String rider;
    public String email;
    public String userToken;
    public String code;
    public String key;
    public long timestamp;
    public List<String> content;

    public Authentication() {
    }

    public Authentication(String customer, String customerId, String riderId, String rider, String email, String userToken, String code, String key) {
        this.customer = customer;
        this.customerId = customerId;
        this.riderId = riderId;
        this.rider = rider;
        this.email = email;
        this.userToken = userToken;
        this.code = code;
        this.timestamp = System.currentTimeMillis();
        this.content = new ArrayList<>(0);
        this.key = key;
    }

    protected Authentication(Parcel in) {
        customer = in.readString();
        customerId = in.readString();
        riderId = in.readString();
        rider = in.readString();
        email = in.readString();
        userToken = in.readString();
        code = in.readString();
        timestamp = in.readLong();
        content = in.createStringArrayList();
        key = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(customer);
        dest.writeString(customerId);
        dest.writeString(riderId);
        dest.writeString(rider);
        dest.writeString(email);
        dest.writeString(userToken);
        dest.writeString(code);
        dest.writeLong(timestamp);
        dest.writeStringList(content);
        dest.writeString(key);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Authentication> CREATOR = new Creator<Authentication>() {
        @Override
        public Authentication createFromParcel(Parcel in) {
            return new Authentication(in);
        }

        @Override
        public Authentication[] newArray(int size) {
            return new Authentication[size];
        }
    };

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getRiderId() {
        return riderId;
    }

    public void setRiderId(String riderId) {
        this.riderId = riderId;
    }

    public String getRider() {
        return rider;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setRider(String rider) {
        this.rider = rider;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public List<String> getContent() {
        return content;
    }

    public void setContent(List<String> content) {
        this.content = content;
    }
}
