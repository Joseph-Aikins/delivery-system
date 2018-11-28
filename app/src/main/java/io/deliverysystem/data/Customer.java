package io.deliverysystem.data;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import com.google.firebase.iid.FirebaseInstanceId;

@Entity(tableName = "customer")
public class Customer implements Model, Parcelable {
	public String name;
	@PrimaryKey
	@NonNull
	public String uid;
	public long timestamp;
	public String token;
	public String profile;
	public String email;
	private LocationAddress address;
    public String phone;


    public Customer() {
	
	}
	
	@Ignore
	public Customer(String name,@NonNull String uid, String email, String phone) {
		this.name = name;
		this.uid = uid;
		this.email = email;
		this.phone = phone;
		this.timestamp = System.currentTimeMillis();
		this.token = FirebaseInstanceId.getInstance().getToken();
		this.address = new LocationAddress();
		this.profile = null;
	}
	
	protected Customer(Parcel in) {
		name = in.readString();
		uid = in.readString();
		timestamp = in.readLong();
		token = in.readString();
		profile = in.readString();
		email = in.readString();
		phone = in.readString();
		address = in.readParcelable(LocationAddress.class.getClassLoader());
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeString(uid);
		dest.writeLong(timestamp);
		dest.writeString(token);
		dest.writeString(profile);
		dest.writeString(email);
		dest.writeString(phone);
		dest.writeParcelable(address, flags);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	public static final Creator<Customer> CREATOR = new Creator<Customer>() {
		@Override
		public Customer createFromParcel(Parcel in) {
			return new Customer(in);
		}
		
		@Override
		public Customer[] newArray(int size) {
			return new Customer[size];
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
	
	public String getToken() {
		return token;
	}
	
	public String getProfile() {
		return profile;
	}
	
	public String getEmail() {
		return email;
	}
	
	public LocationAddress getAddress() {
		return address;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
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
	
	public void setProfile(String profile) {
		this.profile = profile;
	}
	
	public void setAddress(LocationAddress address) {
		this.address = address;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	@NonNull
	@Override
	public String toString() {
		return String.format("Customer(%s,%s)", name, uid);
	}
}
