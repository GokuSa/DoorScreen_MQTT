package shine.com.doorscreen.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import shine.com.doorscreen.entity.Ward;

/**
 * author:
 * 时间:2017/7/11
 * qq:1220289215
 * 类描述：
 */
@Dao
public interface WardDao {

    @Query("select * from ward limit 1")
    Ward getRooms();

    @Query("select * from ward limit 1")
    LiveData<Ward> loadWard();

    @Query("select * from ward where _id in (:ids)")
    List<Ward> getRoomsById(String... ids);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Ward ward);

    @Update
    void update(Ward ward);

    @Delete
    void delete(Ward ward);

    @Query("delete from ward")
    void deleteAll();
}
