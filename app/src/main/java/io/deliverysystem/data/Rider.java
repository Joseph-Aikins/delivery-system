package io.deliverysystem.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "rider")
public class Rider implements Model, Parcelable {
	public String name;
	@PrimaryKey
	@NonNull
	private String uid;
	private long timestamp;
	private LocationAddress address;
	private int pin;
	
	public Rider() {
	
	}
	
	@Ignore
	public Rider(String name, @NonNull String uid) {
		this.name = name;
		this.uid = uid;
		this.timestamp = System.currentTimeMillis();
		this.address = new LocationAddress();
		this.pin = 0;
	}
	
	protected Rider(Parcel in) {
		name = in.readString();
		uid = Objects.requireNonNull(in.readString() == null ? "" : in.readString());
		timestamp = in.readLong();
		pin = in.readInt();
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeString(uid);
		dest.writeLong(timestamp);
		dest.writeInt(pin);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	public static final Creator<Rider> CREATOR = new Creator<Rider>() {
		@Override
		public Rider createFromParcel(Parcel in) {
			return new Rider(in);
		}
		
		@Override
		public Rider[] newArray(int size) {
			return new Rider[size];
		}
	};
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String getUid() {
		return uid;
	}
	
	@Override
	public long getTimestamp() {
		return timestamp;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setUid(String uid) {
		this.uid = uid;
	}
	
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public void setAddress(LocationAddress address) {
		this.address = address;
	}
	
	public LocationAddress getAddress() {
		return address;
	}
	
	public int getPin() {
		return pin;
	}
	
	public void setPin(int pin) {
		this.pin = pin;
	}
	
	@NonNull
	@Override
	public String toString() {
		return String.format("Rider(%s,%s)", name, uid);
	}
}
