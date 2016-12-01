package yzb.com.festival_msg.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ProgressBar;

import yzb.com.festival_msg.R;

/**
 * Created by Administrator on 2016/7/22.
 */
public class HorizontalProgressBar extends ProgressBar{
    //设置属性默认值
    private static final int DEFAULT_TEXT_SIZE = 10;//单位sp
    private static final int DEFAULT_TEXT_COLOR = 0XFFFC00D1;
    private static final int DEFAULT_UNREACH_COLOR = 0XFFD3D6DA;
    private static final int DEFAULT_UNREACH_HEIGHT = 2;//单位dp
    private static final int DEFAULT_REACH_COLOR = DEFAULT_TEXT_COLOR;
    private static final int DEFAULT_REACH_HEIGHT = 2;//单位dp
    private static final int DEFAULT_TEXT_OFFSET = 10;//单位dp
    //声明自定义属性
    protected int mTextSize = sp2px(DEFAULT_TEXT_SIZE);
    protected int mTextColor = DEFAULT_TEXT_COLOR;
    protected int mUnReachColor = DEFAULT_UNREACH_COLOR;
    protected int mUnReachHeight = dp2px(DEFAULT_UNREACH_HEIGHT);
    protected int mReachColor = DEFAULT_REACH_COLOR;
    protected int mReachHeight = dp2px(DEFAULT_REACH_HEIGHT);
    protected int mTextOffset = dp2px(DEFAULT_TEXT_OFFSET);//两边进度条的间隙（间隙设置进度字）

    protected Paint mPaint = new Paint();

    private int mRealWidth;//当前控件宽度-panding

    //用户创建对象调用一个参数构造方法
    public HorizontalProgressBar(Context context) {
        super(context,null);//去调用两个参数的构造方法
    }

    public HorizontalProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs,0);//调用三个参数的构造方法
    }

    public HorizontalProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //进行属性获取
        obtainStyledAttrs(attrs);
    }

    /**
     * 获取自定义属性
     * @param attrs
     */
    private void obtainStyledAttrs(AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.HorizontalProgressBar);

        mTextSize = (int) ta.getDimension(R.styleable.HorizontalProgressBar_progress_text_size, mTextSize);
        mTextColor = (int) ta.getColor(R.styleable.HorizontalProgressBar_progress_text_color, mTextColor);
        mTextOffset = (int) ta.getDimension(R.styleable.HorizontalProgressBar_progress_text_offset, mTextOffset);
        mReachColor = (int) ta.getColor(R.styleable.HorizontalProgressBar_progress_reach_color,mReachColor);
        mReachHeight = (int)ta.getDimension(R.styleable.HorizontalProgressBar_progress_reach_height,mReachHeight);
        mUnReachColor = (int) ta.getColor(R.styleable.HorizontalProgressBar_progress_unreach_color,mUnReachColor);
        mReachHeight = (int) ta.getDimension(R.styleable.HorizontalProgressBar_progress_unreach_height,mUnReachHeight);

        ta.recycle();

        mPaint.setTextSize(mTextSize);
    }

    /**
     * 控件测量
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //水平进度条 宽度用户一定要给定.所以宽度不做测量
//        int widthMode = MeasureSpec.getMode(widthMeasureSpec);//宽度模式
        int widthValue = MeasureSpec.getSize(widthMeasureSpec);
        //测量高度
        int heightValue = measureHeight(heightMeasureSpec);
        setMeasuredDimension(widthValue,heightValue);//设置view的宽和高
        mRealWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();//绘制区域的真正宽度
    }

    /**
     * 绘制view
     * @param canvas
     */
    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.translate(getPaddingLeft(),getHeight()/2);//移动坐标 到最左边中心
        boolean noNeedUnReach = false;//当左边reach+textsiz>=整个view的宽度，不需要绘制右边的unreach
        String text = getProgress()+"%";//进度条上文本
        int textWidth = (int) mPaint.measureText(text);//文本的宽度
        float radio = getProgress()*1.0f/getMax();//当前进度转为float/最大进度
        float progressX = radio*mRealWidth;
        if(progressX+textWidth > mRealWidth){
            progressX = mRealWidth - textWidth;//如果不减掉 进度条上的文本就看不到，给文本留出一个 textwidth
            noNeedUnReach = true;
        }
        float endX = progressX - mTextOffset/2;//开始绘制的起点（横向）
        /*
        * 三部分绘制
        * ------第一部分--------第二部分10%------第三部分-------
        * */
        //绘制已完成的进度条
        if(endX > 0){//当有进度的时候 开始绘制 endx>0 表示有进度
            //绘制线条
            mPaint.setColor(mReachColor);
            mPaint.setStrokeWidth(mReachHeight);//线条高度
            canvas.drawLine(0,0,endX,0,mPaint);

        }
        //绘制进度条上的文本
        mPaint.setColor(mTextColor);
        int y = (int) (-(mPaint.descent()+mPaint.ascent())/2);//绘制text 在veiew的中心，所以y在中心点
        canvas.drawText(text,progressX,y,mPaint);
        //绘制未完成的进度条
        if(!noNeedUnReach){
            float startX = progressX+mTextOffset/2+textWidth;//未完成进度条的起点
            mPaint.setColor(mUnReachColor);
            mPaint.setStrokeWidth(mUnReachHeight);
            canvas.drawLine(startX,0,mRealWidth,0,mPaint);
        }

        canvas.restore();//恢复

    }

    /**
     * 根据模式测量高度
     * @param heightMeasureSpec
     * @return
     */
    private int measureHeight(int heightMeasureSpec) {
        int result = 0;
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightValue = MeasureSpec.getSize(heightMeasureSpec);
        if (heightMode == MeasureSpec.EXACTLY){//如果用户给了个精确值或machPrent
            result = heightValue;
        }else {
            int textHeight = (int) (mPaint.descent()-mPaint.ascent());
            result = getPaddingTop()+getPaddingBottom()//上边距+下边距
                    +Math.max(Math.max(mReachHeight,mUnReachHeight),Math.abs(textHeight));
            if(heightMode == MeasureSpec.AT_MOST){//测量值不能超过给定的size
                result = Math.min(result,heightValue);
            }
        }
        return  result;
    }

    protected int dp2px(int dpValue){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dpValue,getResources().getDisplayMetrics());
    }

    protected int sp2px(int spValue){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,spValue,getResources().getDisplayMetrics());
    }
}
