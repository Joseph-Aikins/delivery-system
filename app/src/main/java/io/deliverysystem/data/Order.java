package io.deliverysystem.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Order implements Model, Parcelable {
    private String name;
    private String key;
    private String phone;
    private String city;
    private String country;
    private String rider;
    private long timestamp;
    private boolean status;
    private List<Item> items;
    private LocationAddress address;

    //Default constructor
    public Order() {
    }

    public Order(String name, String key) {
        this.name = name;
        this.key = key;
        this.phone = "";
        this.city = "";
        this.country = "";
        this.rider = "";
        this.timestamp = System.currentTimeMillis();
        this.status = false;
        this.items = new ArrayList<>(0);
        this.address = new LocationAddress();
    }

    protected Order(Parcel in) {
        name = in.readString();
        key = in.readString();
        phone = in.readString();
        city = in.readString();
        country = in.readString();
        rider = in.readString();
        timestamp = in.readLong();
        status = in.readByte() != 0;
        items = in.createTypedArrayList(Item.CREATOR);
        address = in.readParcelable(LocationAddress.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(key);
        dest.writeString(phone);
        dest.writeString(city);
        dest.writeString(country);
        dest.writeString(rider);
        dest.writeLong(timestamp);
        dest.writeByte((byte) (status ? 1 : 0));
        dest.writeTypedList(items);
        dest.writeParcelable(address, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Order> CREATOR = new Creator<Order>() {
        @Override
        public Order createFromParcel(Parcel in) {
            return new Order(in);
        }

        @Override
        public Order[] newArray(int size) {
            return new Order[size];
        }
    };

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getUid() {
        return key;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return getUid();
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getRider() {
        return rider;
    }

    public void setRider(String rider) {
        this.rider = rider;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public LocationAddress getAddress() {
        return address;
    }

    public void setAddress(LocationAddress address) {
        this.address = address;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Order{" +
                "name='" + name + '\'' +
                ", key='" + key + '\'' +
                ", phone='" + phone + '\'' +
                ", city='" + city + '\'' +
                ", country='" + country + '\'' +
                ", rider='" + rider + '\'' +
                ", timestamp=" + timestamp +
                ", status=" + status +
                ", items=" + items +
                ", address=" + address +
                '}';
    }
}
