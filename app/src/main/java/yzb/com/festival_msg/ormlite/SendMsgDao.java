package yzb.com.festival_msg.ormlite;

import android.content.Context;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import yzb.com.festival_msg.bean.SendMsg;

/**
 * Created by Administrator on 2016/7/21.
 */
public class SendMsgDao {
    private Context context;
    private Dao<SendMsg, Integer> sendMsgDaoOpe;
    private DatabaseHelper helper;

    public SendMsgDao(Context context){
        this.context = context;
        try {
            helper = DatabaseHelper.getHelper(context);
            sendMsgDaoOpe = helper.getDao(SendMsg.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 增加一条已发送的短信
     * @param sendMsg
     */
    public void addSendMsg(SendMsg sendMsg){
        try {
            sendMsgDaoOpe.create(sendMsg);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询所有短信发送记录
     * @return
     */
    public List<SendMsg> getSendMsgAll(){
        List<SendMsg> sendMsgs = new ArrayList<>();
        try {
            sendMsgs = sendMsgDaoOpe.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sendMsgs;
    }
}
