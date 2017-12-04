package shine.com.doorscreen.fragment;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import shine.com.doorscreen.R;
import shine.com.doorscreen.adapter.DripAdapter2;
import shine.com.doorscreen.adapter.InsetDecoration;
import shine.com.doorscreen.adapter.PatientAdapter2;
import shine.com.doorscreen.adapter.RecycleViewDivider;
import shine.com.doorscreen.adapter.StaffAdapter;
import shine.com.doorscreen.database.WardDataBase;
import shine.com.doorscreen.databinding.FragmentDoor2Binding;
import shine.com.doorscreen.mqtt.bean.Infusion;
import shine.com.doorscreen.mqtt.bean.Marquee;
import shine.com.doorscreen.mqtt.bean.MarqueeTime;
import shine.com.doorscreen.mqtt.bean.Patient;
import shine.com.doorscreen.mqtt.bean.Staff;
import shine.com.doorscreen.mqtt.bean.Ward;
import shine.com.doorscreen.util.Common;
import shine.com.doorscreen.viewmodel.WardViewModel;

/**
 * A simple {@link } subclass.
 * 门口屏主页面 显示输液进制和医生信息，呼叫信息
 * todo 减少此页面的业务逻辑
 * 1.日期时间的显示逻辑
 * 2.对话框显示逻辑
 * 3.开关机 开关屏 重启 相关逻辑
 */
public class DoorFragment extends Fragment {
    private static final String TAG = DoorFragment.class.getSimpleName();
    public static final int DRIP_UPDATE_INTERVAL = 60 * 1000;
    private static final int DRIP_DOCTOR_SWITCH = 779;
    private static final int DRIP_DOCTOR_SWITCH_INTERVAL = 10 * 1000;
    public static final int TIME_UPDATE_INTERVAL = 60 * 1000;
    private static final int KEEP_TIME = 0;
    private static final int DRIP_UPDATE = 1;
    private static final int UPDATE_MARQUEE = 6;
    private static final int MSG_MARQUEE_ADD = 7;
    private static final int MSG_MARQUEE_REMOVE = 8;
    private static final int MSG_SCHEDULE_MARQUEES = 9;
    private static final int MSG_REFRESH_TOMORROW = 10;
    /**
     * 格式化当前时间，用于标题的时间和输液开始时间格式化
     */
    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);
    /**
     * 格式化跑马的时间
     */
    private SimpleDateFormat mTimeFormat = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);
    /**
     * 格式化跑马的日期
     */
    private SimpleDateFormat mMarqueeDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
    private DateFormat mDateFormat = DateFormat.getDateInstance(DateFormat.FULL, Locale.CHINA);

    private DripAdapter2 mDripAdapter;
    private PatientAdapter2 mPatientAdapter;
    private boolean isVisible = false;
    private boolean isPrepared = false;
    private FragmentDoor2Binding mBinding;
    private StaffAdapter mStaffAdapter;

//    private List<ReStart> mReStartParams = new ArrayList<>();

    private WardViewModel mWardViewModel;
    @SuppressWarnings("handlerleak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case KEEP_TIME:
                    //更新时间
//                    String time = mSimpleDateFormat.format(System.currentTimeMillis());
//                    mBinding.tvTime.setText(time);
//                    判断是否重启
//                    scheduleReStart(time);
                    //一分钟更新一次
//                    sendEmptyMessageDelayed(KEEP_TIME, TIME_UPDATE_INTERVAL);
                    break;
                case DRIP_UPDATE:
                    mDripAdapter.update();
                    sendEmptyMessageDelayed(DRIP_UPDATE, DRIP_UPDATE_INTERVAL);
                    break;
                case DRIP_DOCTOR_SWITCH:
                    if (mBinding.viewSwitchDripAndDoctor.getCurrentView().getId() != R.id.rv_drip) {
                        mBinding.viewSwitchDripAndDoctor.showNext();
                        mBinding.tvInfo.setText(R.string.drip_info);
                    }
                    break;
                case MSG_MARQUEE_ADD:
                    mBinding.marqueeView.add((Marquee) msg.obj);
                    break;
                case MSG_MARQUEE_REMOVE:
                    mBinding.marqueeView.delete((Marquee) msg.obj);
                    break;
                case MSG_SCHEDULE_MARQUEES:
                    scheduleMarquees();
                    break;
                case MSG_REFRESH_TOMORROW:
                   /* String date_week = mDateFormat.format(System.currentTimeMillis());
                    //更新日期
                    mBinding.tvDate.setText(date_week.substring(0, date_week.length() - 3));
                    //更新星期
                    mBinding.tvWeekday.setText(date_week.substring(date_week.length() - 3));
                    //重新安排今天的跑马灯
                    scheduleMarquees();
                    //安排到明天
                    sendEmptyMessageDelayed(MSG_REFRESH_TOMORROW, 24 * 60 *60* 1000);*/
                    break;

            }
        }
    };

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d(TAG, "isVisibleToUser = [" + isVisibleToUser + "]");
        isVisible = isVisibleToUser;
        if (isVisibleToUser) {
            onVisible();
        } else {
            onInvisible();
        }
    }

    public void onInvisible() {
        if (isPrepared) {
            mBinding.marqueeView.stopMarquee();
        }
    }

    private void onVisible() {
        if (isPrepared) {
            mBinding.marqueeView.startMarquee();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView() called ");
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_door2, container, false);

        mStaffAdapter = new StaffAdapter();
        mBinding.rvStaff.addItemDecoration(new InsetDecoration(getContext()));
        mBinding.rvStaff.setAdapter(mStaffAdapter);

        mDripAdapter = new DripAdapter2(getActivity(),mOnDripListener);
        mBinding.rvDrip.addItemDecoration(new InsetDecoration(getContext()));
        mBinding.rvDrip.setAdapter(mDripAdapter);
        //数据更新时recyclerView 有默认动画，导致闪屏,所以取消掉
        ((SimpleItemAnimator) mBinding.rvDrip.getItemAnimator()).setSupportsChangeAnimations(false);

        mPatientAdapter = new PatientAdapter2();
        mBinding.rvPatientInfo.setAdapter(mPatientAdapter);
        mBinding.rvPatientInfo.addItemDecoration(new RecycleViewDivider(getActivity(), LinearLayoutManager.VERTICAL, R.drawable.divider_vertical));

        mBinding.tvTitle.setOnClickListener(v -> {
            int min = new Random().nextInt(5);
            Infusion infusion = new Infusion("aa:bb:cc:dd:ee:ff",min+"床",  min,min);
            addInfusion(infusion);
        });
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated() called with");
        isPrepared = true;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated() called with");
        mWardViewModel = ViewModelProviders.of(this).get(WardViewModel.class);
        //病人信息
        mWardViewModel.getPatientObserver().observe(this, new Observer<List<Patient>>() {
            @Override
            public void onChanged(@Nullable List<Patient> patients) {
                Log.d(TAG, "patients " + patients.toString());
                mPatientAdapter.setPatients(patients);
            }
        });
        //病房和探视时间信息
        mWardViewModel.getWardObserver().observe(this, new Observer<Ward>() {
            @Override
            public void onChanged(@Nullable Ward ward) {
                if (ward != null) {
                    Log.d(TAG, "ward" + ward.toString());
                    setTitle(ward.getRoomname());
                    mBinding.tvVisitPeriodOne.setText(ward.getMorning());
                    mBinding.tvVisitPeriodTwo.setText(ward.getNoon());
                    mBinding.tvVisitPeriodThree.setText(ward.getNight());
                    handleCalling(ward.getCallTip());
                } else {
                    mBinding.tvTitle.setText("");
                    mBinding.tvVisitPeriodOne.setText("");
                    mBinding.tvVisitPeriodTwo.setText("");
                    mBinding.tvVisitPeriodThree.setText("");
                }
            }
        });
        //医护人员信息
        mWardViewModel.getStaffObserver().observe(this, new Observer<List<Staff>>() {
            @Override
            public void onChanged(@Nullable List<Staff> staffs) {
                Log.d(TAG, staffs.toString());
                if (mBinding.viewSwitchDripAndDoctor.getCurrentView().getId() != R.id.rv_staff) {
                    mBinding.viewSwitchDripAndDoctor.showNext();
                    mBinding.tvInfo.setText(R.string.doctor_info);
                    //如果输液有信息，过段时间切换到输液信息
                    if (mDripAdapter != null && mDripAdapter.getItemCount() > 0) {
                        mHandler.sendEmptyMessageDelayed(DRIP_DOCTOR_SWITCH, DRIP_DOCTOR_SWITCH_INTERVAL);
                    }
                }
                mStaffAdapter.setStaffList(staffs);

            }
        });
        //从本地获取重启参数
//        mReStartParams.clear();
//        mReStartParams.addAll(mWardViewModel.getReStartParams());

        //显示当前时间，
        showCurrentDateTime();

        //从本地获取跑马灯数据 安排跑马灯
        //连上网络后先更新本地数据库，所以延迟5秒从本地数据库获取跑马灯数据，防止联网失败
        mHandler.sendEmptyMessageDelayed(MSG_SCHEDULE_MARQUEES, 5000);

    }


    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called");
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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: ");
        mBinding.marqueeView.stopMarquee();
        mBinding.dateTime.stopWork();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    /**
     * 处理呼叫
     *
     * @param callTip
     */
    private void handleCalling(String callTip) {
        if (!TextUtils.isEmpty(callTip)) {
            //有呼叫,当前不在呼叫视图，切换
            if ((mBinding.viewSwitchCallAndVisit.getCurrentView().getId() != R.id.ll_call_info)) {
                mBinding.viewSwitchCallAndVisit.showNext();
            }
            mBinding.tvPatientCall.setText(callTip);
        } else {
            //没有呼叫，不在探视视图，切换
            if ((mBinding.viewSwitchCallAndVisit.getCurrentView().getId() != R.id.ll_visit_info)) {
                mBinding.viewSwitchCallAndVisit.showNext();
            }
        }

    }


    /**
     * 每分钟刷新时间时调用，满足条件执行重启
     *
     * @param time
     */

   /* private void scheduleReStart(String time) {
        if (mReStartParams.size() > 0) {
            //获取今天星期几,从0到6
            int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
            //防止越界
            if (day < mReStartParams.size()) {
                //获取今天的重启参数
                ReStart reStart = mReStartParams.get(day);
                //如果重启
                if (reStart.getReboot() == 1 && time.equals(reStart.getRebootTime())) {
                    //关机60s后重启
                    Common.open(System.currentTimeMillis() + 60 * 1000);
                    DoorService.startService(getActivity(), CLOSE, "");
                }
            }
        }
    }*/


    /**
     * 使用自定义的日期视图处理时间日期的显示
     */
    private void showCurrentDateTime() {
        mBinding.dateTime.startWork();
/*
        Log.d(TAG, "showCurrentDateTime() called");
        //格式化当前时间为 年 月 日  星期几，比如2016年8月10日 星期三
        Calendar calendar = Calendar.getInstance();
        long current = calendar.getTimeInMillis();
        String date_week = mDateFormat.format(current);
        //android 这个格式化方法中间没有空格，不能split
        mBinding.tvDate.setText(date_week.substring(0, date_week.length() - 3));
        mBinding.tvWeekday.setText(date_week.substring(date_week.length() - 3));
        String time = mSimpleDateFormat.format(current);
        mBinding.tvTime.setText(time);
        int current_second = calendar.get(Calendar.SECOND);
        mHandler.removeMessages(KEEP_TIME);
        //整点更新时钟
        mHandler.sendEmptyMessageDelayed(KEEP_TIME, (60 - current_second) * 1000);

        //设置第二天凌晨 更新日期 更新跑马灯灯当天工作
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Log.d(TAG, "tomorrow "+mDateFormat.format(calendar.getTimeInMillis()));
        mHandler.removeMessages(MSG_REFRESH_TOMORROW);
        mHandler.sendEmptyMessageDelayed(MSG_REFRESH_TOMORROW, calendar.getTimeInMillis() - current);*/
    }

    //同步本地时间，todo 与时间相关的操作需要从新规划
    public void synLocalTime() {
        mHandler.post(this::showCurrentDateTime);
    }

    //更新重启参数
    public void updateReStart() {
        Log.d(TAG, "updateReStart() called");
        /*mHandler.post(new Runnable() {
            @Override
            public void run() {
                mReStartParams.clear();
                mReStartParams.addAll(mWardViewModel.getReStartParams());
                Log.d(TAG, "mReStartParams " + mReStartParams.toString());
            }
        });*/

    }

    /**
     * @param title 标题文字，区分中文和其他，显示的时候字体大小及边距区别处理
     */
    private void setTitle(String title) {
        if (null == title) {
            title = "";
        }
        if (title.equals(mBinding.tvTitle.getText().toString())) {
            return;
        }
        boolean chineseCharacter = Common.isChineseCharacter(title);
        mBinding.tvTitle.setText(title);
        if (chineseCharacter) {
            mBinding.tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, 120);
            mBinding.tvTitle.setIncludeFontPadding(true);
        } else {
            mBinding.tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, 140);
            //去除字体边距
            mBinding.tvTitle.setIncludeFontPadding(false);
        }

    }


    public void handleInfusion(Infusion infusion,int type) {
        switch (type) {
            case 1:
                if (infusion.getEvent() == 0) {
                    addInfusion(infusion);
                } else if (infusion.getEvent() == 1) {
                    mDripAdapter.updateInfusion(infusion);
                }
                break;
            case 2:
                mDripAdapter.removeInfusion(infusion.getClientname());
                break;
        }
    }

    //添加输液信息
    private void addInfusion(Infusion infusion) {
        infusion.initilize(mSimpleDateFormat.format(System.currentTimeMillis()));
//        如果当前不是输液视图 需要切换
        if (mBinding.viewSwitchDripAndDoctor.getCurrentView().getId() != R.id.rv_drip) {
            mBinding.viewSwitchDripAndDoctor.showNext();
            mBinding.tvInfo.setText(R.string.drip_info);
        }
        //先移除之前的消息
        mHandler.removeMessages(DRIP_UPDATE);
        mDripAdapter.addInfusion(infusion);
        mHandler.sendEmptyMessageDelayed(DRIP_UPDATE, DRIP_UPDATE_INTERVAL);
    }

    //清空输液信息
    public void clearDrip() {
        if (mDripAdapter != null) {
            mDripAdapter.clear();
        }
    }



  /*  public void terminateDrip() {
        mHandler.removeMessages(DRIP_UPDATE);
    }*/

    //所有的输液信息被移除
 /*   public void stopAllDrip() {
        terminateDrip();
        if (mBinding.viewSwitchDripAndDoctor.getCurrentView().getId() == R.id.rv_drip) {
            if (mStaffAdapter != null && mStaffAdapter.getItemCount() > 0) {
                mBinding.viewSwitchDripAndDoctor.showNext();
                mBinding.tvInfo.setText(R.string.doctor_info);
            }
        }
    }*/

    /*
    *  1. 同步与服务器的跑马灯
    *  2.中途删除 添加了跑马灯
    * 先取消之前延迟的消息，再执行
    **/
    public void updateMarquee() {
//        仅仅是取消页面刚启动发的延迟信息，避免重复更新
        mHandler.removeMessages(MSG_SCHEDULE_MARQUEES);
        //中途跑马灯有变动
        mHandler.removeMessages(MSG_MARQUEE_REMOVE);
        mHandler.removeMessages(MSG_MARQUEE_ADD);
        scheduleMarquees();
    }

    /*
     * 安排跑马灯
    * 从数据库检索今天需要播放的所有活跃的跑马灯
    * 是否在播放时间段还需要进一步判断
    * 第二天凌晨需要再次调用重新安排
    * */
    private void scheduleMarquees() {
        //获取当天日期，格式为2017-05-12
        String current = mMarqueeDateFormat.format(System.currentTimeMillis());
        List<Marquee> marquees =
                WardDataBase.INSTANCE(getActivity()).marqueeDao().queryValidMarquee(current);
//        先停止跑马灯
        mBinding.marqueeView.stopMarquee();

        //对每个跑马灯安排播放和结束
        for (Marquee marquee : marquees) {
            List<MarqueeTime> marqueeTimes = WardDataBase.INSTANCE(getActivity()).marqueeTimeDao().queryMarqueeTime(marquee.getMarqueeid());
            for (MarqueeTime marqueeTime : marqueeTimes) {
                Log.d(TAG, "marquee " + marqueeTime.toString());
                arrangeMarqueeByPlayingTime(marquee,marqueeTime);
            }
        }
    }

    /**
     * 首先跑马灯的播放时间如果是多时间段，是升序排列的
     * Marquee 是一个时间段的跑马灯，多个时间段的跑马灯内容相同
     * 判断当前时间是否在播放时间内，如果在 添加到集合中
     * 为其他未来播放时间段安排跑马灯的添加和删除
     * @param
     */
    private void arrangeMarqueeByPlayingTime(Marquee marquee,MarqueeTime marqueeTime) {
        String today_str = mTimeFormat.format(System.currentTimeMillis());

        try {
            //一定要这样格式化当前时间
            Date todayTime = mTimeFormat.parse(today_str);
            Date startTime = mTimeFormat.parse(String.format("%s:00", marqueeTime.getStart()));
            Date stopTime = mTimeFormat.parse(String.format("%s:00", marqueeTime.getStop()));
            //当前时间与跑马灯的起始结束时间有三种可能，在播放时间之前，在播放时间内，过了播放时间不处理
            long marginWithStart = todayTime.getTime() - startTime.getTime();
            long marginWithStop = todayTime.getTime() - stopTime.getTime();
            if (marginWithStart < 0) {
                //还没到播放时间,安排开始和结束
                Log.d(TAG, "start in "+ -marginWithStart+"for marquee "+marquee.getMarqueeid());
                Log.d(TAG, "stop in "+ -marginWithStop+"for marquee "+marquee.getMarqueeid());
                mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_MARQUEE_ADD, marquee), -marginWithStart);
                mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_MARQUEE_REMOVE, marquee), -marginWithStop);
            } else if (marginWithStart >= 0 && marginWithStop <= 0) {
                //在播放时间内立马添加到播放集合，同时安排结束
                mBinding.marqueeView.add(marquee);
                Log.d(TAG, "start now stop in "+ -marginWithStop+" for marquee "+marquee.getMarqueeid());
                mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_MARQUEE_REMOVE, marquee), -marginWithStop);
            } else {
                Log.d(TAG, "out of play time");
            }
        } catch (ParseException e) {
            Log.e(TAG, "arrangeMarqueeByPlayingTime: fail to parse time");
            e.printStackTrace();
        }

    }

    private DripAdapter2.OnDripListener mOnDripListener=new DripAdapter2.OnDripListener() {
        @Override
        public void stopAll() {
            //输液全部结束后移除消息
            mHandler.removeMessages(DRIP_UPDATE);
            if (mBinding.viewSwitchDripAndDoctor.getCurrentView().getId() == R.id.rv_drip) {
                if (mStaffAdapter != null && mStaffAdapter.getItemCount() > 0) {
                    mBinding.viewSwitchDripAndDoctor.showNext();
                    mBinding.tvInfo.setText(R.string.doctor_info);
                }
            }
        }

        @Override
        public void finishAll() {
            mHandler.removeMessages(DRIP_UPDATE);
        }
    };
}
