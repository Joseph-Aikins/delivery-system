package io.deliverysystem.data.room;

import androidx.room.TypeConverter;
import io.deliverysystem.data.LocationAddress;

import java.util.Locale;

import static io.deliverysystem.util.Utils.logger;

public class TypeConverterFactory {
	
	@TypeConverter
	public LocationAddress toAddress(String location) {
		if (location == null) {
			return null;
		}
		logger(location);
		String[] pieces = location.split(",");
		double lat = Double.parseDouble(pieces[0]);
		double lng = Double.parseDouble(pieces[1]);
		
		return new LocationAddress(lat, lng);
	}
	
	@TypeConverter
	public String toString(LocationAddress address) {
		if (address == null) {
			return "";
		}
		logger(address);
		return String.format(Locale.getDefault(), "%.2f,%.2f", address.getLat(), address.getLng());
	}
}
