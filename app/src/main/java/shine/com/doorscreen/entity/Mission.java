package shine.com.doorscreen.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * 单个播单封装类
 */
public  class Mission {
        /**
         * missionid : 22
         * type : 2
         * tasktype : 1
         * orderno : 2
         * allday : 1
         * startdate : 2016-08-02
         * stopdate : 2016-08-12
         * starttime : 02:00:00
         * stoptime : 15:00:00
         * playtimes : [{"start":"08:00","stop":"18:00"},{"start":"19:00","stop":"20:00"},{"start":"21:00","stop":"21:10"},{"start":"22:10","stop":"23:10"}]
         */

        private int missionid;
        //操作类型：0_暂停，2_发布宣教内容
        private int type;
        private int tasktype;
        private int orderno;
        private int allday;
        private String startdate;
        private String stopdate;
        private String starttime;
        private String stoptime;
        private List<PlayTime> playtimes;
        private ArrayList<Elements> source;

        public int getMissionid() {
            return missionid;
        }

        public void setMissionid(int missionid) {
            this.missionid = missionid;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public int getTasktype() {
            return tasktype;
        }

        public void setTasktype(int tasktype) {
            this.tasktype = tasktype;
        }

        public int getOrderno() {
            return orderno;
        }

        public void setOrderno(int orderno) {
            this.orderno = orderno;
        }

        public int getAllday() {
            return allday;
        }

        public void setAllday(int allday) {
            this.allday = allday;
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

        public List<PlayTime> getPlaytimes() {
            return playtimes;
        }

        public void setPlaytimes(List<PlayTime> playtimes) {
            this.playtimes = playtimes;
        }

        public ArrayList<Elements> getSource() {
            return source;
        }

        public void setSource(ArrayList<Elements> source) {
            this.source = source;
        }




    }