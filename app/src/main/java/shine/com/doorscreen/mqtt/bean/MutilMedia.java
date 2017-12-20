package shine.com.doorscreen.mqtt.bean;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.provider.BaseColumns;

import java.io.Serializable;

/**
 * author:
 * 时间:2017/12/18
 * qq:1220289215
 * 类描述：多媒体信息 含多媒体的名称 下载路径 本地路径  类型 1-视频 2-图片  及时间日期的信息
 * missionid 为外键 关联media_time
 * 与跑马灯不同  一个播单会对应多个多媒体素材
 */
@Entity(tableName = "media")
public class MutilMedia implements Serializable{
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = BaseColumns._ID)
    public long id;
    //    播单id
    private int missionid;
//    private String startdate;
//    private String stopdate;
//    private String starttime;
//    private String stoptime;
//    //    播单状态 0 正常  -1暂停
//    private int status;

    private String name;
//    类型 1-视频 2-图片
    private int type;
    //服务器路径
    private String src;
    //本地路徑
    private String path;

    public MutilMedia() {}

    @Ignore
    public MutilMedia(int missionid, String name, String url, int type, String path) {
        this.missionid=missionid;
        this.name=name;
        src=url;
        this.type = type;
        this.path = path;
    }

    public int getMissionid() {
        return missionid;
    }

    public void setMissionid(int missionid) {
        this.missionid = missionid;
    }

   /* public String getStartdate() {
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

    public String getStarttime() {
        return starttime;
    }

    public void setStarttime(String starttime) {
        this.starttime = starttime;
    }

    public String getStoptime() {
        return stoptime;
    }

    public void setStoptime(String stoptime) {
        this.stoptime = stoptime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
*/
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "MutilMedia{" +
                "missionid=" + missionid +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", src='" + src + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}
