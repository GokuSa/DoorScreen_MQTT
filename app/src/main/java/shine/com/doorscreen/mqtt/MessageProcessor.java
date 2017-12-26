package shine.com.doorscreen.mqtt;

import android.app.AlarmManager;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import shine.com.doorscreen.R;
import shine.com.doorscreen.activity.MainActivity;
import shine.com.doorscreen.app.AppEntrance;
import shine.com.doorscreen.database.WardDataBase;
import shine.com.doorscreen.entity.CallTransfer;
import shine.com.doorscreen.entity.Doctor;
import shine.com.doorscreen.entity.DoorScreenMessage;
import shine.com.doorscreen.entity.Marquee;
import shine.com.doorscreen.entity.MarqueeInfo;
import shine.com.doorscreen.entity.MarqueeList;
import shine.com.doorscreen.entity.MarqueeTime;
import shine.com.doorscreen.entity.Nurse;
import shine.com.doorscreen.entity.Patient;
import shine.com.doorscreen.entity.ReBoot;
import shine.com.doorscreen.entity.ReStart;
import shine.com.doorscreen.entity.Staff;
import shine.com.doorscreen.entity.SynPatient;
import shine.com.doorscreen.entity.SynStaff;
import shine.com.doorscreen.entity.SystemInfo;
import shine.com.doorscreen.entity.Transfer;
import shine.com.doorscreen.entity.VisitTime;
import shine.com.doorscreen.entity.Ward;
import shine.com.doorscreen.entity.WatchTime;
import shine.com.doorscreen.service.DoorLightHelper;
import shine.com.doorscreen.service.DoorService;
import shine.com.doorscreen.service.ScreenManager;
import shine.com.doorscreen.service.VolumeManager;
import shine.com.doorscreen.util.Common;
import shine.com.doorscreen.util.RootCommand;

import static shine.com.doorscreen.activity.MainActivity.INFUSION_NEW;
import static shine.com.doorscreen.activity.MainActivity.INFUSION_REMOVE;
import static shine.com.doorscreen.activity.MainActivity.RESTART;
import static shine.com.doorscreen.activity.MainActivity.SYN_MISSIONS;
import static shine.com.doorscreen.activity.MainActivity.UPDATE_MISSION;
import static shine.com.doorscreen.util.Common.getMacAddress;

/**
 * author:
 * 时间:2017/12/20
 * qq:1220289215
 * 类描述：
 */

public class MessageProcessor {
    private static final String TAG = MessageProcessor.class.getSimpleName();
    private  Gson mGson;
    private  WardDataBase mWardDataBase;
    private MessageHandler mHandler;
    private HandlerThread mHandlerThread;
    private Ward mWard;
    //门灯管理
    private DoorLightHelper mDoorLightHelper;
    private CallTransfer mCallTransfer;

    public MessageProcessor() {
        mHandlerThread = new HandlerThread(MessageProcessor.class.getSimpleName(), Process.THREAD_PRIORITY_DEFAULT);
        mHandlerThread.start();
        mHandler = new MessageHandler(mHandlerThread.getLooper(), this);
        mGson = new Gson();
        mWard = new Ward();
        //数据存储
        mWardDataBase = WardDataBase.INSTANCE(AppEntrance.getAppEntrance());
        mDoorLightHelper = new DoorLightHelper();
    }

    public void stop() {
        Log.d(TAG, "stop: ");
        if (mHandlerThread != null) {
            mHandlerThread.quitSafely();
            mHandlerThread = null;
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        mDoorLightHelper.exit();
        mDoorLightHelper=null;
        mGson=null;
        mWard=null;
        if (mCallTransfer != null) {
            mCallTransfer=null;
        }

    }

    public void handleMsg(String receiver) {
        mHandler.obtainMessage(0, receiver).sendToTarget();
    }


    void handleConnect(long delay) {
        mHandler.sendEmptyMessageDelayed(1, delay);
    }

    private static final class MessageHandler extends Handler {
        private WeakReference<MessageProcessor> mReference;

        public MessageHandler(Looper looper, MessageProcessor messageProcessor) {
            super(looper);
            mReference = new WeakReference<>(messageProcessor);
        }

        @Override
        public void handleMessage(Message msg) {
            MessageProcessor messageProcessor = mReference.get();
            if (messageProcessor == null) {
                return;
            }
            //连接服务器
            try {
                if (msg.what == 1) {
                    MQTTClient.INSTANCE().connect();
                } else {
                    String receive = (String) msg.obj;
                    JSONObject jsonObject = new JSONObject(receive);
                    final String action = jsonObject.optString("action");
                    messageProcessor.process(action, receive, jsonObject);
                }
            } catch (JSONException | MqttException e) {
                e.printStackTrace();
            }
        }
    }


    @WorkerThread
    private void process(String action, String receive, JSONObject jsonObject) {
        try {
            switch (action) {
                case "getdoorscreeninfo":
                    final DoorScreenMessage message = mGson.fromJson(receive, DoorScreenMessage.class);
                    if ("-1".equals(message.getDepartment()) || "-1".equals(message.getRoomId())) {
                        return;
                    }
                    handleDoorScreenMessage(message);
                    break;
                //后台发来的转组通知
                case "transferinsystem":
                    Transfer transfer = mGson.fromJson(receive, Transfer.class);
                    if (transfer != null) {
                        MQTTClient.INSTANCE().getParameter().updateScreenInfo(transfer.getDepartid(), transfer.getRoomid());
                        MQTTClient.INSTANCE().handleTranfer();
                    }
                    break;
                //呼叫转移
                case "transfer":
                    mCallTransfer = mGson.fromJson(receive, CallTransfer.class);
                    if (mCallTransfer != null) {
                        handleCallTransfer(mCallTransfer, action, jsonObject);
                    }
                    break;
                //删除床位
                case "deletefromsystem":
                    handleDeleteFromSystem(jsonObject);
                    break;
                //添加更新床位
                case "addedtosystem":
                case "updatetosystem":
                    handleAddOrUpdateToSystem(action, jsonObject);
                    break;
                case "watchtimeparam":
                    VisitTime visitTime = mGson.fromJson(receive, VisitTime.class);
                    mWard.setMorning(visitTime.getMorningwatchtime());
                    mWard.setNoon(visitTime.getNoonwatchtime());
                    mWard.setNight(visitTime.getNightwatchtime());
                    mWardDataBase.ward().insert(mWard);
                    break;
                case "reinforcement":
                case "service":
                case "call":
                case "position":
                    DoorLightHelper.DoorLightType doorLightType2 = getDoorLightType(action, jsonObject);
                    handleCall(doorLightType2);
                    break;
                case "finish":
                case "cancelposition":
                case "acceptcall":
                    DoorLightHelper.DoorLightType doorLightType = getDoorLightType(action, jsonObject);
                    if (doorLightType != null) {
                        handleCancelCall(doorLightType);
                    }
                    break;
                //目前仅开关屏时间
                case "acceptscreenlight":
                    SystemInfo systemInfo = mGson.fromJson(receive, SystemInfo.class);
                    ScreenManager.getInstance().handleScreenOnOff(systemInfo);
                    break;
                case "acceptsystemvolume":
                    if (13 == jsonObject.optInt("clienttype")) {
                        SystemInfo systemVolume = mGson.fromJson(receive, SystemInfo.class);
                        VolumeManager.getInstance().handleVolumeSet(systemVolume);
                    }
                    break;
                case "reboot":
                    //如果设备类型是门口屏
                    if (13 == jsonObject.optInt("clienttype")) {
                        DoorService.startService(AppEntrance.getAppEntrance(), MainActivity.REBOOT, receive);
                    }
                    break;
                //定时重启
                case "accepttimedreboot":
                    ReBoot reBoot = mGson.fromJson(receive, ReBoot.class);
                    if (reBoot != null && 13 == reBoot.getClienttype()) {
                        List<ReStart> datalist = reBoot.getDatalist();
                        if (datalist != null && datalist.size() > 0) {
                            mWardDataBase.reStartDao().insertAll(datalist);
                            DoorService.startService(AppEntrance.getAppEntrance(), RESTART, "");
                        }
                    }
                    break;
                //后台重启上线后
                case "connected":
                    if ("server".equals(jsonObject.optString("sender"))) {
                        Log.d(TAG, "connected");
                        MQTTClient.INSTANCE().onConnectSucceed();
                    }
                    break;
                //更新病房名称
                case "updatename":
                    String roomName = jsonObject.optString("roomname");
                    if (!TextUtils.isEmpty(roomName)) {
                        mWard.setRoomname(roomName);
                        mWardDataBase.ward().insert(mWard);
                    }
                    break;
                case "marqueeinfo":
                    MarqueeInfo marqueeInfo = mGson.fromJson(receive, MarqueeInfo.class);
                    if (marqueeInfo != null) {
                        handleMarqueeInfo(marqueeInfo);
                    }
                    break;
                //同步后台跑马灯列表，防止离线 换服务器时数据不同步
                case "marqueelist":
                    MarqueeList marqueeList = mGson.fromJson(receive, MarqueeList.class);
                    if (marqueeList != null) {
                        synMarqueeList(marqueeList);
                    }
                    break;
                case "synpatient":
                    SynPatient synPatient = mGson.fromJson(receive, SynPatient.class);
                    if (synPatient != null) {
                        handleSynPatient(synPatient);
                    }
                    break;
                case "workersinfo":
                    SynStaff synStaff = mGson.fromJson(receive, SynStaff.class);
                    if (synStaff != null) {
                        handleSynStaff(synStaff);
                    }
                    break;
                case "missionlist":
                    MainActivity.sendUpdate(SYN_MISSIONS,receive,false);
//                    synVideoMissions(receive);
                    break;
                case "missioninfo":
                    MainActivity.sendUpdate(UPDATE_MISSION,receive,false);
                    break;
                case "submitinfusion":
                    MainActivity.sendUpdate(INFUSION_NEW,receive,false);
                    break;
                case "submitinfusionfinish":
                    MainActivity.sendUpdate(INFUSION_REMOVE,receive,false);
                    break;
            }
        } catch (JsonSyntaxException | MqttException e) {
            e.printStackTrace();
        }
    }

    private void handleDoorScreenMessage(DoorScreenMessage message) throws MqttException {
        //一定要先更新科室信息
        MQTTClient.INSTANCE().getParameter().updateScreenInfo(message.getDepartment(),message.getRoomId());
        MQTTClient.INSTANCE().handleSubscribe();
        synLocalTime(message.getTime());
        updateDataBase(message);
    }

    /**
     * 与后台服务器时间同步
     *
     * @param time
     */
    private void synLocalTime(long time) {
        if (Math.abs(System.currentTimeMillis() - time) > 3 * 1000) {
            if (time > 0 && time / 1000 < Integer.MAX_VALUE) {
                ((AlarmManager) AppEntrance.getAppEntrance().getSystemService(Context.ALARM_SERVICE)).setTime(time);
                boolean result = new RootCommand().checkTime();
                Log.d(TAG, "syn local time result:" + result);
                MainActivity.sendUpdate(MainActivity.SYN_TIME);

            }
        }
    }

    /**
     * 更新数据库信息
     *
     * @param message
     */
    private void updateDataBase(DoorScreenMessage message) {
        mWardDataBase.patient().deleteAll();
        List<Patient> patientlist = message.getPatientlist();
        for (Patient patient : patientlist) {
            //床号长度超过4，比如233+床就换行显示床
            if (patient.getBedno().length() > 4) {
                String result = patient.getBedno().replace("床", "\n床");
                patient.setBedno(result);
            }
            //使用正则提取第一个数字字符串,转化成数字用来排序，没有数字就是-1
            int bedNum = Common.getNumbers(patient.getBedno());
            patient.setBedNum(bedNum);

        }
        mWardDataBase.patient().insertAll(patientlist);

        updateStaff(message.getDoctorlist(), message.getNurselist());

        //病房信息，拼凑而成
        WatchTime watchtime = message.getWatchtime();
        mWard.setDepartment(message.getDepartment());
        mWard.setRoomId(message.getRoomId());
        mWard.setRoomname(message.getRoomname());
        mWard.setMorning(watchtime.getMorning());
        mWard.setNoon(watchtime.getNoon());
        mWard.setNight(watchtime.getNight());
        mWardDataBase.ward().insert(mWard);
    }

    /**
     * 更新医护信息，本病房下所有医护信息
     * 不同于患者，医护信息更新是整体的，病人可以单个修改
     * 先清空表，再重新插入最新的所有数据
     *
     * @param doctorlist
     * @param nurselist
     */
    private void updateStaff(List<Doctor> doctorlist, List<Nurse> nurselist) {
        List<Staff> staffs = new ArrayList<>();
        if (doctorlist != null) {
            staffs.addAll(doctorlist);
        }
        if (nurselist != null) {
            staffs.addAll(nurselist);
        }

        mWardDataBase.staff().deleteAll();
        if (staffs.size() != 0) {
            mWardDataBase.staff().insertAll(staffs);
        }
    }

    /**
     * 处理呼叫转移，交由MainActivy处理对话框显示
     * 显示定位对话框
     * f lag=1 显示呼叫转移对话框  flag=0 取消 手动点击对话框的取消 要发送取消命令
     */
    private void handleCallTransfer(CallTransfer callTransfer, String action, JSONObject jsonObject) {
        MainActivity.sendUpdate(MainActivity.CALL_TRANSFER, callTransfer.getDestinationname(), callTransfer.getFlag() != 0);
        //如果存在其他呼叫增援等状态时，就不需要关心开关屏，否则开关屏与显示对话框同步
        if (mDoorLightHelper.getDoorLightTypes().size() == 0) {
            notifyCallOnOff(callTransfer.getFlag() != 0);
        }
        DoorLightHelper.DoorLightType doorLightType = getDoorLightType(action, jsonObject);
        //如果不是期望的类型返回为空
        if (null == doorLightType) {
            return;
        }
        Log.d(TAG, "handleCallTransfer " + doorLightType.toString());
        //显示呼叫转移，显示之前会取消之前的呼叫
        if (callTransfer.getFlag() == 1) {
            mDoorLightHelper.put(doorLightType);
        } else {
//                    取消呼叫转移
            mDoorLightHelper.removeCallTransfer();
        }
        //获取队列的的所有提示消息
        mWard.setCallTip(mDoorLightHelper.getTips());
        mWardDataBase.ward().insert(mWard);
    }

    /**
     * @param action     其他设备发出的服务类型，如 增援，呼叫，输液提醒
     * @param jsonObject 判读优先级，具体设备的数据
     * @return
     */
    private DoorLightHelper.DoorLightType getDoorLightType(String action, final JSONObject jsonObject) {
        DoorLightHelper.DoorLightType doorLightType = new DoorLightHelper.DoorLightType();
        String sender = jsonObject.optString("sender");
        //护士站对应科室下所有门口屏，门口屏对应病房，判断消息针对本病房的门口屏
        if ("station".equals(sender)) {
            JSONObject data = jsonObject.optJSONObject("data");
            if (data != null) {
                String roomid = data.optString("roomid");
                Log.d(TAG, "roomid " + roomid);
                //如果护士站发来的消息不是本病房的， 忽略
                if (!MQTTClient.INSTANCE().getParameter().isMyRoom(roomid)) {
                    return null;
                }
                //呼叫转移的时候，不处理护士站的呼叫
                if ("call".equals(action)) {
                    return null;
                }
            }
        }
        String clientmac = jsonObject.optString("clientmac");
        String clientname = jsonObject.optString("clientname");
        //如果是护士站发来的呼叫转移，需要获取destinationmac，而不是clientmac
        if ("station".equals(sender) && "transfer".equals(action)) {
            clientmac = jsonObject.optString("destinationmac");
        }
        doorLightType.setClientmac(clientmac);
        switch (action) {
            //增援
            case "reinforcement":
                doorLightType.setPriority(1);
                doorLightType.setInstruction(AppEntrance.getAppEntrance().getResources().getString(R.string.long_orange));
                doorLightType.setTip(clientname + "请求增援");
                break;
            case "service":
                doorLightType.setPriority(2);
                doorLightType.setInstruction(AppEntrance.getAppEntrance().getResources().getString(R.string.long_green));
                doorLightType.setTip(clientname + "请求服务");
                break;
            case "call":
                doorLightType.setPriority(3);
                doorLightType.setCurrentTime(System.currentTimeMillis());
                doorLightType.setTip(clientname + "正在呼叫");
                if ("screen".equals(sender)) {
                    doorLightType.setInstruction(AppEntrance.getAppEntrance().getResources().getString(R.string.long_red));
                } else if ("bathroom".equals(sender)) {
                    doorLightType.setInstruction(AppEntrance.getAppEntrance().getResources().getString(R.string.long_blue));
                }
                break;
            case "transfer":
                doorLightType.setPriority(4);
                doorLightType.setCurrentTime(System.currentTimeMillis());
                doorLightType.setInstruction(AppEntrance.getAppEntrance().getResources().getString(R.string.long_green));
                doorLightType.setTip(clientname + "呼叫转移");
                break;
        }

        return doorLightType;
    }

    //通知后台呼叫状态，方便调整开关屏
    private void notifyCallOnOff(boolean callOn) {
        if (callOn) {
            DoorService.startService(AppEntrance.getAppEntrance(), MainActivity.CALL_ON, "");
        } else {
            DoorService.startService(AppEntrance.getAppEntrance(), MainActivity.CALL_OFF, "");
        }
    }

    /**
     * 处理呼叫
     *
     * @param doorLightType 门灯类型
     */
    private void handleCall(DoorLightHelper.DoorLightType doorLightType) {
        notifyCallOnOff(true);
        //添加到门灯显示队列，由其根据优先级负责正确的显示
        mDoorLightHelper.put(doorLightType);
        //获取队列的的所有提示消息
        mWard.setCallTip(mDoorLightHelper.getTips());
        mWardDataBase.ward().insert(mWard);
    }

    /**
     * 处理取消呼叫
     *
     * @param doorLightType 门灯类型
     */
    private void handleCancelCall(DoorLightHelper.DoorLightType doorLightType) {
        //  从门灯队列移除一个，重新显示队列门灯
        mDoorLightHelper.remove(doorLightType);
        //获取提示
        mWard.setCallTip(mDoorLightHelper.getTips());
        mWardDataBase.ward().insert(mWard);
        //没有定位 或有其他呼叫
        if (mCallTransfer == null || 0 == mCallTransfer.getFlag()) {
            //没有呼叫
            if (mDoorLightHelper.getDoorLightTypes().size() == 0) {
                //通知关屏
                notifyCallOnOff(false);
            }
        }
    }

    /**
     * 如果从后台删除门口屏
     * 清空数据库内容,UI会自动更新
     * 重新请求数据，方便后台重新添加
     */
    private void handleDeleteFromSystem(JSONObject jsonObject) {
        int clienttype = jsonObject.optInt("clienttype");
        String clientmac = jsonObject.optString("clientmac");
        //删除床头屏
        if (1 == clienttype) {
            if ("platform".equals(jsonObject.optString("sender"))) {
                Patient patient = new Patient(clientmac);
                //根据mac删除
                mWardDataBase.patient().delete(patient);
            }
        } else if (13 == clienttype && getMacAddress().equals(clientmac)) {
            //删除门口屏相关数据，UI在监听会自动更新
            mWardDataBase.patient().deleteAll();
            mWardDataBase.staff().deleteAll();
            mWardDataBase.ward().deleteAll();
            //UI没监听，需要通知
            mWardDataBase.reStartDao().deleteAll();
//            此时仅仅取消关机设置
            DoorService.startService(AppEntrance.getAppEntrance(), RESTART, "");
            MQTTClient.INSTANCE().onConnectSucceed();
        }
    }

    /**
     * 添加更新床位
     *
     * @param jsonObject
     */
    private void handleAddOrUpdateToSystem(String action, JSONObject jsonObject) {
        int clienttype = jsonObject.optInt("clienttype");
        String clientmac = jsonObject.optString("clientmac");
        if (1 == clienttype) {
            if ("platform".equals(jsonObject.optString("sender"))) {
                String bedno = jsonObject.optString("clientname");
                //区别对待添加和更新，否则更新的时候会覆盖患者信息
                if ("addedtosystem".equals(action)) {
                    Patient patient = new Patient(clientmac);
                    patient.setBedno(bedno);
                    mWardDataBase.patient().replace(patient);
                } else if ("updatetosystem".equals(action)) {
                    mWardDataBase.patient().update(bedno, clientmac);
                }
            }
        } else if (13 == clienttype) {
            MQTTClient.INSTANCE().onConnectSucceed();
        }
    }

    //处理单个跑马灯，新建一个跑马灯和更新一个是不能区分的
    private void handleMarqueeInfo(MarqueeInfo marqueeInfo) {
        switch (marqueeInfo.getType()) {
            //停止没有开启方法，和删除是同一个意思
            case 0:
            case 1:
                mWardDataBase.marqueeDao().stopMarquee(marqueeInfo.getMarqueeid());
                MainActivity.sendUpdate(MainActivity.MARQUEE_UPDATE);
                break;
            //更新跑马灯
            case 2:
                updateSingleMarquee(marqueeInfo);
                break;
        }
    }

    //更新单个跑马灯
    private void updateSingleMarquee(MarqueeInfo marqueeInfo) {
        Marquee marquee = marqueeInfo.getData();
        if (marquee != null) {
            marquee.setMarqueeid(marqueeInfo.getMarqueeid());
            List<MarqueeTime> playTime = marquee.getPlaytimes();
            if (playTime == null || playTime.size() == 0) {
                return;
            }
            for (MarqueeTime time : playTime) {
                if (time == null) {
                    continue;
                }
                time.setMarqueeid(marquee.getMarqueeid());
            }
            try {
                mWardDataBase.beginTransaction();
//                插入最新的信息，相同的会替换
                mWardDataBase.marqueeDao().insertMarquee(marquee);
//                删除旧跑马灯的时间表 重新插入
                mWardDataBase.marqueeTimeDao().deleteMarqueeTime(marquee.getMarqueeid());
                mWardDataBase.marqueeTimeDao().insertAll(playTime);
                mWardDataBase.setTransactionSuccessful();
            } finally {
                mWardDataBase.endTransaction();
            }
            MainActivity.sendUpdate(MainActivity.MARQUEE_UPDATE);
        }
    }

    //同步后台跑马灯列表，防止离线 换服务器时数据不同步
    private void synMarqueeList(MarqueeList marqueeList) {
        Log.d(TAG, "synMarqueeList");
        try {
            //先清空数据库
            mWardDataBase.beginTransaction();
//            会级联删除
            mWardDataBase.marqueeDao().deleteAllMarquees();
            if (marqueeList != null) {
                List<Marquee> marquees = marqueeList.getData();
                //先插入所有跑马灯信息
//                mWardDataBase.marqueeDao().insertAll(marquees);
                for (Marquee marquee : marquees) {
                    //修改状态，不是播放状态 都为-1
                    if (2 != marquee.getType()) {
                        marquee.setStatus(-1);
                    }
                    mWardDataBase.marqueeDao().insertMarquee(marquee);
                    List<MarqueeTime> playtimes = marquee.getPlaytimes();
                    for (MarqueeTime playtime : playtimes) {
                        playtime.setMarqueeid(marquee.getMarqueeid());
                    }
//                    为每个跑马灯插入时刻表数据
                    mWardDataBase.marqueeTimeDao().insertAll(playtimes);
                }
                mWardDataBase.setTransactionSuccessful();
            }
            MainActivity.sendUpdate(MainActivity.MARQUEE_UPDATE);
        } finally {
            mWardDataBase.endTransaction();
        }
    }

    //处理病室患者信息的更新
    private void handleSynPatient(SynPatient synPatient) {
        SynPatient.DataBean data = synPatient.getData();
        Patient patient = null;
        if (data != null) {
//                    0_修改，1_删除，2_增加
            switch (data.getType()) {
                //删除病人信息，但保留床位
                case 1:
                    patient = new Patient(data.getDevicemac());
                    patient.setBedno(data.getBednum());
                    break;
                case 0:
                case 2:
                    patient = new Patient(data.getUsername(), data.getBednum(),
                            data.getDoctorname(), data.getDevicemac(), false);
                    break;
                default:
                    Log.d(TAG, "unkown opertaion");

            }
            if (patient != null && !TextUtils.isEmpty(patient.getClientmac())) {
                mWardDataBase.patient().replace(patient);
            }
        }
    }

    //处理医护信息的更新
    private void handleSynStaff(SynStaff synStaff) {
        Log.d(TAG, "handleSynStaff() called with: receive ");
        List<Doctor> doctorlist = synStaff.getDoctorlist();
        List<Nurse> nurselist = synStaff.getNurselist();
        updateStaff(doctorlist, nurselist);
    }

//    门口屏手动取消呼叫
    public void handleCancelCallTransfer(String topic) {
        if (mCallTransfer != null) {
            mCallTransfer.setFlag(0);
            mCallTransfer.setSender("station");
            String message = mGson.toJson(mCallTransfer);
            Log.d(TAG, "cancle call transfer" + message);
            MQTTClient.INSTANCE().publishMessage(message,topic);
            //如果存在其他呼叫增援等状态时，就不需要关心开关屏，否则开关屏与显示对话框同步
            if (mDoorLightHelper.getDoorLightTypes().size() == 0) {
                notifyCallOnOff(false);
            }

        }
    }

}
