package shine.com.doorscreen;

import org.junit.Test;

import java.util.TreeSet;

/**
 * author:
 * 时间:2017/9/29
 * qq:1220289215
 * 类描述：
 */

public class DoorLightTest {

    @Test
    public void test() {
        DoorLightType doorLightType=new DoorLightType("12",1,"23",3);
        DoorLightType doorLightType2=new DoorLightType("123",2,"23",4);
        DoorLightType doorLightType3=new DoorLightType("1234",2,"23",2);

        addDoorLightType(doorLightType);
        addDoorLightType(doorLightType2);
        addDoorLightType(doorLightType3);
        System.out.println(mDoorLightTypes.toString());
    }
    /**
     * 可根据门灯显示优先级排序的集合
     */
    private TreeSet<DoorLightType> mDoorLightTypes = new TreeSet<>();

    public void addDoorLightType(DoorLightType doorLight) {
        boolean found=false;
        for (DoorLightType light : mDoorLightTypes) {
            //如果优先级相同，后来的替换之前的
            //看有没有已存在的指令，如果有，并且优先级低，替换
            if (light.getClientmac().equals(doorLight.getClientmac())) {
                found=true;
                if (light.getPrivilidge() > doorLight.getPrivilidge()) {
                    light.setClientmac(doorLight.getClientmac());
                    light.setInstruction(doorLight.getInstruction());
                    light.setPriority(doorLight.getPrivilidge());
                    light.setTip(doorLight.getTip());
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
            if (lightType.getClientmac().equals(doorLightType.getClientmac())) {
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
