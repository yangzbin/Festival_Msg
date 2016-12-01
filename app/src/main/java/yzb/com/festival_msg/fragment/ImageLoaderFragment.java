package yzb.com.festival_msg.fragment;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import yzb.com.festival_msg.R;
import yzb.com.festival_msg.bean.Folder;
import yzb.com.festival_msg.utils.ToastUtil;
import yzb.com.festival_msg.view.ImgDirPopWindow;
import yzb.com.festival_msg.adapter.ImgAdapter;

public class ImageLoaderFragment extends Fragment {
    private GridView mGridView;
    private List<String> mImgs;//gridview的数据集
    private ImgAdapter mAdapter;
    private RelativeLayout mBottomLayout;
    private TextView mDirName;
    private TextView mPicCount;
    private ProgressDialog mDialog;

    private File mCurrentDir;
    private int mMaxCount;

    private List<Folder> mFolders = new ArrayList<>();

    private static final int DATA_LOAD = 0X110;

    private ImgDirPopWindow mPopWindow;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == DATA_LOAD){
                mDialog.dismiss();
                //绑定数据到view中
                data2View();
                //初始化popwindow
                initImgPopWindow();
            }
        }
    };

    /**
     * 初始化popwindow
     */
    private void initImgPopWindow() {
        mPopWindow = new ImgDirPopWindow(getActivity(),mFolders);
        mPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                //让屏幕变亮
                lightOn();
            }
        });
        mPopWindow.setDirSelectedListener(new ImgDirPopWindow.OnDirSelectedListener() {
            @Override
            public void onSelected(Folder folder) {
                mCurrentDir = new File(folder.getDir());//更新目录
                //更新图片路径
                mImgs = Arrays.asList(mCurrentDir.list(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String filename) {
                        if(filename.endsWith(".jpg")||filename.endsWith(".jpeg")||filename.endsWith(".png")){
                            return true;
                        }
                        return false;
                    }
                }));
                mAdapter = new ImgAdapter(getActivity(),mImgs,mCurrentDir.getAbsolutePath());
                mGridView.setAdapter(mAdapter);
                mDirName.setText(folder.getDirName());
                mPicCount.setText(folder.getCount()+"");
                mPopWindow.dismiss();
            }
        });
    }

    /**
     * popwindow消失后 让屏幕变亮
     */
    private void lightOn() {
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        lp.alpha = 1.0f;
        getActivity().getWindow().setAttributes(lp);
    }

    /**
     * 将数据显示在view中
     */
    private void data2View() {
        if(mCurrentDir == null){
            ToastUtil.showShortToast(getActivity(),"未扫描到任何图片");
            return;
        }
        mImgs = Arrays.asList(mCurrentDir.list());//将数组包装成list
        mAdapter = new ImgAdapter(getActivity(),mImgs,mCurrentDir.getAbsolutePath());
        mGridView.setAdapter(mAdapter);
        mPicCount.setText(mMaxCount+"");
        mDirName.setText(mCurrentDir.getName().substring(0));
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
       return inflater.inflate(R.layout.fragment_image_loader,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        initDatas();
        initEvents();
    }

    private void initViews(View view) {
        mGridView = (GridView) view.findViewById(R.id.id_imggridview);
        mBottomLayout = (RelativeLayout) view.findViewById(R.id.id_bottom_rl);
        mDirName = (TextView) view.findViewById(R.id.id_dir_name);
        mPicCount = (TextView) view.findViewById(R.id.id_pic_count);
    }

    private void initEvents() {
        mBottomLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopWindow.setAnimationStyle(R.style.dir_popwindow_anim);//设置popwindow出现动画
                //显示在底部之上
                mPopWindow.showAsDropDown(mBottomLayout,0,0);
                //屏幕变暗
                lightOff();

            }
        });
    }

    /**
     * popwindow弹出后 屏幕变暗
     */
    private void lightOff() {
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        lp.alpha = 0.3f;
        getActivity().getWindow().setAttributes(lp);
    }

    /**
     * 利用ContentProvider扫描手机中的所有图片
     */
    private void initDatas() {
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            ToastUtil.showShortToast(getActivity(),"当前存储卡不可用!");
            return;
        }
        mDialog = ProgressDialog.show(getActivity(),null,"正在加载...");
        //开启线程 扫描所有图片
        new Thread(){
            @Override
            public void run() {
                Uri mImgUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                ContentResolver cr = getActivity().getContentResolver();
                Cursor cursor = cr.query(mImgUri, null, MediaStore.Images.Media.MIME_TYPE + "= ? or "
                                + MediaStore.Images.Media.MIME_TYPE + "= ?", new String[]{"image/jpeg", "image/png"},
                        MediaStore.Images.Media.DATE_MODIFIED);
                Set<String> mDirPaths = new HashSet<String>();//方法结束 内存自动回收
                while (cursor.moveToNext()){
                    //得到图片路径
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    //根据图片路径找到父路径
                    File parentFile = new File(path).getParentFile();
                    if(parentFile == null){
                        continue;
                    }
                    String dirPath = parentFile.getAbsolutePath();
                    Folder folder = null;
                    if(mDirPaths.contains(dirPath)){
                        continue;
                    }else {
                        mDirPaths.add(dirPath);
                        folder = new Folder();
                        folder.setDir(dirPath);
                        folder.setFirstImgPath(path);
                    }
                    if(parentFile.list() == null){
                        continue;
                    }
                    //得到图片的数量
                    int picSize = parentFile.list(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String filename) {
                            if(filename.endsWith(".jpg")||filename.endsWith(".jpeg")||filename.endsWith(".png")){
                                return true;
                            }
                            return false;
                        }
                    }).length;
                    folder.setCount(picSize);//当前文件夹下的图片数量
                    mFolders.add(folder);
                    //设置当前文件夹的名称和当前文件夹图片数量
                    if(picSize>mMaxCount){
                        mMaxCount = picSize;
                        mCurrentDir = parentFile;
                    }
                }
                cursor.close();
                //通知handler图片扫描完成
                mHandler.sendEmptyMessage(DATA_LOAD);
            }
        }.start();

    }

}
