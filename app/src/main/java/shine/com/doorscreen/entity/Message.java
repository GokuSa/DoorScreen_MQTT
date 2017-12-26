package shine.com.doorscreen.entity;

/**
 * author:
 * 时间:2017/6/30
 * qq:1220289215
 * 类描述：发布到服务端的消息 上下线通知，获取门口屏信息
 */

public class Message {
    private String action="";
    private String clientmac;
    private String clientip;
    private String sender="doorscreen";

    public Message() {
    }

    public Message(String action, String clientmac, String clientip) {
        this.action = action;
        this.clientmac = clientmac;
        this.clientip = clientip;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getClientmac() {
        return clientmac;
    }

    public void setClientmac(String clientmac) {
        this.clientmac = clientmac;
    }

    public String getClientip() {
        return clientip;
    }

    public void setClientip(String clientip) {
        this.clientip = clientip;
    }

    @Override
    public String toString() {
        return "Message{" +
                "action='" + action + '\'' +
                ", clientmac='" + clientmac + '\'' +
                ", clientip='" + clientip + '\'' +
                ", sender='" + sender + '\'' +
                '}';
    }
}
