package shine.com.doorscreen.mqtt;

import android.app.AlarmManager;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import shine.com.doorscreen.R;
import shine.com.doorscreen.activity.MainActivity;
import shine.com.doorscreen.app.AppEntrance;
import shine.com.doorscreen.database.WardDataBase;
import shine.com.doorscreen.mqtt.bean.CallTransfer;
import shine.com.doorscreen.mqtt.bean.Doctor;
import shine.com.doorscreen.mqtt.bean.DoorScreenMessage;
import shine.com.doorscreen.mqtt.bean.Marquee;
import shine.com.doorscreen.mqtt.bean.MarqueeInfo;
import shine.com.doorscreen.mqtt.bean.MarqueeList;
import shine.com.doorscreen.mqtt.bean.Message;
import shine.com.doorscreen.mqtt.bean.Nurse;
import shine.com.doorscreen.mqtt.bean.Patient;
import shine.com.doorscreen.mqtt.bean.ReBoot;
import shine.com.doorscreen.mqtt.bean.ReStart;
import shine.com.doorscreen.mqtt.bean.Staff;
import shine.com.doorscreen.mqtt.bean.SynPatient;
import shine.com.doorscreen.mqtt.bean.SynStaff;
import shine.com.doorscreen.mqtt.bean.SystemInfo;
import shine.com.doorscreen.mqtt.bean.SystemLight;
import shine.com.doorscreen.mqtt.bean.Transfer;
import shine.com.doorscreen.mqtt.bean.VisitTime;
import shine.com.doorscreen.mqtt.bean.Ward;
import shine.com.doorscreen.mqtt.bean.WatchTime;
import shine.com.doorscreen.service.DoorLightHelper;
import shine.com.doorscreen.service.DoorService;
import shine.com.doorscreen.service.ScreenManager;
import shine.com.doorscreen.service.VolumeManager;
import shine.com.doorscreen.util.Common;
import shine.com.doorscreen.util.IniReaderNoSection;
import shine.com.doorscreen.util.RootCommand;

import static java.lang.Integer.parseInt;
import static shine.com.doorscreen.util.Common.getMacAddress;


/**
 * author:
 * 时间:2017/6/29
 * qq:1220289215
 * 类描述：mqtt通讯客户端
 * 1.需要设置用户名和密码
 * <p>
 * <p>
 * 网络通信处理有待优化
 * 统一信息处理方式，在HandlerThread 的子线程处理
 */

public class MQTTClient {
    private static final String TAG = "MQTTClient";
    public static final String ETHERNET_PATH = "/extdata/work/show/system/network.ini";

    private String subscriptionTopic = "";
    private Gson mGson = new Gson();
    private String mDepartmentId = "1";
    private String mRoomId = "1";
    private static final String USER_NAME = "shine";
    private static final String PASSWORD = "shine";


    private MqttAndroidClient mqttAndroidClient;
    /**
     * 服务送达消息的质量，0～2，2表示肯定送达
     */
    private static final int mQos = 1;
    /**
     * 消息是否被服务器保留
     */
    private static final boolean mRetained = false;
    /**
     * 主机地址
     */
    private String mHost = "";
    /**
     * 主机mac
     */
    private String mClientId = "";
    /**
     * 订阅的主题,用来取消订阅
     */
    private HashSet<String> mTopicSubscribed = new HashSet<>();
    private Ward mWard = new Ward();
    private WardDataBase mWardDataBase;
    private Handler mHandler;
    //是否获取后台最新的数据
    private boolean isUpdate = false;
    private Context mContext;
    //门灯管理
    private DoorLightHelper mDoorLightHelper = new DoorLightHelper();
    private boolean onceSucceed = false;

    private boolean exiting = false;
    private MqttListener mMqttListener;
    private CallTransfer mCallTransfer;

    public MQTTClient() {
    }

    public MQTTClient(MqttListener mqttListener) {
        mMqttListener = mqttListener;
    }


    /**
     * 与服务端连接，建立通信
     */
    public void startConnect(Context context) {
        if (context == null) {
            throw new NullPointerException("context can not be null");
        }
        mContext = context;
        dismissDialog();
        //数据存储
        mWardDataBase = WardDataBase.INSTANCE(context);

        HandlerThread handlerThread = new HandlerThread("worker");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());

//        从本地获取服务器的IP和端口
        IniReaderNoSection inir = new IniReaderNoSection(ETHERNET_PATH);
        String serverIp = inir.getValue("commuip");
        String port = inir.getValue("commuport");
//        没有网线连接的时候 此方法获取的IP无效
        mHost = inir.getValue("ip");
//        MQTT需要的格式化地址
        final String serverUri = String.format(Locale.CHINA, "tcp://%s:%s", serverIp, port);
//        final String serverUri = "tcp://172.168.1.9:1883";
//        使用mac地址作为客户唯一标识,如E06417050201，无分隔符
        mClientId = getMacAddress();

        mqttAndroidClient = new MqttAndroidClient(context.getApplicationContext(), serverUri, mClientId);
        mqttAndroidClient.setCallback(mCallbackExtended);
        try {
            mqttAndroidClient.connect(getMqttConnectOptions(), null, mConnectListener);
        } catch (MqttException ex) {
            ex.printStackTrace();
        }
    }

    private static String getEncodeString(String raw) {
        if (TextUtils.isEmpty(raw)) {
            return null;
        }
        return Base64.encodeToString(raw.getBytes(), Base64.DEFAULT);
    }

    /**
     * 生成门口屏订阅的主题
     *
     * @param departmentId
     * @param roomId
     * @param mac
     * @return
     */
    private String getSubscriptionTopic(String departmentId, String roomId, String mac) {
        return String.format(Locale.CHINA, "/shineecall/%s/13/%s/%s/sub", departmentId, roomId, mac);
    }

    /**
     * 生成门口屏发布的主题
     *
     * @param departmentId
     * @param roomId
     * @param mac
     * @return
     */
    private String getpublishTopic(String departmentId, String roomId, String mac) {
        return String.format(Locale.CHINA, "/shineecall/%s/13/%s/%s/pub", departmentId, roomId, mac);
    }

    /**
     * 获取MQTT连接选项
     * 在此设置遗嘱信息
     *
     * @return
     */
    private MqttConnectOptions getMqttConnectOptions() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setConnectionTimeout(30);
        mqttConnectOptions.setKeepAliveInterval(60);
        mqttConnectOptions.setCleanSession(true);
        mqttConnectOptions.setUserName(USER_NAME);
        mqttConnectOptions.setPassword(PASSWORD.toCharArray());
        //断开连接的主题
        String topic = String.format(Locale.CHINA, "/shineecall/%s/disconnect", mClientId);
        //断开的消息
        String payLoad = getDisconnectNotification(mHost, mClientId);
        mqttConnectOptions.setWill(topic, Base64.encode(payLoad.getBytes(), Base64.DEFAULT), mQos, mRetained);
        return mqttConnectOptions;
    }


    private DisconnectedBufferOptions getBufferOpt() {
        DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
        disconnectedBufferOptions.setBufferEnabled(true);
        disconnectedBufferOptions.setBufferSize(100);
        disconnectedBufferOptions.setPersistBuffer(false);
        disconnectedBufferOptions.setDeleteOldestMessages(false);
        return disconnectedBufferOptions;
    }

    /**
     * 连接服务器监听
     */
    private IMqttActionListener mConnectListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            Log.d(TAG, "onSuccess() called with " + mqttAndroidClient.isConnected());
            onceSucceed = true;
            dismissDialog();
            onConnectSucceed();
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            Log.d(TAG, "Failed to connect to: " + mqttAndroidClient.isConnected());
            //在断交换机重连时会出现重连成功后又调用此回调，导致断网对话框再次显示
            if (mqttAndroidClient.isConnected()) {
                return;
            }
            showDialog();

            //在没连接MQTT服务器成功的情况下没有自动重连，需要自己重连服务器，
            // 如果成功了，也不要自己重新，MQTT会报错
            if (!onceSucceed) {
                mHandler.removeCallbacks(mRunnable);
                mHandler.postDelayed(mRunnable, 5000);
            }
        }
    };

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                mqttAndroidClient.connect(getMqttConnectOptions(), null, mConnectListener);
            } catch (MqttException ex) {
                ex.printStackTrace();
            }
        }
    };

    private MqttCallbackExtended mCallbackExtended = new MqttCallbackExtended() {
        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            dismissDialog();
            if (reconnect) {
                Log.d(TAG, "Reconnected to : " + serverURI);
                onConnectSucceed();
            } else {
                Log.d(TAG, "Connected to : " + serverURI);
            }
        }

        @Override
        public void connectionLost(Throwable cause) {
            Log.d(TAG, "The Connection was lost. " + mqttAndroidClient.isConnected());
            //ondestory时调用退出exit发送断开命令，会回调此方法，此时context可能为空，加一个判断
            if (!exiting) {
                showDialog();
            }
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            final String receive = new String(Base64.decode(message.getPayload(), Base64.DEFAULT));
            Log.d(TAG, "from topic:" + topic + "  Incoming message: " + receive);
            final JSONObject jsonObject = new JSONObject(receive);
            final String action = jsonObject.optString("action");
            switch (action) {
                case "getdoorscreeninfo":
                    int department = jsonObject.optInt("department");
                    int roomId = jsonObject.optInt("roomId");
                    if (-1 == department || -1 == roomId) {
                        return;
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final DoorScreenMessage doorScreenMessage = mGson.fromJson(receive, DoorScreenMessage.class);
                                handleSubscribe(doorScreenMessage.getDepartment(), doorScreenMessage.getRoomId());
                                synLocalTime(doorScreenMessage.getTime());
                                updateDataBase(doorScreenMessage);
                                isUpdate = true;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    break;
                //后台发来的转组通知
                case "transferinsystem":
                    try {
                        Transfer transfer = mGson.fromJson(receive, Transfer.class);
                        if (transfer != null) {
                            handleTranfer(transfer.getDepartid(), transfer.getRoomid());
                        }
                    } catch (JsonSyntaxException | MqttException e) {
                        e.printStackTrace();
                    }
                    break;
                case "transfer":
                    handleCallTransfer(receive, action, jsonObject);
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
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            DoorLightHelper.DoorLightType doorLightType = getDoorLightType(action, jsonObject);
                            //如果不是期望的类型返回为空
                            if (null == doorLightType) {
                                return;
                            }

                            notifyCallOnOff(true);
//                            添加到门灯显示队列，由其根据优先级负责正确的显示
                            mDoorLightHelper.put(doorLightType);
//                            mDoorLightHelper.add(doorLightType);
                            //获取队列的的所有提示消息
                            mWard.setCallTip(mDoorLightHelper.getTips());
                            mWardDataBase.ward().insert(mWard);
                        }
                    });
                    break;
                case "finish":
                case "cancelposition":
                case "acceptcall":
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            DoorLightHelper.DoorLightType doorLightType = getDoorLightType(action, jsonObject);
                            if (null == doorLightType) {
                                return;
                            }

//                                从门灯队列移除一个，重新显示队列门灯
                            mDoorLightHelper.remove(doorLightType);
                            //获取提示
                            mWard.setCallTip(mDoorLightHelper.getTips());
                            mWardDataBase.ward().insert(mWard);
                            //没有定位 或有其他呼叫
                            if (mCallTransfer == null || 0 == mCallTransfer.getFlag()) {
                                //没有呼叫
                                if (mDoorLightHelper.getDoorLightTypes().size() == 0) {
//                                    通知关屏
                                    notifyCallOnOff(false);
                                }
                            }
                        }
                    });

                    break;
                //目前仅开关屏时间
                case "acceptscreenlight":
                    handleScreenOnOff(receive);
                    break;
                case "acceptsystemvolume":
                    if (13 == jsonObject.optInt("clienttype")) {
                        handleVolumeSet(receive);
                    }
                    break;
                case "reboot":
                    //如果设备类型是门口屏
                    if (13 == jsonObject.optInt("clienttype")) {
                        DoorService.startService(mContext, MainActivity.REBOOT, receive);
                    }
                    break;
                //定时重启
                case "accepttimedreboot":
                    ReBoot reBoot = mGson.fromJson(receive, ReBoot.class);
                    if (reBoot != null && 13 == reBoot.getClienttype()) {
                        List<ReStart> datalist = reBoot.getDatalist();
                        if (datalist != null && datalist.size() > 0) {
                            mWardDataBase.reStartDao().insertAll(datalist);
                            if (mMqttListener != null) {
                                mMqttListener.updateReStart();
                            }
                        }
                    }
                    break;
                //后台重启上线后
                case "connected":
                    Log.d(TAG, "connected");
                    if ("server".equals(jsonObject.optString("sender"))) {
                        onConnectSucceed();
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
                    handleMarqueeInfo(receive);
                    break;
                //同步后台跑马灯列表，防止离线 换服务器时数据不同步
                case "marqueelist":
                    synMarqueeList(receive);
                    break;
                case "synpatient":
                    handleSynPatient(receive);
                    break;
                case "workersinfo":
                    handleSynStaff(receive);
                    break;
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {

        }
    };

    /**
     * 处理音量设置
     *
     * @param json
     */
    private void handleVolumeSet(String json) {
        try {
            SystemInfo systemVolume = mGson.fromJson(json, SystemInfo.class);
            if (systemVolume != null) {
                List<SystemLight> list = systemVolume.getDatalist();
                if (list != null && list.size() > 1) {
                    //这个只用来获取晚上音量
                    SystemLight volumeNight = list.get(0);
                    //最后一个数据有白天，晚上的分割点，及音量
                    SystemLight volumeDay = list.get(list.size() - 1);
                    int mVolumeNight = volumeNight.getValue();
                    int mVolumeDay = volumeDay.getValue();
                    Log.d(TAG, "volumeNightValue:" + mVolumeNight + "volumeDayValue:" + mVolumeDay);
                    int mVolumeDayHour = 0;
                    int mVolumeDayMinute = 0;
                    int mVolumeNightHour = 0;
                    int mVolumeNightMinute = 0;

                    String[] start = volumeDay.getStart().split(":");
                    if (start.length > 1) {
                        mVolumeDayHour = parseInt(start[0]);
                        mVolumeDayMinute = parseInt(start[1]);
                        Log.d(TAG, "volumeDayPoint:" + mVolumeDayHour + "-" + mVolumeDayMinute);
                    }
                    String[] end = volumeDay.getStop().split(":");
                    if (end.length > 1) {
                        mVolumeNightHour = parseInt(end[0]);
                        mVolumeNightMinute = parseInt(end[1]);
                        Log.d(TAG, "volumeNightPoint:" + mVolumeNightHour + "--" + mVolumeNightMinute);
                    }
                    VolumeManager.getInstance(mContext).saveVolumeParam(mVolumeDay, mVolumeNight, mVolumeDayHour, mVolumeDayMinute,
                            mVolumeNightHour, mVolumeNightMinute);
                    DoorService.startService(mContext, MainActivity.VOLUME_SWITCH, "");

                }
            }

        } catch (JsonSyntaxException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    //通知后台呼叫状态，方便调整开关屏
    private void notifyCallOnOff(boolean callOn) {
        if (callOn) {
            DoorService.startService(mContext, MainActivity.CALL_ON, "");
        } else {
            DoorService.startService(mContext, MainActivity.CALL_OFF, "");
        }
    }

    /**
     * 处理开关屏
     *
     * @param receive
     */
    private void handleScreenOnOff(String receive) {
        try {
            SystemInfo systemInfo = mGson.fromJson(receive, SystemInfo.class);
            //13表示类型是门口屏，其他类型不处理
            if (systemInfo != null && 13 == systemInfo.getClienttype()) {
                List<SystemLight> datalist = systemInfo.getDatalist();
                if (datalist != null && datalist.size() > 0) {
                    //最后一个数据是有用的，其他无用
                    SystemLight lightParam = datalist.get(datalist.size() - 1);
                    String[] start = lightParam.getStart().split(":");
                    int openScreenHour = 0;
                    int openScreenMinute = 0;
                    int closeScreenHour = 0;
                    int closeScreenMinute = 0;
                    if (start.length > 1) {
                        openScreenHour = parseInt(start[0]);
                        openScreenMinute = parseInt(start[1]);
                    }
                    String[] end = lightParam.getStop().split(":");
                    if (end.length > 1) {
                        closeScreenHour = parseInt(end[0]);
                        closeScreenMinute = parseInt(end[1]);
                    }
                    //保存到本地
                    ScreenManager.getInstance(mContext)
                            .saveScreenOnOffParams(openScreenHour, openScreenMinute, closeScreenHour, closeScreenMinute, lightParam.getValue());
//                    String json = mGson.toJson(lightParam);
                    //通知后台更新
                    DoorService.startService(mContext, MainActivity.SCREEN_SWITCH, "");
                }
            }
        } catch (JsonSyntaxException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理呼叫转移，交由MainActivy处理对话框显示
     * 显示定位对话框
     *
     * @param receive    flag=1 显示呼叫转移对话框  flag=0 取消
     *                   手动点击对话框的取消 要发送取消命令
     * @param action
     * @param jsonObject
     */
    private void handleCallTransfer(String receive, String action, JSONObject jsonObject) {
        try {
            mCallTransfer = mGson.fromJson(receive, CallTransfer.class);
            if (mCallTransfer != null) {
                mMqttListener.handleCallTransfer(mCallTransfer.getDestinationname(), mCallTransfer.getFlag() != 0);
                //如果存在其他呼叫增援等状态时，就不需要关心开关屏，否则开关屏与显示对话框同步
                if (mDoorLightHelper.getDoorLightTypes().size() == 0) {
                    notifyCallOnOff(mCallTransfer.getFlag() != 0);
                }
                DoorLightHelper.DoorLightType doorLightType = getDoorLightType(action, jsonObject);
                //如果不是期望的类型返回为空
                if (null == doorLightType) {
                    return;
                }
                Log.d(TAG, "handleCallTransfer " + doorLightType.toString());

                //显示呼叫转移，显示之前会取消之前的呼叫
                if (mCallTransfer.getFlag() == 1) {
//                    mDoorLightHelper.add(doorLightType);
                    mDoorLightHelper.put(doorLightType);
                } else {
//                    取消呼叫转移
//                    mDoorLightHelper.remove(doorLightType);
                    mDoorLightHelper.removeCallTransfer();
                }
                //获取队列的的所有提示消息
                mWard.setCallTip(mDoorLightHelper.getTips());
                mWardDataBase.ward().insert(mWard);

            }
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * 取消呼叫转移
     */
    public void handleCancelCallTransfer() {
        if (mCallTransfer != null) {
            mCallTransfer.setFlag(0);
            mCallTransfer.setSender("station");
            String message = mGson.toJson(mCallTransfer);
            Log.d(TAG, "cancle call transfer" + message);
            publishMessage(message, String.format(Locale.CHINA, "/shineecall/%s/broadcast", mDepartmentId));
            //如果存在其他呼叫增援等状态时，就不需要关心开关屏，否则开关屏与显示对话框同步
            if (mDoorLightHelper.getDoorLightTypes().size() == 0) {
                notifyCallOnOff(false);
            }

        }
    }

    //处理医护信息的更新
    private void handleSynStaff(String receive) {
        Log.d(TAG, "handleSynStaff() called with: receive ");
        try {
            SynStaff synStaff = mGson.fromJson(receive, SynStaff.class);
            if (synStaff != null) {
                List<Doctor> doctorlist = synStaff.getDoctorlist();
                List<Nurse> nurselist = synStaff.getNurselist();
                updateStaff(doctorlist, nurselist);
            }
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }

    }

    //处理病室患者信息的更新
    private void handleSynPatient(String receive) {
        try {
            SynPatient synPatient = mGson.fromJson(receive, SynPatient.class);
            if (synPatient != null) {
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
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
    }

    //处理单个跑马灯，新建一个跑马灯和更新一个是不能区分的
    private void handleMarqueeInfo(String receive) {
        try {
            MarqueeInfo marqueeInfo = mGson.fromJson(receive, MarqueeInfo.class);
            if (marqueeInfo != null) {
                switch (marqueeInfo.getType()) {
                    //停止没有开启方法，和删除是同一个意思
                    case 0:
                    case 1:
                        mWardDataBase.marqueeDao().stopMarquee(marqueeInfo.getMarqueeid());
                        if (mMqttListener != null) {
                            mMqttListener.onMarqueeUpdate();
                        }
//                        DoorScreenDataBase.getInstance(mContext).stopMarquee(marqueeInfo.getMarqueeid());
//                        DoorService.startService(mContext, MARQUEE_STOP, "");
                        break;
                    //删除
                   /* case 1:
                        mWardDataBase.marqueeDao().deleteMarquee(marqueeInfo.getMarqueeid());
                        if (mMqttListener != null) {
                            mMqttListener.onMarqueeDelete(marqueeInfo.getMarqueeid());
                        }
//                        DoorScreenDataBase.getInstance(mContext).deleteMarquee(marqueeInfo.getMarqueeid());
                        break;*/
                    //更新跑马灯
                    case 2:
//                        DoorScreenDataBase.getInstance(mContext).insertMarquee(data, marqueeInfo.getMarqueeid());
//                        DoorService.startService(mContext, MARQUEE_UPDATE, "");
                        updateSingleMarquee(marqueeInfo);
                        break;
                }

            }
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
    }

    //更新单个跑马灯
    private void updateSingleMarquee(MarqueeInfo marqueeInfo) {
        List<Marquee> marquees = new ArrayList<>();
        MarqueeInfo.DataBean data = marqueeInfo.getData();
        if (data != null) {
            List<MarqueeInfo.DataBean.PlaytimesBean> playTime = data.getPlaytimes();
            if (playTime == null || playTime.size() == 0) {
                Log.e(TAG, "invalid play times");
                return;
            }
            for (MarqueeInfo.DataBean.PlaytimesBean time : playTime) {
                if (time == null) {
                    Log.e(TAG, "play time is null");
                    continue;
                }
                Marquee marquee = new Marquee(marqueeInfo.getMarqueeid(), data.getMessage(), data.getStartdate(),
                        data.getStopdate(), time.getStart(), time.getStop(), 0);
                marquees.add(marquee);
            }
        }
        if (marquees.size() > 0) {
            //先删除旧的，因为新旧时间段不同导致个数不同，所以不能替换
            mWardDataBase.marqueeDao().deleteMarquee(marqueeInfo.getMarqueeid());
            mWardDataBase.marqueeDao().insertAll(marquees);
            if (mMqttListener != null) {
                mMqttListener.onMarqueeUpdate();
            }
        }
    }

    //同步后台跑马灯列表，防止离线 换服务器时数据不同步
    private void synMarqueeList(String receive) {
        Log.d(TAG, "synMarqueeList");
        try {
            MarqueeList marqueeList = mGson.fromJson(receive, MarqueeList.class);
            //先清空数据库
            mWardDataBase.marqueeDao().deleteAllMarquees();
            if (marqueeList != null) {
                //把数据封装成我们需要的样子
                List<MarqueeList.DataBean> data = marqueeList.getData();
                List<Marquee> marquees = processMarqueeTransfer(data);
               /* for (Marquee marquee : marquees) {
                    Log.d(TAG, "marquee " + marquee.toString());
                }*/
                if (mMqttListener != null && marquees.size() > 0) {
                    mWardDataBase.marqueeDao().insertAll(marquees);
                    mMqttListener.onMarqueeUpdate();
                }
            }

        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }

    }

    /**
     * 提取数据把跑马灯封装成我们需要的格式
     * 跑马灯有日期和多个时间段组成，以前分开存储，现在存在一个表上,每个时间段都有跑马灯数据，会重复
     *
     * @param marqueeList
     * @return
     */
    public List<Marquee> processMarqueeTransfer(@NonNull List<MarqueeList.DataBean> marqueeList) {
        List<Marquee> marquees = new ArrayList<>();
        for (MarqueeList.DataBean marqueeBean : marqueeList) {
            if (marqueeBean == null) {
                Log.e(TAG, "marquee is null");
                continue;
            }
            List<MarqueeList.DataBean.PlaytimesBean> playtimes = marqueeBean.getPlaytimes();
            if (playtimes == null) {
                Log.e(TAG, "play time is null");
                continue;
            }
            //根据类型判断跑马灯状态,如果不是发布状态直接忽略，停止和删除一样无效
            if (2 != marqueeBean.getType()) {
                continue;
            }
            for (MarqueeList.DataBean.PlaytimesBean time : playtimes) {
                Marquee marquee = new Marquee(marqueeBean.getMarqueeid(), marqueeBean.getMessage(),
                        marqueeBean.getStartdate(), marqueeBean.getStopdate(), time.getStart(), time.getStop(), 0);
                marquees.add(marquee);
            }
        }

        return marquees;

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
        } else if (13 == clienttype && mClientId.equals(clientmac)) {
            //删除门口屏相关数据，UI在监听会自动更新
            mWardDataBase.patient().deleteAll();
            mWardDataBase.staff().deleteAll();
            mWardDataBase.ward().deleteAll();
            //UI没监听，需要通知
            mWardDataBase.reStartDao().deleteAll();
            if (mMqttListener != null) {
                mMqttListener.updateReStart();
            }
            onConnectSucceed();
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
            onConnectSucceed();
        }
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
                if (mMqttListener != null) {
                    mMqttListener.onSynSucceed();
                }
            }
        }
    }

    /**
     * Incoming message: {"destinationname":"1床","destinationmac":"E06417180956","destinationip":"","action":"transfer","stationip":"172.168.32.114","stationmac":"E06420173214","sender":"station","flag":0}
     *
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
                if (!mRoomId.equals(roomid)) {
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
                doorLightType.setInstruction(mContext.getResources().getString(R.string.long_orange));
                doorLightType.setTip(clientname + "请求增援");
                break;
            case "service":
                doorLightType.setPriority(2);
                doorLightType.setInstruction(mContext.getResources().getString(R.string.long_green));
                doorLightType.setTip(clientname + "请求服务");
                break;
            case "call":
                doorLightType.setPriority(3);
                doorLightType.setCurrentTime(System.currentTimeMillis());
                doorLightType.setTip(clientname + "正在呼叫");
                if ("screen".equals(sender)) {
                    doorLightType.setInstruction(mContext.getResources().getString(R.string.long_red));
                } else if ("bathroom".equals(sender)) {
                    doorLightType.setInstruction(mContext.getResources().getString(R.string.long_blue));
                }
                break;
//            case "position":
//                呼叫转移
            case "transfer":
                doorLightType.setPriority(4);
                doorLightType.setCurrentTime(System.currentTimeMillis());
                doorLightType.setInstruction(mContext.getResources().getString(R.string.long_green));
                doorLightType.setTip(clientname + "呼叫转移");
                break;
            case "cancelposition":
            case "finish":
            case "acceptcall":
                //根据Mac地址移除门灯指令
                break;
        }

        return doorLightType;
    }


    /**
     * 更新数据库信息
     *
     * @param message
     * @throws Exception
     */
    private void updateDataBase(DoorScreenMessage message) throws Exception {
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
     * 没连上或与服务器断线的时候显示对话框
     */
    public void showDialog() {
        if (mMqttListener != null) {
            mMqttListener.handleOutOfInternet();
        }
    }


    private void dismissDialog() {
        if (mMqttListener != null) {
            mMqttListener.handleInternetRecovery();
        }
    }


    /**
     * 订阅主题
     *
     * @param subscriptionTopic 所需要订阅的主题，字符串形式
     * @param needPublish       是否需要发布消息
     */
    private void subscribeToTopic(final String subscriptionTopic, final boolean needPublish) {
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, mQos, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "subscribeToTopic:" + subscriptionTopic);
                    if (needPublish) {
                        //获取门口屏信息
                        Message message = new Message("getdoorscreeninfo", mClientId, mHost);
                        String getDoorScreenInfo = mGson.toJson(message);
                        String publishTopic = getpublishTopic(mDepartmentId, mRoomId, mClientId);
                        publishMessage(getDoorScreenInfo, publishTopic);
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "Failed to subscribe");
                }
            });

        } catch (MqttException ex) {
            Log.e(TAG, "Exception whilst subscribing");
            ex.printStackTrace();
        }
    }

    /**
     * @param publishMessage 要发布的信息
     * @param publishTopic   要发布的主题
     */
    private void publishMessage(String publishMessage, String publishTopic) {
        try {
            mqttAndroidClient.publish(publishTopic,
                    Base64.encode(publishMessage.getBytes(), android.util.Base64.DEFAULT), mQos, mRetained);
            Log.d(TAG, "publishMessage " + publishMessage + "\npublishTopic " + publishTopic);
           /* if (!mqttAndroidClient.isConnected()) {
                Log.d(TAG, mqttAndroidClient.getBufferedMessageCount() + " messages in buffer.");
            }*/
        } catch (MqttException e) {
            Log.d(TAG, "Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * 与服务器连接成功的处理
     */
    private void onConnectSucceed() {
        Log.d(TAG, "Connect onSuccess");
        mqttAndroidClient.setBufferOpts(getBufferOpt());
        //连接成功后订阅主题,由于此时还不知道科室id和房间id，使用通配符+,
        // 当获取到门口屏具体信息，需要取消此主题，重新订阅,否则就重复订阅，收到重复数据
        String topicForDoorSceen = getSubscriptionTopic("+", "+", mClientId);
        //用于取消通配符订阅
        subscriptionTopic = topicForDoorSceen;
        //true表示订阅成功后发布主题
        subscribeToTopic(topicForDoorSceen, true);

        //业务服务器死亡/上线通知，如果MQTT重启，此设备先于业务服务器连接，就收不到最新数据
        //所以必须提前订阅业务服务器的上线通知，更新数据,不取消订阅
        subscribeToTopic("/shineecall/callserver/broadcast", false);
//        mTopicSubscribed.add("/shineecall/callserver/broadcast");
    }

    //获取门口屏信息
    private void getDoorScreenInfo() {
        Message message = new Message("getdoorscreeninfo", mClientId, mHost);
        String getDoorScreenInfo = mGson.toJson(message);
        String publishTopic = getpublishTopic(mDepartmentId, mRoomId, mClientId);
        publishMessage(getDoorScreenInfo, publishTopic);
    }

    /**
     * 处理转组
     */
    public void handleTranfer(String departmentId, String roomId) throws MqttException {
        mDepartmentId = departmentId;
        mRoomId = roomId;
        //取消之前的订阅
        unSubscribe();
        String topForDoorScreen = getSubscriptionTopic(mDepartmentId, mRoomId, mClientId);
        //重新订阅，并发布消息
        subscribeToTopic(topForDoorScreen, true);
    }

    /**
     * 护士站，床头屏，卫生间和门口屏都是通过科室和房间号关联的
     * 当接收到门口屏科室和房间号后开始订阅护士站，床头屏，卫生间的动态
     */
    private void handleSubscribe(String departmentId, String roomId) throws MqttException {
        mqttAndroidClient.unsubscribe(subscriptionTopic);
        mDepartmentId = departmentId;
        mRoomId = roomId;
        subscirbe();
    }

    /**
     * 订阅需要的数据：门口屏的，床头屏的等等
     * 在获取到准确的房间号，或断线重连使用
     */
    private void subscirbe() {
        //门口屏接收数据
        String topForDoorScreen = getSubscriptionTopic(mDepartmentId, mRoomId, mClientId);
        subscribeToTopic(topForDoorScreen, false);
        mTopicSubscribed.add(topForDoorScreen);

        //床头屏发布数据 （所在病房所有床头屏）
        String bedScreen = String.format(Locale.CHINA, "/shineecall/%s/1/%s/+/pub", mDepartmentId, mRoomId);
        subscribeToTopic(bedScreen, false);
        mTopicSubscribed.add(bedScreen);

        //所在科室所有护士站
        String nurseStation = String.format(Locale.CHINA, "/shineecall/%s/2/+/broadcast", mDepartmentId);
        subscribeToTopic(nurseStation, false);
        mTopicSubscribed.add(nurseStation);


        //卫生间发布数据 （所在病房所有卫生间）
        String washRoom = String.format(Locale.CHINA, "/shineecall/%s/7/%s/+/pub", mDepartmentId, mRoomId);
        subscribeToTopic(washRoom, false);
        mTopicSubscribed.add(washRoom);

        //管理后台发布的对病房的数据
        String platform = String.format(Locale.CHINA, "/shineecall/%s/%s/broadcast", mDepartmentId, mRoomId);
        subscribeToTopic(platform, false);
        mTopicSubscribed.add(platform);

        //呼叫转移
        String transfer = String.format(Locale.CHINA, "/shineecall/%s/broadcast", mDepartmentId);
        subscribeToTopic(transfer, false);
        mTopicSubscribed.add(transfer);

        //订阅系统消息，不取消订阅
        subscribeToTopic("/shineecall/system/broadcast", false);

        //通知后台设备已连接
        Message message = new Message("connected", mClientId, mHost);
        String deviceConnected = mGson.toJson(message);
        String publishTopic = getpublishTopic(mDepartmentId, mRoomId, mClientId);
        publishMessage(deviceConnected, publishTopic);
    }

    /**
     * 断开连接的通知
     * 在连接服务器时设置为遗嘱，这样程序异常退出，服务器会收到
     * 如果是主动退出，需要发布端口连接消息到disconnect主题
     *
     * @param ip
     * @param mac
     * @return
     */
    private String getDisconnectNotification(String ip, String mac) {
        Message message = new Message("disconnect", mac, ip);
        return mGson.toJson(message);
    }

    /**
     * 取消订阅
     *
     * @throws MqttException
     */
    private void unSubscribe() throws MqttException {
        if (mTopicSubscribed.size() > 0) {
            String[] topics = new String[mTopicSubscribed.size()];
            mTopicSubscribed.toArray(topics);
            mTopicSubscribed.clear();
            mqttAndroidClient.unsubscribe(topics);
        }
    }

    /**
     * 退出程序时调用
     */
    public void exit() {
        try {
            exiting = true;
            if (mMqttListener != null) {
                mMqttListener = null;
            }
            dismissDialog();
            mDoorLightHelper.exit();
            if (mHandler != null) {
                mHandler.getLooper().quit();
                mHandler.removeCallbacksAndMessages(null);
                mHandler = null;
            }

            //不为空，已连接
            if (mqttAndroidClient != null) {
                String publishMessage = getDisconnectNotification(mHost, mClientId);
                String publishTopic = String.format(Locale.CHINA, "/shineecall/%s/disconnect", mClientId);
                //断开之前发布通知
                publishMessage(publishMessage, publishTopic);
                //取消之前的订阅
                unSubscribe();
                mqttAndroidClient.disconnect();
            }

        } catch (MqttException e) {
            Log.d(TAG, "exit exception:" + e);
            e.printStackTrace();
        }
    }

    public interface MqttListener {
        //与后台同步时间
        void onSynSucceed();

        //重启
        void updateReStart();

        //处理呼叫转移
        void handleCallTransfer(String tip, boolean show);

        void handleOutOfInternet();

        void handleInternetRecovery();

        void onMarqueeUpdate();


    }


}
