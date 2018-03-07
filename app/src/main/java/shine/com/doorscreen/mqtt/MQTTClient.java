package shine.com.doorscreen.mqtt;

import android.support.annotation.WorkerThread;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;

import shine.com.doorscreen.activity.MainActivity;
import shine.com.doorscreen.app.AppEntrance;
import shine.com.doorscreen.entity.Message;
import shine.com.doorscreen.service.LocalParameter;
import shine.com.doorscreen.util.Common;

import static shine.com.doorscreen.activity.MainActivity.INTERNET_OUT;
import static shine.com.doorscreen.activity.MainActivity.INTERNET_RECOVERY;


/**
 * author:
 * 时间:2017/6/29
 * qq:1220289215
 * 类描述：mqtt通讯客户端
 * 1.需要设置用户名和密码
 * <p>通信是全局的，设置成单例模式
 * <p>
 * <p>
 * 统一信息处理方式，在HandlerThread 的子线程处理
 * 主要处理与MQTT的通信 上下线  订阅 退出等
 *
 */

public class MQTTClient {
    private static final String TAG = "MQTTClient";
    private String subscriptionTopic = "";
    private Gson mGson;
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
     * 订阅的主题,用来取消订阅
     */
    private HashSet<String> mTopicSubscribed;
    private volatile static MQTTClient sMQTTClient;
    /**
     * 本地参数 服务器的ip 端口 本地ip 科室id 房间id等
     */
    private LocalParameter mParameter;
    /**
     * 处理Mqtt接受到的消息 使用BroadCast通知相关页面处理
     */
    private MessageProcessor mMessageProcessor;

    private MQTTClient() {
        mMessageProcessor = new MessageProcessor();
        mTopicSubscribed = new HashSet<>();
        mGson = new Gson();

    }

    public static MQTTClient INSTANCE() {
        if (sMQTTClient == null) {
            synchronized (MQTTClient.class) {
                if (sMQTTClient == null) {
                    sMQTTClient = new MQTTClient();
                }
            }
        }
        return sMQTTClient;
    }

    /**
     * 与服务端连接，建立通信
     * 需要在子线程执行 所以借用带有thread的MessageProcessor
     * 最终连接还是在connect方法中
     */
    public void startConnect() throws IOException {
        Log.d(TAG, "startConnect()");
        mParameter = Common.fetchParameter(LocalParameter.ETHERNET_PATH);
        mqttAndroidClient = new MqttAndroidClient(AppEntrance.getAppEntrance(), mParameter.getServerUri(), mParameter.getMac());
        mqttAndroidClient.setCallback(mCallbackExtended);
        mMessageProcessor.handleConnect(0);

    }

//    连接的最终实现
    @WorkerThread
    void connect() throws MqttException {
        Log.d(TAG, "try to connect");
        if (!mqttAndroidClient.isConnected()) {
            mqttAndroidClient.connect(getMqttConnectOptions(), null, mConnectListener);
        } else {
            Log.e(TAG, "startConnect: already connected");
        }

    }

    /**
     * 生成门口屏订阅的主题
     *只在连接成功 没有 科室id和房间id时使用
     * @param departmentId
     * @param roomId
     * @param mac
     * @return
     */

    private String getSubscriptionTopic(String departmentId, String roomId, String mac) {
        return String.format(Locale.CHINA, "/shineecall/%s/13/%s/%s/sub", departmentId, roomId, mac);
    }

    /**
     * 获取门口屏的订阅主题
     * @return
     */
    private String getSubscriptionTopic() {
        return String.format(Locale.CHINA, "/shineecall/%s/13/%s/%s/sub",
                mParameter.getDepartmentId(), mParameter.getRoomId(), mParameter.getMac());
    }

    /**
     * 生产其他设备的订阅主题 如 床头屏 后台等
     * @param device
     * @return
     */
    private String getSubscriptionTopic(String device) {
        return String.format(Locale.CHINA, device,
                mParameter.getDepartmentId(), mParameter.getRoomId());
    }

    /**
     * 生成门口屏发布的主题
     * @return
     */
    private String getpublishTopic() {
        return String.format(Locale.CHINA, "/shineecall/%s/13/%s/%s/pub",
                mParameter.getDepartmentId(), mParameter.getRoomId(), mParameter.getMac());
    }

    /**
     * 获取MQTT连接选项
     * 在此设置遗嘱信息
     *
     * @return
     */
    private MqttConnectOptions getMqttConnectOptions() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(false);
//        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setConnectionTimeout(30);
        mqttConnectOptions.setKeepAliveInterval(60);
        mqttConnectOptions.setCleanSession(true);
        mqttConnectOptions.setUserName(USER_NAME);
        mqttConnectOptions.setPassword(PASSWORD.toCharArray());
        //断开连接的主题
        String topic = String.format(Locale.CHINA, "/shineecall/%s/disconnect", mParameter.getMac());
        //断开的消息
        String payLoad = getDisconnectNotification(mParameter.getHost(), mParameter.getMac());
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
            //最终调用本类的connect（）方法
            mMessageProcessor.handleConnect(8000);
        }
    };

    /**
     * MQTT的回调
     */
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
            showDialog();
            mMessageProcessor.handleConnect(0);
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            final String receive = new String(Base64.decode(message.getPayload(), Base64.DEFAULT));
            Log.d(TAG, "from topic:" + topic + "  Incoming message: " + receive);
            mMessageProcessor.handleMsg(receive);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {

        }
    };

    /**
     * 取消呼叫转移
     */
    public void handleCancelCallTransfer() {
        String topic = String.format(Locale.CHINA, "/shineecall/%s/broadcast", mParameter.getDepartmentId());
        mMessageProcessor.handleCancelCallTransfer(topic);
    }

    /**
     * 没连上或与服务器断线的时候显示对话框
     */
    private void showDialog() {
        MainActivity.sendUpdate(INTERNET_OUT);
    }


    private void dismissDialog() {
        MainActivity.sendUpdate(INTERNET_RECOVERY);
    }


    /**
     * 订阅主题
     *
     * @param subscriptionTopic 所需要订阅的主题，字符串形式
     * @param needPublish       是否需要发布消息 只有在订阅门口屏信息成功时才为true
     */
    private void subscribeToTopic(final String subscriptionTopic, final boolean needPublish) {
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, mQos, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "subscribeToTopic:" + subscriptionTopic);
                    if (needPublish) {
                        //获取门口屏信息
                        Message message = new Message("getdoorscreeninfo", mParameter.getMac(), mParameter.getHost());
                        String getDoorScreenInfo = mGson.toJson(message);
                        String publishTopic = getpublishTopic();
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
    void publishMessage(String publishMessage, String publishTopic) {
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
    void onConnectSucceed() {
        Log.d(TAG, "Connect onSuccess");
        mqttAndroidClient.setBufferOpts(getBufferOpt());
        //连接成功后订阅主题,由于此时还不知道科室id和房间id，使用通配符+,
        // 当获取到门口屏具体信息，需要取消此主题，重新订阅,否则就重复订阅，收到重复数据
        String topicForDoorSceen = getSubscriptionTopic("+", "+", mParameter.getMac());
        //用于取消通配符订阅
        subscriptionTopic = topicForDoorSceen;
        //true表示订阅成功后发布主题
        subscribeToTopic(topicForDoorSceen, true);

        //业务服务器死亡/上线通知，如果MQTT重启，此设备先于业务服务器连接，就收不到最新数据
        //所以必须提前订阅业务服务器的上线通知，更新数据,不取消订阅
        subscribeToTopic("/shineecall/callserver/broadcast", false);
    }

    //获取门口屏信息
    private void getDoorScreenInfo() {
        Message message = new Message("getdoorscreeninfo", mParameter.getMac(), mParameter.getHost());
        String getDoorScreenInfo = mGson.toJson(message);
        String publishTopic = getpublishTopic();
        publishMessage(getDoorScreenInfo, publishTopic);
    }

    /**
     * 处理转组
     */
    void handleTranfer() throws MqttException {
        //取消之前的订阅
        unSubscribe();
        String topForDoorScreen = getSubscriptionTopic();
        //重新订阅，并发布消息
        subscribeToTopic(topForDoorScreen, true);
    }

    /**
     * 护士站，床头屏，卫生间和门口屏都是通过科室和房间号关联的
     * 当接收到门口屏科室和房间号后开始订阅护士站，床头屏，卫生间的动态
     */
    void handleSubscribe() throws MqttException {
        mqttAndroidClient.unsubscribe(subscriptionTopic);
        subscirbe();
    }

    /**
     * 订阅需要的数据：门口屏的，床头屏的等等
     * 在获取到准确的房间号，或断线重连使用
     */
   private void subscirbe() {
        //门口屏接收数据
        String topForDoorScreen = getSubscriptionTopic();
        subscribeToTopic(topForDoorScreen, false);
        mTopicSubscribed.add(topForDoorScreen);

        //床头屏发布数据 （所在病房所有床头屏）
//        String bedScreen = String.format(Locale.CHINA, "/shineecall/%s/1/%s/+/pub", mDepartmentId, mRoomId);
        String bedScreen = getSubscriptionTopic("/shineecall/%s/1/%s/+/pub");
        subscribeToTopic(bedScreen, false);
        mTopicSubscribed.add(bedScreen);

        //所在科室所有护士站
        String nurseStation = String.format(Locale.CHINA, "/shineecall/%s/2/+/broadcast", mParameter.getDepartmentId());
        subscribeToTopic(nurseStation, false);
        mTopicSubscribed.add(nurseStation);


        //卫生间发布数据 （所在病房所有卫生间）
        String washRoom = getSubscriptionTopic("/shineecall/%s/7/%s/+/pub");
//        String washRoom = String.format(Locale.CHINA, "/shineecall/%s/7/%s/+/pub", mDepartmentId, mRoomId);
        subscribeToTopic(washRoom, false);
        mTopicSubscribed.add(washRoom);

        //管理后台发布的对病房的数据
//        String platform = String.format(Locale.CHINA, "/shineecall/%s/%s/broadcast", mDepartmentId, mRoomId);
        String platform =getSubscriptionTopic("/shineecall/%s/%s/broadcast");
        subscribeToTopic(platform, false);
        mTopicSubscribed.add(platform);

        //呼叫转移
        String transfer = String.format(Locale.CHINA, "/shineecall/%s/broadcast", mParameter.getDepartmentId());
        subscribeToTopic(transfer, false);
        mTopicSubscribed.add(transfer);

        //订阅系统消息，不取消订阅
        subscribeToTopic("/shineecall/system/broadcast", false);

        //通知后台设备已连接
        Message message = new Message("connected", mParameter.getMac(), mParameter.getHost());
        String deviceConnected = mGson.toJson(message);
        String publishTopic = getpublishTopic();
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

    LocalParameter getParameter() {
        return mParameter;
    }

    private void exitMqtt() {
        //不为空，已连接
        if (mqttAndroidClient != null) {
            try {
                mqttAndroidClient.setCallback(null);
                String publishMessage = getDisconnectNotification(mParameter.getHost(), mParameter.getMac());
                String publishTopic = String.format(Locale.CHINA, "/shineecall/%s/disconnect", mParameter.getMac());
                //断开之前发布通知
                publishMessage(publishMessage, publishTopic);
                //取消之前的订阅
                unSubscribe();
//                mqttAndroidClient.disconnect();
                mqttAndroidClient.close();
                mqttAndroidClient.unregisterResources();
                mqttAndroidClient=null;
            } catch (MqttException e) {
                Log.d(TAG, "exit exception:" + e);
                e.printStackTrace();
            }
        }
    }

    /**
     * 退出程序时调用
     */
    public void exit() {
        Log.d(TAG, "exit: ");
        dismissDialog();
        exitMqtt();
        mMessageProcessor.stop();
        mMessageProcessor=null;
//        释放此单例
        sMQTTClient=null;

    }

}
