package shine.com.doorscreen.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.provider.BaseColumns;

/**
 * author:
 * 时间:2017/7/11
 * qq:1220289215
 * 类描述：
 */
@Entity(tableName = "ward")
public class Ward {
    //主键没有设置为自动增长，数据库只会有一条数据，新插入的会更新当前数据
    @PrimaryKey
    @ColumnInfo(name = BaseColumns._ID)
    private long id;
    private String department="";
    private String roomId="";
    private String roomname="";
    private String morning="";
    private String noon="";
    private String night="";
    //呼叫提示，为空显示探视时间
    private String callTip="";
    @Ignore
    public Ward() {
    }

    public Ward(String department, String roomId, String roomname,  String morning, String noon, String night) {
        this.department = department;
        this.roomId = roomId;
        this.roomname = roomname;
        this.morning = morning;
        this.noon = noon;
        this.night = night;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomname() {
        return roomname;
    }

    public void setRoomname(String roomname) {
        this.roomname = roomname;
    }

    public String getMorning() {
        return morning;
    }

    public void setMorning(String morning) {
        this.morning = morning;
    }

    public String getNoon() {
        return noon;
    }

    public void setNoon(String noon) {
        this.noon = noon;
    }

    public String getNight() {
        return night;
    }

    public void setNight(String night) {
        this.night = night;
    }

    public String getCallTip() {
        return callTip;
    }

    public void setCallTip(String callTip) {
        this.callTip = callTip;
    }

    @Override
    public String toString() {
        return "Ward{" +
                "id=" + id +
                ", department='" + department + '\'' +
                ", roomId='" + roomId + '\'' +
                ", roomname='" + roomname + '\'' +
                ", morning='" + morning + '\'' +
                ", noon='" + noon + '\'' +
                ", night='" + night + '\'' +
                ", callTip='" + callTip + '\'' +
                '}';
    }
}
