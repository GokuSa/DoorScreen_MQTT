package shine.com.doorscreen.mqtt.bean;

/**
 * author:
 * 时间:2017/7/31
 * qq:1220289215
 * 类描述：跑马灯信息
 * type 表明具体的操作 0_暂停，1_删除，2_发布跑马灯
 */

public class MarqueeInfo {

    /**
     * action : marqueeinfo
     * type : 2
     * marqueeid : 46
     * data : {"message":"assad","startdate":"2017-07-31","stopdate":"2017-07-31","starttime":"08:00","stoptime":"18:00","direction":"1","fontsize":"24","fontcolor":"#000000","fontname":"黑体","background":"#000034","left":"0","right":"0","bottom":"0","speed":"1","allday":"2","playtimes":[{"start":"08:00","stop":"18:00"},{"start":"19:00","stop":"20:00"}],"tasktype":"1"}
     * sender : platform
     */

    private String action;
    private int type;
    private int marqueeid;
    private Marquee data;
    private String sender;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getMarqueeid() {
        return marqueeid;
    }

    public void setMarqueeid(int marqueeid) {
        this.marqueeid = marqueeid;
    }

    public Marquee getData() {
        return data;
    }

    public void setData(Marquee data) {
        this.data = data;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

  /*  public static class DataBean {
        *//**
         * message : assad
         * startdate : 2017-07-31
         * stopdate : 2017-07-31
         * starttime : 08:00
         * stoptime : 18:00
         * direction : 1
         * fontsize : 24
         * fontcolor : #000000
         * fontname : 黑体
         * background : #000034
         * left : 0
         * right : 0
         * bottom : 0
         * speed : 1
         * allday : 2
         * playtimes : [{"start":"08:00","stop":"18:00"},{"start":"19:00","stop":"20:00"}]
         * tasktype : 1
         *//*

        private String message;
        private String startdate;
        private String stopdate;
        private String starttime;
        private String stoptime;
        private String direction;
        private String fontsize;
        private String fontcolor;
        private String fontname;
        private String background;
        private String left;
        private String right;
        private String bottom;
        private String speed;
        private String allday;
        private String tasktype;
        private List<PlaytimesBean> playtimes;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getStartdate() {
            return startdate;
        }

        public void setStartdate(String startdate) {
            this.startdate = startdate;
        }

        public String getStopdate() {
            return stopdate;
        }

        public void setStopdate(String stopdate) {
            this.stopdate = stopdate;
        }

        public String getStarttime() {
            return starttime;
        }

        public void setStarttime(String starttime) {
            this.starttime = starttime;
        }

        public String getStoptime() {
            return stoptime;
        }

        public void setStoptime(String stoptime) {
            this.stoptime = stoptime;
        }

        public String getDirection() {
            return direction;
        }

        public void setDirection(String direction) {
            this.direction = direction;
        }

        public String getFontsize() {
            return fontsize;
        }

        public void setFontsize(String fontsize) {
            this.fontsize = fontsize;
        }

        public String getFontcolor() {
            return fontcolor;
        }

        public void setFontcolor(String fontcolor) {
            this.fontcolor = fontcolor;
        }

        public String getFontname() {
            return fontname;
        }

        public void setFontname(String fontname) {
            this.fontname = fontname;
        }

        public String getBackground() {
            return background;
        }

        public void setBackground(String background) {
            this.background = background;
        }

        public String getLeft() {
            return left;
        }

        public void setLeft(String left) {
            this.left = left;
        }

        public String getRight() {
            return right;
        }

        public void setRight(String right) {
            this.right = right;
        }

        public String getBottom() {
            return bottom;
        }

        public void setBottom(String bottom) {
            this.bottom = bottom;
        }

        public String getSpeed() {
            return speed;
        }

        public void setSpeed(String speed) {
            this.speed = speed;
        }

        public String getAllday() {
            return allday;
        }

        public void setAllday(String allday) {
            this.allday = allday;
        }

        public String getTasktype() {
            return tasktype;
        }

        public void setTasktype(String tasktype) {
            this.tasktype = tasktype;
        }

        public List<PlaytimesBean> getPlaytimes() {
            return playtimes;
        }

        public void setPlaytimes(List<PlaytimesBean> playtimes) {
            this.playtimes = playtimes;
        }

        public static class PlaytimesBean {
            *//**
             * start : 08:00
             * stop : 18:00
             *//*

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
        }
    }*/
}
