package shine.com.doorscreen.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import shine.com.doorscreen.mqtt.bean.MarqueeTime;

/**
 * author:
 * 时间:2017/11/30
 * qq:1220289215
 * 类描述：跑马灯多时间段数据的添加删除
 */
@Dao
public interface MarqueeTimeDao {


    //插入跑马灯列表
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<MarqueeTime> marqueeTimes);

    //检索跑马灯的时间
    @Query("select * from marquee_time where marqueeId =:marqueeId")
    List<MarqueeTime> queryMarqueeTime(int marqueeId);

    @Query("delete  from marquee_time where marqueeId =:marqueeId")
    void  deleteMarqueeTime(int marqueeId);

}
