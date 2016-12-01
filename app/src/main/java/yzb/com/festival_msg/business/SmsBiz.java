package yzb.com.festival_msg.business;

import android.app.PendingIntent;
import android.content.Context;
import android.telephony.SmsManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import yzb.com.festival_msg.bean.SendMsg;
import yzb.com.festival_msg.ormlite.SendMsgDao;

/**
 * Created by Administrator on 2016/7/20.
 */
public class SmsBiz {
    private Context mContext;
    public SmsBiz(Context context){
        mContext = context;
    }
    /**
     *发送单个人
     * @param number 电话号码
     * @param msg 消息内容
     * @param sendPi 发送意图
     * @param deliverPi 监听发送状态
     * @return
     */
    public int sendMsg(String number, String msg, PendingIntent sendPi,PendingIntent deliverPi){
        SmsManager smsManager = SmsManager.getDefault();
        ArrayList<String> contents = smsManager.divideMessage(msg);//分割短信内容
        for(String content:contents){
            smsManager.sendTextMessage(number,null,content,sendPi,deliverPi);
        }
        return contents.size();
    }

    /**
     * 发送多个人 并保存短信
     * @param numbers
     * @param msg
     * @param sendPi
     * @param deliverPi
     * @return
     */
    public int sendMsg(Set<String> numbers, SendMsg msg, PendingIntent sendPi, PendingIntent deliverPi){
        saveSendMsg(msg);
        int result = 0;
        for(String number:numbers){
          int count =  sendMsg(number,msg.getMsg(),sendPi,deliverPi);
            result += count;
        }
        return result;
    }

    /**
     * 保存发送的短信
     * @param sendMsg
     */
    public void saveSendMsg(SendMsg sendMsg){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        sendMsg.setDateStr(df.format(new Date()));
        new SendMsgDao(mContext).addSendMsg(sendMsg);
    }
    public List<SendMsg> getSendMsgAll(){
        return new SendMsgDao(mContext).getSendMsgAll();
    }
}
