package shine.com.doorscreen.entity;

import java.util.List;

/**
 * author:
 * 时间:2017/7/4
 * qq:1220289215
 * 类描述：跑马灯列表
 * 在业务服务器上线时会发布跑马灯数据，更新本地数据和服务器保持同步
 */

public class MarqueeList {

    /**
     * action : marqueelist
     * data : [{"type":"2","marqueeid":"55","message":"今日新闻今日新闻今日新闻今日新闻","startdate":"2016-08-02","stopdate":"2016-08-06","starttime":"06:00:00","stoptime":"12:00:00","direction":"1","fontsize":"24","fontcolor":"#00ff00","fontname":"微软雅黑","background":"#ff0000","left":"12","right":"12","bottom":"50","speed":"1","allday":"1","playtimes":[{"start":"08:00","stop":"18:00"},{"start":"19:00","stop":"20:00"},{"start":"21:00","stop":"21:10"},{"start":"22:10","stop":"23:10"}],"tasktype":"1"}]
     * sender : server
     */

    private String action;
    private String sender;
    private List<Marquee> data;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

   /* public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }*/

    public List<Marquee> getData() {
        return data;
    }

    public void setData(List<Marquee> data) {
        this.data = data;
    }

    /* public static class DataBean {
        *//**
         * type : 2
         * marqueeid : 55
         * message : 今日新闻今日新闻今日新闻今日新闻
         * startdate : 2016-08-02
         * stopdate : 2016-08-06
         * starttime : 06:00:00
         * stoptime : 12:00:00
         * direction : 1
         * fontsize : 24
         * fontcolor : #00ff00
         * fontname : 微软雅黑
         * background : #ff0000
         * left : 12
         * right : 12
         * bottom : 50
         * speed : 1
         * allday : 1
         * playtimes : [{"start":"08:00","stop":"18:00"},{"start":"19:00","stop":"20:00"},{"start":"21:00","stop":"21:10"},{"start":"22:10","stop":"23:10"}]
         * tasktype : 1
         *//*

        private int type;
        private int marqueeid;
        private String message;
        private String startdate;
        private String stopdate;
//        private String starttime;
//        private String stoptime;
//        private String direction;
//        private String fontsize;
//        private String fontcolor;
//        private String fontname;
//        private String background;
//        private String left;
//        private String right;
//        private String bottom;
//        private String speed;
//        private String allday;
//        private String tasktype;
//        private List<PlaytimesBean> playtimes;
        private List<MarqueeTime> playtimes;

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

        public List<MarqueeTime> getPlaytimes() {
            return playtimes;
        }

        *//*
        public List<PlaytimesBean> getPlaytimes() {
            return playtimes;
        }

        public void setPlaytimes(List<PlaytimesBean> playtimes) {
            this.playtimes = playtimes;
        }

        public static class PlaytimesBean {
            *//**//**
             * start : 08:00
             * stop : 18:00
             *//**//*

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
        }*//*
    }*/
}
