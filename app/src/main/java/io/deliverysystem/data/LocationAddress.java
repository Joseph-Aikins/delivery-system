package io.deliverysystem.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Locale;

import androidx.annotation.NonNull;

public class LocationAddress implements Parcelable {
	//Initialize fields for latitude and longitude
	private double lat = 0.00, lng = 0.00;
	
	//Default constructor
	public LocationAddress() {
	}
	
	//Other constructor
	public LocationAddress(double lat, double lng) {
		this.lat = lat;
		this.lng = lng;
	}
	
	protected LocationAddress(Parcel in) {
		lat = in.readDouble();
		lng = in.readDouble();
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeDouble(lat);
		dest.writeDouble(lng);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	public static final Creator<LocationAddress> CREATOR = new Creator<LocationAddress>() {
		@Override
		public LocationAddress createFromParcel(Parcel in) {
			return new LocationAddress(in);
		}
		
		@Override
		public LocationAddress[] newArray(int size) {
			return new LocationAddress[size];
		}
	};
	
	public double getLat() {
		return lat;
	}
	
	public double getLng() {
		return lng;
	}
	
	public void setLat(double lat) {
		this.lat = lat;
	}
	
	public void setLng(double lng) {
		this.lng = lng;
	}
	
	@NonNull
	@Override
	public String toString() {
		return String.format(Locale.getDefault(), "LocationAddress: %.4f, %.4f", lat, lng);
	}
}
