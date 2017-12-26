package shine.com.doorscreen.service;

import java.util.Objects;

/**
 * author:
 * 时间:2017/12/21
 * qq:1220289215
 * 类描述：本地参数 ip mac 服务器地址
 */

public class LocalParameter {
    public static final String ETHERNET_PATH = "/extdata/work/show/system/network.ini";
    /**
     * 主机地址
     */
    private String mHost = "";
    /**
     * 主机mac
     */
    private String mMac = "";
    /**
     * 门口屏所在科室
     */
    private String mDepartmentId = "1";
    /**
     * 门口屏房间号
     */
    private String mRoomId = "1";
    /**
     * MQTT需要的格式化地址 "tcp://172.168.1.9:1883";
     */
    private String mServerUri = "";


    public LocalParameter() {
    }

    /**
     * 判断房间号是否是本门口屏的
     * @param roomId
     * @return
     */
    public boolean isMyRoom(String roomId) {
        return Objects.equals(mRoomId, roomId);
    }

    /**
     * 更新门口屏 科室和房间信息
     * @param departmentId
     * @param roomId
     */
    public void updateScreenInfo(String departmentId, String roomId) {
        mDepartmentId = departmentId;
        mRoomId = roomId;
    }

    public LocalParameter(String host, String mac, String serverUri) {
        mHost = host;
        mMac = mac;
        mServerUri = serverUri;
    }

       public String getHost() {
        return mHost;
    }

    public void setHost(String host) {
        mHost = host;
    }

    public String getMac() {
        return mMac;
    }

    public void setMac(String mac) {
        mMac = mac;
    }

    public String getDepartmentId() {
        return mDepartmentId;
    }

    public void setDepartmentId(String departmentId) {
        mDepartmentId = departmentId;
    }

    public String getRoomId() {
        return mRoomId;
    }

    public void setRoomId(String roomId) {
        mRoomId = roomId;
    }

    public String getServerUri() {
        return mServerUri;
    }

    public void setServerUri(String serverUri) {
        mServerUri = serverUri;
    }

    @Override
    public String toString() {
        return "LocalParameter{" +
                "mHost='" + mHost + '\'' +
                ", mMac='" + mMac + '\'' +
                ", mDepartmentId='" + mDepartmentId + '\'' +
                ", mRoomId='" + mRoomId + '\'' +
                ", mServerUri='" + mServerUri + '\'' +
                '}';
    }
}
