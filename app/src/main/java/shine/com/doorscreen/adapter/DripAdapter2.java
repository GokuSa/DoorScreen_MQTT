package shine.com.doorscreen.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import shine.com.doorscreen.R;
import shine.com.doorscreen.databinding.ItemDripBinding;
import shine.com.doorscreen.mqtt.bean.Infusion;

/**
 * Created by Administrator on 2016/8/16.
 * 输液情况适配器
 */
public class DripAdapter2 extends RecyclerView.Adapter<DripAdapter2.DripHolder> {
    private static final String TAG = "DripAdapter";
    private CopyOnWriteArrayList<Infusion> mWarningList;
    private Context mContext;
    private final OnDripListener mOnDripListener;

    public DripAdapter2(Context context, OnDripListener onDripListener) {
        mContext = context;
        mOnDripListener = onDripListener;
        mWarningList = new CopyOnWriteArrayList<>();
    }

  /*  public void onDateChange(List<DripInfo.Infusionwarnings> warningList) {
        if (warningList != null && warningList.size() > 0) {
            mWarningList.clear();
            mWarningList.addAll(warningList);
            notifyDataSetChanged();
        }
    }*/

    public void addInfusion(Infusion infusion) {
        mWarningList.add(infusion);
        notifyItemRangeChanged(0, getItemCount());
    }

    /**
     * 更新输液为即将完成状态
     * 因为界面需要显示开始时间 否则使用set集合 可以直接替换
     *
     * @param infusion
     */
    public void updateInfusion(Infusion infusion) {
        for (int i = 0; i < mWarningList.size(); i++) {
            if (Objects.equals(mWarningList.get(i).getClientmac(), infusion.getClientmac())) {
                mWarningList.get(i).setEvent(infusion.getEvent());
                notifyItemChanged(i);
            }
        }

    }

    public void clear() {
        mWarningList.clear();
        notifyDataSetChanged();
    }

    public void removeInfusion(@NonNull String clientName) {
        for (int i = 0; i < mWarningList.size(); i++) {
            if (clientName.equals(mWarningList.get(i).getClientname())) {
                mWarningList.remove(mWarningList.get(i));
                notifyItemRemoved(i);
            }
        }
        if (mWarningList.size() == 0 && mOnDripListener != null) {
            mOnDripListener.stopAll();
           /* LocalBroadcastManager.getInstance(mContext).
                    sendBroadcast(MainActivity.newIntent(MainActivity.STOP_ALL_DRIP, ""));*/

        }

    }

    public void update() {
        int count = 0;
        for (int i = 0; i < mWarningList.size(); i++) {
            Infusion infusion = mWarningList.get(i);
            if (infusion.getLeft() > 0) {
                infusion.setCurrentNumber();
                infusion.countDown();
                notifyItemChanged(i);
            } else {
                count++;
            }
        }
        //表示输液全部结束
        if (count == mWarningList.size() && mOnDripListener != null) {
            mOnDripListener.finishAll();
//            LocalBroadcastManager.getInstance(mContext).sendBroadcast(MainActivity.newIntent(MainActivity.DRIP_DONE, ""));
        }
    }

    @Override
    public DripHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemDripBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_drip, parent, false);
        return new DripHolder(binding, mContext);
    }

    @Override
    public void onBindViewHolder(DripHolder holder, int position) {
        Infusion infusion = mWarningList.get(position);
        holder.bind(infusion);
    }

    @Override
    public int getItemCount() {
        return mWarningList.size();
    }

    static class DripHolder extends RecyclerView.ViewHolder implements ViewSwitcher.ViewFactory {
        private Context context;
        private final ItemDripBinding mDripBinding;

        DripHolder(ItemDripBinding binding, Context context) {
            super(binding.getRoot());
            this.context = context;
            mDripBinding = binding;
            mDripBinding.textSwitchLeft.setFactory(this);
            mDripBinding.textSwitchRight.setFactory(this);
            mDripBinding.textSwitchMiddle.setFactory(this);
        }

        public void bind(Infusion infusion) {
            //0 表示新加输液信息  1表示即将结束状态
            boolean isSelected = infusion.getEvent()==1;
           /* if (infusion.getLeft() <= 3) {
                isSelected = true;
            }*/
            mDripBinding.tvDripTitle.setSelected(isSelected);
            mDripBinding.llTimeBoard.setSelected(isSelected);
            mDripBinding.tvTimeLeft.setSelected(isSelected);
            mDripBinding.tvDripTitle.setText(infusion.getClientname());
            mDripBinding.tvDripInfo.setText(
                    String.format(Locale.CHINA, "开始时间\n%s\n%s滴/分钟", infusion.getBegin(), infusion.getSpeed()));
            int dripPackageResourceId = infusion.getCurrentDripPackage();
            if (dripPackageResourceId != -1) {
                mDripBinding.ivDripPackage.setImageDrawable(context.getResources().getDrawable(dripPackageResourceId));
            }

            if (infusion.getCurrent_bai() == infusion.getNext_bai()) {
                mDripBinding.textSwitchLeft.setCurrentText(String.valueOf(infusion.getCurrent_bai()));
            } else {
                mDripBinding.textSwitchLeft.setText(String.valueOf(infusion.getNext_bai()));
            }
            if (infusion.getCurrent_shi() == infusion.getNext_shi()) {
                mDripBinding.textSwitchMiddle.setCurrentText(String.valueOf(infusion.getCurrent_shi()));
            } else {
                mDripBinding.textSwitchMiddle.setText(String.valueOf(infusion.getNext_shi()));
            }
            //个位数的动画出现跳跃，显示的是x，出去的是X+1，进来的是x-1，所以先设置当前量，再切换
            mDripBinding.textSwitchRight.setCurrentText(String.valueOf(infusion.getCurrent_ge()));
            if (infusion.getLeft() > 0) {
                mDripBinding.textSwitchRight.setText(String.valueOf(infusion.getNext_ge()));
                if (mDripBinding.ivWaterDrip.getAnimation() == null) {
                    mDripBinding.ivWaterDrip.setAnimation(getAnimation(1500));
                }
            } else {
                mDripBinding.textSwitchRight.setText("0");
                mDripBinding.ivWaterDrip.clearAnimation();
                mDripBinding.ivWaterDrip.setVisibility(View.INVISIBLE);
            }

        }

        TranslateAnimation getAnimation(int duration) {
            TranslateAnimation animation = new TranslateAnimation(0, 0, 0, 80);
            animation.setDuration(duration);
            animation.setInterpolator(new AccelerateInterpolator());
            animation.setFillAfter(false);
            animation.setRepeatCount(-1);
            return animation;
        }

        @Override
        public View makeView() {
            TextView textView = new TextView(context);
            textView.setLayoutParams(new TextSwitcher.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(22f);
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            textView.setTextColor(context.getResources().getColor(android.R.color.white));
            return textView;
        }
    }

    public interface OnDripListener {
        void stopAll();

        void finishAll();
    }


}
