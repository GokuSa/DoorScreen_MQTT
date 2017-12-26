package shine.com.doorscreen.entity;

/**
 * author:
 * 时间:2017/9/12
 * qq:1220289215
 * 类描述：护士站的呼叫转移
 * 显示护士正在XX床服务
 */

public class CallTransfer {

    /**
     * destinationmac : E06417062917
     * destinationip :
     * action : transfer
     * stationip : 172.168.32.114
     * stationmac : E06420173214
     * flag : 1
     */

    private String destinationmac;
    private String destinationname;
    private String destinationip;
    private String action;
    private String stationip;
    private String stationmac;
    private String sender = "";
    //1:设置转移，0：取消转移
    private int flag;

    public String getDestinationmac() {
        return destinationmac;
    }

    public void setDestinationmac(String destinationmac) {
        this.destinationmac = destinationmac;
    }

    public String getDestinationip() {
        return destinationip;
    }

    public void setDestinationip(String destinationip) {
        this.destinationip = destinationip;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getStationip() {
        return stationip;
    }

    public void setStationip(String stationip) {
        this.stationip = stationip;
    }

    public String getStationmac() {
        return stationmac;
    }

    public void setStationmac(String stationmac) {
        this.stationmac = stationmac;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public String getDestinationname() {
        return destinationname;
    }

    public void setDestinationname(String destinationname) {
        this.destinationname = destinationname;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}
