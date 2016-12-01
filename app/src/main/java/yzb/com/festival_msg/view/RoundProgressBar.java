package yzb.com.festival_msg.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;

import yzb.com.festival_msg.R;

/**
 * Created by Administrator on 2016/7/22.
 */
public class RoundProgressBar extends HorizontalProgressBar {
    private int mRadius = dp2px(30);//圆形bar默认半径 30dp
    private int mMaxPaintWidth;//最大画笔宽度
    public RoundProgressBar(Context context) {
        super(context,null);
    }

    public RoundProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs,0);
    }

    public RoundProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mReachHeight = (int) (mUnReachHeight*2.5f);
        TypedArray ta = context.obtainStyledAttributes(attrs,R.styleable.RoundProgressBar);
        mRadius = (int) ta.getDimension(R.styleable.RoundProgressBar_radius,mRadius);

        ta.recycle();

        mPaint.setStyle(Paint.Style.STROKE);//画圆 不能使fill
        mPaint.setAntiAlias(true);//设置抗锯齿
        mPaint.setDither(true);//仿抖动
        mPaint.setStrokeCap(Paint.Cap.ROUND);//设置连接处为弧形
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mMaxPaintWidth = Math.max(mReachHeight,mUnReachHeight);
        //默认用户设置四个padding一致
        int viewWidth = mRadius*2+mMaxPaintWidth+getPaddingLeft()+getPaddingRight();
        //测量出高度和宽度
        int width = resolveSize(viewWidth,widthMeasureSpec);
        int height = resolveSize(viewWidth,heightMeasureSpec);

        int realWidth = Math.min(width,height);
        mRadius = (realWidth - getPaddingLeft() - getPaddingRight() - mMaxPaintWidth)/2;
        setMeasuredDimension(realWidth,realWidth);//绘制整体view
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        String text = getProgress()+"%";
        float textWidth = mPaint.measureText(text);
        //Ascent： 字符顶部到baseLine的距离。Descent： 字符底部到baseLine的距离。
        float textHeight = (mPaint.descent()+mPaint.ascent())/2;

        canvas.save();
        canvas.translate(getPaddingLeft()+mMaxPaintWidth/2,getPaddingTop()+mMaxPaintWidth/2);
        mPaint.setStyle(Paint.Style.STROKE);
        //绘制没有进行的进度条 是一个圆
        mPaint.setColor(mUnReachColor);
        mPaint.setStrokeWidth(mUnReachHeight);
        canvas.drawCircle(mRadius,mRadius,mRadius,mPaint);
        //绘制已进行的进度条，在上边圆上绘制弧线
        mPaint.setColor(mReachColor);
        mPaint.setStrokeWidth(mReachHeight);
        float sweepAngle = getProgress()*1.0f/getMax()*360;//已完成进度的弧度
        canvas.drawArc(new RectF(0,0,mRadius*2,mRadius*2),0,sweepAngle,false,mPaint);
        //绘制进度百分比
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mTextColor);
        canvas.drawText(text,mRadius-textWidth/2,mRadius-textHeight,mPaint);
        canvas.restore();

    }
}
