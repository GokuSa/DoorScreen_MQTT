package shine.com.doorscreen.database;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

/**
 * author:
 * 时间:2017/9/29
 * qq:1220289215
 * 类描述：Room数据库使用的日期转化，把Date-》long存储到数据库 long-》Date封装成对象
 */

public class DateConverter {

    @TypeConverter
    public static Date toDate(Long timeStamp) {
        return null == timeStamp ? null : new Date(timeStamp);
    }

    public static Long toTimeStamp(Date date) {
        return null == date ? null : date.getTime();
    }
}
