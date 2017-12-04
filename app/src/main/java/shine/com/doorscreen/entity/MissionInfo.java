package shine.com.doorscreen.entity;

/**
 * author:
 * 时间:2017/11/27
 * qq:1220289215
 * 类描述：单个视频宣教信息，一个播单
 */

public class MissionInfo {

    /**
     * action : missioninfo
     * type : 2
     * missionid : 22
     * data : {"tasktype":1,"startdate":"2016-08-02","stopdate":"2016-08-12","playtimes":[{"start":"08:00","stop":"18:00"},{"start":"19:00","stop":"20:00"},{"start":"21:00","stop":"21:10"},{"start":"22:10","stop":"23:10"}],"source":[{"name":"功夫熊猫","type":1,"src":"/ftproot/video/xm.mpg","life":66}]}
     * sender : platform
     */

    private String action;
    private int type;
    private int missionid;
    private Mission data;
    private String sender;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public int getType() {
        return type;
    }



    public int getMissionid() {
        return missionid;
    }

    public void setMissionid(int missionid) {
        this.missionid = missionid;
    }

    public Mission getData() {
        return data;
    }

    public void setData(Mission data) {
        this.data = data;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }


}
