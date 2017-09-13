package shine.com.doorscreen.mqtt.bean;

import android.support.annotation.NonNull;

import java.util.TreeSet;

/**
 * author:
 * 时间:2017/7/17
 * qq:1220289215
 * 类描述：
 */

public class Tesy {
    public static void main(String[] args) {
        Tesy helper =new Tesy();
        helper.add(new Tesy.DoorLightType("mac1233", 1, "instru32"));
        helper.add(new Tesy.DoorLightType("mac123", 2, "instru32"));
        helper.add(new Tesy.DoorLightType("mac123", 3, "instru32"));
        helper.add(new Tesy.DoorLightType("mac1323", 3, "instru32"));
        System.out.println(helper.getDoorLightTypes().toString());
    }

     static class DoorLightType implements Comparable<DoorLightType> {
        private int privilidge;
        private String clientmac;
        private String instruction;

        public DoorLightType() {
        }

        public DoorLightType(String clientmac, int privilidge, String instruction) {
            this.clientmac = clientmac;
            this.privilidge = privilidge;
            this.instruction = instruction;
        }

        @Override
        public int compareTo(@NonNull DoorLightType o) {
            //根据优先级排序，数字越小排序越靠前
            return this.privilidge - o.privilidge;
        }

    /*    @Override
        public int hashCode() {
            return clientmac.hashCode()+privilidge+instruction.hashCode();
        }*/

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof DoorLightType)) {
                return false;
            }
            DoorLightType doorLightType = (DoorLightType) obj;
//            return doorLightType.clientmac.equals(this.clientmac);
            return doorLightType.privilidge == this.privilidge;
//                    && doorLightType.action.equals(this.action)
//                    && doorLightType.type.equals(this.type);
        }

        @Override
        public String toString() {
            return "DoorLightType{" +
                    "privilidge=" + privilidge +
                    ", clientmac='" + clientmac + '\'' +
                    ", instruction='" + instruction + '\'' +
                    '}';
        }

        public int getPrivilidge() {
            return privilidge;
        }

        public void setPrivilidge(int privilidge) {
            this.privilidge = privilidge;
        }

        public String getClientmac() {
            return clientmac;
        }

        public void setClientmac(String clientmac) {
            this.clientmac = clientmac;
        }

        public String getInstruction() {
            return instruction;
        }

        public void setInstruction(String instruction) {
            this.instruction = instruction;
        }
    }

//   private List<DoorLightType> mDoorLightTypes=new ArrayList<>();
    /**
     * 可根据门灯显示优先级排序的集合
     */
    private TreeSet<DoorLightType> mDoorLightTypes = new TreeSet<>();

    public void add(DoorLightType doorLightType) {
        //判断列表中的类型与参数的优先级
        if (mDoorLightTypes.size() == 0) {
            mDoorLightTypes.add(doorLightType);
        } else {
            for (DoorLightType lightType : mDoorLightTypes) {
                //优先级相同的直接添加替换
                if (lightType.privilidge == doorLightType.privilidge) {
                    mDoorLightTypes.add(doorLightType);
                    break;
                } else if (lightType.clientmac.equals(doorLightType.clientmac)) {
                    //比较发现参数优先级更高
                    if (lightType.privilidge > doorLightType.privilidge) {
                        //移除当前的
                        mDoorLightTypes.remove(lightType);
                        mDoorLightTypes.add(doorLightType);
                    } else {
                        //不处理
                        return;
                    }
                }
            }
        }

//        handleInstruction(mDoorLightTypes.first().instruction);
    }


    public void remove(DoorLightType doorLightType) {
        //根据mac找到对应的指令
        for (DoorLightType lightType : mDoorLightTypes) {
            if (lightType.clientmac.equals(doorLightType.clientmac)) {
                mDoorLightTypes.remove(doorLightType);
                break;
            }
        }
        System.out.println(mDoorLightTypes.toString());
        if (mDoorLightTypes.size() > 0) {
//            handleInstruction(mDoorLightTypes.first().instruction);
        } else {
//            handleInstruction("7E1001000000000000000000000000010001000011AA");
        }
    }

    public TreeSet<DoorLightType> getDoorLightTypes() {
        return mDoorLightTypes;
    }
}
