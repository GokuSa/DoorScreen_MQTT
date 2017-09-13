package shine.com.doorscreen.mqtt.bean;

/**
 * author:
 * 时间:2017/7/4
 * qq:1220289215
 * 类描述：后台发来的转组消息，取消取消之前的订阅，根据新消息重新订阅
 */

public class Transfer {

    /**
     * action : transferinsystem
     * transfertype : 0
     * departid : 313
     * departname : 消化内科（消化内科）
     * roomid : 332
     * roomname : 12病房
     * clienttype : 13
     * clientip : 172.168.55.39
     * clientmac : E06417050201
     * sender : platform
     */

    private String action;
    private int transfertype;
    private String departid;
    private String departname;
    private String roomid;
    private String roomname;
    private String clienttype;
    private String clientip;
    private String clientmac;
    private String sender;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public int getTransfertype() {
        return transfertype;
    }

    public void setTransfertype(int transfertype) {
        this.transfertype = transfertype;
    }

    public String getDepartid() {
        return departid;
    }

    public void setDepartid(String departid) {
        this.departid = departid;
    }

    public String getDepartname() {
        return departname;
    }

    public void setDepartname(String departname) {
        this.departname = departname;
    }

    public String getRoomid() {
        return roomid;
    }

    public void setRoomid(String roomid) {
        this.roomid = roomid;
    }

    public String getRoomname() {
        return roomname;
    }

    public void setRoomname(String roomname) {
        this.roomname = roomname;
    }

    public String getClienttype() {
        return clienttype;
    }

    public void setClienttype(String clienttype) {
        this.clienttype = clienttype;
    }

    public String getClientip() {
        return clientip;
    }

    public void setClientip(String clientip) {
        this.clientip = clientip;
    }

    public String getClientmac() {
        return clientmac;
    }

    public void setClientmac(String clientmac) {
        this.clientmac = clientmac;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}
