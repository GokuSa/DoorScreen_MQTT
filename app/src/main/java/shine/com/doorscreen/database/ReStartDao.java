package shine.com.doorscreen.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import shine.com.doorscreen.mqtt.bean.ReStart;

/**
 * author:
 * 时间:2017/7/21
 * qq:1220289215
 * 类描述：
 */
@Dao
public interface ReStartDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ReStart> reStartList);

    @Query("select * from restart")
    LiveData<List<ReStart>> loadAll();

    @Query("select * from restart")
    List<ReStart> getAll();

    @Query("delete from restart")
    void deleteAll();
}
