package shine.com.doorscreen.entity;

import java.util.List;

/**
 * author:
 * 时间:2017/6/21
 * qq:1220289215
 * 类描述：
 */

public class DoorScreenMessage {

    /**
     * action : getdoorscreeninfo
     * department : 313
     * roomId : 314
     * roomname : 1病房
     * infusionwarnings : []
     * doctorlist : [{"img":"","title":"住院医师","doctorname":"王春晓"},{"img":"","title":"主治医师","doctorname":"鲍彦娜"},{"img":"http://172.168.1.9/admin/images/upload/33595cb142b0fe482617d5f5108cdcdc.jpg","title":"住院医师","doctorname":"冯丽丽"},{"img":"","title":"住院医师","doctorname":"王春晓"},{"img":"","title":"主治医师","doctorname":"鲍彦娜"},{"img":"http://172.168.1.9/admin/images/upload/33595cb142b0fe482617d5f5108cdcdc.jpg","title":"住院医师","doctorname":"冯丽丽"}]
     * nurselist : [{"img":"","title":"","nursename":""},{"img":"","title":"无","nursename":"朱晓"}]
     * patientlist : [{"patientname":"","bedno":"+1床","doctorname":"王春晓"},{"patientname":"朱晓","bedno":"2床","doctorname":"鲍彦娜"},{"patientname":"朱晓","bedno":"1床","doctorname":"鲍彦娜"},{"patientname":"朱晓","bedno":"+22床","doctorname":"冯丽丽"}]
     * setting : {"systemvolume":"","systemlight":""}
     * sender : server
     */

    private String action;
    private String department;
    private String roomId;
    private String roomname;
    private Settings setting;
    private String sender;
    private WatchTime watchtime;
    private long time;
    private List<?> infusionwarnings;
    private List<Doctor> doctorlist;
    private List<Nurse> nurselist;
    private List<Patient> patientlist;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDepartment() {
        return department;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getRoomname() {
        return roomname;
    }

    public void setRoomname(String roomname) {
        this.roomname = roomname;
    }

    public Settings getSetting() {
        return setting;
    }

    public void setSetting(Settings setting) {
        this.setting = setting;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public List<?> getInfusionwarnings() {
        return infusionwarnings;
    }

    public void setInfusionwarnings(List<?> infusionwarnings) {
        this.infusionwarnings = infusionwarnings;
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

    public List<Patient> getPatientlist() {
        return patientlist;
    }

    public void setPatientlist(List<Patient> patientlist) {
        this.patientlist = patientlist;
    }


    public WatchTime getWatchtime() {
        return watchtime;
    }

    public long getTime() {
        return time;
    }

}
