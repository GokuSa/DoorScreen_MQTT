package shine.com.doorscreen.entity;

import java.util.List;

/**
 * author:
 * 时间:2017/7/13
 * qq:1220289215
 * 类描述：
 */

public class SystemInfo {

    /**
     * action : acceptsystemvolume
     * datalist : [{"day":0,"start":"00:00","stop":"07:00","value":50},{"day":1,"start":"00:00","stop":"07:00","value":50},{"day":2,"start":"00:00","stop":"07:00","value":50},{"day":3,"start":"00:00","stop":"07:00","value":50},{"day":4,"start":"00:00","stop":"07:00","value":50},{"day":5,"start":"00:00","stop":"07:00","value":50},{"day":6,"start":"00:00","stop":"07:00","value":50},{"day":0,"start":"18:00","stop":"23:59","value":50},{"day":1,"start":"18:00","stop":"23:59","value":50},{"day":2,"start":"18:00","stop":"23:59","value":50},{"day":3,"start":"18:00","stop":"23:59","value":50},{"day":4,"start":"18:00","stop":"23:59","value":50},{"day":5,"start":"18:00","stop":"23:59","value":50},{"day":6,"start":"18:00","stop":"23:59","value":50},{"day":0,"start":"07:00","stop":"18:00","value":50},{"day":1,"start":"07:00","stop":"18:00","value":50},{"day":2,"start":"07:00","stop":"18:00","value":50},{"day":3,"start":"07:00","stop":"18:00","value":50},{"day":4,"start":"07:00","stop":"18:00","value":50},{"day":5,"start":"07:00","stop":"18:00","value":50},{"day":6,"start":"07:00","stop":"18:00","value":50}]
     * sender : platform
     */

    private String action;
    private String sender;
    private int clienttype;
    private List<SystemLight> datalist;

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

    public List<SystemLight> getDatalist() {
        return datalist;
    }

    public void setDatalist(List<SystemLight> datalist) {
        this.datalist = datalist;
    }

    public int getClienttype() {
        return clienttype;
    }
}
