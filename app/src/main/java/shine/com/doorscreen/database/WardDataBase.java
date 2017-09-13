package shine.com.doorscreen.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.text.TextUtils;

import shine.com.doorscreen.mqtt.bean.Patient;
import shine.com.doorscreen.mqtt.bean.ReStart;
import shine.com.doorscreen.mqtt.bean.Staff;
import shine.com.doorscreen.mqtt.bean.Ward;


/**
 * author:
 * 时间:2017/7/11
 * qq:1220289215
 * 类描述：
 */
@Database(entities = {Ward.class, Staff.class, Patient.class, ReStart.class},version =8,exportSchema = false)
public abstract class WardDataBase extends RoomDatabase {

    public abstract WardDao ward();
    public abstract PatientDao patient();
    public abstract StaffDao staff();

    public abstract ReStartDao reStartDao();
    private static WardDataBase sRoomDataBase;

    public synchronized static WardDataBase INSTANCE(Context context) {
        if (sRoomDataBase == null) {
            sRoomDataBase= Room.databaseBuilder(context.getApplicationContext(),WardDataBase.class,"Ward")
                    .allowMainThreadQueries()
                    .build();
        }
        return sRoomDataBase;
    }

    public void clear(String tableName) {
        if (!TextUtils.isEmpty(tableName)) {
            sRoomDataBase.getOpenHelper().getWritableDatabase().execSQL("delete from "+tableName);
        }
    }
}
