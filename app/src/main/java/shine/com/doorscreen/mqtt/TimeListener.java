package shine.com.doorscreen.mqtt;

/**
 * author:
 * 时间:2017/7/14
 * qq:1220289215
 * 类描述：与后台时间同步监听，重启设置监听
 */

public interface TimeListener {
    //同步后台时间
    void onSynSucceed();
    //更新重启时间
    void updateReStart();
}
