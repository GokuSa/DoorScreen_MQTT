package shine.com.doorscreen.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import shine.com.doorscreen.R;
import shine.com.doorscreen.adapter.DoorInfoPagerAdapter;
import shine.com.doorscreen.customview.CustomViewPager;
import shine.com.doorscreen.fragment.CallTransferDialog;
import shine.com.doorscreen.fragment.DoorFragment;
import shine.com.doorscreen.fragment.MediaFragment;
import shine.com.doorscreen.fragment.WaitingDialog;
import shine.com.doorscreen.mqtt.MQTTClient;
import shine.com.doorscreen.mqtt.bean.Infusion;
import shine.com.doorscreen.service.DoorService;
import shine.com.doorscreen.util.Common;

/**
 * 主页面,承载门口屏和视频宣教页面，
 * 1. 启动后加载这两个页面，并显示门口屏DoorFragment，为了和后台同步，在重启等情况下其从本地数据库查询输液，医生，患者，标题信息完成初始化
 * 2.添加服务端数据监听，接受如输液信息，跑马灯信息，开关屏设置等，这些信息都会保存在本地
 * 3.添加广播用于通信，如输液结束，播放视频，显示呼叫，可用广播通知相关页面更新
 * 4.启动后台服务，10 s后（即10秒延时初始化，防止占资源）使用本地保存的信息设置开关屏和音量，
 * 12 s后启动宣教信息和跑马灯信息定时（60s）扫描一次，检索符合当前时间播放的多媒体和跑马灯id
 * 通过广播发给主页面处理，多媒体处理逻辑如下：
 * a.如果需要播发宣教视频
 * 当前正在呼叫，只通知多媒体页面（MediaFragment）更新数据，并标记播放状态，但不切换到此页面，当呼叫结束才会切换到此页面
 * 不在呼叫，切换到多媒体播放页面并更新数据
 * 正在播放，后台有更新，则更新数据，否则不处理
 * b.如果不需要播放视频
 * 当前在播放，停止并切换到门口屏页面
 * 不在播放，不做处理
 * 跑马灯播放逻辑类似：
 * 如果在门口屏页面：更新并且播放，不在门口屏页面，只更新不播放，无更新 不处理
 * <p>
 * 5 .停止或删除宣教或跑马灯id，都会先更新数据库内容然后通知后台服务重新定时扫描
 * <p>
 * 暂时通过broadcast通知相关页面更新，以后可能使用content provider和loader
 * 使用DoorFragment 展示住院相关信息为主页面，有输液信息优先展示，没有显示医生信息
 * 使用MediaFragment 播放视频，收到后台插播的宣教切换到此页面，在播放过程中如果有呼叫切回输液界面
 * <p>
 * 使用MQTT代替Netty
 * <p>
 * 此页面是MQTT通信与具体业务逻辑的中间桥梁
 */
public class MainActivity extends AppCompatActivity implements  MQTTClient.MqttListener {
    private static final String TAG = "MainActivity";
    //宣教信息下載廣播action
    public static final String MEDIA_ACTION = "com.action.media";
    public static final int CONNECT_SERVER = 0;
    public static final int MARQUEE_UPDATE = 1;
    public static final int MARQUEE_STOP = 2;
    public static final int MARQUEE_DELETE = 3;
    public static final int DOOR_TITLE_UPDATE = 4;
    public static final int MEDIA_DOWNLOAD = 5;
    public static final int MEDIA_STOP = 6;
    public static final int MEDIA_DELETE = 7;
    public static final int DRIP_UPDATE = 8;
    public static final int DOCTOR_INFO = 9;
    public static final int NURSOR_INFO = 44;
    public static final int CALL_INFO = 10;
    public static final int PATIENT_INFO = 11;
    public static final int SCREEN_SWITCH = 12;
    public static final int VOLUME_SWITCH = 13;
    public static final int REBOOT = 14;
    public static final int VOLUME_SET = 20;
    public static final int SWITCH_SET = 23;
    public static final int DOWNLOAD_DONE = 40;
    public static final int DRIP_DONE = 42;
    public static final int SCAN_MEDIA = 45;
    public static final int SCAN_MEDIA_INTERVAL = 60 * 1000;
    public static final int SCAN_MARQUEE = 46;
    public static final int SCAN_MARQUEE_INTERVAL = 60 * 1000;
    public static final int STOP_DRIP = 51;
    public static final int STOP_ALL_DRIP = 52;
    public static final int CHECK_TIME = 53;
    //显示门灯
    public static final int SHOW_DOOR_LIGHT = 54;
    public static final int PUSHPOSITION = 55;
    public static final int CLOSE = 55;
    public static final int REMOVE_CLOSE = 56;
    public static final int CALL_ON = 57;
    public static final int CALL_OFF = 58;
    public static final int RESTART = 59;
    //视频和图片目录
    private File mFileMovies = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
    private File mFilePicture = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

    /**
     * 门口屏信息主要界面
     * 显示医生、输液、患者、呼叫和跑马灯信息
     */
    private DoorFragment mDoorFragment;
    /**
     * 多媒体播放界面
     * 用于插播宣教视频
     */
    private MediaFragment mMediaFragment;
    /**
     * 使用自定义ViewPager管理门口屏页面和宣教视频页面
     */
    private CustomViewPager mViewPager;


    /**
     * 宣教信息是否在播，此时有人呼叫换到门口屏，呼叫结束返回播放页面
     */
    private boolean isMediaPlaying = false;
    private boolean isCalling = false;
    private MQTTClient mMQTTClient;

    //是否成功连接过
    private boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        //创建视频和图片下载目录
        setLocalStorage();
        mMQTTClient = new MQTTClient(this);
//        网络连接状态变化广播监听
        if (Common.isNetworkAvailable(this)) {
            isConnected = true;
            mMQTTClient.startConnect(this);
        } else {
            showOutOfInternetDialog();
        }

        //注册广播
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(MEDIA_ACTION));
        registerReceiver(mNetWorkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        //开启后台服务设置系统音量和开关屏
        DoorService.startService(this, -1, "");
    }

    /**
     * 显示网络断开连接对话框
     */
    private void showOutOfInternetDialog() {
        WaitingDialog dialog = (WaitingDialog) getSupportFragmentManager().findFragmentByTag("dialog_out_of_internet");
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (dialog != null) {
            fragmentTransaction.remove(dialog);
        }
        dialog = WaitingDialog.newInstance("");
        dialog.show(fragmentTransaction, "dialog_out_of_internet");
    }

    /**
     * 关闭网络端口提示
     */
    private void dismissOutOfInternetDialog() {
        WaitingDialog dialog = (WaitingDialog) getSupportFragmentManager().findFragmentByTag("dialog_out_of_internet");
        if (dialog != null) {
            dialog.dismiss();
        }
    }


    /**
     * 初始化主要页面，使用Viewpager 管理fragment
     * DoorFragment 显示输液 和 医生信息
     * VideoFragment 播放宣教视频
     */
    private void initView() {
        mViewPager = findViewById(R.id.viewpager);
        mMediaFragment = new MediaFragment();
        mDoorFragment = new DoorFragment();
        List<Fragment> fragmentList = new ArrayList<>();
        //不要改变添加顺序
        fragmentList.add(mDoorFragment);
        fragmentList.add(mMediaFragment);
        DoorInfoPagerAdapter doorInfoPagerAdapter = new DoorInfoPagerAdapter(getSupportFragmentManager(), fragmentList);
        mViewPager.setAdapter(doorInfoPagerAdapter);

    }

    private void setLocalStorage() {
        if (!mFileMovies.exists()) {
            if (!mFileMovies.mkdirs()) {
                Log.e(TAG, "fail to create dir movies");
            }
        }
        if (!mFilePicture.exists()) {
            if (!mFilePicture.mkdirs()) {
                Log.e(TAG, "fail to create dir picture");
            }
        }
    }

    /**
     * 更新本地宣教信息
     * 根据宣教播放时间段不同存储不同字段
     * 存储的时间是决定播放的关键
     */

    /*public void updateLocalMedia(@NonNull PushMission pushMission) {

        //插入播发时间段
//        DoorScreenDataBase.getInstance(this).insertMediaTime(pushMission);

        //数据结构为以前多媒体结构，很多无用数据，
        List<PushMission.Templates> elementlist = pushMission.getTemplates();
        Log.d(TAG, "elementlist.size():" + elementlist.size());
        if (elementlist.size() < 1) {
            return;
        }
        List<PushMission.Templates.Regions> regions = elementlist.get(0).getRegions();
        Log.d(TAG, "regions.size():" + regions.size());
        if (regions.size() < 1) {
            return;
        }
        //这个就是最新宣教素材
        ArrayList<Elements> elements = regions.get(0).getElements();
        if (elements == null || elements.size() < 1) {
            return;
        }
        Log.d(TAG, "elementsToUpdate:" + elements);
        //本地关于服务器下载的配置文件
        IniReaderNoSection inir = new IniReaderNoSection(AppEntrance.ETHERNET_PATH);
        //需要下载文件的路径前缀，包括ftp地址，端口
        String mHeader = String.format("ftp://%s:%s", inir.getValue("ftpip"), inir.getValue("ftpport"));

        //设置下载元素内容
        for (Elements element : elements) {
            //设置下载的完整路径
            element.setSrc(String.format("%s%s", mHeader, element.getSrc()));
            //设置播单id
            element.setId(pushMission.getId());

            //分别设置图片和视频的下载路径
            if (element.getType() == 1) {
                element.setPath(String.format("%s/%s", mFileMovies.getAbsolutePath(), element.getName()));
            } else if (element.getType() == 2) {
                element.setPath(String.format("%s/%s", mFilePicture.getAbsolutePath(), element.getName()));
            }
        }

        if (elements.size() > 0) {
            //启动后台服务下载
            Intent intent = new Intent(this, DownLoadService.class);
            intent.putExtra("elements", elements);
            intent.putExtra("id", pushMission.getId());
            startService(intent);
        }

    }*/

    /**
     * 网络状态监测
     */
    private BroadcastReceiver mNetWorkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //只联一次，startConnect不能重复启动
            if (Common.isNetworkAvailable(MainActivity.this) && !isConnected) {
                isConnected = true;
                mMQTTClient.startConnect(MainActivity.this);
            }
        }
    };

    //宣教信息的接受監聽
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //更新完成通知
            int action = intent.getIntExtra("flag", -1);
//            Log.d(TAG, "mReceiver " + action);
            switch (action) {
                //后台每分钟检索一次多媒体信息
                case SCAN_MEDIA:
                    String param = intent.getStringExtra("param");
                    if (TextUtils.isEmpty(param)) {
                        //非宣教视频阶段，切换到门口屏
                        if (isMediaPlaying) {
                            isMediaPlaying = false;
                            Log.d(TAG, "switch from media to door framgent ");
                            mViewPager.setCurrentItem(0);
                        }

                    } else {
                        isMediaPlaying = true;
                        if (!isCalling) {
                            Log.d(TAG, "is not calling ,switch to media fragment");
                            mViewPager.setCurrentItem(1);
                        } else {
                            Log.d(TAG, "is  calling ,just to update media fragment");
                        }
                        mMediaFragment.updateMedia(param);
                    }
                    break;

            }
        }
    };

    public static Intent newIntent(int flag, String param) {
        Intent intent = new Intent(MEDIA_ACTION);
        intent.putExtra("flag", flag);
        intent.putExtra("param", param);
        return intent;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //退出时确保移除所有监听
        mMQTTClient.exit();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        unregisterReceiver(mNetWorkReceiver);
        stopService(DoorService.newIntent(this, -1, ""));
    }

    /**
     * 从门口屏取消呼叫转移
     * 关闭对话框，调用Mqtt发送取消命令
     */
    public void cancelCallTransfer() {
        if (mMQTTClient != null) {
            mMQTTClient.handleCancelCallTransfer();
        }
    }

    @Override
    public void onSynSucceed() {
        mDoorFragment.synLocalTime();
    }

    //收到最新的重启参数
    @Override
    public void updateReStart() {
        mDoorFragment.updateReStart();
    }

    @Override
    public void handleOutOfInternet() {
        //断网时先关闭呼叫转移对话框如果存在的话
        handleCallTransfer("",false);
        showOutOfInternetDialog();
    }

    @Override
    public void handleInternetRecovery() {
        dismissOutOfInternetDialog();
    }

    /**
     * 处理呼叫转移对话框
     * 先移除之前的对话框，显示最新的
     * 如果show=false,就是关闭
     *
     * @param tip
     * @param show
     */
    @Override
    public void handleCallTransfer(String tip, boolean show) {
        CallTransferDialog fragment = (CallTransferDialog) getSupportFragmentManager().findFragmentByTag("call_transfer");
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        }
        if (show) {
            fragment = CallTransferDialog.newInstance(tip);
            fragment.show(getSupportFragmentManager(), "call_transfer");
        }
    }


    @Override
    public void onMarqueeUpdate() {
        mDoorFragment.updateMarquee();
    }

    @Override
    public void handleInfusion(Infusion infusion,int type) {
        mDoorFragment.handleInfusion(infusion,type);
    }
}
