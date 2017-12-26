package shine.com.doorscreen.fragment;


import android.databinding.DataBindingUtil;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Gallery;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.VideoView;
import android.widget.ViewSwitcher;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import shine.com.doorscreen.R;
import shine.com.doorscreen.activity.MainActivity;
import shine.com.doorscreen.databinding.FragmentMediaBinding;
import shine.com.doorscreen.entity.Element;
import shine.com.doorscreen.entity.Mission;
import shine.com.doorscreen.entity.MissionInfo;
import shine.com.doorscreen.entity.Missions;
import shine.com.doorscreen.entity.PlayTime;
import shine.com.doorscreen.service.DownLoadService;
import shine.com.doorscreen.util.DateFormatManager;

/**
 * A simple {@link Fragment} subclass.
 * <p>
 * 视频和图片轮播
 */
public class MediaFragment extends Fragment implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, ViewSwitcher.ViewFactory {
    private static final String TAG = "MediaFragment";
    //视频和图片目录
    private File mFileMovies = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
    private File mFilePicture = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    private VideoView mVideoView;
    private ImageSwitcher mImageSwitch;
    private ViewSwitcher mViewSwitch;
    /**
     * 需要播放的视频文件
     */
    private CopyOnWriteArrayList<File> mVideoPath = new CopyOnWriteArrayList<>();
    /**
     * 展示的图片文件
     */
    private CopyOnWriteArrayList<File> mImagePath = new CopyOnWriteArrayList<>();

    private SparseArray<Mission> mMissions;
    /**
     * 当前播放视频文件的索引，用来循环播放
     */
    private int mVideoIndex = 0;
    /**
     * 当前展示图片的索引
     */
    private int mPictureIndex = 0;

    //頁面是否可見
    private boolean isVisible = false;
    //控件是否初始化
    private boolean isPrepared = false;
    /**
     * 图片切换的间隙，后台有传数据，目前本地写死
     */
    private static final long INTERVAL = 30 * 1000;
    private FragmentMediaBinding mMediaBinding;
    private DateFormatManager mDateFormatManager;
    private MainHandler mHandler;

    private static class MainHandler extends Handler {
        private WeakReference<MediaFragment> mReference;
        private static final int MSG_RESCHEDULE = 1;
        //重新获取播放列表标记
        private static final int MSG_UPDATE = 2;
        private static final int MSG_NEXT = 3;

        public MainHandler(MediaFragment mediaFragment) {
            mReference = new WeakReference<>(mediaFragment);
        }

        public void invalidate() {
            mReference.clear();
        }

        @Override
        public void handleMessage(Message msg) {
            MediaFragment mediaFragment = mReference.get();
            if (mediaFragment == null) {
                return;
            }
            switch (msg.what) {
                case MSG_UPDATE:
                    mediaFragment.scheduleMutilMedia();
                    break;
                case MSG_NEXT:
                    mediaFragment.next();
                    break;
            }
        }
    }

    /*图片到视频的切换*/
    private void next() {
        if (mPictureIndex + 1 == mImagePath.size()) {
            //并且有视频
            if (mVideoPath.size() > 0) {
                if (mViewSwitch.getCurrentView().getId() != R.id.videoView) {
                    Log.d(TAG, "switch to video");
                    mViewSwitch.showNext();
                }
                mPictureIndex = 0;
                showVideo();
            } else {
                //没有视频，从头播放图片
                mPictureIndex = 0;
                showPicture();
            }
        } else {
            mPictureIndex++;
            showPicture();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        mMediaBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_media, container, false);
        return mMediaBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated() called with: ");
        mVideoView = mMediaBinding.videoView;
        mImageSwitch = mMediaBinding.imageSwitch;
        mViewSwitch = mMediaBinding.viewSwitch;

        mImageSwitch.setFactory(this);
        isPrepared = true;
        mDateFormatManager = new DateFormatManager();
        mHandler = new MainHandler(this);
        mMissions = new SparseArray<>();

        // TODO: 2017/12/19 没网10秒后 自己检索（没有数据库不行）
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called");
        mVideoView.setOnCompletionListener(this);
        mVideoView.setOnErrorListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called");
        mVideoView.setOnCompletionListener(null);
        mVideoView.setOnErrorListener(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mHandler.invalidate();
        mHandler.removeCallbacksAndMessages(null);

        Log.d(TAG, "onDestroyView: ");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d(TAG, "setUserVisibleHint: " + isVisibleToUser);
        isVisible = isVisibleToUser;
        if (isVisibleToUser) {
            playMedia();
        } else {
            onInVisible();
        }
    }

    private void onInVisible() {
        if (isPrepared) {
            Log.d(TAG, "停止多媒体的播放");
            mVideoView.stopPlayback();
            mHandler.removeMessages(MainHandler.MSG_NEXT);
        }
    }


    /**
     * 播放视频
     * 如果更新列表为空，此时本地文件都被删除，更新后的列表就为空
     * 所以先判断视频文件是否为空
     */
    private void showVideo() {
        if (mVideoPath.size() > 0) {
            File file = mVideoPath.get(mVideoIndex);
            Log.d(TAG, "file:" + file.exists() + "----file:" + file.getPath());
            mVideoView.setVideoURI(Uri.fromFile(file));
            mVideoView.start();
        }
    }

    private void showPicture() {
        if (mImagePath.size() > 0) {
            File file = mImagePath.get(mPictureIndex);
            ImageView imageView = (ImageView) mImageSwitch.getNextView();
            Log.d(TAG, file.getAbsolutePath());
            Glide.with(this).load(file).into(imageView);
            mImageSwitch.showNext();
            mHandler.sendEmptyMessageDelayed(MainHandler.MSG_NEXT, INTERVAL);
        }
    }


    /**
     * 1.后台每分钟检查一次多媒体素材播放时间有效性，并筛选出播单
     * 2.有变更会调用此方法 从数据库重新加载本地文件
     * 3.播放
     */
    @Deprecated
    public void updateMedia(String ids) {
        Log.d(TAG, "updateMedia: ");
        mPictureIndex = 0;
        mVideoIndex = 0;
        if (isVisible && isPrepared) {
            //如果当前是视频，停止
            if (mViewSwitch.getCurrentView().getId() == R.id.videoView) {
                mVideoView.stopPlayback();
            } else {
                //如果是图片，停止切换
                mHandler.removeMessages(MainHandler.MSG_NEXT);
            }
        }
        playMedia();
    }


    public void playMedia() {
        mPictureIndex = 0;
        mVideoIndex = 0;
        if (isVisible && isPrepared) {
            //如果有视频文件就优先播
            if (mVideoPath.size() > 0) {
                Log.d(TAG, "播放多媒体");
                //如果当前不是视频视图就先切换一下
                if (mViewSwitch.getCurrentView().getId() != R.id.videoView) {
                    mViewSwitch.showNext();
                }
                showVideo();
            } else if (mImagePath.size() > 0) {
                if (mViewSwitch.getCurrentView().getId() != R.id.imageSwitch) {
                    mViewSwitch.showNext();
                }
                showPicture();
            }
        }
    }

    /**
     * 播放完成，切换到下一个视频
     * 也是视频到图片的切换
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(TAG, "on completion");
        //如果是最后一个视频
        if (mVideoIndex + 1 == mVideoPath.size()) {
            //并且图片不为空
            if (mImagePath.size() > 0) {
                //如果当前是视频视图，应该是视频视图，先判断一下
                if (mViewSwitch.getCurrentView().getId() != R.id.imageSwitch) {
                    Log.d(TAG, "switch to image");
                    mVideoView.stopPlayback();
                    mViewSwitch.showNext();
                }
                mPictureIndex = 0;
                showPicture();
            } else {
                //图片为空,从头继续播放视频
                mVideoIndex = 0;
                showVideo();
            }
        } else {
            //播放下一个视频
            mVideoIndex++;
            showVideo();
        }

    }

    @Override
    public View makeView() {
        ImageView imageView = new ImageView(getActivity());
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(new ImageSwitcher.LayoutParams(Gallery.LayoutParams.MATCH_PARENT,
                Gallery.LayoutParams.MATCH_PARENT));
        return imageView;
    }

    /**
     * 播放错误处理
     *
     * @return 返回true 表示不弹出错误提示对话框
     */
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.d(TAG, "Play Error:::onError called");
        Log.e(TAG, "what:" + what + "---extra:" + extra);
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d(TAG, "mVideoPath.get(mVideoIndex):" + mVideoPath.get(mVideoIndex));
                Log.e(TAG, "Play Error::: MEDIA_ERROR_SERVER_DIED");
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.e(TAG, "Play Error::: MEDIA_ERROR_UNKNOWN");
                break;
        }
        return true;
    }


    /**
     * 安排多媒体播放  如果检索到本地有在当前时间段播放的多媒体会请求Activity切换
     * 先从多媒体日期表获取符合条件的播单id，在根据时间表安排播放时间
     * 1.获取符合条件的播放列表
     * 2.安排下一次更新
     */
    public void scheduleMutilMedia() {
        Log.d(TAG, "scheduleMutilMedia: ");
        mHandler.removeCallbacksAndMessages(null);
        //安排明天凌晨的更新
//        mHandler.sendEmptyMessageDelayed(MainHandler.MSG_RESCHEDULE, mDateFormatManager.millisToWeeHour());
        List<Element> playList = new ArrayList<>();
        for (int i = 0, size = mMissions.size(); i < size; i++) {
            Mission mission = mMissions.valueAt(i);
            Log.d(TAG, "mission " + mission.toString());
//            当前播单是否在播放日期内,并且是发布状态
            if (mDateFormatManager.isMeidaDateBetween(mission.getStartdate(), mission.getStopdate()) && mission.getType() == 2) {
                Log.d(TAG, "date pass");
                List<PlayTime> playtimes = mission.getPlaytimes();
                for (PlayTime playtime : playtimes) {
                    try {
//                        获取当前时间和播放开始和结束时间差
                        long[] margins = mDateFormatManager.calculatePlayingTime(playtime.getStart(), playtime.getStop());
                        if (margins[0] < 0) {
                            //还没到播放时间,安排定时更新 重新检索
                            Log.d(TAG, "scheduleMutilMedia: not yet");
                            mHandler.sendEmptyMessageDelayed(MainHandler.MSG_UPDATE, -margins[0]);
//                            只要有一个playtime满足 就没必要计算下一个
                            break;
                        } else if (margins[0] >= 0 && margins[1] <= 0) {
                            //在播放时间内立马添加到播放集合，同时安排下次结束更新
                            Log.d(TAG, "just in time");
                            playList.addAll(mission.getSource());
                            mHandler.sendEmptyMessageDelayed(MainHandler.MSG_UPDATE, -margins[1]);
                            break;
                        } else {
                            Log.d(TAG, "out of play time");
                        }
                    } catch (ParseException | IllegalArgumentException e) {
                        e.printStackTrace();
                        Log.e(TAG, "scheduleMutilMedia: out of expect");
                    }
                }
            }
        }
        updatePlayList(playList);
    }


    /**
     * 更新播放列表
     *
     * @param elements
     */
    private void updatePlayList(List<Element> elements) {
        Log.d(TAG, "updatePlayList: ");
        if (mVideoView.isPlaying()) {
            mVideoView.stopPlayback();
        }
        mVideoPath.clear();
        mImagePath.clear();
        for (Element mutilMedia : elements) {
            if (mutilMedia.getType() == 1) {
                File file = new File(mFileMovies.getAbsolutePath(), mutilMedia.getName());
                //播单可能会有重复的素材，不重复加
                if (file.exists()) {
                    mVideoPath.addIfAbsent(file);
                }
            } else if (mutilMedia.getType() == 2) {
                File file = new File(mFilePicture.getAbsolutePath(), mutilMedia.getName());
                if (file.exists()) {
                    mImagePath.addIfAbsent(file);
                }
            }
        }
        Log.d(TAG, "video" + mVideoPath.toString());
        Log.d(TAG, "image " + mImagePath.toString());
        MainActivity activity = (MainActivity) getActivity();
        boolean canPlay = mVideoPath.size() > 0 || mImagePath.size() > 0;
        //可见
        if (isVisible && canPlay) {
            playMedia();
        } else if (isVisible && !canPlay) {
//            可见 不能播 切换
            activity.requestPlayMedia(false);
        } else if (!isVisible && canPlay) {
//            不可见 能播
            activity.requestPlayMedia(true);
        } else {
            Log.d(TAG, "不可见 不能播");
        }

    }

    /**
     * 与服务器同步本地宣教信息 在应用启动的时候，后台会发送
     * 根据宣教播放时间段不同存储不同字段
     * 存储的时间是决定播放的关键
     *
     * @param receive
     */
    public void synVideoMissions(String receive) {
        Log.d(TAG, "synVideoMissions: ");
        try {
            Missions missions = new Gson().fromJson(receive, Missions.class);
            List<Mission> missionList = missions.getData();
            //一般启动联网时调用，此时为空 不需要清空 防止中途调用
            mMissions.clear();
//            所有要下载的元素
            ArrayList<Element> elements = new ArrayList<>();
//            分类保存在内存中
            for (Mission mission : missionList) {
                mMissions.put(mission.getMissionid(), mission);
                elements.addAll(mission.getSource());
            }
//           空集合也要通知后台下载 然后在BrocastReciver中等待下载完成通知
            DownLoadService.startService(elements);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理单个视频宣教信息  0_暂停，1_删除，2_发布宣教内容
     * 类型是发布需要先下载，完成后再通知Frament更新数据 重新检索
     * 暂停通知Fragment更新数据 重新检索
     * 停止 删除文件后通知Fragment更新数据 重新检索
     * 因为可能多个播单共享一个视频 ，删除后其他播单不能使用 所以相关Fragment添加播放列表时需要判断本地文件是否存在
     * Mission 是用来统一单个播单和多个播单的封装类
     *
     * @param receive 后台发来的播单数据
     */
    public void handleSingleVideoMission(String receive) {
        try {
            MissionInfo missionInfo = new Gson().fromJson(receive, MissionInfo.class);
            int missionid = missionInfo.getMissionid();
            int type = missionInfo.getType();
            switch (missionInfo.getType()) {
                case 0:
                case 1:
                    invalidateMission(type, missionid);
                    break;
                case 2:
                    Mission mission = missionInfo.getData();
                    mission.setMissionid(missionid);
                    mission.setType(type);
                    //            更新播单
                    mMissions.put(mission.getMissionid(), mission);
                    // 通知下载
                    DownLoadService.startService(mission.getSource());
                    break;
            }
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止或删除单个播单
     *
     * @param type      0_暂停，1_删除
     * @param missionId 播单Id
     */
    private void invalidateMission(int type, int missionId) {
        //type 0 停止  1-删除 删除之前必须先停止
        Mission mission = mMissions.get(missionId);
        Log.d(TAG, "mission:" + mission);
        if (mission != null && type == 1) {
            ArrayList<Element> elements = mission.getSource();
            for (Element mutilMedia : elements) {
                Log.d(TAG, "mutilMedia:" + mutilMedia);
                if (mutilMedia.getType() == 1) {
                    File file = new File(mFileMovies.getAbsolutePath(), mutilMedia.getName());
                    if (file.exists()) {
                        Log.d(TAG, file.getName() + " 删除");
                        file.delete();
                    }
                } else if (mutilMedia.getType() == 2) {
                    File file = new File(mFilePicture.getAbsolutePath(), mutilMedia.getName());
                    Log.d(TAG, file.getName() + " 删除");
                    file.delete();
                }
            }
            mMissions.remove(missionId);
        } else if (mission != null && type == 0) {
//                设为停止状态
            mission.setType(0);
        }
        scheduleMutilMedia();
    }


}
