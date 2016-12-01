package yzb.com.festival_msg.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import yzb.com.festival_msg.R;
import yzb.com.festival_msg.utils.CommonAdapter;
import yzb.com.festival_msg.utils.ImageLoader;
import yzb.com.festival_msg.utils.ViewHolder;

/**
 * Created by Administrator on 2016/8/4.
 */
public class ImgAdapter extends CommonAdapter<String> {
    //很重要
    private static Set<String> mSelectImg = new HashSet<>();
    private String mDirPath;
    private List<String> mImgPaths;
    public ImgAdapter(Context context, List<String> datas, String dirPath){
        super(context,datas, R.layout.img_gridview_item);
        this.mDirPath = dirPath;
        this.mImgPaths = datas;
    }
    @Override
    public void conVert(ViewHolder viewHolder, String s) {
        //重置状态
        final int position = viewHolder.getmPosition();
        final ImageView mImageView = viewHolder.getView(R.id.id_img_item);
        final ImageButton mImageButton = viewHolder.getView(R.id.id_imgbt_item);
        mImageView.setImageResource(R.drawable.picture_no);
        mImageView.setColorFilter(null);
        mImageButton.setImageResource(R.drawable.picture_unselect);

        ImageLoader.getmInstance(3, ImageLoader.Type.LIFO)
                .loadImage(mDirPath + "/" + mImgPaths.get(position), mImageView);
        //使用完整路径判断 防止不同文件夹下有同名文件
        final String imgPath = mDirPath+"/"+mImgPaths.get(position);
        //设置点击事件
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mSelectImg.contains(imgPath)){//已经被选择，点击移除
                    mSelectImg.remove(imgPath);
                    mImageView.setColorFilter(null);
                    mImageButton.setImageResource(R.drawable.picture_unselect);
                }else {
                    mSelectImg.add(imgPath);
                    mImageView.setColorFilter(Color.parseColor("#77000000"));
                    mImageButton.setImageResource(R.drawable.picture_select);
                }
                //notifyDataSetChanged();引起闪屏
            }
        });
        if(mSelectImg.contains(imgPath)){
            mImageView.setColorFilter(Color.parseColor("#77000000"));
            mImageButton.setImageResource(R.drawable.picture_select);
        }
    }
}
