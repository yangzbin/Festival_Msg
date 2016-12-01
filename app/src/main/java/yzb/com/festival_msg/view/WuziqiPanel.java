package yzb.com.festival_msg.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import yzb.com.festival_msg.R;
import yzb.com.festival_msg.utils.ToastUtil;

/**
 * Created by Administrator on 2016/7/27.
 */
public class WuziqiPanel extends View {
    /**
     * 整个view(panel)边长
     */
    private int mPanelWidth;
    /**
     * 行高
     */
    private float mLineHeight;
    /**
     * 最大行数
     */
    private int MAX_LINE = 10;
    private int MAX_COUNT_IN_LINE = 5;//最多五个连线

    private Paint mPaint = new Paint();//必须在这里初始化 要不然空指针

    private Bitmap mWhitePiece;//黑色棋子
    private Bitmap mBlackPiece;//白色棋子
    private float mPieceWOfLineH = 3 * 1.0f / 4;//棋子的大小占行高的3/4
    //白子先手或者当前轮到白子下
    private boolean isWhite = true;
    //  用来存放白子和黑子的坐标；
    private ArrayList<Point> mWhiteArray = new ArrayList<>();
    private ArrayList<Point> mBlackArray = new ArrayList<>();

    private boolean mIsGameOver;//是否游戏结束
    private boolean mIsWhiteWin;//是否白子赢
    private boolean humanComputer = false;//默认人人对战


    public WuziqiPanel(Context context, AttributeSet attrs) {
        super(context,attrs);
        initPaint();
    }

    /**
     * 初始化paint
     */
    private void initPaint() {
        mPaint.setColor(0x88000000);//半透明黑色
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);//设置画笔为空心
        mPaint.setStrokeWidth(dp2px(1));

        mWhitePiece = BitmapFactory.decodeResource(getResources(), R.drawable.stone_w2);
        mBlackPiece = BitmapFactory.decodeResource(getResources(),R.drawable.stone_b1);

    }

    /**
     * 测量
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthModel = MeasureSpec.getMode(widthMeasureSpec);//得到模式
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);//得到尺寸

        int heightModel = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        //绘制一个正方形棋盘 取高度和宽度的最小值
        int width = Math.min(widthSize,heightSize);
        /**
         * mode共有三种情况，取值分别为MeasureSpec.UNSPECIFIED, MeasureSpec.EXACTLY, MeasureSpec.AT_MOST。
         MeasureSpec.EXACTLY是精确尺寸，当我们将控件的layout_width或layout_height指定为具体数值时如andorid:layout_width="50dip"，
         或者为FILL_PARENT是，都是控件大小已经确定的情况，都是精确尺寸。
         MeasureSpec.AT_MOST是最大尺寸，当控件的layout_width或layout_height指定为WRAP_CONTENT时，
         控件大小一般随着控件的子空间或内容进行变化，此时控件尺寸只要不超过父控件允许的最大尺寸即可。
         因此，此时的mode是AT_MOST，size给出了父控件允许的最大尺寸。
         MeasureSpec.UNSPECIFIED是未指定尺寸，这种情况不多，一般都是父控件是AdapterView，通过measure方法传入的模式。
         */
        if(widthModel == MeasureSpec.UNSPECIFIED){
            width = heightSize;//正方形边长由高度决定
        }else if (heightModel == MeasureSpec.UNSPECIFIED){
            width = widthSize;
        }
        //设置宽高
        setMeasuredDimension(width,width);
    }
    //当尺寸发生变化时 调用
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mPanelWidth = w;
        mLineHeight = mPanelWidth*1.0f/MAX_LINE;

        int picesWidth = (int) (mLineHeight * mPieceWOfLineH);
        //棋子尺寸根据控件大小变换
        mWhitePiece = Bitmap.createScaledBitmap(mWhitePiece,picesWidth,picesWidth,false);
        mBlackPiece = Bitmap.createScaledBitmap(mBlackPiece,picesWidth,picesWidth,false);
    }
    //触摸棋盘事件
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(mIsGameOver){
            return false;//不再落子
        }
        int action = event.getAction();
        if(action == MotionEvent.ACTION_UP){//如果是MotionEvent.ACTION_UP，那么如果用户滚动屏幕，就会落子，不合适
            int x = (int) event.getX();
            int y = (int) event.getY();
//            Point point = new Point(x,y);//仅存x,y坐标是不行的 当用户点击不在这个点上 也可以下棋，会引起黑白子重复
            Point point = getValidPoint(x,y);
            //如果该点已经有棋子了
            if(mWhiteArray.contains(point) || mBlackArray.contains(point)){
                return false;
            }
            if(!humanComputer){
                if(isWhite){
                    mWhiteArray.add(point);
                }else {
                    mBlackArray.add(point);
                }
                isWhite = !isWhite;//改变当前黑白先后状态 人人对战时才改变状态
            }else {
                //人机交互逻辑
                if(isWhite){//白棋先手
                    mWhiteArray.add(point);
                    Point computerP = getComputerPoint(point.x,point.y,mWhiteArray);
                    mBlackArray.add(computerP);//机器生成
                }
//                else {
//                    mBlackArray.add(point);
//                    mWhiteArray.add(computerP);
//                }
            }
            invalidate();//重绘
        }
        return true;
    }

    /**
     * 简单算法 随机在落子点周围生成一个点
     * @param x
     * @param y
     * @return
     */
    private Point getComputerPoint(int x, int y,List<Point> points) {//x,y已经落子
            int[][] temp = {{x - 1, y - 1}, {x, y - 1}, {x + 1, y - 1},
                    {x - 1, y}, {x + 1, y}, {x - 1, y + 1}, {x, y + 1},
                    {x + 1, y + 1}};
            ArrayList<Point> pointTemp = new ArrayList<>();
            for(int i=0;i<temp.length;i++){
                int tx = temp[i][0];
                int ty = temp[i][1];
                Point tempP = new Point(tx,ty);
                if(tempP.x>0 && tempP.x<10 && tempP.y>0 && tempP.y<10){//保证点都在棋盘内
                    if(!mWhiteArray.contains(tempP)&&!mBlackArray.contains(tempP)){//没落子的点
                        pointTemp.add(tempP);
                    }
                }

            }
            int num = (int) (Math.random() * pointTemp.size());
            return pointTemp.get(num);

    }

    //根据坐标点取得合理落子区域
    private Point getValidPoint(int x, int y) {
        //如用户点击的是（0,0）或（0,0）附近的坐标（小与行高的1/2）
        //x/mLineHeight取整后一定是（0，0）
        return new Point((int) (x/mLineHeight),(int) (y/mLineHeight));
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBoard(canvas);
        drawPieces(canvas);
        checkGameOver();
    }

    /**
     * 检查游戏结束
     */
    private void checkGameOver() {
        boolean whiteWin = checkFiveInLine(mWhiteArray,MAX_COUNT_IN_LINE);
        boolean blackWin = checkFiveInLine(mBlackArray,MAX_COUNT_IN_LINE);
        if(whiteWin || blackWin){
            mIsGameOver = true;
            mIsWhiteWin = whiteWin;
            String tosMsg = mIsWhiteWin?"白棋胜利":"黑棋胜利";
            ToastUtil.showShortToast(getContext(),tosMsg);
        }
    }

    /**
     * 判断是否五子连线，横线 竖线 斜线
     * @param points
     */
    private boolean checkFiveInLine(List<Point> points,int max_count) {
        for (Point point:points){
            int x = point.x;
            int y = point.y;

            boolean win = checkHorizontal(x,y,points,max_count);
            if(win)return true;
            win = checkVertical(x,y,points,max_count);
            if(win)return true;
            win = checkLeftDiagonal(x,y,points,max_count);
            if(win)return true;
            win = checkRightDiagonal(x,y,points,max_count);
            if(win)return true;
        }

        return false;
    }
    /**
     * 判断是否五子连线，横线 竖线 斜线
     * @param points
     */
    private boolean checkThreeInLine(List<Point> points,int max_count) {
        for (Point point:points){
            int x = point.x;
            int y = point.y;

            boolean win = checkHorizontal(x,y,points,max_count);
            if(win)return true;
            win = checkVertical(x,y,points,max_count);
            if(win)return true;
            win = checkLeftDiagonal(x,y,points,max_count);
            if(win)return true;
            win = checkRightDiagonal(x,y,points,max_count);
            if(win)return true;
        }

        return false;
    }

    /**
     * 判断x，y的位置横向是否有相邻的五个一致。
     * @param x
     * @param y
     * @param points
     * @return
     */
    private boolean checkHorizontal(int x, int y, List<Point> points,int max_count) {
        int count = 1;
        //判断左边是否有相同的棋子
        for(int i=1;i<max_count;i++){
            if(points.contains(new Point(x-i,y))){
                count++;
            }else{
                break;
            }
        }
        if(count == max_count){
            return true;
        }
        //判断右边
        for(int i=1;i<max_count;i++){
            if(points.contains(new Point(x+i,y))){
                count++;
            }else{
                break;
            }
        }
        if(count == max_count){
            return true;
        }
        return false;
    }
    /**
     * 判断x，y的位置纵向是否有相邻的五个一致。
     * @param x
     * @param y
     * @param points
     * @return
     */
    private boolean checkVertical(int x, int y, List<Point> points,int max_count) {
        int count = 1;
        //判断上边是否有相同的棋子
        for(int i=1;i<max_count;i++){
            if(points.contains(new Point(x,y-i))){
                count++;
            }else{
                break;
            }
        }
        if(count == max_count){
            return true;
        }
        //判断下边
        for(int i=1;i<max_count;i++){
            if(points.contains(new Point(x,y+i))){
                count++;
            }else{
                break;
            }
        }
        if(count == max_count){
            return true;
        }
        return false;
    }
    /**
     * 判断x，y的位置左斜线是否有相邻的五个一致。
     * @param x
     * @param y
     * @param points
     * @return
     */
    private boolean checkLeftDiagonal(int x, int y, List<Point> points,int max_count) {
        int count = 1;
        //判断上边是否有相同的棋子
        for(int i=1;i<max_count;i++){
            if(points.contains(new Point(x+i,y-i))){
                count++;
            }else{
                break;
            }
        }
        if(count == max_count){
            return true;
        }
        //判断下边
        for(int i=1;i<max_count;i++){
            if(points.contains(new Point(x-i,y+i))){
                count++;
            }else{
                break;
            }
        }
        if(count == max_count){
            return true;
        }
        return false;
    }
    /**
     * 判断x，y的位置右斜线是否有相邻的五个一致。
     * @param x
     * @param y
     * @param points
     * @return
     */
    private boolean checkRightDiagonal(int x, int y, List<Point> points,int max_count) {
        int count = 1;
        //判断上边是否有相同的棋子
        for(int i=1;i<max_count;i++){
            if(points.contains(new Point(x-i,y-i))){
                count++;
            }else{
                break;
            }
        }
        if(count == max_count){
            return true;
        }
        //判断下边
        for(int i=1;i<max_count;i++){
            if(points.contains(new Point(x+i,y+i))){
                count++;
            }else{
                break;
            }
        }
        if(count == max_count){
            return true;
        }
        return false;
    }


    /**
     * 绘制棋子
     * @param canvas
     */
    private void drawPieces(Canvas canvas) {
        for(int i=0,n = mWhiteArray.size();i<n;i++){//这样值调用一次mWhiteArray.size()，提高效率
            Point whitePoint = mWhiteArray.get(i);
            canvas.drawBitmap(mWhitePiece, (whitePoint.x + (1 - mPieceWOfLineH) / 2) * mLineHeight,
                    (whitePoint.y + (1 - mPieceWOfLineH) / 2) * mLineHeight, null);
        }
        for(int i=0,n = mBlackArray.size();i<n;i++){//这样值调用一次mWhiteArray.size()，提高效率
            Point blackPoint = mBlackArray.get(i);
            canvas.drawBitmap(mBlackPiece, (blackPoint.x + (1 - mPieceWOfLineH) / 2) * mLineHeight,
                    (blackPoint.y + (1 - mPieceWOfLineH) / 2) * mLineHeight, null);
        }
    }

    /**
     * 绘制棋盘
     * @param canvas
     */
    private void drawBoard(Canvas canvas) {
        int w = mPanelWidth;//整个view（panel边长）
        float lineHeight = mLineHeight;
        for(int i=0;i<MAX_LINE;i++){
            /*----------------------绘制横线--------------------------------*/
            //横坐标起点和终点
            int startX = (int) (lineHeight/2);//距这个view边部 半个行高的距离
            int endX = (int) (w-lineHeight/2);
            //纵坐标
            int y = (int) ((0.5+i)*lineHeight);
            canvas.drawLine(startX,y,endX,y,mPaint);
            /*---------------------绘制纵线---------------------------------------*/
            canvas.drawLine(y,startX,y,endX,mPaint);
        }
    }
    protected int dp2px(int dpValue){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dpValue,getResources().getDisplayMetrics());
    }

    /**
     * 再来一局
     */
    public void startAgin(){
        mWhiteArray.clear();
        mBlackArray.clear();
        mIsGameOver = false;
        mIsWhiteWin = false;
        invalidate();
    }

    /**
     * 人人对战
     */
    public void ptop(){
        humanComputer = false;
    }

    /**
     * 人机对战
     */
    public void ptoc(){
        humanComputer = true;
    }

    /**
     * view 的存储于恢复
     */
    private static final String INSTANCE = "instance";
    private static final String INSTANCE_GAMEOVER = "instance_gameover";
    private static final String INSTANCE_WHITEARRAY = "instance_whitearray";
    private static final String INSTANCE_BLACKARRAY = "instance_blackarray";

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE,super.onSaveInstanceState());
        bundle.putBoolean(INSTANCE_GAMEOVER,mIsGameOver);
        bundle.putParcelableArrayList(INSTANCE_WHITEARRAY,mWhiteArray);
        bundle.putParcelableArrayList(INSTANCE_BLACKARRAY,mBlackArray);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if(state instanceof Bundle){//state 是否是Bundle类型
            Bundle bundle = (Bundle) state;
            mIsGameOver = bundle.getBoolean(INSTANCE_GAMEOVER);
            mWhiteArray = bundle.getParcelableArrayList(INSTANCE_WHITEARRAY);
            mBlackArray = bundle.getParcelableArrayList(INSTANCE_BLACKARRAY);
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE));
            return;
        }
        super.onRestoreInstanceState(state);
    }
}
