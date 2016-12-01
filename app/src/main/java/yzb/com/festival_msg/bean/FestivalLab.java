package yzb.com.festival_msg.bean;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import yzb.com.festival_msg.ormlite.MsgDao;

/**
 * Created by Administrator on 2016/7/19.
 */
public class FestivalLab {
    private Context mContext;
    public static FestivalLab mInstance;
    private List<Festival> mFestivals = new ArrayList<>();
    private List<Msg> mMsgs = new ArrayList<>();
    private FestivalLab(Context context){
        mContext = context;
        mFestivals.add(new Festival(1,"国庆节"));
        mFestivals.add(new Festival(2,"中秋节"));
        mFestivals.add(new Festival(3,"元旦"));
        mFestivals.add(new Festival(4,"春节"));
        mFestivals.add(new Festival(5,"端午节"));
        mFestivals.add(new Festival(6,"七夕节"));
        mFestivals.add(new Festival(7,"圣诞节"));
        mFestivals.add(new Festival(8,"儿童节"));

        new MsgDao(context).addMsg(new Msg(1,1,"国庆快乐"));
        new MsgDao(context).addMsg(new Msg(2,2,"中秋快乐"));
        new MsgDao(context).addMsg(new Msg(3,3,"元旦快乐"));
        new MsgDao(context).addMsg(new Msg(4,4,"春节快乐"));
        new MsgDao(context).addMsg(new Msg(5,5,"端午快乐"));
        new MsgDao(context).addMsg(new Msg(6,6,"七夕快乐"));
        new MsgDao(context).addMsg(new Msg(7,7,"圣诞快乐"));
        new MsgDao(context).addMsg(new Msg(8,8,"儿童节快乐"));
        new MsgDao(context).addMsg(new Msg(9,1,"国庆快乐哈哈"));
        new MsgDao(context).addMsg(new Msg(10,1,"国庆快乐哈哈"));
        new MsgDao(context).addMsg(new Msg(11,1,"乌龙竞舞振兴志； 新矿宏开奋起图。 乘风誓兴鹏程路； 兴厂功高有志人。 无限春光无限路； 有为时代有为人。 万众一心齐奋力； 百舸千里竞争流。 四化腾飞天永盛；千军奋进业方兴。"));
        new MsgDao(context).addMsg(new Msg(12,1,"国庆快乐哈哈"));
        new MsgDao(context).addMsg(new Msg(13,1,"国庆快乐哈哈"));

    }
    public List<Festival> getmFestivals(){
        return new ArrayList<Festival>(mFestivals);//返回节日名称副本 防止别的页面对数据操作，队员数据更改
    }
    public Festival getFestivalById(int fesId){
        for(Festival festival:mFestivals){
            if(festival.getId() == fesId){
                return  festival;
            }
        }
        return null;
    }
    public Msg getmMsgsById(int id){
        return new MsgDao(mContext).getmMsgsById(id);
    }
    public List<Msg> getmMsgsByfesId(int fesId){
        return new MsgDao(mContext).getmMsgsByfesId(fesId);
    }
    public static FestivalLab getmInstance(Context context){
        if(mInstance == null){
            synchronized (FestivalLab.class){
                if(mInstance == null){
                    mInstance = new FestivalLab(context);
                }
            }
        }
        return mInstance;
    }
}
