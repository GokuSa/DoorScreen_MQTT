package shine.com.doorscreen.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * author:
 * 时间:2017/9/19
 * qq:1220289215
 * 类描述：开关屏管理类，应该设计成单例
 * 要求：在开屏时间类开屏，否则就关屏
 */

public class ScreenManager {
    private static final String TAG = "ScreenManager";
    private static final boolean SCREEN_ON = true;
    private static final boolean SCREEN_OFF = false;

    /**
     * 格式化开关屏时间 发给底层的字符串指令就是这种格式
     */
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    private Context mContext;
    private static ScreenManager sScreenManager;
    /**
     * 当前屏幕状态，默认开启
     */
    private boolean mScreenStatus = SCREEN_ON;
    private long mOperationDelay = 0;


    private ScreenManager(Context context) {
        if (context == null) {
            throw new NullPointerException("context can not be null");
        }
        mContext = context.getApplicationContext();
    }

    public synchronized static ScreenManager getInstance(Context context) {
        if (sScreenManager == null) {
            sScreenManager = new ScreenManager(context);
        }
        return sScreenManager;
    }



    /**
     * 保存开关屏时间点
     * light_value  屏的亮度  0--表示关屏  1--表示开屏
     * start_hour start_min 开屏或关屏的起始时间，light_value决定
     * end_hour  end_min 开屏或关屏的结束时间，light_value决定
     */
    public void saveScreenOnOffParams(int start_hour, int start_min, int end_hour, int end_min, int light_value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        preferences.edit().
                putInt("start_hour", start_hour).
                putInt("start_min", start_min).
                putInt("end_hour", end_hour).
                putInt("end_min", end_min).
                putInt("light_value", light_value).
                apply();
    }

    /**
     * 计算开关机时间
     * light_value>0 表示开屏  发来的时间节点为开屏的起始和结束点
     * light_value=0 表示关屏  发来的时间节点为关屏的起始和结束点
     */
    public void scheduleScreenOnOff() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        int startHour = preferences.getInt("start_hour", 0);
        int startMin = preferences.getInt("start_min", 1);
        int endHour = preferences.getInt("end_hour", 23);
        int endMin = preferences.getInt("end_min", 59);
        int light_value = preferences.getInt("light_value", 1);
        Log.d(TAG, "ligth_value:" + light_value);
        Calendar calendar = Calendar.getInstance();
        long current = calendar.getTimeInMillis();
        Log.d(TAG, "当前时间为：" + mDateFormat.format(current));
        //设置结束时间点
        calendar.set(Calendar.HOUR_OF_DAY, endHour);
        calendar.set(Calendar.MINUTE, endMin);
        calendar.set(Calendar.SECOND, 0);
        //计算结束的时间节点,light value决定是开屏结束还是关屏结束
        long endTime = calendar.getTimeInMillis();
        Log.d(TAG, "当天状态结束时间节点为：" + mDateFormat.format(endTime));
        //设置开始时间点，先设置结束时间点，后设置开始时间点，是为了方便到明天的状态计算
        calendar.set(Calendar.HOUR_OF_DAY, startHour);
        calendar.set(Calendar.MINUTE, startMin);
        //获取开始的时间节点,light value决定是 开始开屏还是 开始关屏
        long startTime = calendar.getTimeInMillis();
        Log.d(TAG, "当天状态开始时间节点为：" + mDateFormat.format(startTime));
        //如果还没到开始几点
        if (current < startTime) {
            //light vaule>0，startTime是开屏起始，没到就是关屏状态，反过来就是开屏状态
            mScreenStatus = light_value>0?SCREEN_OFF:SCREEN_ON;
            mOperationDelay = startTime - current;
            Log.d(TAG, "下个状态执行延迟" + mOperationDelay);
        } else if (current >= startTime && current < endTime) {
            //如果在时间里,就是light vaule的状态
            mScreenStatus = light_value>0?SCREEN_ON:SCREEN_OFF;
            mOperationDelay = endTime - current;
            Log.d(TAG, "下个状态执行延迟" + mOperationDelay);
        } else {
//            mScreenStatus = SCREEN_OFF;
            //过了结束时间点，状态翻转
            mScreenStatus = light_value>0?SCREEN_OFF:SCREEN_ON;
            //设置明天状态，前提是后设置开始时间点
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            mOperationDelay = calendar.getTimeInMillis() - current;
            Log.d(TAG, "超过结束时间，下个状态执行明天延迟:" + mOperationDelay);
        }

    }

    public long getOperationDelay() {
        return mOperationDelay;
    }

    public boolean isScreenOn() {
        return mScreenStatus;
    }
}
