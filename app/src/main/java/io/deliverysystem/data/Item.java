package io.deliverysystem.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Item implements Model, Parcelable {
	
	private String name;
	private long timestamp;
	private String key;
	private double price;
	private String category;
	private boolean status;
	private long quantity;
	
	public Item() {
	}
	
	public Item(String name, String key, String category, double price, boolean status, long quantity) {
		this.timestamp = System.currentTimeMillis();
		this.key = key;
		this.name = name;
		this.price = price;
		this.category = category;
		this.status = status;
		this.quantity = quantity;
	}
	
	protected Item(Parcel in) {
		name = in.readString();
		timestamp = in.readLong();
		key = in.readString();
		price = in.readDouble();
		category = in.readString();
		status = in.readByte() != 0;
		quantity = in.readLong();
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeLong(timestamp);
		dest.writeString(key);
		dest.writeDouble(price);
		dest.writeString(category);
		dest.writeByte((byte) (status ? 1 : 0));
		dest.writeLong(quantity);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	public static final Creator<Item> CREATOR = new Creator<Item>() {
		@Override
		public Item createFromParcel(Parcel in) {
			return new Item(in);
		}
		
		@Override
		public Item[] newArray(int size) {
			return new Item[size];
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
	
	public double getPrice() {
		return price;
	}
	
	public long getQuantity() {
		return quantity;
	}
	
	public String getCategory() {
		return category;
	}
	
	public void setCategory(String category) {
		this.category = category;
	}
	
	public void setPrice(double price) {
		this.price = price;
	}
	
	public void setQuantity(long quantity) {
		this.quantity = quantity;
	}
	
	public void setStatus(boolean status) {
		this.status = status;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
}
