package shine.com.doorscreen;


import org.junit.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class UnitTest {

    @Test
    public void isDateValid() throws ParseException {
        //判断当前是否在指定日期内
        String startDate="2017-09-29 00:00:00";
        String endDate="2017-09-29 23:59:59";
        Date date_today = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        String string_today = dateFormat.format(date_today);
        System.out.println("date tiem"+string_today);
        Date dateStart = dateFormat.parse(startDate);
        Date dateEnd = dateFormat.parse(endDate);
        int i = date_today.compareTo(dateStart);
        int i2 = date_today.compareTo(dateEnd);
        System.out.println(i+"margin "+i2);
    }

    @Test
    public void testTime() {
        Date today = new Date();
        DateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);
        String format1 = format.format(today);
        System.out.println(format1);
        String start = "08:12";
        String end = "18:57";
        try {
            Date dateStart = format.parse(String.format("%s:00", start));
            Date dateEnd = format.parse(String.format("%s:00", end));
            Date dateToday = format.parse(format1);
            boolean after = dateToday.after(dateStart);
            System.out.println("after"+after);
            boolean before = dateToday.before(dateEnd);
            System.out.println("before"+before);
            long l = dateEnd.getTime() - dateToday.getTime();
            System.out.println("margin"+l);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    @Test
    public void retrieveNumberFromString() {
        String content = "23黑";
        Pattern compile = Pattern.compile("\\d+");
        Matcher matcher = compile.matcher(content);
        if (matcher.find()) {
            String group = matcher.group(0);
            System.out.println(group);
        }
    }

}