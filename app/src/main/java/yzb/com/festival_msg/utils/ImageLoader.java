package yzb.com.festival_msg.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * 图片加载工具类
 *
 * @author yzb
 *         整个项目只用这一个 采用单例模式
 */
public class ImageLoader {
    private static ImageLoader mInstance;
    /**
     * 图片缓存核心对象
     */
    private LruCache<String, Bitmap> mLurCache;

    /**
     * 线程池
     */
    private ExecutorService mThreadPool;
    /**
     * 默认线程数为1
     */
    private static final int DEFULT_THREAD_COUNT = 1;
    /**
     * 队列的调度方式 默认后进先出
     */
    private Type mType = Type.FIFO;

    public enum Type {
        FIFO, LIFO;//加载模式 先进先出 后进先出；
    }

    /**
     * 任务列表 采用LinkedList 其中有从头部和尾部取一个对象的方法 arraylist只能循环取
     */
    private LinkedList<Runnable> mTaskQueue;
    /**
     * 后台轮询线程
     */
    private Thread mPoolThread;
    /**
     * 用于给后台轮询线程发送消息
     */
    private Handler mPoolThreadHandler;
    /**
     * 通过信号量来同步mPoolThreadHandler
     */
    private Semaphore mSemaphorePoolThreadHandler = new Semaphore(0);
    /**
     * 通过信号量来控制线程池,防止线程阻塞
     */
    private Semaphore mSemaphoreThreadPool;
    /**
     * 用于更新图片
     */
    private Handler mUIHandler;

    /**
     * 构造函数
     * @param threadCount 线程数
     * @param type 调度类型
     */
    private ImageLoader(int threadCount,Type type) {
        init(threadCount,type);
    }

    /**
     * 初始化操作
     * @param threadCount
     * @param type
     */
    private void init(int threadCount, Type type) {
        //后台轮询线程
        mPoolThread = new Thread(){
            @Override
            public void run() {
                Looper.prepare();//准备轮询
                mPoolThreadHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        //通知线程池取出一个任务去执行
                        mThreadPool.execute(getTask());
                        try {
                            mSemaphoreThreadPool.acquire();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };
                //确定mPoolThreadHandler不是null，释放一个信号量
                mSemaphorePoolThreadHandler.release();
                Looper.loop();//轮询
            }


        };
        mPoolThread.start();
        //获取应用最大可用内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheMemory = maxMemory/8;
        //初始化缓存
        mLurCache = new LruCache<String, Bitmap>(cacheMemory){
            //测量每个bitmap占据的内存
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes()*value.getHeight();
            }
        };
        //初始化线程池
        mThreadPool = Executors.newFixedThreadPool(threadCount);
        //初始化任务队列
        mTaskQueue = new LinkedList<Runnable>();
        mType = type;
        mSemaphoreThreadPool = new Semaphore(threadCount);

    }

    /**
     * 从任务队列获取任务
     * @return
     */
    private Runnable getTask() {
        if(mType == Type.FIFO){
            return mTaskQueue.removeFirst();
        }else if(mType == Type.LIFO){
            return mTaskQueue.removeLast();
        }
        return null;
    }

    /**
     * 采用单例模式
     * 懒加载
     *
     * @return
     */
    public static ImageLoader getmInstance() {
        if (mInstance == null) {//提高效率
            synchronized (ImageLoader.class) {//同步处理
                if (mInstance == null) {
                    mInstance = new ImageLoader(DEFULT_THREAD_COUNT,Type.FIFO);
                }
            }
        }
        return mInstance;
    }
    public static ImageLoader getmInstance(int threadCount,Type type) {
        if (mInstance == null) {//提高效率
            synchronized (ImageLoader.class) {//同步处理
                if (mInstance == null) {
                    mInstance = new ImageLoader(threadCount,type);
                }
            }
        }
        return mInstance;
    }

    /**
     * 根据url路径加载图片
     * @param path
     * @param imgView 用来显示图片
     */
    public void loadImage(final String path, final ImageView imgView){
        imgView.setTag(path);//设置tag，方式imgview显示多长
        if(mUIHandler == null){
            mUIHandler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    //获取图片，为imgview回调设置图片
                    ImgBeanHolder holder = (ImgBeanHolder) msg.obj;
                    Bitmap bitmap = holder.bitmap;
                    ImageView imgView = holder.imgView;
                    String path = holder.path;
                    if(imgView.getTag().toString().equals(path)){//imgview复用,如果imgveiw一致，设置图片
                        imgView.setImageBitmap(bitmap);
                    }
                }
            };
        }
        //根据path在缓存中获取bitmap
        Bitmap bitmap = getBitmapFromLruCache(path);
        if(bitmap != null){
            refreashBitmap(bitmap, imgView, path);
        }else {
            addTask(new Runnable(){
                @Override
                public void run() {
                    //加载图片
                    //图片的压缩
                    //1.获得图片需要显示的大小
                    ImageSize imageSize = getImageViewSize(imgView);
                    //2.得到压缩图片
                    Bitmap bitmap = decodeSampledBitmapFromPath(path,imageSize.width,imageSize.height);
                    //3.将压缩图片加入到缓存
                    addBitmapToLruCache(path,bitmap);
                    //刷新显示图片
                    refreashBitmap(bitmap, imgView, path);

                    mSemaphoreThreadPool.release();//加进去一个task，释放一个


                }
            });
        }
    }

    /**
     * 更新图片
     * @param bitmap
     * @param imgView
     * @param path
     */
    private void refreashBitmap(Bitmap bitmap, ImageView imgView, String path) {
        Message message = Message.obtain();
        ImgBeanHolder imgBeanHolder = new ImgBeanHolder();
        imgBeanHolder.bitmap = bitmap;
        imgBeanHolder.imgView = imgView;
        imgBeanHolder.path = path;
        message.obj = imgBeanHolder;//通过调用obtainMessage方法获取Message对象就能避免创建对象，从而减少内存的开销
        mUIHandler.sendMessage(message);
    }

    /**
     * 将图片加入到缓存
     * @param path
     * @param bitmap
     */
    private void addBitmapToLruCache(String path, Bitmap bitmap) {
        if(getBitmapFromLruCache(path) == null){
            if(bitmap!=null){
                mLurCache.put(path,bitmap);
            }
        }
    }

    /**
     * 根据path获取图片根据需要显示的宽高进行压缩
     * @param path
     * @param width
     * @param height
     * @return
     */
    private Bitmap decodeSampledBitmapFromPath(String path, int width, int height) {
        //获得图片的宽高 并不把图片加载到内存中
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path,options);
        options.inSampleSize = caculateInSampleSize(options,width,height);
        //使用获取到inSampleSize解析图片
        options.inJustDecodeBounds = false;//将图片加载到内存
        Bitmap bitmap = BitmapFactory.decodeFile(path,options);
        return bitmap;
    }

    /**
     * 根据需要的宽高和图片实际宽高计算SampleSize
     * @param options 包含了图片实际的宽和高
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private int caculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int width = options.outWidth;
        int height = options.outHeight;
        int inSampleSize = 1;
        if(width>reqWidth||height>reqHeight){
            //图片实际宽高大于需要压缩的宽高，进行压缩
            int widthRadio = Math.round(width*1.0f/reqWidth);//压缩比例四舍五入
            int heightRadio = Math.round(height*1.0f/reqHeight);
            inSampleSize = Math.max(widthRadio,heightRadio);//按最大比例压缩
        }
        return inSampleSize;
    }

    /**
     * 根据imgview获取适当的压缩的宽和高
     * @param imgView
     * @return
     */
    private ImageSize getImageViewSize(ImageView imgView) {
        ImageSize imageSize = new ImageSize();
        //获取屏幕大小
        DisplayMetrics displayMetrics = imgView.getContext().getResources().getDisplayMetrics();
        LayoutParams lp = imgView.getLayoutParams();//获取布局参数
        int width = imgView.getWidth();//获取imgview的实际宽度
        if(width<=0){
            width = lp.width;//获取imgview在layout中声明的宽度
        }
        if(width<=0){//imgviewa设置是wrapcontent或matchparent
//            width = imgView.getMaxWidth();//检查最大值
            width = getImageViewFieldValue(imgView,"mMaxWidth");
        }
        if(width<=0){
            width = displayMetrics.widthPixels;
        }

        int height = imgView.getHeight();//获取imgview的实际高度
        if(height<=0){
            height = lp.height;//获取imgview在layout中声明的高度
        }
        if(height<=0){//imgviewa设置是wrapcontent或matchparent
//            height = imgView.getMaxHeight();//检查最大值
            height = getImageViewFieldValue(imgView,"mMaxHeight");
        }
        if(height<=0){
            height = displayMetrics.heightPixels;
        }
        imageSize.width = width;
        imageSize.height = height;
        return imageSize;
    }

    /**
     * 通过反射获取imgview的某个属性值
     * @param object
     * @param fieldName
     * @return
     */
    private static int getImageViewFieldValue(Object object,String fieldName){
        int value= 0;
        try {
            Field field = ImageView.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            int fieldValue = field.getInt(object);
            if(fieldValue>0 && fieldValue<Integer.MAX_VALUE){
                value = fieldValue;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }
    /**
     * 添加任务,避免死锁
     * @param runnable
     */
    private synchronized void addTask(Runnable runnable) {
        mTaskQueue.add(runnable);
        //防止mPoolThreadHandler空指针
        try {
            if(mPoolThreadHandler == null)
            mSemaphorePoolThreadHandler.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mPoolThreadHandler.sendEmptyMessage(0x110);//发送通知，
    }

    /**
     * 根据path从lru缓存中获取图片
     * @param key
     * @return
     */
    private Bitmap getBitmapFromLruCache(String key) {
        return mLurCache.get(key);
    }

    /**
     * 用来持用bitmap，防止再次调用mUIHandler导致数据错乱
     */
    private class ImgBeanHolder{
        Bitmap bitmap;
        ImageView imgView;
        String path;
    }
    private class ImageSize{
        int width;
        int height;
    }
}
