package shine.com.doorscreen.mqtt.bean;

import java.util.List;

/**
 * author:
 * 时间:2017/7/21
 * qq:1220289215
 * 类描述：定时重启
 */

public class ReBoot {

    /**
     * action : accepttimedreboot
     * datalist : [{"day":"0","reboot":0,"rebootTime":""},{"day":"1","reboot":0,"rebootTime":""},{"day":"2","reboot":0,"rebootTime":""},{"day":"3","reboot":0,"rebootTime":""},{"day":"4","reboot":1,"rebootTime":"18:00"},{"day":"5","reboot":0,"rebootTime":""},{"day":"6","reboot":0,"rebootTime":""}]
     * sender : platform
     */

    private String action;
    private String sender;
    private int clienttype;
    private List<ReStart> datalist;

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

    public List<ReStart> getDatalist() {
        return datalist;
    }

    public void setDatalist(List<ReStart> datalist) {
        this.datalist = datalist;
    }

    public int getClienttype() {
        return clienttype;
    }

    public void setClienttype(int clienttype) {
        this.clienttype = clienttype;
    }
}
