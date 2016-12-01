package yzb.com.festival_msg.view;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import yzb.com.festival_msg.R;


public class RefreshListView extends ListView implements OnScrollListener{
	private int downY;               // 按下时y轴的偏移量
	private int headerViewHeight;   // 头布局的高度
	private View headerView;       // 头布局对象
	 private final int DOWN_PULL = 0;                // 头布局状态: 下拉刷新
	    private final int RELEASE_REFRESH = 1;          // 头布局状态:　释放刷新
	    private final int REFRESHING = 2;               // 头布局状态:　正在刷新中..

	    private int currentState = DOWN_PULL;           // 头布局当前的状态, 默认为: 下拉刷新
	    private RotateAnimation upAnimation;            // 头布局向上旋转的动画
	    private RotateAnimation downAnimation;          // 头布局向下旋转的动画
	    private ImageView ivArrow;                      // 头布局的箭头
	    private ProgressBar mProgressBar;               // 头布局的进度圈
	    private TextView tvState;                       // 头布局的状态
	    private TextView tvLastUpdateTime;              // 头布局最后刷新时间

	    private OnRefreshListener mOnRefreshListener;   // 使用者的回调事件
	    private View footerView;                        // 脚布局对象
	    private int footerViewHeight;                   // 脚布局的高度
	    private boolean isLoadingMore = false;          // 是否正在加载更多中, 默认为: 没有正在加载
	    private ImageView id_header_aniam;
	

	public RefreshListView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		 initHeaderView();
	     initFooterView();
	     setOnScrollListener(this);
	}
	 public RefreshListView(Context context, AttributeSet attrs) {
	        super(context, attrs);
	        initHeaderView();
	        initFooterView();
	        setOnScrollListener(this);
	    }
	 private void initFooterView() {
	        footerView = View.inflate(getContext(), R.layout.listview_footer, null);
	        // 设置脚布局的paddingTop为自己高度的负数
	        footerView.measure(0, 0);
	        footerViewHeight = footerView.getMeasuredHeight();
	        footerView.setPadding(0, -footerViewHeight, 0, 0);

	        this.addFooterView(footerView);
	    }
	 private void initHeaderView() {
	        headerView = View.inflate(getContext(), R.layout.listview_header, null);
	        ivArrow = (ImageView) headerView.findViewById(R.id.iv_listview_header_arrow);
	        id_header_aniam = (ImageView) headerView.findViewById(R.id.id_header_aniam);
	        mProgressBar = (ProgressBar) headerView.findViewById(R.id.pb_listview_header);
	        tvState = (TextView) headerView.findViewById(R.id.tv_listview_header_state);
	        tvLastUpdateTime = (TextView) headerView.findViewById(R.id.tv_listview_header_last_update_time);

	        tvLastUpdateTime.setText("最后刷新时间: " + getCurrentTime());

	        // 测量头布局的高度.
	        headerView.measure(0, 0);                   // 让系统框架去帮我们测量头布局的宽和高.

	        // 取出头布局的高度.
//			headerView.getHeight();                     // 此方法是控件没有显示到屏幕上之前是获取不到值的, 一直都是0
	        headerViewHeight = headerView.getMeasuredHeight(); // 获得一个测量后的高度, 只有在measure方法被调用完毕后才可以得到具体高度.
	        System.out.println("头布局的高度: " + headerViewHeight);

	        // 隐藏头布局. paddingTop
	        headerView.setPadding(0, -headerViewHeight, 0, 0);

	        // 向ListView的顶部追加一个布局.
	        this.addHeaderView(headerView);

	        initAnimation();
	    }
	  private void initAnimation() {
	        upAnimation = new RotateAnimation(
	                0, -180,
	                Animation.RELATIVE_TO_SELF, 0.5f,
	                Animation.RELATIVE_TO_SELF, 0.5f);
	        upAnimation.setDuration(500);
	        upAnimation.setFillAfter(true);                     // 让控件停止在动画结束的状态下

	        downAnimation = new RotateAnimation(
	                -180, -360,
	                Animation.RELATIVE_TO_SELF, 0.5f,
	                Animation.RELATIVE_TO_SELF, 0.5f);
	        downAnimation.setDuration(500);
	        downAnimation.setFillAfter(true);                   // 让控件停止在动画结束的状态下
	    }
	  public boolean onTouchEvent(MotionEvent ev) {
	        switch (ev.getAction()) {
	            case MotionEvent.ACTION_DOWN:
	                downY = (int) ev.getY();
	                break;
	            case MotionEvent.ACTION_MOVE:
	                // 当前的状态是否是正在刷新中, 如果是, 直接跳出.
	                if (currentState == REFRESHING) {
	                    break;
	                }

	                int moveY = (int) ev.getY();

	                // 间距 = 移动y - 按下y;
	                int diffY = moveY - downY;
	                // 计算头布局最新的paddingTop = -头布局高度 + 间距.
	                int paddingTop = -headerViewHeight + diffY;
//				System.out.println("paddingTop: " + paddingTop);

	                // 如果paddingTop的值 < -headerViewHeight, 不进行下拉刷新头的滑动操作.
	                // 并且ListView顶部第一个显示的条目的索引为: 0, 才可以进行滑动.

	                // 获取ListView顶部第一个显示的条目的索引
	                int firstVisiblePosition = getFirstVisiblePosition();
//				System.out.println("firstVisiblePosition: " + firstVisiblePosition);
	                if (paddingTop > -headerViewHeight
	                        && firstVisiblePosition == 0) {

	                    if (paddingTop > 0 && currentState == DOWN_PULL) {              // 头布局完全显示, 并且当前状态是下拉刷新, 进入到松开刷新的状态
	                        System.out.println("松开刷新");
	                        currentState = RELEASE_REFRESH;
	                        refreshHeaderViewState();
	                    } else if (paddingTop < 0 && currentState == RELEASE_REFRESH) { // 头布局没有完全显示, 并且当前状态是松开刷新, 进入到下拉刷新的状态
	                        System.out.println("下拉刷新");
	                        currentState = DOWN_PULL;
	                        refreshHeaderViewState();
	                    }
	                    headerView.setPadding(0, paddingTop, 0, 0);
	                    return true;            // 自己处理用户触摸滑动的事件.
	                }
	                break;
	            case MotionEvent.ACTION_UP:
	                // 判断当前的状态是哪一种
	                if (currentState == DOWN_PULL) {                     // 当前是在下拉刷新状态下松开了, 什么都不做, 把头布局隐藏就可以.
	                    headerView.setPadding(0, -headerViewHeight, 0, 0);
	                } else if (currentState == RELEASE_REFRESH) {           // 当前的状态属于释放刷新, 并且松开了. 应该把头布局正常显示, 进入正在刷新中状态.
	                    headerView.setPadding(0, 0, 0, 0);
	                    currentState = REFRESHING;
	                    refreshHeaderViewState();

	                    // 调用用户的监听事件.
	                    if (mOnRefreshListener != null) {
	                        mOnRefreshListener.onPullDownRefresh();
	                    }
	                }
	                break;
	            default:
	                break;
	        }
	        return super.onTouchEvent(ev);                          // ListView默认的滑动效果.
	    }
	  private void refreshHeaderViewState() {
	        AnimationDrawable drawable;
	        switch (currentState) {
	            case DOWN_PULL:     // 下拉刷新
	                //ivArrow.startAnimation(downAnimation);
	                id_header_aniam.setImageResource(R.drawable.ic_launcher);
					//此处应该是自定义的 AnimationDrawable 否则类型转换错误
//	                drawable = (AnimationDrawable) id_header_aniam.getDrawable();
//	                drawable.start();
	                tvState.setText("下拉刷新");
	                break;
	            case RELEASE_REFRESH: // 松开刷新
	                // ivArrow.startAnimation(upAnimation);
	                id_header_aniam.setImageResource(R.drawable.ic_launcher);

//	                drawable = (AnimationDrawable) id_header_aniam.getDrawable();
//	                drawable.start();
	                tvState.setText("松开刷新");
	                break;
	            case REFRESHING: // 正在刷新中
	                ivArrow.clearAnimation(); // 把自己身上的动画清除掉
	                ivArrow.setVisibility(View.INVISIBLE);
	                mProgressBar.setVisibility(View.VISIBLE);
	                tvState.setText("正在刷新..");
	                break;
	            default:
	                break;
	        }
	    }
	  public void onRefreshFinish() {
	        if (isLoadingMore) { // 当前属于加载更多中
	            // 隐藏脚布局
	            footerView.setPadding(0, -footerViewHeight, 0, 0);
	            isLoadingMore = false;

	        } else { // 下拉刷新操作
	            // 隐藏头布局
	            headerView.setPadding(0, -headerViewHeight, 0, 0);
	            currentState = DOWN_PULL;
	            mProgressBar.setVisibility(View.INVISIBLE);
	            ivArrow.setVisibility(View.VISIBLE);
	            tvState.setText("下拉刷新");
	            tvLastUpdateTime.setText("最后刷新时间: " + getCurrentTime());
	        }
	    }
	  private String getCurrentTime() {
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        return sdf.format(new Date());
	    }
	  public void setOnRefreshListener(OnRefreshListener listener) {
	        mOnRefreshListener = listener;
	    }
	  public interface OnRefreshListener {

	        /**
	         * 当下拉刷新时回调此方法
	         */
	        public void onPullDownRefresh();

	        /**
	         * 当加载更多时调用此方法
	         */
	        public void onLoadingMore();
	    }
	  



	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
		if ((scrollState == OnScrollListener.SCROLL_STATE_IDLE    // 当前是停滞或者是快速滑动时
                || scrollState == OnScrollListener.SCROLL_STATE_FLING)
                && getLastVisiblePosition() == (getCount() - 1)
                && !isLoadingMore) {
            System.out.println("滑动到底部, 可以加载更多数据了.");

            isLoadingMore = true;
            footerView.setPadding(0, 0, 0, 0);
            setSelection(getCount()); // 滑动到最底部

            if (mOnRefreshListener != null) {
                mOnRefreshListener.onLoadingMore();
            }
        }
		
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
		
		
	}

}
