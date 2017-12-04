package shine.com.doorscreen.util;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * author:
 * 时间:2017/12/1
 * qq:1220289215
 * 类描述：
 */

public class DateFormatManager {
    SimpleDateFormat mDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
//    为2012年12月3日
    DateFormat mDateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.CHINA);
    DateFormat mTimeFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);
    DateFormat mWeekFormat = new SimpleDateFormat("EEE",Locale.CHINA);

    public Date parseTime(String time){
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
}
