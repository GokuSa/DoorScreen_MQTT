package com.example;

import java.util.TreeSet;


/**
 * author:
 * 时间:2017/7/14
 * qq:1220289215
 * 类描述：门口屏门灯显示助手
 * 如果是服务开关门口，使用以下命令
 * adb shell am startservice -n shine.com.doorscreen/.service.DoorService --ei "action" 54 --es "data" 7E1001000000000000000000000000010001000011AA
 * <p>
 * 门灯显示优先级
 * 呼叫转移（护士站）>增援（床头屏）>	服务（护士站）>	呼叫（床头屏）>	输液结束提醒（床头屏）>	定位（床头屏）>	取消呼叫转移>取消定位
 * 绿 长亮             橙 闪烁         绿 长亮         红 长亮           红 长亮                 绿 长亮                 灭           灭
 *                                                 呼叫（卫生间 蓝 长亮
 */

public class DoorLightHelper {
    private static final String TAG = "DoorLightHelper";
    private static final String PATH = "/dev/ttyS4";
  
    //波特率
    private static final int BAUDRATE = 9600;
    private boolean hasDoorLight = true;

   




    public static void main(String[] args) {
        DoorLightType doorLightType=new DoorLightType("12",1,"23",3);
        DoorLightType doorLightType2=new DoorLightType("123",2,"23",4);
        DoorLightType doorLightType3=new DoorLightType("1234",2,"23",2);
        DoorLightHelper doorLightHelper=new DoorLightHelper();
        doorLightHelper.mDoorLightTypes.add(doorLightType);
        doorLightHelper.mDoorLightTypes.add(doorLightType2);
        doorLightHelper.mDoorLightTypes.add(doorLightType3);
        System.out.println(doorLightHelper.mDoorLightTypes.toString());
    }

   

    public static class DoorLightType implements Comparable<DoorLightType> {
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

    /**
     * 可根据门灯显示优先级排序的集合
     */
    private TreeSet<DoorLightType> mDoorLightTypes = new TreeSet<>();

    public void add(DoorLightType doorLight) {
        boolean found=false;
        for (DoorLightType light : mDoorLightTypes) {
            //如果优先级相同，后来的替换之前的
           /* if (light.priority == doorLight.priority) {
                found=true;
                light.mac=doorLight.mac;
                light.instruction=doorLight.instruction;
                light.tip=doorLight.tip;
                //找到了就不需循环了，只有一个
                break;
            }*/
            //看有没有已存在的指令，如果有，并且优先级低，替换
            if (light.mac.equals(doorLight.mac)) {
                found=true;
                if (light.priority > doorLight.priority) {
                    light.mac=doorLight.mac;
                    light.instruction=doorLight.instruction;
                    light.priority=doorLight.priority;
                    light.tip=doorLight.tip;
                    break;
                }
            }
        }
        //如果有就不需要添加新的指令
        if(!found){
            mDoorLightTypes.add(doorLight);
        }

    }


    public void remove(DoorLightType doorLightType) {
        //根据mac找到对应的指令
        for (DoorLightType lightType : mDoorLightTypes) {
            //同一mac地址只能存在一种门口指令，移除后退出循环，否则会异常
            if (lightType.mac.equals(doorLightType.mac)) {
                mDoorLightTypes.remove(lightType);
                break;
            }
        }

    }

    public TreeSet<DoorLightType> getDoorLightTypes() {
        return mDoorLightTypes;
    }


    public String getTips() {
        StringBuilder stringBuilder = new StringBuilder();
        for (DoorLightType doorLightType : mDoorLightTypes) {
            stringBuilder.append(doorLightType.getTip()).append("\n");
        }
        return stringBuilder.toString();
    }
}
