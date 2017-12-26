package shine.com.doorscreen.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import shine.com.doorscreen.entity.MutilMedia;

/**
 * author:
 * 时间:2017/9/29
 * qq:1220289215
 * 类描述：多媒体信息的数据库操作
 * 以一个播单为单位  可能会包含多个多媒体信息
 * 插入之前先删除旧播单信息
 */
@Dao
public interface MutilMediaDao {

    //插入多媒体列表
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMission(List<MutilMedia> mutilMediaList);

    @Query("delete from media")
    void deleteAllMission();

    @Query("delete from media  where missionid=:missionid")
    void deleteMission(int missionid);

    @Query("select * from media where missionid=:missionid")
    List<MutilMedia> queryMedia(int missionid);
}
