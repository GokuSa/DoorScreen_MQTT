package shine.com.doorscreen.entity;
//第一次请求的探视时间数据
public  class WatchTime {
        /**
         * morning : 上午8:30~11:30
         * noon : 下午14:00~17:00
         * night : 晚上18:00~20:00
         */

        private String morning;
        private String noon;
        private String night;

        public String getMorning() {
            return morning;
        }

        public void setMorning(String morning) {
            this.morning = morning;
        }

        public String getNoon() {
            return noon;
        }

        public void setNoon(String noon) {
            this.noon = noon;
        }

        public String getNight() {
            return night;
        }

        public void setNight(String night) {
            this.night = night;
        }
    }
