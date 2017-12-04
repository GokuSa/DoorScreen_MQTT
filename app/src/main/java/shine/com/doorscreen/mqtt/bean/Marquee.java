package shine.com.doorscreen.mqtt.bean;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.util.List;

/**
 * author:
 * 时间:2017/9/29
 * qq:1220289215
 * 类描述：跑马灯信息
 * 如果字段发生变化 set get 方法也要更新 否则找不到
 */
@Entity(tableName = "marquee")
public class Marquee {
//    private static final String TAG = "Marquee";
    //设置成自动增长很重要，否则加入策略为replace时只能存最后一条
    /*@PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = BaseColumns._ID)
    public long id;*/
    @PrimaryKey
    private int marqueeid;
    private String message;
    private String startdate;
    private String stopdate;
//    private String startTime;
//    private String stopTime;
    private int status=0;
    @Ignore
    private List<MarqueeTime> playtimes;
    public Marquee() {}

    @Ignore
    public Marquee(int marqueeId, String message, String startDate, String stopDate, String startTime, String stopTime, int status) {
//        this.marqueeId = marqueeId;
        this.message = message;
//        this.startDate = startDate;
//        this.stopDate = stopDate;
//        this.startTime = startTime;
//        this.stopTime = stopTime;
        this.status = status;
    }

    public int getMarqueeid() {
        return marqueeid;
    }

    public void setMarqueeid(int marqueeid) {
        this.marqueeid = marqueeid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public void setPlaytimes(List<MarqueeTime> playtimes) {
        this.playtimes = playtimes;
    }

   /*  public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getStopTime() {
        return stopTime;
    }

    public void setStopTime(String stopTime) {
        this.stopTime = stopTime;
    }*/

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<MarqueeTime> getPlaytimes() {
        return playtimes;
    }

    @Override
    public String toString() {
        return "Marquee{" +
//                "id=" + id +
                ", marqueeId=" + marqueeid +
                ", message='" + message + '\'' +
                ", startDate=" + startdate +
                ", stopDate=" + stopdate +
//                ", startTime='" + startTime + '\'' +
//                ", stopTime='" + stopTime + '\'' +
                ", status=" + status +
                '}';
    }
}
