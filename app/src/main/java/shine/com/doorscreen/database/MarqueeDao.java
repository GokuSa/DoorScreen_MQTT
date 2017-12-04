package shine.com.doorscreen.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import shine.com.doorscreen.mqtt.bean.Marquee;

/**
 * author:
 * 时间:2017/9/29
 * qq:1220289215
 * 类描述：跑马灯的数据库操作
 */
@Dao
public interface MarqueeDao {
    //插入单个跑马灯
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMarquee(Marquee marquee);

    //插入跑马灯列表
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Marquee> marquees);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateMarquee(Marquee marquee);

    @Delete
    void deleteMarquee(Marquee marquee);

    @Query("delete from marquee")
    void deleteAllMarquees();

    //检索当天在有效日期内的跑马灯，到明天零点需再次检索
    @Query("select * from marquee where date(:currentDate) between date(startDate) and date(stopDate) and status =0")
    List<Marquee> queryValidMarquee(String currentDate);

    @Query("update marquee set status=-1 where marqueeId=:marqueeId")
    void stopMarquee(int marqueeId);

    @Query("delete from marquee  where marqueeId=:marqueeId")
    void deleteMarquee(int marqueeId);
}
