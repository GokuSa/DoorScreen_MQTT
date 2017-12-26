package shine.com.doorscreen.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import shine.com.doorscreen.activity.MainActivity;
import shine.com.doorscreen.app.AppEntrance;
import shine.com.doorscreen.entity.SystemInfo;
import shine.com.doorscreen.entity.SystemLight;

import static java.lang.Integer.parseInt;

/**
 * author:
 * 时间:2017/9/19
 * qq:1220289215
 * 类描述：多媒体音量管理  只需要一个实体 设计成单例模式
 * 在不同的时间段自动切换不同的音量
 */

public class VolumeManager {
    private static final String TAG = "VolumeManager";
    private volatile static VolumeManager sVolumeManager;
    private long mOperationDelay=0;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

  /*  private VolumeManager(Context context) {
        if (context == null) {
            throw new NullPointerException("context can not be null");
        }
        mContext = context.getApplicationContext();
    }*/
    private VolumeManager(){}

    public  static VolumeManager getInstance() {
        if (sVolumeManager == null) {
            synchronized (VolumeManager.class) {
                if (sVolumeManager == null) {
                    sVolumeManager = new VolumeManager();
                }
            }
        }
        return sVolumeManager;
    }

    /**
     * 音量调节同开关屏 不同时间段音量不同
     * 获取白天和晚上的节点
     */
    public void scheduleVolume() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AppEntrance.getAppEntrance());
        //白天音量
        int dayVolume = preferences.getInt("volumeDay", 30);
        //晚上音量
        int nightVolume = preferences.getInt("volumeNight", 20);
        int volumeDayStartHour = preferences.getInt("volumeDayStartHour", 8);
        int volumeDayStartMin = preferences.getInt("volumeDayStartMin", 30);
        int volumeDayEndHour = preferences.getInt("volumeDayEndHour", 19);
        int volumeDayEndMin = preferences.getInt("volumeDayEndMin", 30);
        Calendar calendar = Calendar.getInstance();
        long current = calendar.getTimeInMillis();
        Log.d(TAG, "设置音量 当前时间为：" + mDateFormat.format(current));

        calendar.set(Calendar.HOUR_OF_DAY, volumeDayEndHour);
        calendar.set(Calendar.MINUTE, volumeDayEndMin);
        calendar.set(Calendar.SECOND, 0);
        //白天进入晚上的时间节点
        long volumeNight = calendar.getTimeInMillis();
        Log.d(TAG, "设置夜间音量的起始时间为：" + mDateFormat.format(volumeNight));

        //凌晨进入白天的时间节点
        calendar.set(Calendar.HOUR_OF_DAY, volumeDayStartHour);
        calendar.set(Calendar.MINUTE, volumeDayStartMin);
        long volumeDay = calendar.getTimeInMillis();
        Log.d(TAG, "设置白天音量的起始时间为：" + mDateFormat.format(volumeDay));

        if (volumeDay >= volumeNight) {
            Log.e(TAG, "白天起始点不应该大于夜晚起始点");
            return;
        }

        int volume = 0;
        //如果还没到白天音量节点
        if (current < volumeDay) {
            volume = nightVolume;
            mOperationDelay=volumeDay-current;
        } else if (current >= volumeDay && current < volumeNight) {
            volume = dayVolume;
            mOperationDelay=volumeNight-current;
        } else {
            volume = nightVolume;
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            mOperationDelay = calendar.getTimeInMillis() - current;
            Log.d(TAG, "设置明天白天音量时间" + mDateFormat.format(calendar.getTimeInMillis()));
        }
        Log.d(TAG, "设置当前音量:" + volume);
        AudioManager audioManager = (AudioManager) AppEntrance.getAppEntrance().getSystemService(Context.AUDIO_SERVICE);
        int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,max * volume / 100, AudioManager.FLAG_SHOW_UI);
    }

    public long getOperationDelay() {
        return mOperationDelay;
    }

    public  void saveVolumeParam(int volumeDay, int volumeNight, int volumeDayStartHour,
                                 int volumeDayStartMin, int volumeDayEndHour, int volumeDayEndMin) {
        PreferenceManager.getDefaultSharedPreferences(AppEntrance.getAppEntrance()).edit().
                putInt("volumeDay",volumeDay).
                putInt("volumeNight",volumeNight).
                putInt("volumeDayStartHour",volumeDayStartHour).
                putInt("volumeDayStartMin",volumeDayStartMin).
                putInt("volumeDayEndHour",volumeDayEndHour).
                putInt("volumeDayEndMin",volumeDayEndMin).
                apply();
    }

    /**
     * 处理音量设置
     *
     */
    public void handleVolumeSet(SystemInfo systemVolume) {
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
                    saveVolumeParam(mVolumeDay, mVolumeNight, mVolumeDayHour, mVolumeDayMinute,
                            mVolumeNightHour, mVolumeNightMinute);
                    DoorService.startService(AppEntrance.getAppEntrance(), MainActivity.VOLUME_SWITCH, "");

                }
            }
    }

}
