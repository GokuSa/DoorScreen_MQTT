package shine.com.doorscreen.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

/**
 * author:
 * 时间:2017/12/18
 * qq:1220289215
 * 类描述：多媒体播单时间日期的信息， 包含视频和图片播单的id，播放日期和时间 状态
 */
@Entity(tableName = "media_time")
public class MutilMediaTime {
    /*@PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = BaseColumns._ID)
    public long id;*/
    //    播单id
    @PrimaryKey
    private int missionid;
    private String startdate;
    private String stopdate;
    private String time;
//    播单状态 0 正常  -1暂停
    private int status=0;
//    多媒体名称 用来下载完成更新status状态
    private String name;

    public MutilMediaTime() {
    }

    @Ignore
    public MutilMediaTime(String time, int missionid,String startdate,String stopdate,/*,String name*/ int type) {
       this.time=time;
        this.missionid = missionid;
        this.startdate = startdate;
        this.stopdate = stopdate;
        status=type==2?0:-1;
//        this.name=name;
    }

    public int getMissionid() {
        return missionid;
    }

    public void setMissionid(int missionid) {
        this.missionid = missionid;
    }

    public String getStartdate() {
        return startdate;
    }

    public void setStartdate(String startdate) {
        this.startdate = startdate;
    }

    public String getStopdate() {
        return stopdate;
    }

    public void setStopdate(String stopdate) {
        this.stopdate = stopdate;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "MutilMediaTime{" +
                ", missionid=" + missionid +
                ", startdate='" + startdate + '\'' +
                ", stopdate='" + stopdate + '\'' +
                ", stoptime='" + time + '\'' +
                ", status=" + status +
                '}';
    }
}
