package io.deliverysystem.data.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import io.deliverysystem.data.Customer;
import io.deliverysystem.data.Rider;

import java.util.List;

@Dao
public interface DatabaseDao {

    @Query("SELECT * FROM customer WHERE uid IS :uid")
    LiveData<List<Customer>> getCustomer(String uid);

    @Query("SELECT * FROM rider WHERE uid IS :uid LIMIT 1")
    LiveData<List<Rider>> getRider(String uid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void registerCustomer(Customer customer);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void registerRider(Rider rider);

}
