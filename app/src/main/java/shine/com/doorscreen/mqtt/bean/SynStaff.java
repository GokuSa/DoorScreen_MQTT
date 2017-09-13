package shine.com.doorscreen.mqtt.bean;

import java.util.List;

/**
 * author:
 * 时间:2017/8/2
 * qq:1220289215
 * 类描述：同步医护工作工作人员
 * 在修改病人信息时需要同步
 */

public class SynStaff {

    /**
     * action : workersinfo
     * department : 532
     * roomid : 5
     * roomname : 病房
     * doctorlist : [{"doctorname":"李艳","title":"责任医生","img":"http://172.168.1.9:8989/admin/images/upload/0.jpg"}]
     * nurselist : [{"nursename":"张阳","title":"责任护士","img":"http://172.168.1.9:8989/admin/images/upload/0.jpg"}]
     * sender : platform
     */

    private String action;
    private String department;
    private String roomid;
    private String roomname;
    private String sender;
    private List<Doctor> doctorlist;
    private List<Nurse> nurselist;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
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

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public List<Doctor> getDoctorlist() {
        return doctorlist;
    }

    public void setDoctorlist(List<Doctor> doctorlist) {
        this.doctorlist = doctorlist;
    }

    public List<Nurse> getNurselist() {
        return nurselist;
    }

    public void setNurselist(List<Nurse> nurselist) {
        this.nurselist = nurselist;
    }

}
