package shine.com.doorscreen.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
 */

public class DateTimeView extends RelativeLayout {
    private static final String TAG = DateTimeView.class.getSimpleName();
    DateFormat mDateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.CHINA);
    DateFormat mTimeFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);
    DateFormat mWeekFormat = new SimpleDateFormat("EEE",Locale.CHINA);
    private TextView mTvDate;
    private TextView mTvTime;
    private TextView mTvWeek;

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
    }

    /**
     * 为了在整点同步会重新发送延迟消息
     * 初始化标题
     * 主要是当前日期
     * 并触发一分钟刷新一次时间
     */
    public void startWork() {
        Log.d(TAG, "showCurrentDateTime() called");
        //格式化当前时间为 年 月 日  星期几，比如2016年8月10日 星期三
        showDateTime();

        Calendar calendar = Calendar.getInstance();
        long current = calendar.getTimeInMillis();
        //当前秒
        int current_second = calendar.get(Calendar.SECOND);
        removeCallbacks(this::showTime);
        //整点更新分钟
        postDelayed(this::showTime, (60 - current_second) * 1000);

        //设置第二天凌晨 更新日期
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        Log.d(TAG, "tomorrow "+mDateFormat.format(calendar.getTimeInMillis()));
        removeCallbacks(this::showDateTime );
        postDelayed(this::showDateTime, calendar.getTimeInMillis() - current);
    }

    public void stopWork() {
        removeCallbacks(this::showTime);
        removeCallbacks(this::showDateTime);
    }

    //一分钟更新一次
    private void showTime() {
        mTvTime.setText(mTimeFormat.format(System.currentTimeMillis()));
        postDelayed(this::showTime, 60  * 1000);
    }

//    一天更新一次
    private void showDateTime() {
        long current = System.currentTimeMillis();
        mTvDate.setText(mDateFormat.format(current));
        mTvTime.setText(mTimeFormat.format(current));
        mTvWeek.setText(mWeekFormat.format(current));
        postDelayed(this::showDateTime, 24 * 60 * 60 * 1000);
    }

}
