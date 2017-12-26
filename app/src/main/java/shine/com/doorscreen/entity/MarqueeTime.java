package shine.com.doorscreen.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.provider.BaseColumns;

/**
 * author:
 * 时间:2017/11/30
 * qq:1220289215
 * 类描述：跑马灯有效时间段 外键为marqueeId
 */
@Entity(tableName = "marquee_time",foreignKeys = {
        @ForeignKey(entity = Marquee.class,
                parentColumns ="marqueeid" ,
        childColumns = "marqueeid",
        onDelete = ForeignKey.CASCADE)},
        indices={@Index(value = "marqueeid")})
public class MarqueeTime {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = BaseColumns._ID)
    public long id;
    private int marqueeid;
    private String start;
    private String stop;


    public MarqueeTime() {
    }


    public int getMarqueeid() {
        return marqueeid;
    }

    public void setMarqueeid(int marqueeid) {
        this.marqueeid = marqueeid;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getStop() {
        return stop;
    }

    public void setStop(String stop) {
        this.stop = stop;
    }

    @Override
    public String toString() {
        return "MarqueeTime{" +
                "id=" + id +
                ", marqueeId=" + marqueeid +
                ", start='" + start + '\'' +
                ", stop='" + stop + '\'' +
                '}';
    }
}
