package shine.com.doorscreen.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

//重启参数
@Entity(tableName = "restart")
public class ReStart {

    @PrimaryKey
    private int day;
    private int reboot;
    private String rebootTime;

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getReboot() {
        return reboot;
    }

    public void setReboot(int reboot) {
        this.reboot = reboot;
    }

    public String getRebootTime() {
        return rebootTime;
    }

    public void setRebootTime(String rebootTime) {
        this.rebootTime = rebootTime;
    }

    @Override
    public String toString() {
        return "ReStart{" +
                "day=" + day +
                ", reboot=" + reboot +
                ", rebootTime='" + rebootTime + '\'' +
                '}';
    }
}