package shine.com.doorscreen.mqtt.bean;

/**
 * author:
 * 时间:2017/7/31
 * qq:1220289215
 * 类描述：同步患者数据
 */

public class SynPatient {

    /**
     * action : synpatient
     * data : {"type":"2","devicemac":"201705090129","departId":"411","roomId":"5","roomname":"5病房","bednum":"172.168.31.41","username":"张三","patienttime":"2016-06-01","sex":"1","age":"29","patientid":"222","doctorname":"张明","nursename":"丽丽","levelid":"106","levelname":"一级护理","levelcolor":"#000000","levelbgcolor":"#000000"}
     * sender : platform
     */

    private String action;
    private DataBean data;
    private String sender;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public static class DataBean {
        /**
         * type : 2
         * devicemac : 201705090129
         * departId : 411
         * roomId : 5
         * roomname : 5病房
         * bednum : 172.168.31.41
         * username : 张三
         * patienttime : 2016-06-01
         * sex : 1
         * age : 29
         * patientid : 222
         * doctorname : 张明
         * nursename : 丽丽
         * levelid : 106
         * levelname : 一级护理
         * levelcolor : #000000
         * levelbgcolor : #000000
         */

        private int type;
        private String devicemac;
        private String departId;
        private String roomId;
        private String roomname;
        private String bednum;
        private String username;
        private String patienttime;
        private String sex;
        private String age;
        private String patientid;
        private String doctorname;
        private String nursename;
        private String levelid;
        private String levelname;
        private String levelcolor;
        private String levelbgcolor;

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getDevicemac() {
            return devicemac;
        }

        public void setDevicemac(String devicemac) {
            this.devicemac = devicemac;
        }

        public String getDepartId() {
            return departId;
        }

        public void setDepartId(String departId) {
            this.departId = departId;
        }

        public String getRoomId() {
            return roomId;
        }

        public void setRoomId(String roomId) {
            this.roomId = roomId;
        }

        public String getRoomname() {
            return roomname;
        }

        public void setRoomname(String roomname) {
            this.roomname = roomname;
        }

        public String getBednum() {
            return bednum;
        }

        public void setBednum(String bednum) {
            this.bednum = bednum;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPatienttime() {
            return patienttime;
        }

        public void setPatienttime(String patienttime) {
            this.patienttime = patienttime;
        }

        public String getSex() {
            return sex;
        }

        public void setSex(String sex) {
            this.sex = sex;
        }

        public String getAge() {
            return age;
        }

        public void setAge(String age) {
            this.age = age;
        }

        public String getPatientid() {
            return patientid;
        }

        public void setPatientid(String patientid) {
            this.patientid = patientid;
        }

        public String getDoctorname() {
            return doctorname;
        }

        public void setDoctorname(String doctorname) {
            this.doctorname = doctorname;
        }

        public String getNursename() {
            return nursename;
        }

        public void setNursename(String nursename) {
            this.nursename = nursename;
        }

        public String getLevelid() {
            return levelid;
        }

        public void setLevelid(String levelid) {
            this.levelid = levelid;
        }

        public String getLevelname() {
            return levelname;
        }

        public void setLevelname(String levelname) {
            this.levelname = levelname;
        }

        public String getLevelcolor() {
            return levelcolor;
        }

        public void setLevelcolor(String levelcolor) {
            this.levelcolor = levelcolor;
        }

        public String getLevelbgcolor() {
            return levelbgcolor;
        }

        public void setLevelbgcolor(String levelbgcolor) {
            this.levelbgcolor = levelbgcolor;
        }
    }
}
