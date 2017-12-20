package shine.com.doorscreen.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

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
import shine.com.doorscreen.service.DoorService;

/**
 * 主页面,承载门口屏和视频宣教页面，
 * 1. 启动后加载这两个页面，并显示门口屏DoorFragment，为了和后台同步，在重启等情况下其从本地数据库查询输液，医生，患者，标题信息完成初始化
 * 2.添加服务端数据监听，接受如输液信息，跑马灯信息，开关屏设置等，这些信息都会保存在本地
 * 3.添加广播用于通信，如输液结束，播放视频，显示呼叫，可用广播通知相关页面更新
 * 4.启动后台服务，10 s后（即10秒延时初始化，防止占资源）使用本地保存的信息设置开关屏和音量，
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
 * <p>
 * 暂时通过broadcast通知相关页面更新，以后可能使用content provider和loader
 * 使用DoorFragment 展示住院相关信息为主页面，有输液信息优先展示，没有显示医生信息
 * 使用MediaFragment 播放视频，收到后台插播的宣教切换到此页面，在播放过程中如果有呼叫切回输液界面
 * <p>
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        MQTTClient.INSTANCE().startConnect();
        MQTTClient.INSTANCE().setMqttListner(mMqttListener);
        //开启后台服务设置系统音量和开关屏
        DoorService.startService(this, -1, "");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //退出时确保移除所有监听
        MQTTClient.INSTANCE().exit();
        stopService(DoorService.newIntent(this, -1, ""));
//        todo 检查内存泄露
        System.exit(0);
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
        mViewPager.setPagingEnabled(false);
    }


    private MqttListener mMqttListener = new MqttListener() {
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
            //切换页面
            requestShowDoorFragment(show);
        }

        @Override
        public void handleOutOfInternet() {
            //断网时先关闭呼叫转移对话框如果存在的话
            handleCallTransfer("", false);
            showOutOfInternetDialog();
        }

        @Override
        public void handleInternetRecovery() {
            dismissOutOfInternetDialog();
        }
    };

    /**
     * 视频宣教页面请求播放多媒体
     */
    public void requestPlayMedia(boolean play) {
        Log.d(TAG, "requestPlayMedia: ");
        isMediaPlaying = play;
        if (isMediaPlaying&&!isCalling) {
            Log.d(TAG, "is not calling ,switch to media fragment");
            mViewPager.setCurrentItem(1, true);
        } else {
            mViewPager.setCurrentItem(0, true);
            Log.d(TAG, "exit media fragment");
        }

    }


//    信息页面接到呼叫请求或取消
    public void requestShowDoorFragment(boolean call) {
        isCalling=call;
        //正在呼叫
        if (isCalling) {
            mViewPager.setCurrentItem(0, true);
        }else if(isMediaPlaying){
            //呼叫结束,如果多媒体是播放状态
            mViewPager.setCurrentItem(1, true);
        }
    }

    public interface MqttListener {
        //处理呼叫转移
        void handleCallTransfer(String tip, boolean show);

        void handleOutOfInternet();

        void handleInternetRecovery();

    }


}
