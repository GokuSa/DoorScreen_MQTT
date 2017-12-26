package shine.com.doorscreen.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.WindowManager;
import android.widget.Toast;

import com.shine.timingboot.TimingBootUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import shine.com.doorscreen.service.LocalParameter;


/**
 * Created by Administrator on 2016/6/17.
 * 常用工具
 * java -jar E:\sign\801\signapk.jar E:\myWork\2016\r16\platform.x509.pem E:\myWork\2016\r16\platform.pk8 E:\myWork\2016\r16\Launcher\bin\Launcher.apk C:\Users\PANPAN\Desktop\Launcher-R16.apk
 */
public class Common {
    private static final String TAG = "CommonUtil";

    /**
     * 获得屏幕高度
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    /**
     * 获得屏幕宽度
     *
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }

    public static int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics());
    }

    public static float sp2px(Context context, float dpVal) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                dpVal, context.getResources().getDisplayMetrics());
    }


    /**
     * 获取MAC地址
     *
     * @return
     */
    public static String getMacAddress() {
//        String strMacAddr = "";
        StringBuilder buffer = new StringBuilder();

        try {
            NetworkInterface NIC = NetworkInterface.getByName("eth0");
            if (NIC == null) {
                return "";
            }
            //6个字节，48位
            byte[] bytes = NIC.getHardwareAddress();
            if (null == bytes || bytes.length == 0) {
                return "";
            }
            for (byte b : bytes) {
                String str = Integer.toHexString(b & 0xff);
                buffer.append(str.length() == 1 ? 0 + str : str);
            }
//            strMacAddr = buffer.deleteCharAt(0).toString();
//            strMacAddr = buffer.toString();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "Mac Address : " + buffer.toString());
        return buffer.toString().toUpperCase();
    }

    /**
     * 获取ip地址
     *
     * @return
     */
    public static String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface intf = netInterfaces.nextElement();
                if (intf.getName().toLowerCase().equals("eth0") || intf.getName().toLowerCase().equals("wlan0")) {
                    Enumeration<InetAddress> inetAddresses = intf.getInetAddresses();
                    while (inetAddresses.hasMoreElements()) {
                        InetAddress inetAddress = inetAddresses.nextElement();
                        //不是环回地址,不是ip6地址
                        if (!inetAddress.isLoopbackAddress() && !inetAddress.getHostAddress().contains("::")) {
                            ip = inetAddress.getHostAddress();
                            Log.d(TAG, ip);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ip;
    }


    public static boolean isNetworkAvailable(Context context) {
        boolean result = false;
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        result = activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
       /* if (!result) {
            Toast.makeText(context, "网络不可连接", Toast.LENGTH_SHORT).show();
        }*/
        return result;
    }

    public static String getFileName(String pathandname) {
        int start = pathandname.lastIndexOf("/");
        int end = pathandname.lastIndexOf(".");
        if (start != -1) {
            return pathandname.substring(start + 1);
        } else {
            return "";
        }
    }

    public static String getFileSuffix(String pathandname) {
        int start = pathandname.lastIndexOf(".");
        if (start != -1) {
            return pathandname.substring(start + 1);
        } else {
            return "";
        }
    }

    private static Toast toast = null;

    public static void showToast(Context context, int text) {
        if (toast == null) {
            toast = Toast.makeText(context.getApplicationContext(), text, Toast.LENGTH_SHORT);
        } else {
            toast.setText(text);
        }
        toast.show();
    }


    public static void open(Long date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        String start = sdf.format(date);
        Log.d(TAG, "开机时间为" + start);
        int result = new TimingBootUtils().setRtcTime(start);
    }


    /**
     * 取消开机，否则会重启
     */
    public static void cancelOpen() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        String openTime = sdf.format(calendar.getTimeInMillis());
        Log.d(TAG, "timeOpen " + openTime);
        int result = new TimingBootUtils().setRtcTime(openTime);
        Log.d(TAG, "cancel open result:" + result);
    }

    public static final boolean isChineseCharacter(String chineseStr) {
        char[] charArray = chineseStr.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            if ((charArray[i] >= 0x4e00) && (charArray[i] <= 0x9fbb)) {
                return true;
            }
        }
        return false;
    }

    public static boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }

    //截取数字
    public static int getNumbers(String content) {
        int num = -1;
        if (TextUtils.isEmpty(content)) {
            return num;
        }
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            if (!TextUtils.isEmpty(matcher.group(0))) {
                try {
                    num = Integer.parseInt(matcher.group(0));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        return num;
    }

    public static LocalParameter fetchParameter(String path) throws IOException,IllegalArgumentException {
        if (TextUtils.isEmpty(path)) {
            throw new IllegalArgumentException("path can't be null");
        }
        File file = new File(path);
        Properties properties = new Properties();
        properties.load(new FileInputStream(file));
        String serverIp = properties.getProperty("commuip","");
        String port =  properties.getProperty("commuport","");
//        MQTT需要的格式化地址 "tcp://172.168.1.9:1883";
         String serverUri = String.format(Locale.CHINA, "tcp://%s:%s", serverIp, port);
//        没有网线连接的时候 Common.getIpAddress()获取的IP无效
        String host = properties.getProperty("ip","");
        return new LocalParameter(host, getMacAddress(), serverUri);

    }
}