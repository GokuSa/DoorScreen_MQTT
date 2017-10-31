package shine.com.doorscreen.mqtt.bean;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;

import shine.com.doorscreen.util.Common;

/**
 * 住院的病人信息，没有病人也要显示床位，床位和床头屏对应，增删依赖其mac地址
 * Patient应该是床位的意思，
 */
@Entity(tableName = "patient")
public class Patient {
    @ColumnInfo(name = BaseColumns._ID)
    public long id;
    @ColumnInfo(name = "name")
    private String patientname;
    private String bedno;
    private int bedNum=-1;
    private String doctorname;
    //床头屏mac地址
    @PrimaryKey
    @NonNull
    private String clientmac;
    private boolean isCalling;

    public Patient() {}

    @Ignore
    public Patient(String patientname, String bedno, String doctorname, String clientmac, boolean isCalling) {
        this.patientname = patientname;
        this.bedno = bedno;
        this.doctorname = doctorname;
        this.clientmac = clientmac;
        this.isCalling = isCalling;
        this.bedNum = Common.getNumbers(bedno);
    }

    @Ignore
    public Patient(String clientmac) {
        this.clientmac = clientmac;
    }

    public String getPatientname() {
        return patientname;
    }

    public void setPatientname(String patientname) {
        this.patientname = patientname;
    }

    public String getBedno() {
        return bedno;
    }

    public void setBedno(String bedno) {
        this.bedno = bedno;
        this.bedNum = Common.getNumbers(bedno);
    }

    public String getDoctorname() {
        return doctorname;
    }

    public void setDoctorname(String doctorname) {
        this.doctorname = doctorname;
    }

    public boolean isCalling() {
        return isCalling;
    }

    public void setCalling(boolean calling) {
        isCalling = calling;
    }

    public String getClientmac() {
        return clientmac;
    }

    public void setClientmac(String clientmac) {
        this.clientmac = clientmac;
    }

    public int getBedNum() {
        return bedNum;
    }

    public void setBedNum(int bedNum) {
        this.bedNum = bedNum;
    }

    @Override
    public String toString() {
        return "Patient{" +
                "patientname='" + patientname + '\'' +
                ", bedno='" + bedno + '\'' +
                ", doctorname='" + doctorname + '\'' +
                ", isCalling=" + isCalling +
                '}';
    }
}
