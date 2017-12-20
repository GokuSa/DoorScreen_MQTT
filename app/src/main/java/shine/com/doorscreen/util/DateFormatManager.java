package shine.com.doorscreen.util;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * author:
 * 时间:2017/12/1
 * qq:1220289215
 * 类描述：
 */

public class DateFormatManager {
    private static final String TAG = DateFormatManager.class.getSimpleName();
    private SimpleDateFormat mDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    //    为2012年12月3日
    private DateFormat mDateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.CHINA);
    private DateFormat mTimeFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);
    private DateFormat mWeekFormat = new SimpleDateFormat("EEE", Locale.CHINA);
    private SimpleDateFormat mMediaDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
    private SimpleDateFormat mMediaTimeFormat = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);

    public Date parseTime(String time) {
        if (!TextUtils.isEmpty(time)) {
            try {
                mTimeFormat.parse(time);
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    public String formatDateTime(@NonNull Object object) {
        return mDateTimeFormat.format(object);
    }

    public String formatMediaDate(@NonNull Object object) {
        return mMediaDateFormat.format(object);
    }

    public String formatMediaTime(@NonNull Object object) {
        return mMediaTimeFormat.format(object);
    }

    public Date parseMediaTime(@NonNull String time) throws ParseException {
        return mMediaTimeFormat.parse(time);

    }

    /**
     * 开始日期小于结束日期
     * 今天的日期大于等于开始日期
     * 今天的日期小于等于结束日期
     *
     * @param start 开始日期
     * @param end   结束日期
     */
    public boolean isMeidaDateBetween(String start, String end) {
        if (TextUtils.isEmpty(start) || TextUtils.isEmpty(end)) {
            return false;
        }
        try {
            Date startDate = mMediaDateFormat.parse(start);
            Date EndDate = mMediaDateFormat.parse(end);
            String today = mMediaDateFormat.format(System.currentTimeMillis());
            Date todayDate = mMediaDateFormat.parse(today);
            return startDate.compareTo(EndDate) <= 0 &&
                    todayDate.compareTo(startDate) >= 0 &&
                    todayDate.compareTo(EndDate) <= 0;

        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 计算开始时间和结束时间与当前时间的差
     *
     * @param start 开始时间
     * @param end   结束时间 格式为  HH：mm 没有精确到秒
     * @return 开始时间和结束时间与当前时间的差
     */
    public long[] calculatePlayingTime(String start, String end) throws ParseException {
        if (TextUtils.isEmpty(start) || TextUtils.isEmpty(end)) {
            throw new IllegalArgumentException("start and end can't be empty");
        }
        //一定要这样格式化当前时间
        Date startTime = mMediaTimeFormat.parse(String.format("%s:00", start));
        Date stopTime = mMediaTimeFormat.parse(String.format("%s:00", end));
        if (startTime.after(startTime)) {
            throw new IllegalArgumentException("start time can't be later");
        }
        String today_str = mMediaTimeFormat.format(System.currentTimeMillis());
        Date todayTime = mMediaTimeFormat.parse(today_str);
        long[] margins=new long[2];
        //当前时间与播放的起始结束时间有三种可能，在播放时间之前，在播放时间内，过了播放时间不处理
        long marginWithStart = todayTime.getTime() - startTime.getTime();
        long marginWithStop = todayTime.getTime() - stopTime.getTime();
        margins[0]=marginWithStart;
        margins[1]=marginWithStop;

       return margins;
    }

    //当前时间到凌晨的毫秒苏
    public long millisToWeeHour() {
        //计算到第二天凌晨时间 更新日期
        Calendar calendar = Calendar.getInstance();
        long current = calendar.getTimeInMillis();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        return calendar.getTimeInMillis()-current;
    }


}
