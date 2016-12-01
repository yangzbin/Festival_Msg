package yzb.com.festival_msg.view;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;

import java.util.List;

import yzb.com.festival_msg.R;
import yzb.com.festival_msg.bean.Folder;
import yzb.com.festival_msg.utils.CommonAdapter;
import yzb.com.festival_msg.utils.ImageLoader;
import yzb.com.festival_msg.utils.ViewHolder;

/**
 * Created by Administrator on 2016/8/4.
 */
public class ImgDirPopWindow extends PopupWindow {
    private int mWidth;
    private int mHeight;

    private View mConvertView;
    private List<Folder> mList;

    private ListView mListView;
    private ImgListAdapter adapter;

    public interface OnDirSelectedListener{
        void onSelected(Folder folder);
    }

    public void setDirSelectedListener(OnDirSelectedListener mListener) {
        this.mListener = mListener;
    }

    public OnDirSelectedListener mListener;

    public ImgDirPopWindow(Context context,List<Folder> lists){
        calWidthAndHeight(context);

        mConvertView = LayoutInflater.from(context).inflate(R.layout.pop_img_select,null);
        mList = lists;

        setContentView(mConvertView);
        setWidth(mWidth);
        setHeight(mHeight);
        //设置可触摸 可点击
        setFocusable(true);
        setTouchable(true);
        //设置外部区域可点击
        setOutsideTouchable(true);
        setBackgroundDrawable(new BitmapDrawable());

        setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_OUTSIDE){
                    dismiss();
                    return true;
                }
                return false;
            }
        });
        initViews(context);
        initEvents();
    }

    private void initEvents() {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mListener != null){
                    mListener.onSelected(mList.get(position));
                }
            }
        });
    }

    private void initViews(Context context) {
        mListView = (ListView) mConvertView.findViewById(R.id.id_list_dir);
        adapter = new ImgListAdapter(context,mList);
        mListView.setAdapter(adapter);
    }

    /**
     * 计算popWindow的宽度和高度
     * @param context
     */
    private void calWidthAndHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);//获得屏幕的长宽高
        mWidth = displayMetrics.widthPixels;
        mHeight = (int) (displayMetrics.heightPixels*0.7);
    }
    class ImgListAdapter extends CommonAdapter<Folder>{
        private List<Folder> mDatas;
        public ImgListAdapter(Context context,List<Folder> mDatas){
            super(context,mDatas,R.layout.img_pop_item);
            this.mDatas = mDatas;
        }

        @Override
        public void conVert(ViewHolder viewHolder, Folder folder) {
            int position = viewHolder.getmPosition();
            Folder bean = mDatas.get(position);
            //重置 防止用户返回加载第一屏图片时 还停留在第二屏图片上
            viewHolder.setImgRes(R.id.id_imgdir_item_img,R.drawable.picture_no);

            ImageLoader.getmInstance().loadImage(bean.getFirstImgPath(), (ImageView) viewHolder.getView(R.id.id_imgdir_item_img));
            viewHolder.setTvText(R.id.id_imgdir_item_name,bean.getDirName());
            viewHolder.setTvText(R.id.id_imgdir_item_count,bean.getCount()+"");
        }
    }
}
