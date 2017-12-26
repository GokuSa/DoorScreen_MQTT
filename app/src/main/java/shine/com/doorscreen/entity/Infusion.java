package shine.com.doorscreen.entity;

import shine.com.doorscreen.R;

/**
 * author:
 * 时间:2017/12/1
 * qq:1220289215
 * 类描述：床头屏发来的输液信息
 */

public class Infusion {

    /**
     * action : submitinfusion
     * event : 0
     * department : 411
     * roomid : 5
     * clientip : 10.0.2.8
     * clientmac : aa:bb:cc:dd:ee:ff
     * clientname : 1床
     * username : 张三
     * type : 10
     * speed : 40
     * capacity : 500
     * left : 32
     * nursecard : 132123
     * sender : screen
     */

    private String action;
    private int event;
    private String department;
    private String roomid;
    private String clientip;
    private String clientmac;
    private String clientname;
    private String username;
    private int type;
    private int speed;
    private int capacity;
    private long start;
    private int left;
    private int nursecard;
    private String sender;
    private int total;
    private int current_bai;
    private int current_shi;
    private int current_ge;
    private int next_bai;
    private int next_shi;
    private int next_ge;
    private String begin;

    private final static int[] sResIds = new int[]{R.drawable.drip_package_empty, R.drawable.drip_package_five, R.drawable.drip_package_ten,
            R.drawable.drip_package_fifteen, R.drawable.drip_package_twenty, R.drawable.drip_package_thirty,
            R.drawable.drip_package_forty, R.drawable.drip_package_fifty, R.drawable.drip_package_sixty,
            R.drawable.drip_package_seventy, R.drawable.drip_package_eighty, R.drawable.drip_package_ninty, R.drawable.drip_package_full};

    public Infusion() {
    }

    public Infusion(String clientmac, String clientname, int left,int speed) {
        this.clientmac = clientmac;
        this.clientname = clientname;
        this.left = left;
//        this.begin=begin;
        this.speed=speed;
        this.total = left;
//        setCurrentNumber();
//        setNextNumber();


    }

    public void initilize(String begin) {
        this.begin=begin;
        setCurrentNumber();
        setNextNumber();
    }
    public void setCurrentNumber() {
        if (left >= 0) {
            current_bai=left/100;
            current_shi=(left-100*current_bai)/10;
            current_ge=left-100*current_bai-10*current_shi;
        }
    }
    public void setNextNumber() {
        next_bai=left/100;
        next_shi=(left-100*next_bai)/10;
        next_ge=left-100*next_bai-10*next_shi;
    }
    public void countDown() {
        if (left > 0) {
            left--;
            setNextNumber();
        }
    }

    public String getBegin() {
        return begin;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getCurrentDripPackage() {
        if (total==0) {
            return sResIds[0];
        }
        int  rank = left *12/ total;
        return sResIds[rank];
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public int getEvent() {
        return event;
    }

    public void setEvent(int event) {
        this.event = event;
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

    public String getClientname() {
        return clientname;
    }

    public void setClientname(String clientname) {
        this.clientname = clientname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getNursecard() {
        return nursecard;
    }

    public void setNursecard(int nursecard) {
        this.nursecard = nursecard;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public int getCurrent_bai() {
        return current_bai;
    }

    public int getCurrent_shi() {
        return current_shi;
    }

    public int getCurrent_ge() {
        return current_ge;
    }

    public int getNext_bai() {
        return next_bai;
    }

    public int getNext_shi() {
        return next_shi;
    }

    public int getNext_ge() {
        return next_ge;
    }
}
