package shine.com.doorscreen.entity;

public class Nurse extends Staff {
    private String nursename;

    public String getNursename() {
        return nursename;
    }

    public void setNursename(String nursename) {
        this.nursename = nursename;
    }



    @Override
    public int getFlag() {
        return 1;
    }

    @Override
    public String getName() {
        return nursename;
    }

    @Override
    public String toString() {
        return "Nurse{" +
                "nursename='" + nursename + '\'' +
                '}';
    }
}
