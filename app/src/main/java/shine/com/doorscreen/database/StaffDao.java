package shine.com.doorscreen.database;

/**
 * author:
 * 时间:2017/7/12
 * qq:1220289215
 * 类描述：医院工作人员
 */

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import shine.com.doorscreen.entity.Staff;

@Dao
public interface StaffDao {

    @Query("select * from staff")
    LiveData<List<Staff>> loadAllStaff();

    @Insert
    void insertAll(List<Staff> staffs) ;



    @Query("delete from staff")
    void deleteAll();
}
