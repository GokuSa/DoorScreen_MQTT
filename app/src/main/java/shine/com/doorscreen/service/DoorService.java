package shine.com.doorscreen.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.shine.utilitylib.A64Utility;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import shine.com.doorscreen.activity.MainActivity;
import shine.com.doorscreen.database.WardDataBase;
import shine.com.doorscreen.entity.ReStart;
import shine.com.doorscreen.util.Common;
import shine.com.doorscreen.util.DateFormatManager;
import shine.com.doorscreen.util.RootCommand;

import static shine.com.doorscreen.activity.MainActivity.CALL_OFF;
import static shine.com.doorscreen.activity.MainActivity.CALL_ON;
import static shine.com.doorscreen.activity.MainActivity.CLOSE;
import static shine.com.doorscreen.activity.MainActivity.DOWNLOAD_DONE;
import static shine.com.doorscreen.activity.MainActivity.MEDIA_DELETE;
import static shine.com.doorscreen.activity.MainActivity.MEDIA_STOP;
import static shine.com.doorscreen.activity.MainActivity.REBOOT;
import static shine.com.doorscreen.activity.MainActivity.REMOVE_CLOSE;
import static shine.com.doorscreen.activity.MainActivity.RESTART;
import static shine.com.doorscreen.activity.MainActivity.SCAN_MEDIA;
import static shine.com.doorscreen.activity.MainActivity.SCREEN_SWITCH;
import static shine.com.doorscreen.activity.MainActivity.SWITCH_SET;
import static shine.com.doorscreen.activity.MainActivity.VOLUME_SET;
import static shine.com.doorscreen.activity.MainActivity.VOLUME_SWITCH;


/**
 * 使用service和ThreadHandler结合
 * 在后台子线程中处理任务
 * IntentService 没发现延时处理功能
 * 重启需要系统签名，并添加Reboot权限，在manifest根节点添加 android:sharedUserId="android.uid.system"
 * 处理不需要在UI中处理的逻辑 开关机 开关屏  重启
 */
public class DoorService extends Service implements Handler.Callback {
    private static final String TAG =DoorService.class.getSimpleName();
    public static final String ACTION = "action";
    public static final String DATA = "data";

    private Handler mHandler;
    private DateFormat mCurrentDateFormat;
    /**
     * 当前宣教视频播单id
     */
    private String mMediaIds;

    private A64Utility mA64Utility;
    private DateFormatManager mDateFormatManager;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Deprecated
    public static Intent newIntent(Context context, int action, String data) {
        Intent intent = new Intent(context, DoorService.class);
        intent.putExtra(ACTION, action);
        intent.putExtra(DATA, data);
        return intent;
    }

    public static void startService(Context context, int action, String data) {
        Intent intent = new Intent(context, DoorService.class);
        intent.putExtra(ACTION, action);
        intent.putExtra(DATA, data);
        context.startService(intent);
    }

    /**
     * 服务首次启动从本地获取开关屏及音量参数
     * 使用handlerThread处理请求
     * 设置当前音量和关屏时间
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate() called");
        mA64Utility = new A64Utility();
        //多媒体和跑马灯检索时间格式，后台的时间精确到分，我们设置格式到秒，这样检索不会有1分钟误差
        mCurrentDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:02", Locale.CHINA);
//        格式化日期类
        mDateFormatManager=new DateFormatManager();

        HandlerThread handlerThread = new HandlerThread("door_service");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper(), this);
        //当前秒数
        int current_second = Calendar.getInstance().get(Calendar.SECOND);
        Log.d(TAG, "current_second:" + current_second);
        //音量设置
        mHandler.sendEmptyMessageDelayed(VOLUME_SET, 10 * 1000);
        //开关屏设置
        mHandler.sendEmptyMessage(SWITCH_SET);
        //重启设置
        mHandler.sendEmptyMessageDelayed(RESTART, 5 * 1000);
//        mHandler.postDelayed(this::handleRestart, 5 * 1000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final int action = intent.getIntExtra(ACTION, -1);
        switch (action) {
            case SCREEN_SWITCH:
                mHandler.sendEmptyMessage(SWITCH_SET);
                break;
            case VOLUME_SWITCH:
                mHandler.sendEmptyMessage(VOLUME_SET);
                break;
            case REBOOT:
                mHandler.post(() -> new RootCommand().exeCommand("reboot"));
                break;
            case DOWNLOAD_DONE:
            case MEDIA_STOP:
            case MEDIA_DELETE:
//                updateScanMedia();
                break;
            case CLOSE:
                Log.d(TAG, "收到最新的关机操作 ");
                mHandler.postDelayed(mShunDown, 300);
                break;
            case REMOVE_CLOSE:
                mHandler.removeCallbacks(mShunDown);
                break;
            //处理系统呼叫，如果在关屏状态需要开屏
            case CALL_ON:
                mHandler.post(() -> {
                    if (!ScreenManager.getInstance().isScreenOn()) {
                        mA64Utility.OpenScreen();
                    }
                });

                break;
            //呼叫结束，如果之前是关屏状态要恢复
            case CALL_OFF:
                mHandler.post(() -> {
                    if (!ScreenManager.getInstance().isScreenOn()) {
                        mA64Utility.CloseScreen();
                    }
                });
                break;
            case RESTART:
                handleRestart();
                break;
        }

        return START_NOT_STICKY;
    }

    /**
     * 处理重启
     * 从本地数据库获取数据 安排最近的一次重启
     */
    private void handleRestart() {
        Log.d(TAG, "handleRestart() called");
        //  先移除再设置关机操作
        mHandler.removeMessages(CLOSE);
        List<ReStart> mReStartParams = WardDataBase.INSTANCE(this.getApplication()).reStartDao().getAll();
        int size=mReStartParams.size();
        Calendar calendar = Calendar.getInstance();
        //获取今天星期几,从0到6
        int day = calendar.get(Calendar.DAY_OF_WEEK) - 1;
//        遍历一周每一天找到最近的开关机设置
        for (int i = 0; i < size; i++) {
//            取模防止明天的数字比今天小 比如今天周六 是 6 明天是0
            day=(day+i)%size;
            ReStart reStart = mReStartParams.get(day);
            //如果重启
            if (reStart.getReboot() == 1 ) {
//                设置到重启的那天
                calendar.add(Calendar.DAY_OF_MONTH, i);
                String rebootTime = reStart.getRebootTime();
                Date date = mDateFormatManager.parseTime(rebootTime);
                if (date != null) {
                    calendar.set(Calendar.HOUR_OF_DAY,date.getHours());
                    calendar.set(Calendar.MINUTE,date.getMinutes());
                    calendar.set(Calendar.SECOND,0);
                    String dateTime = mDateFormatManager.formatDateTime(calendar);
                    Log.d(TAG, "dateTime"+dateTime);
//                    防止刚好是今天并且时间过期，比如刚重启
                    if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                        continue;
                    }
                    //设置开机时间
                    Common.open(calendar.getTimeInMillis() + 60 * 1000);
                    mHandler.sendEmptyMessageDelayed(CLOSE, calendar.getTimeInMillis()-System.currentTimeMillis());
                }else{
                    Log.e(TAG, "handleRestart: 解析时间异常");
                }
                break;
            }
        }
    }

    //多媒体有更新后，先移除先前的message，重置mMediaIds,再开始扫描
    private void updateScanMedia() {
        mHandler.removeMessages(SCAN_MEDIA);
        mMediaIds = "";
        mHandler.sendEmptyMessage(SCAN_MEDIA);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case SWITCH_SET:
                executeScreenOnOff();
                break;
            case VOLUME_SET:
                executeVolumeSet();
                break;
            //每一分钟从数据库检索一次合适的多媒体素材
            case SCAN_MEDIA:
//                scanMedia();
//                mHandler.sendEmptyMessageDelayed(SCAN_MEDIA, SCAN_MEDIA_INTERVAL);
                break;

        }
        return true;
    }


    /**
     * 从数据库检索符合当前时间段的多媒体播单，若有变更发送给主页面处理
     */
    @Deprecated
    private void scanMedia() {/*
        String[] current = mCurrentDateFormat.format(System.currentTimeMillis()).split(" ");
//        Log.d(TAG, current[0] + "--" + current[1]);
        //使用格式化的当前日期和时间查询数据库符合条件的多媒体
        String ids = DoorScreenDataBase.getInstance(this).queryMediaIds(current[0], current[1]);
        Intent intent;
        //如果检索的播单为空，说明这个时间点没有多媒体播放，结束多媒体界面
        if (TextUtils.isEmpty(ids)) {
            intent = MainActivity.newIntent(MainActivity.SCAN_MEDIA, "");
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
//            sendBroadcastAsUser(intent,android.os.Process.myUserHandle());
        } else if (!ids.equals(mMediaIds)) {
            //如果为空删除无效的播单时间
            if (DoorScreenDataBase.getInstance(this).queryMedia(ids).size() == 0) {
                Log.d(TAG, "invalid media time");
                intent = MainActivity.newIntent(MainActivity.SCAN_MEDIA, "");
//                sendBroadcastAsUser(intent,android.os.Process.myUserHandle());
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                DoorScreenDataBase.getInstance(this).deleteInValidMediaTime(ids);
            } else {
                //如果播单有所更新，并且内容不为空通知界面调整
                mMediaIds = ids;
                intent = MainActivity.newIntent(MainActivity.SCAN_MEDIA, ids);
//                sendBroadcastAsUser(intent,android.os.Process.myUserHandle());
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            }
        }*/
    }


    /**
     * 执行开关屏
     */
    private void executeScreenOnOff() {
        ScreenManager screenManager = ScreenManager.getInstance();
        screenManager.scheduleScreenOnOff();
        if (screenManager.isScreenOn()) {
            mA64Utility.OpenScreen();
        } else {
            mA64Utility.CloseScreen();
        }
        //根据计算结果安排下次开关屏
        mHandler.removeMessages(MainActivity.SWITCH_SET);
        mHandler.sendEmptyMessageDelayed(MainActivity.SWITCH_SET, screenManager.getOperationDelay());
    }

    /**
     * 执行音量设置
     */
    private void executeVolumeSet() {
        VolumeManager volumeManager = VolumeManager.getInstance();
        volumeManager.scheduleVolume();
        mHandler.removeMessages(VOLUME_SET);
        mHandler.sendEmptyMessageDelayed(VOLUME_SET, volumeManager.getOperationDelay());
    }

    private Runnable mShunDown = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "执行关机");
            Intent intent = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
            intent.putExtra("android.intent.extra.KEY_CONFIRM", false);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            stopSelf();
            System.exit(0);
        }
    };

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy() called");
        mHandler.getLooper().quit();
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    /**
     * 设置开关屏时间,在开屏时间类开屏，否则就关屏
     */

   /* private void onOffScreen() {
        Calendar calendar = Calendar.getInstance();
        long current = calendar.getTimeInMillis();
        Log.d(TAG, "设置关屏 当前时间为：" + mDateFormat.format(current));
        calendar.set(Calendar.HOUR_OF_DAY, mCloseScreenHour);
        calendar.set(Calendar.MINUTE, mCloseScreenMinute);
        calendar.set(Calendar.SECOND, 0);
        //当天关屏时间起点
        long closeScreenTime = calendar.getTimeInMillis();
        Log.d(TAG, "设置当天关屏时间为：" + mDateFormat.format(closeScreenTime));
        calendar.set(Calendar.HOUR_OF_DAY, mOpenScreenHour);
        calendar.set(Calendar.MINUTE, mOpenScreenMinute);
        //当天开屏时间
        long openScreenTime = calendar.getTimeInMillis();
        Log.d(TAG, "设置当天开屏时间为：" + mDateFormat.format(openScreenTime));

        //如果还没到开屏时间，关屏并发送消息到点开屏
        if (current < openScreenTime) {
            Log.d(TAG, "小于开屏时间，关屏");
            mA64Utility.CloseScreen();
            Log.d(TAG, "(openScreenTime-current):" + (openScreenTime - current));
            mHandler.removeMessages(MainActivity.SWITCH_SET);
            mHandler.sendEmptyMessageDelayed(MainActivity.SWITCH_SET, openScreenTime - current);

        } else if (current >= openScreenTime && current < closeScreenTime) {
            //如果在开屏时间里
            Log.d(TAG, "在开屏时间，开屏");
            mA64Utility.OpenScreen();
            //发送关屏信息
            Log.d(TAG, (closeScreenTime - current) + " 后关屏");

            mHandler.removeMessages(MainActivity.SWITCH_SET);
            mHandler.sendEmptyMessageDelayed(MainActivity.SWITCH_SET, closeScreenTime - current);
        } else {
            Log.d(TAG, "大于开屏时间，关屏");
            mA64Utility.CloseScreen();
            //设置明天开屏时间，
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            Log.d(TAG, "延迟到明天开屏:" + (calendar.getTimeInMillis() - current));
            mHandler.removeMessages(MainActivity.SWITCH_SET);
            mHandler.sendEmptyMessageDelayed(MainActivity.SWITCH_SET, calendar.getTimeInMillis() - current);
        }
    }*/

    /**
     * 保存系统亮度
     * 目前仅开关屏时间
     * 使用集合中的最后一个数据即可
     */

   /* private void saveSystemLight(String json) {
        Log.d(TAG, "saveSystemLight");
        SystemLight lightParam = new Gson().fromJson(json, SystemLight.class);
        String[] start = lightParam.getStart().split(":");
        if (start.length > 1) {
            mOpenScreenHour = Integer.parseInt(start[0]);
            mOpenScreenMinute = Integer.parseInt(start[1]);
        }
        Log.d(TAG, "mOpenScreenHour:" + mOpenScreenHour);
        String[] end = lightParam.getStop().split(":");
        if (end.length > 1) {
            mCloseScreenHour = Integer.parseInt(end[0]);
            mCloseScreenMinute = Integer.parseInt(end[1]);
        }
        Log.d(TAG, "mCloseScreenHour:" + mCloseScreenHour);
        SharePreferenceUtil.saveDisplayTime(this, mOpenScreenHour, mOpenScreenMinute, mCloseScreenHour, mCloseScreenMinute);
        //取消之前的设置
        mHandler.removeMessages(SWITCH_SET);
//        onOffScreen();
    }*/

    /**
     * 保存音量设置
     *
     * @param json 后台发来的数据，数据比较冗余，只取关键数据
     *             SystemVolume和SystemLight数据一样
     */
    /*private void saveSystemVolume(String json) {
        try {
            SystemInfo systemVolume = new Gson().fromJson(json, SystemInfo.class);
            if (systemVolume != null) {
                List<SystemLight> list = systemVolume.getDatalist();
                if (list != null && list.size() > 1) {
                    //这个只用来获取晚上音量
                    SystemLight volumeNight = list.get(0);
                    //最后一个数据有白天，晚上的分割点，及音量
                    SystemLight volumeDay = list.get(list.size() - 1);
                    mVolumeNight = volumeNight.getValue();
                    mVolumeDay = volumeDay.getValue();
                    Log.d(TAG, "volumeNightValue:" + mVolumeNight);
                    Log.d(TAG, "volumeDayValue:" + mVolumeDay);

                    String[] start = volumeDay.getStart().split(":");
                    if (start.length > 1) {
                        mVolumeDayHour = Integer.parseInt(start[0]);
                        mVolumeDayMinute = Integer.parseInt(start[1]);
                        Log.d(TAG, "volumeDayPoint:" + mVolumeDayHour + "-" + mVolumeDayMinute);
                    }
                    String[] end = volumeDay.getStop().split(":");
                    if (end.length > 1) {
                        mVolumeNightHour = Integer.parseInt(end[0]);
                        mVolumeNightMinute = Integer.parseInt(end[1]);
                        Log.d(TAG, "volumeNightPoint:" + mVolumeNightHour + "--" + mVolumeNightMinute);
                    }
                    SharePreferenceUtil.saveVolumeParam(this, mVolumeDay, mVolumeNight, mVolumeDayHour, mVolumeDayMinute,
                            mVolumeNightHour, mVolumeNightMinute);
                    //取消之前的消息，重新设置
                    mHandler.removeMessages(VOLUME_SET);
                    setCurrentVolume();
                }
            }

        } catch (JsonSyntaxException e) {
            Log.e(TAG, "fail to parse json:" + e.toString());
        }
    }*/

    /**
     * 设置当前音量,根据当前时间设置对应的音量，并设置时间点启动下一次设置
     */
    /*private void setCurrentVolume() {
        Calendar calendar = Calendar.getInstance();
        long current = calendar.getTimeInMillis();
        Log.d(TAG, "设置音量 当前时间为：" + mDateFormat.format(current));

        calendar.set(Calendar.HOUR_OF_DAY, mVolumeNightHour);
        calendar.set(Calendar.MINUTE, mVolumeNightMinute);
        calendar.set(Calendar.SECOND, 0);
        long volumeNight = calendar.getTimeInMillis();
        Log.d(TAG, "设置夜间音量的起始时间为：" + mDateFormat.format(volumeNight));

        calendar.set(Calendar.HOUR_OF_DAY, mVolumeDayHour);
        calendar.set(Calendar.MINUTE, mVolumeDayMinute);
        long volumeDay = calendar.getTimeInMillis();
        Log.d(TAG, "设置白天音量的起始时间为：" + mDateFormat.format(volumeDay));

        if (volumeDay >= volumeNight) {
            Toast.makeText(this, "白天起始点不应该大于夜晚起始点", Toast.LENGTH_SHORT).show();
            return;
        }

        int volume = 0;
        if (current < volumeDay) {
            volume = mVolumeNight;
            mHandler.sendEmptyMessageAtTime(VOLUME_SET, volumeDay);
        } else if (current >= volumeDay && current < volumeNight) {
            volume = mVolumeDay;
            mHandler.sendEmptyMessageAtTime(VOLUME_SET, volumeNight);
        } else {
            volume = mVolumeNight;
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            Log.d(TAG, "设置明天白天音量时间" + mDateFormat.format(calendar.getTimeInMillis()));
            mHandler.sendEmptyMessageAtTime(VOLUME_SET, calendar.getTimeInMillis());
        }
        Log.d(TAG, "设置当前音量:" + volume);
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//        Log.d(TAG, "max volume:" + max);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                max * volume / 100, AudioManager.FLAG_SHOW_UI);
//        int streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//        Log.d(TAG, "streamVolume:" + streamVolume);
    }*/
}

