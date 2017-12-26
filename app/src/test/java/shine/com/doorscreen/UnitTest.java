package shine.com.doorscreen;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.junit.Test;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import shine.com.doorscreen.entity.PlayTime;
import shine.com.doorscreen.entity.Transfer;

import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

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
        DateFormat mTimeFormat2 = DateFormat.getTimeInstance(DateFormat.MEDIUM, Locale.CHINA);
        DateFormat mTimeFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);
        DateFormat mWeekFormat = new SimpleDateFormat("EEE", Locale.CHINA);
        System.out.println(mDateFormat.format(System.currentTimeMillis()));
        System.out.println(mTimeFormat2.format(System.currentTimeMillis()));
        System.out.println(mWeekFormat.format(System.currentTimeMillis()));

        Date parse = null;
        try {
            parse = mTimeFormat.parse("123");
            Calendar instance = Calendar.getInstance();
            instance.setTime(parse);
            int i = instance.get(Calendar.HOUR_OF_DAY);
            int j = instance.get(Calendar.MINUTE);
            System.out.printf("hour=%d min=%d", i, j);
            System.out.printf("hour=%d min=%d", parse.getHours(), parse.getMinutes());
        } catch (ParseException e) {
            System.out.println("fail to parse");
            e.printStackTrace();
        }
    }

    @Test
    public void testJson() {
        Gson gson = new Gson();
        List<PlayTime> playTimes = new ArrayList<>();
        PlayTime playTime = new PlayTime();
        playTime.setStart("12:22");
        playTime.setStop("122:22");
        playTimes.add(playTime);
        playTimes.add(playTime);
        String output = gson.toJson(playTimes);
        System.out.println(output);
        Type typeCollection = new TypeToken<List<PlayTime>>() {}.getType();
        List<PlayTime> playTimes2 = gson.fromJson(output, typeCollection);
        System.out.println(playTimes2.toString());

        Transfer transfer = gson.fromJson("", Transfer.class);
        assertNull(transfer);
    }

    @Test
    public void isTodayValid() throws ParseException {
        SimpleDateFormat mMediaDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        Date startDate = mMediaDateFormat.parse("2017-10-12");
        Date EndDate = mMediaDateFormat.parse("2017-12-19");
        String today = mMediaDateFormat.format(System.currentTimeMillis());
        Date todayDate = mMediaDateFormat.parse(today);
//        开始日期小于结束日期
        assertTrue(startDate.compareTo(EndDate) <= 0);
//        今天的日期大于等于开始日期
        assertTrue(todayDate.compareTo(startDate) >= 0);
//        今天的日期小于等于结束日期
        assertTrue(todayDate.compareTo(EndDate) <= 0);

        SimpleDateFormat mMediaTimeFormat = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);
        String today_str = mMediaTimeFormat.format(System.currentTimeMillis());

        //一定要这样格式化当前时间
        Date todayTime = mMediaTimeFormat.parse(today_str);
        Date startTime = mMediaTimeFormat.parse("09:12:13");
        Date stopTime = mMediaTimeFormat.parse("09:12:14");

        assertTrue(startTime.before(stopTime));
        //当前时间与播放的起始结束时间有三种可能，在播放时间之前，在播放时间内，过了播放时间不处理
        long marginWithStart = todayTime.getTime() - startTime.getTime();
        long marginWithStop = todayTime.getTime() - stopTime.getTime();
        if (marginWithStart < 0) {
            //还没到播放时间,安排定时更新 重新检索
            System.out.println("还没到播放时间,");
        } else if (marginWithStart >= 0 && marginWithStop <= 0) {
            //在播放时间内立马添加到播放集合，同时安排更新
            System.out.println("在播放时间,");
        } else {
            System.out.println("过了播放时间,");
        }
    }


}