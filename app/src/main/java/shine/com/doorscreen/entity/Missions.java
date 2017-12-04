package shine.com.doorscreen.entity;

import java.util.List;

/**
 * author:
 * 时间:2017/11/27
 * qq:1220289215
 * 类描述：所有视频宣教信息 多个播单
 */

public class Missions {

    /**
     * action : missionlist
     * data : [{"missionid":22,"type":"2","tasktype":1,"orderno":2,"allday":1,"startdate":"2016-08-02","stopdate":"2016-08-12","starttime":"02:00:00","stoptime":"15:00:00","playtimes":[{"start":"08:00","stop":"18:00"},{"start":"19:00","stop":"20:00"},{"start":"21:00","stop":"21:10"},{"start":"22:10","stop":"23:10"}],"source":[{"name":"功夫熊猫","type":1,"src":"/ftproot/video/xm.mpg","life":66}]}]
     * sender : server
     */

    private String action;
    private String sender;
    private List<Mission> data;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public List<Mission> getData() {
        return data;
    }

    public void setData(List<Mission> data) {
        this.data = data;
    }


}
