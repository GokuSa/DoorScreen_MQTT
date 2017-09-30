package shine.com.doorscreen.mqtt.bean;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.provider.BaseColumns;

/**
 * author:
 * 时间:2017/9/29
 * qq:1220289215
 * 类描述：跑马灯信息
 */
@Entity(tableName = "marquee")
public class Marquee {
    private static final String TAG = "Marquee";
    //设置成自动增长很重要，否则加入策略为replace时只能存最后一条
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = BaseColumns._ID)
    public long id;
    private int marqueeId;
    private String message;
    private String startDate;
    private String stopDate;
    private String startTime;
    private String stopTime;
    private int status;

    public Marquee() {}

    @Ignore
    public Marquee(int marqueeId, String message, String startDate, String stopDate, String startTime, String stopTime, int status) {
        this.marqueeId = marqueeId;
        this.message = message;
        this.startDate = startDate;
        this.stopDate = stopDate;
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.status = status;
    }

    public int getMarqueeId() {
        return marqueeId;
    }

    public void setMarqueeId(int marqueeId) {
        this.marqueeId = marqueeId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getStopDate() {
        return stopDate;
    }

    public void setStopDate(String stopDate) {
        this.stopDate = stopDate;
    }

    public String getStartTime() {
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
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Marquee{" +
                "id=" + id +
                ", marqueeId=" + marqueeId +
                ", message='" + message + '\'' +
                ", startDate=" + startDate +
                ", stopDate=" + stopDate +
                ", startTime='" + startTime + '\'' +
                ", stopTime='" + stopTime + '\'' +
                ", status=" + status +
                '}';
    }
}
