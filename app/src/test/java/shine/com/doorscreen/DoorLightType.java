package shine.com.doorscreen;

public  class DoorLightType implements Comparable<DoorLightType> {
        private int priority;
        private String mac;
        private String instruction;
        private String tip;
        //设备类型，
        private int clientType;
        public DoorLightType() {
        }

        public DoorLightType(String clientmac, int priority, String instruction) {
            this.mac = clientmac;
            this.priority = priority;
            this.instruction = instruction;
        }
        public DoorLightType(String clientmac, int priority, String instruction, int clientType) {
            this.mac = clientmac;
            this.priority = priority;
            this.instruction = instruction;
            this.clientType=clientType;
        }

        @Override
        public int compareTo( DoorLightType o) {
            //根据优先级排序，数字越小排序越靠前
            int result=this.priority - o.priority;
            if (result == 0) {
                return this.clientType-o.clientType;
            }
            return result;
        }


        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof DoorLightType)) {
                return false;
            }
            DoorLightType doorLightType = (DoorLightType) obj;
            return doorLightType.mac.equals(this.mac);
//            return doorLightType.priority == this.priority;
//                    && doorLightType.action.equals(this.action)
//                    && doorLightType.type.equals(this.type);
        }

        @Override
        public String toString() {
            return "DoorLightType{" +
                    "privilidge=" + priority +
                    ", clientmac='" + mac + '\'' +
                    ", instruction='" + instruction + '\'' +
                    ", instruction='" + clientType + '\'' +
                    '}';
        }

        public int getPrivilidge() {
            return priority;
        }

        public void setPriority(int privilidge) {
            this.priority = privilidge;
        }

        public String getClientmac() {
            return mac;
        }

        public void setClientmac(String clientmac) {
            this.mac = clientmac;
        }

        public String getInstruction() {
            return instruction;
        }

        public void setInstruction(String instruction) {
            this.instruction = instruction;
        }

        public String getTip() {
            return tip;
        }

        public void setTip(String tip) {
            this.tip = tip;
        }


}