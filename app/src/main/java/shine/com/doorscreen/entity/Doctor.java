package shine.com.doorscreen.entity;

public class Doctor extends Staff {

    private String doctorname;

    public String getDoctorname() {
        return doctorname;
    }

    public void setDoctorname(String doctorname) {
        this.doctorname = doctorname;
    }

    @Override
    public void setName(String name) {
        super.setName(doctorname);
    }

    @Override
    public String getName() {
        return doctorname;
    }

    @Override
    public void setFlag(int flag) {
        super.setFlag(2);
    }

    @Override
    public int getFlag() {
        return 2;
    }

    @Override
    public String toString() {
        return "Doctor{" +
                "doctorname='" + doctorname + '\'' +
                '}';
    }
}
