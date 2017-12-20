package shine.com.doorscreen.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.shine.curl.JniCurl;
import shine.com.doorscreen.activity.MainActivity;
import shine.com.doorscreen.app.AppEntrance;
import shine.com.doorscreen.entity.Element;
import shine.com.doorscreen.fragment.MediaFragment;
import shine.com.doorscreen.util.IniReaderNoSection;
import shine.com.doorscreen.util.LogUtil;


/**
 * Created by Administrator on 2016/6/29.
 * 远程多媒体文件下载
 * 67 ftp用户名或者密码错误
 * 7 FtpServer未启动或终端FTP服务器（Ip地址、端口号）设置错误
 * 78 元素找不到，如果只是远程文件不存在的原因
 * 23 如果是调用finish导致中断
 * -1 ftp已停止
 * -2 下载空间满的了
 * 正确下载路径ftp://10.0.1.64:21/video.wmv
 * <p>
 * 6 传入的curl 地址格式错误，shine_av_stream://@:11128(172.168.2.21/4360/0)
 * 9传入的curl 地址格式错误，如/shine_av_stream://@:11128(172.168.2.21/4360/0)
 * 等于28 有一种情况是下载速度变化 现在停止  所以需要重新下载
 * 下载空间满的了返回码是 -2
 * 当终端断电的时候，文件有可能返回23，这时候要删除掉该文件
 */
public class DownLoadService extends IntentService {
    private static final String TAG = DownLoadService.class.getSimpleName();
    //视频和图片目录
    private File mFileMovies = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
    private File mFilePicture = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    public DownLoadService() {
        super("DownLoadService");
    }

    public static void startService(ArrayList<Element> elements) {
        Intent intent = new Intent(AppEntrance.getAppEntrance(), DownLoadService.class);
        intent.putExtra("elements", elements);
        AppEntrance.getAppEntrance().startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent() called with: " + "intent = [" + intent + "]");
        if (intent != null) {
           /* ArrayList<Element> elementsToLoad=(ArrayList<Element>) intent.getSerializableExtra("elements");
            int id = intent.getIntExtra("id", -1);
            //批量插入多媒体数据
            DoorScreenDataBase.getInstance(this).bulkInsertMedia(id,elementsToLoad);
           downLoad(elementsToLoad);*/

            ArrayList<Element> mediaArrayList=(ArrayList<Element>) intent.getSerializableExtra("elements");
            startDownLoad(mediaArrayList);
        }
    }



    //根据id从本地数据库获取已存在的文件集合，和后台消息比较，没有则更新，有则从a中移除记录，最后剩下的就是要删除的文件记录
   @Deprecated
    public int downLoad(List<Element> elements) {
        int success = 1;
        try {
            // 初始化curl init 一次
            JniCurl  curl = new JniCurl();
            JniCurl.useEPSV = true;
            int init = curl.initOnce();
            if (init == 0) {
                // 初始化失败了。
                return 0;
            }
            curl.setOnlyHeader(0);
            for (Element element : elements) {
                File file = new File(element.getPath());
                //如果本地存在就不用下载
                if (file.exists()) {
                    continue;
                }
                curl.setUrl(element.getSrc());
                Log.d(TAG, element.getSrc()+"--"+element.getPath());
                curl.setLocalPath(element.getPath());
                curl.setRange(0);
                int start = curl.start();
                Log.d(TAG, "start:" + start);
                switch (start) {
                    case 0:
                        // 更改数据库状态与实际下载对应
                        Log.d(TAG, "下载成功");
                        break;
                    default:
                        if (file.exists()) {
                            file.delete();
                        }
                        break;
                }
            }
            curl.finishOnce();
            //通知后台重新检索
            startService(DoorService.newIntent(this, MainActivity.DOWNLOAD_DONE, ""));
        } catch (Exception e) {
            LogUtil.d(TAG, e.toString());
        }
        return success;
    }


    public void startDownLoad(List<Element> elements) {
        int success = 1;
        try {
            // 初始化curl init 一次
            JniCurl  curl = new JniCurl();
            JniCurl.useEPSV = true;
            int init = curl.initOnce();
            if (init == 0) {
                // 初始化失败了。
                return ;
            }
            //本地关于服务器下载的配置文件
            IniReaderNoSection inir = new IniReaderNoSection(AppEntrance.ETHERNET_PATH);
            //需要下载文件的路径前缀，包括ftp地址，端口
            String mHeader = String.format("ftp://%s:%s", inir.getValue("ftpip"), inir.getValue("ftpport"));
            curl.setOnlyHeader(0);
            for (Element element : elements) {
                //设置下载的完整路径
                element.setSrc(String.format("%s%s", mHeader, element.getSrc()));
                //分别设置图片和视频的下载路径
                if (element.getType() == 1) {
                    element.setPath(String.format("%s/%s", mFileMovies.getAbsolutePath(), element.getName()));
                } else if (element.getType() == 2) {
                    element.setPath(String.format("%s/%s", mFilePicture.getAbsolutePath(), element.getName()));
                }
                File file = new File(element.getPath());
                //如果本地存在就不用下载
                if (file.exists()) {
                    continue;
                }
                curl.setUrl(element.getSrc());
                Log.d(TAG, element.getSrc()+" -- "+element.getPath());
                curl.setLocalPath(element.getPath());
                curl.setRange(0);
                int start = curl.start();
                Log.d(TAG, "start:" + start);
                switch (start) {
                    case 0:
                        // 更改数据库状态与实际下载对应
                        Log.d(TAG, element.getName()+" 下载成功");
                        break;
                    default:
                        if (file.exists()) {
                            file.delete();
                        }
                        break;
                }
            }
            curl.finishOnce();
            //通知多媒体页面重新检索
            MediaFragment.notifyUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        return success;
    }

}
