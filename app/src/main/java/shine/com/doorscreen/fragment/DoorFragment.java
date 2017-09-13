package shine.com.doorscreen.fragment;


import android.arch.lifecycle.LifecycleFragment;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import shine.com.doorscreen.R;
import shine.com.doorscreen.activity.MainActivity;
import shine.com.doorscreen.adapter.DripAdapter;
import shine.com.doorscreen.adapter.InsetDecoration;
import shine.com.doorscreen.adapter.PatientAdapter2;
import shine.com.doorscreen.adapter.RecycleViewDivider;
import shine.com.doorscreen.adapter.StaffAdapter;
import shine.com.doorscreen.customview.MarqueeView;
import shine.com.doorscreen.database.DoorScreenDataBase;
import shine.com.doorscreen.databinding.FragmentDoor2Binding;
import shine.com.doorscreen.entity.DripInfo;
import shine.com.doorscreen.mqtt.bean.Patient;
import shine.com.doorscreen.mqtt.bean.ReStart;
import shine.com.doorscreen.mqtt.bean.Staff;
import shine.com.doorscreen.mqtt.bean.Ward;
import shine.com.doorscreen.service.DoorService;
import shine.com.doorscreen.util.Common;
import shine.com.doorscreen.viewmodel.WardViewModel;

import static shine.com.doorscreen.activity.MainActivity.CLOSE;

/**
 * A simple {@link } subclass.
 * 门口屏主页面 显示输液进制和医生信息，呼叫信息
 */
public class DoorFragment extends LifecycleFragment {
    private static final String TAG = "DoorFragment2";
    public static final int DRIP_UPDATE_INTERVAL = 60 * 1000;
    private static final int DRIP_DOCTOR_SWITCH = 779;
    private static final int DRIP_DOCTOR_SWITCH_INTERVAL = 10 * 1000;
    public static final int TIME_UPDATE_INTERVAL = 60 * 1000;
    private static final int KEEP_TIME = 0;
    private static final int DRIP_UPDATE = 1;
    private static final int UPDATE_MARQUEE = 6;

    /**
     * 格式化当前时间，用于标题的时间和输液开始时间格式化
     */
    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);
    private DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL, Locale.CHINA);

    private DripAdapter mDripAdapter;
    private PatientAdapter2 mPatientAdapter;
    private boolean isVisible = false;
    private boolean isPrepared = false;
    private FragmentDoor2Binding mBinding;
    private StaffAdapter mStaffAdapter;


    /**
     * 当前跑马灯信息
     */
    List<String> mMarquees = new ArrayList<>();
    private List<ReStart> mReStartParams=new ArrayList<>();

    private WardViewModel mWardViewModel;

    @SuppressWarnings("handlerleak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case KEEP_TIME:
                    String date_week = dateFormat.format(System.currentTimeMillis());
                    //更新日期
                    mBinding.tvDate.setText(date_week.substring(0, date_week.length() - 3));
                    //更新星期
                    mBinding.tvWeekday.setText(date_week.substring(date_week.length() - 3));
                    //更新时间
                    String time = mSimpleDateFormat.format(System.currentTimeMillis());
                    mBinding.tvTime.setText(time);
                    //判断是否重启
                    scheduleReStart(time);
                    //一分钟更新一次
                    sendEmptyMessageDelayed(KEEP_TIME, TIME_UPDATE_INTERVAL);
                    break;
                case DRIP_UPDATE:
                    mDripAdapter.update();
                    sendEmptyMessageDelayed(DRIP_UPDATE, DRIP_UPDATE_INTERVAL);
                    break;
                case DRIP_DOCTOR_SWITCH:
                    /*if (mViewSwitchDripAndDoctor.getCurrentView().getId() != R.id.rv_drip) {
                        mViewSwitchDripAndDoctor.showNext();
                        mTvInfo.setText(R.string.drip_info);
                    }*/
                    break;

            }
        }
    };
    private MarqueeView mMarqueeView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView() called ");
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_door2, container, false);
        mStaffAdapter = new StaffAdapter();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1, GridLayoutManager.HORIZONTAL, false);
        mBinding.rvDoctor.addItemDecoration(new InsetDecoration(getContext()));
        mBinding.rvDoctor.setLayoutManager(gridLayoutManager);
        mBinding.rvDoctor.setAdapter(mStaffAdapter);

        mPatientAdapter = new PatientAdapter2();
        mBinding.rvPatientInfo.setAdapter(mPatientAdapter);
        mBinding.rvPatientInfo.addItemDecoration(new RecycleViewDivider(getActivity(), LinearLayoutManager.VERTICAL, R.drawable.divider_vertical));

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated() called with");
        mMarqueeView = (MarqueeView) view.findViewById(R.id.marqueeView);
        mBinding.tvTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity) getActivity();
                activity.handleCallTransfer("",true);
            }
        });
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
                }else{
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
                if (mBinding.viewSwitchDripAndDoctor.getCurrentView().getId() != R.id.rv_doctor) {
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
        mReStartParams.clear();
        mReStartParams.addAll(mWardViewModel.getReStartParams());
        //显示当前时间，
        showCurrentDateTime();

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
     * @param time
     */
    private void scheduleReStart(String time) {
        if (mReStartParams.size() > 0) {
            //获取今天星期几,从0到6
            int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)-1;
            //防止越界
            if (day < mReStartParams.size()) {
                //获取今天的重启参数
                ReStart reStart = mReStartParams.get(day);
                //如果重启
                if (reStart.getReboot() == 1&&time.equals(reStart.getRebootTime())) {
                    //关机60s后重启
                    Common.open(System.currentTimeMillis() + 60 * 1000);
                    DoorService.startService(getActivity(), CLOSE, "");
                }
            }
        }
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
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

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
            mMarqueeView.terminate();

        }
    }

    private void onVisible() {
        if (isPrepared) {
            if (mMarquees.size() > 0) {
                mMarqueeView.setContent(mMarquees);
            }
        }
    }


    /**
     * 为了在整点同步会重新发送延迟消息
     * 初始化标题
     * 主要是当前日期
     * 并触发一分钟刷新一次时间
     */
    private void showCurrentDateTime() {
        Log.d(TAG, "showCurrentDateTime() called");
        //格式化当前时间为 年 月 日  星期几，比如2016年8月10日 星期三
        Calendar calendar = Calendar.getInstance();
        long current = calendar.getTimeInMillis();
        String date_week = dateFormat.format(current);
        //android 这个格式化方法中间没有空格，不能split
        mBinding.tvDate.setText(date_week.substring(0, date_week.length() - 3));
        mBinding.tvWeekday.setText(date_week.substring(date_week.length() - 3));
        String time = mSimpleDateFormat.format(current);
        mBinding.tvTime.setText(time);
        int current_second = calendar.get(Calendar.SECOND);
        mHandler.removeMessages(KEEP_TIME);
        //整点更新时钟
        mHandler.sendEmptyMessageDelayed(KEEP_TIME, (60 - current_second) * 1000);

    }

    //同步本地时间
    public void synLocalTime() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                showCurrentDateTime();
            }
        });
    }

    //更新重启参数
    public void updateReStart() {
        Log.d(TAG, "updateReStart() called");
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mReStartParams.clear();
                mReStartParams.addAll(mWardViewModel.getReStartParams());
                Log.d(TAG,"mReStartParams "+ mReStartParams.toString());
            }
        });

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


    /**
     * 初始化输液信息
     */
    private void initializeDripInfo() {
        mDripAdapter = new DripAdapter(getActivity());
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1, GridLayoutManager.HORIZONTAL, false);
       /* mRvDrip.addItemDecoration(new InsetDecoration(getContext()));
        mRvDrip.setLayoutManager(gridLayoutManager);
        mRvDrip.setAdapter(mDripAdapter);
        //数据更新时recyclerView 有默认动画，导致闪屏,所以取消掉
        ((SimpleItemAnimator) mRvDrip.getItemAnimator()).setSupportsChangeAnimations(false);*/

    }

    /**
     * 开始输液信息
     * 必须切换到可见
     */
    public void startDrip(DripInfo dripInfo) {
        if (dripInfo != null) {
            List<DripInfo.Infusionwarnings> dripInfos = dripInfo.getInfusionwarnings();
            if (dripInfos != null && dripInfos.size() > 0) {
                Log.d(TAG, "update dripInfo:" + dripInfos);
                for (DripInfo.Infusionwarnings drip : dripInfos) {
                    drip.initilize(mSimpleDateFormat.format(drip.getStart()));
                    //此时剩余时间就是总时间，涉及输液袋的状态
                    drip.setTotal(drip.getLeft());
                }
                /*if (mViewSwitchDripAndDoctor.getCurrentView().getId() != R.id.rv_drip) {
                    mViewSwitchDripAndDoctor.showNext();
                    mTvInfo.setText(R.string.drip_info);
                }*/
                //先移除之前的消息
                mHandler.removeMessages(DRIP_UPDATE);
                mDripAdapter.onDateChange(dripInfos);
                mHandler.sendEmptyMessageDelayed(DRIP_UPDATE, DRIP_UPDATE_INTERVAL);
            } else {
                clearDrip();
                Log.d(TAG, "没有输液信息 显示医生信息");
                if (mStaffAdapter != null && mStaffAdapter.getItemCount() > 0) {
                   /* if (mViewSwitchDripAndDoctor.getCurrentView().getId() != R.id.rv_doctor) {
                        Log.d(TAG, "change to doctor view");
                        mViewSwitchDripAndDoctor.showNext();
                        mTvInfo.setText(R.string.doctor_info);
                    }*/
                }
            }
        } else {
            clearDrip();
        }
    }

    //清空输液信息
    public void clearDrip() {
        if (mDripAdapter != null) {
            mDripAdapter.clear();
        }
    }

    /**
     * 停止输液 移除对应床号的输液提醒
     *
     * @param bedno
     */
    public void stopDrip(String bedno) {
        if (!TextUtils.isEmpty(bedno)) {
            mDripAdapter.onDateRemoved(bedno);
        }
    }

    //输液全部结束后移除消息
    public void terminateDrip() {
        mHandler.removeMessages(DRIP_UPDATE);
    }

    //所有的输液信息被移除
    public void stopAllDrip() {
        terminateDrip();
        /*if (mViewSwitchDripAndDoctor.getCurrentView().getId() == R.id.rv_drip) {
            if (mDoctorAdapter != null && mDoctorAdapter.getItemCount() > 0) {
                mViewSwitchDripAndDoctor.showNext();
                mTvInfo.setText(R.string.doctor_info);
            }
        }*/
    }


    public void updateMarquee(String paramMarquee) {
        List<String> content = DoorScreenDataBase.getInstance(getActivity()).queryMarquee(paramMarquee);
        mMarquees.clear();
        mMarquees.addAll(content);
        if (isVisible && mMarquees.size() > 0) {
            mMarqueeView.setContent(content);
        }
    }

    /**
     * 停止跑马灯
     *
     * @param
     */
    public void stopMarquee() {
        if (isVisible) {
            mMarqueeView.terminate();
        }
    }

    public void moveToCallingPatient(int i) {
//        mRvPatients.scrollToPosition(i);
    }


}
