package shine.com.doorscreen;


import org.junit.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
        String startDate = "2017-09-29 00:00:00";
        String endDate = "2017-09-29 23:59:59";
        Date date_today = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        String string_today = dateFormat.format(date_today);
        System.out.println("date tiem" + string_today);
        Date dateStart = dateFormat.parse(startDate);
        Date dateEnd = dateFormat.parse(endDate);
        int i = date_today.compareTo(dateStart);
        int i2 = date_today.compareTo(dateEnd);
        System.out.println(i + "margin " + i2);
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
            System.out.println("after" + after);
            boolean before = dateToday.before(dateEnd);
            System.out.println("before" + before);
            long l = dateEnd.getTime() - dateToday.getTime();
            System.out.println("margin" + l);
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

    @Test
    public void trimCommnet() {
        String s = "{\n" +
                "\"action\":\"submitinfusion\", /**请求类型:提交输液提醒参数*/\n" +
                "\"http://www.event\":0,//事件类型：0_新置输液提醒参数，1_输液即将完成提醒\n" +
                "\"department\":\"411\",//科室id\n" +
                "\"roomid\":\"5\",  //病房id\n" +
                "\"clientip\":\"10.0.2.8\",//床头屏ip\n" +
                "\"clientmac\":\"aa:bb:cc:dd:ee:ff\", //床头屏mac地址\n" +
                "\"clientname\":\"1床\", //床头屏名称\n" +
                "\"username\":\"张三\",//床头屏昵称：该床头屏对应的患者名字\n" +
                "\"type\":10,//输液带类型,10ML/滴.\n" +
                "\"speed\":40,//滴速，40滴每分钟\n" +
                "\"capacity\":500,//容量，单位毫升ML\n" +
                "\"left\":32,//输液剩余时间。单位分钟\n" +
                "\"nursecard\":132123,//护士工号，不需要验证传递-1\n" +
                "\"sender\":\"screen\" //终端类型:床头屏\n" +
                "}";
        //去掉行级注释 与带斜杠的协议如http://冲突
        String regex = "\\/\\/[^\\n]*";
//        优化版的去掉行级注释 防止去掉协议中的双斜杠
        String regex2 = "(?<!:)\\/\\/.*";
//        去掉块级别注释
        String regex3 = "\\/\\*(\\s|.)*?\\*\\/";
//即去掉行也去掉块级的注释
        String regex4 = "(?<!:)\\/\\/.*|\\/\\*(\\s|.)*?\\*\\/";
        s = s.replaceAll(regex4, "");
        System.out.println(s);
    }

    @Test
    public void testDate() {

         DateFormat mDateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.CHINA);
         DateFormat mTimeFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);
         DateFormat mWeekFormat = new SimpleDateFormat("EEE",Locale.CHINA);
        System.out.println(mDateFormat.format(System.currentTimeMillis()));
        System.out.println(mTimeFormat.format(System.currentTimeMillis()));
        System.out.println(mWeekFormat.format(System.currentTimeMillis()));

        Date parse = null;
        try {
            parse = mTimeFormat.parse("123");
            Calendar instance = Calendar.getInstance();
            instance.setTime(parse);
            int i = instance.get(Calendar.HOUR_OF_DAY);
            int j = instance.get(Calendar.MINUTE);
            System.out.printf("hour=%d min=%d",i,j);
            System.out.printf("hour=%d min=%d",parse.getHours(),parse.getMinutes());
        } catch (ParseException e) {
            System.out.println("fail to parse");
            e.printStackTrace();
        }

    }

}