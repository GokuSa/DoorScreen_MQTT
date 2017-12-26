package shine.com.doorscreen.app;

import android.app.Application;

/**
 * Created by Administrator on 2016/8/4.
 * 应用程序入口
 */
public class AppEntrance extends Application {
    private static AppEntrance sAppEntrance;

    /*public static int[] resIds = new int[]{R.drawable.drip_package_empty, R.drawable.drip_package_five, R.drawable.drip_package_ten,
            R.drawable.drip_package_fifteen, R.drawable.drip_package_twenty, R.drawable.drip_package_thirty,
            R.drawable.drip_package_forty, R.drawable.drip_package_fifty, R.drawable.drip_package_sixty,
            R.drawable.drip_package_seventy, R.drawable.drip_package_eighty, R.drawable.drip_package_ninty, R.drawable.drip_package_full};
*/
    @Override
    public void onCreate() {
        super.onCreate();
        sAppEntrance = this;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

    }

    public static AppEntrance getAppEntrance() {
        return sAppEntrance;
    }
}
