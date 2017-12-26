package shine.com.doorscreen.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import shine.com.doorscreen.entity.Patient;

/**
 * author:
 * 时间:2017/7/11
 * qq:1220289215
 * 类描述：病房的病人信息，实际描述的是床位信息
 */

@Dao
public interface PatientDao {

    @Query("select * from patient")
    List<Patient> getPatientList();

    @Query("select * from patient order by bedNum asc")
    LiveData<List<Patient>> loadPatients();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Patient> patients);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void replace(Patient patient);

    @Query("update patient set bedno=:bedNum where clientmac=:mac")
    void update(String bedNum,String mac);

    @Delete
    void delete(Patient patient);

    @Delete
    void deleteAll(List<Patient> patients);

    @Query("delete from patient")
    void deleteAll();
}
