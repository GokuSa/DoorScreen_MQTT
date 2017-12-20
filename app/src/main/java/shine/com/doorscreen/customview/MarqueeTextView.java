package shine.com.doorscreen.customview;

import android.content.Context;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.OverScroller;

import java.util.concurrent.CopyOnWriteArrayList;

import shine.com.doorscreen.mqtt.bean.Marquee;
import shine.com.doorscreen.util.Common;

/**
 * author:
 * 时间:2017/9/28
 * qq:1220289215
 * 类描述：
 */

public class MarqueeTextView extends AppCompatTextView {
    private static final String TAG = "MarqueeTextView";

    private OverScroller mOverScroller;
    private boolean isStop=true;
    private TextPaint mTextPaint;
    private CopyOnWriteArrayList<Marquee> mMarquees = new CopyOnWriteArrayList<>();
    //跑马灯索引
    private int mIndex=0;
    //动画时长 与文字长度有关
    private int mDuration = 5000;
    private int mStartX=1200;
    private int mScreenWidth=0;

    public MarqueeTextView(Context context) {
        super(context);
        init(context);
    }

    public MarqueeTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);

    }

    public MarqueeTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);

    }

    private void init(Context context) {
        mOverScroller=new OverScroller(context,new LinearInterpolator());
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        float textSize = getTextSize();
        mTextPaint.setTextSize(textSize);
        mScreenWidth = Common.getScreenWidth(context);
    }


    //开启跑马灯
    private void startMarquee() {
        stopScroll();
        mIndex=0;
        startScroll();
    }

    public void stopMarquee() {
        stopScroll();
        setText("");
        mMarquees.clear();
    }

    public void add(Marquee marquee) {
        mMarquees.addIfAbsent(marquee);
        startMarquee();
    }

    public void delete(Marquee marquee) {
        mMarquees.remove(marquee);
        if (mMarquees.size() > 0) {
            startMarquee();
        }else{
            stopMarquee();
        }
    }

    private void startScroll() {
        if (mMarquees.size()==0) {
            return;
        }
        isStop=false;
        mIndex = ++mIndex % mMarquees.size();
        String content = mMarquees.get(mIndex).getMessage();
        setText(content);
        //考虑左右边距，否则显示不全
        int measureText = (int) mTextPaint.measureText(content)+getPaddingLeft()+getPaddingRight();
//        Log.d(TAG, "content of marquee "+content+"length "+measureText);
        //设置布局参数，否则文字超过父控件文字不能显示
        FrameLayout.LayoutParams layoutParams=null;
        if (measureText > mScreenWidth) {
            layoutParams = new FrameLayout.LayoutParams(measureText, ViewGroup.LayoutParams.WRAP_CONTENT);
        }else{
            layoutParams=new FrameLayout.LayoutParams(mScreenWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        layoutParams.gravity=Gravity.CENTER_VERTICAL;
        setLayoutParams(layoutParams);
        mDuration=(mStartX+measureText)*6;
        mOverScroller.startScroll(-mStartX, 0, mStartX+measureText, 0, mDuration);
        invalidate();
    }

    private void stopScroll() {
        isStop=true;
        mOverScroller.forceFinished(true);
    }
    @Override
    public void computeScroll() {
        super.computeScroll();
        //每次设置setText都会调用此方法，防止无线循环
        if (isStop) {
            return;
        }
        if (mOverScroller.computeScrollOffset()) {
            scrollTo(mOverScroller.getCurrX(), 0);
            invalidate();
        }else{
            //一个播放结果开始下一个
            isStop=true;
            startScroll();
        }
    }
}
