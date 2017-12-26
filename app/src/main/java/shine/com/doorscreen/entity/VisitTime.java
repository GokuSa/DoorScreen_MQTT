package shine.com.doorscreen.entity;

/**
 * author:
 * 时间:2017/7/19
 * qq:1220289215
 * 类描述：后台修改探视时间发送的数据
 */

public class VisitTime {

    /**
     * action : watchtimeparam
     * morningwatchtime : 上午8:30~11:30
     * ["noonwatchtime":"下午14:00~17:00"  //中午探视时间]
     ["nightwatchtime":"晚上18:00~20:00"  //晚上探视时间]
     */

    private String action;
    private String morningwatchtime;
    private String noonwatchtime;
    private String nightwatchtime;
    private String sender;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getMorningwatchtime() {
        return morningwatchtime;
    }

    public void setMorningwatchtime(String morningwatchtime) {
        this.morningwatchtime = morningwatchtime;
    }

    public String getNoonwatchtime() {
        return noonwatchtime;
    }

    public void setNoonwatchtime(String noonwatchtime) {
        this.noonwatchtime = noonwatchtime;
    }

    public String getNightwatchtime() {
        return nightwatchtime;
    }

    public void setNightwatchtime(String nightwatchtime) {
        this.nightwatchtime = nightwatchtime;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}
