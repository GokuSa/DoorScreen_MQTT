package shine.com.doorscreen.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import shine.com.doorscreen.mqtt.bean.MutilMediaTime;

/**
 * author:
 * 时间:2017/9/29
 * qq:1220289215
 * 类描述：多媒体播放时间的数据库操作
 * 以一个播单为单位  本播单的播放时刻表
 * 发布的播单可能已存在 每次插入之前先删除旧的再插入  ？更好的更新方法
 *
 */
@Dao
public interface MutilMediaTimeDao {

    //插入多媒体播单时刻表
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMissionsTime(List<MutilMediaTime> mutilMediaTimes);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMissionTime(MutilMediaTime mutilMediaTimes);

    @Query("delete from media_time")
    void deleteAll();

    //检索当天在有效日期内的播单
    @Query("select * from media_time where date(:currentDate) between date(startdate) and date(stopdate) and status =0")
    List<MutilMediaTime> queryValidDate(String currentDate);

    @Query("update media_time set status=-1 where missionid=:missionid")
    void stopMissionTime(int missionid);

//    下载成功后设置状态为0
    @Query("update media_time set status=0 where name=:name")
    void activateMissionTime(String name );

    @Query("delete from media_time  where missionid=:missionid")
    void deleteMissionTime(int missionid);
}
