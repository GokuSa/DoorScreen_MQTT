package shine.com.doorscreen.entity;

import java.io.Serializable;

public  class SystemLight implements Serializable{
            /**
             * day : 0
             * start : 00:00
             * stop : 07:00
             * value : 0
             */

            private int day;
            private String start;
            private String stop;
            private int value;

            public int getDay() {
                return day;
            }

            public void setDay(int day) {
                this.day = day;
            }

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

            public int getValue() {
                return value;
            }

            public void setValue(int value) {
                this.value = value;
            }
        }