package shine.com.doorscreen.customview;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import shine.com.doorscreen.R;

/**
 * author:
 * 时间:2017/12/1
 * qq:1220289215
 * 类描述：组合自定义控件 显示日期
 * 旋转是否会泄露
 * 格式化当前时间为 年 月 日  星期几，比如2016年8月10日 星期三
 */

public class DateTimeView extends RelativeLayout {
    private static final String TAG = DateTimeView.class.getSimpleName();
    DateFormat mDateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.CHINA);
        DateFormat mTimeFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);
//    DateFormat mTimeFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM, Locale.CHINA);
    DateFormat mWeekFormat = new SimpleDateFormat("EEE", Locale.CHINA);
    private TextView mTvDate;
    private TextView mTvTime;
    private TextView mTvWeek;
    private MainHandler mMainHandler;

    public DateTimeView(Context context) {
        super(context);
        init(context);
    }

    public DateTimeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DateTimeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_date_time, this, true);
        mTvDate = findViewById(R.id.tv_date);
        mTvTime = findViewById(R.id.tv_time);
        mTvWeek = findViewById(R.id.tv_weekday);
        mMainHandler = new MainHandler(this);
    }

    private static class MainHandler extends Handler {
        private static final int MSG_SHOW_CURRENT_DATE = 0;
        private static final int MSG_UPDATE_TIME = 1;
        private static final int MSG_UPDATE_DATE_TIME = 2;
        private WeakReference<DateTimeView> mWeakReference;
        private boolean mPause =false;
        public MainHandler(DateTimeView dateTimeView) {
            mWeakReference = new WeakReference<>(dateTimeView);
        }

        public void invalidate() {
            mWeakReference.clear();
        }

        public void setPause(boolean pause) {
            mPause = pause;
        }

        @Override
        public void handleMessage(Message msg) {
            DateTimeView dateTimeView = mWeakReference.get();
            if (dateTimeView == null||mPause) {
                return;
            }

            switch (msg.what) {
                case MSG_SHOW_CURRENT_DATE:
                    dateTimeView.scheduleDateTime();
                    break;
//                更新分钟
                case MSG_UPDATE_TIME:
                    dateTimeView.showTime();
                    sendEmptyMessageDelayed(MSG_UPDATE_TIME,  1000);
                    break;
//                    每天凌晨更新日期
                case MSG_UPDATE_DATE_TIME:
                    dateTimeView.showDateTime();
                    sendEmptyMessageDelayed(MSG_UPDATE_DATE_TIME, 24 * 60 * 60 * 1000);
                    break;
            }

        }
    }

    /**
     * 显示当前日期时间 安排下分钟的更新和明天凌晨的更新
     */
    private void scheduleDateTime() {
        Log.d(TAG, "scheduleDateTime: ");
//        先移除之前所有的安排
        mMainHandler.removeCallbacksAndMessages(null);

        Calendar calendar = Calendar.getInstance();
        long current = calendar.getTimeInMillis();
//        显示当前
        mTvDate.setText(mDateFormat.format(current));
        mTvTime.setText(mTimeFormat.format(current));
        mTvWeek.setText(mWeekFormat.format(current));
        //当前秒
        int current_second = calendar.get(Calendar.SECOND);
        //整点更新分钟
//        mMainHandler.sendEmptyMessageDelayed(MainHandler.MSG_UPDATE_TIME, (60 - current_second) * 1000);
//        每秒更新一次
        mMainHandler.sendEmptyMessageDelayed(MainHandler.MSG_UPDATE_TIME, 1000);

        //计算到第二天凌晨时间 更新日期
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        //显示明天凌晨的
        mMainHandler.sendEmptyMessageDelayed(MainHandler.MSG_UPDATE_DATE_TIME, calendar.getTimeInMillis() - current);
    }

    /**
     * 为了在整点同步会重新发送延迟消息
     * 初始化标题
     * 主要是当前日期
     * 并触发一分钟刷新一次时间
     */
    public void startWork() {
        Log.d(TAG, "startWork: ");
//        防止在子线程调用
        mMainHandler.setPause(false);
        mMainHandler.sendEmptyMessage(MainHandler.MSG_SHOW_CURRENT_DATE);
    }

    //暂停工作，可以重新startWork
    public void pauseWork() {
        Log.d(TAG, "pauseWork() called");
        mMainHandler.setPause(true);
    }

//    停止工作，不能重新开始
    public void stopWork() {
        mMainHandler.invalidate();
        mMainHandler.removeCallbacksAndMessages(null);
    }

    //一分钟更新一次
    private void showTime() {
        mTvTime.setText(mTimeFormat.format(System.currentTimeMillis()));
    }

    //    一天更新一次
    private void showDateTime() {
        Log.d(TAG, "showDateTime: ");
        long current = System.currentTimeMillis();
        mTvDate.setText(mDateFormat.format(current));
        mTvTime.setText(mTimeFormat.format(current));
        mTvWeek.setText(mWeekFormat.format(current));
    }

}
