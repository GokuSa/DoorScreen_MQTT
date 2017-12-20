package shine.com.doorscreen.entity;

public class PlayTime {
    /**
     * start : 08:00
     * stop : 18:00
     */

    private String start;
    private String stop;

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getStop() {
        return stop;
    }

    public void setStop(String stop) {
        this.stop = stop;
    }

    @Override
    public String toString() {
        return "PlayTime{" +
                "start='" + start + '\'' +
                ", stop='" + stop + '\'' +
                '}';
    }
}