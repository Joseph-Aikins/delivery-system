package io.deliverysystem.data.room;

import android.content.Context;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import io.deliverysystem.data.Customer;
import io.deliverysystem.data.Rider;
import io.deliverysystem.util.Utils;

@androidx.room.Database(entities = {Rider.class, Customer.class}, version = Utils.DATABASE_VERSION, exportSchema = false)
@TypeConverters(value = TypeConverterFactory.class)
public abstract class Database extends RoomDatabase {

    private static volatile Database instance = null;
    private static final Object LOCK = new Object();

    //Create an instance of the database
    public static Database getInstance(Context context) {

        if (instance == null) {
            synchronized (LOCK) {
                instance = Room.databaseBuilder(context, Database.class, Utils.DATABASE_NAME)
                        .allowMainThreadQueries()
                        .fallbackToDestructiveMigration()
                        .build();
            }
        }
        return instance;
    }


    public abstract DatabaseDao dao();
}
