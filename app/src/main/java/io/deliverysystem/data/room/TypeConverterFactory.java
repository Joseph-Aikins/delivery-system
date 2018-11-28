package io.deliverysystem.data.room;

import androidx.room.TypeConverter;
import io.deliverysystem.data.LocationAddress;
import io.deliverysystem.util.Utils;

import java.util.Locale;

import static io.deliverysystem.util.Utils.logger;

public class TypeConverterFactory {

    @TypeConverter
    public LocationAddress toAddress(String location) {
        if (location == null) {
            return null;
        }
        logger(location);
        double lat = 0;
        double lng = 0;
        try {
            String[] pieces = location.split(",");
            lat = Double.parseDouble(pieces[0]);
            lng = Double.parseDouble(pieces[1]);
        } catch (NumberFormatException e) {
            Utils.logger(e.getLocalizedMessage());
        }

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
