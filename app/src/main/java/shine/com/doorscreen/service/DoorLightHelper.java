package shine.com.doorscreen.service;

import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.TreeSet;

import android_serialport_api.SerialPort;
import shine.com.doorscreen.util.RootCommand;

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
    private SerialPort mSerialPort;
    private OutputStream mOutputStream;
    //波特率
    private static final int BAUDRATE = 9600;
    private boolean hasDoorLight = true;

    @WorkerThread
    public void handleInstruction(String instruction) {
        if (!hasDoorLight || TextUtils.isEmpty(instruction)) {
            return;
        }
        try {
            if (mOutputStream == null) {
                initialize();
            }
            if (mOutputStream != null) {
                mOutputStream.write(hex2Bytes(instruction));
                mOutputStream.write('\n');
            }
        } catch (IOException e) {
            Log.d(TAG, e.toString());
        }
    }

    private void initialize() {
        File file = new File(PATH);
        boolean isGranted = true;
        if (!file.canRead() || !file.canWrite()) {
            RootCommand rootCommand = new RootCommand();
            isGranted = rootCommand.grand(file.getAbsolutePath());
        }
        Log.d(TAG, "can Read " + file.canRead() + "canWrite" + file.canWrite());
        if (file.canRead() && file.canWrite()) {
            mSerialPort = new SerialPort(file, BAUDRATE, 0);
            mOutputStream = mSerialPort.getOutputStream();
        } else {
            Log.e(TAG, "打开串口/dev/ttyS4失败");
        }
    }

    public void exit() {
        if (mSerialPort != null) {
            mSerialPort.close();
        }
    }


    //十六进制转字节数组
    private byte[] hex2Bytes(String src) {
        byte[] res = new byte[src.length() / 2];
        char[] chs = src.toCharArray();
        int[] b = new int[2];

        for (int i = 0, c = 0; i < chs.length; i += 2, c++) {
            for (int j = 0; j < 2; j++) {
                if (chs[i + j] >= '0' && chs[i + j] <= '9') {
                    b[j] = (chs[i + j] - '0');
                } else if (chs[i + j] >= 'A' && chs[i + j] <= 'F') {
                    b[j] = (chs[i + j] - 'A' + 10);
                } else if (chs[i + j] >= 'a' && chs[i + j] <= 'f') {
                    b[j] = (chs[i + j] - 'a' + 10);
                }
            }
            b[0] = (b[0] & 0x0f) << 4;
            b[1] = (b[1] & 0x0f);
            res[c] = (byte) (b[0] | b[1]);
        }

        return res;
    }

    /**
     * 门灯类型
     * 如果优先权相同，比如多个床头屏呼叫和卫生间，按时间排序
     */
    public static class DoorLightType implements Comparable<DoorLightType> {
        private int priority;
        private String mac;
        private String instruction;
        private String tip;
        //设备类型，
        private long currentTime;
        public DoorLightType() {
        }

        public DoorLightType(String clientmac, int priority, String instruction) {
            this.mac = clientmac;
            this.priority = priority;
            this.instruction = instruction;
        }

        @Override
        public int compareTo(@NonNull DoorLightType o) {
            //根据优先级排序，数字越小排序越靠前
            int result=this.priority - o.priority;
            //优先权相同，比较时间
            if (result == 0) {
                return (int)(this.currentTime-o.currentTime);
            }
            return result;
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

        public void setCurrentTime(long currentTime) {
            this.currentTime = currentTime;
        }
    }

    /**
     * 可根据门灯显示优先级排序的集合
     */
    private TreeSet<DoorLightType> mDoorLightTypes = new TreeSet<>();

    public void add(DoorLightType doorLight) {
        boolean found=false;
        for (DoorLightType light : mDoorLightTypes) {
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
        Log.d(TAG, "DoorLightTypes " + mDoorLightTypes.toString());
        handleInstruction(mDoorLightTypes.first().instruction);
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
        Log.d(TAG, "DoorLightTypes " + mDoorLightTypes.toString());
        if (mDoorLightTypes.size() > 0) {
            handleInstruction(mDoorLightTypes.first().instruction);
        } else {
            handleInstruction("7E1001000000000000000000000000010001000011AA");
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
