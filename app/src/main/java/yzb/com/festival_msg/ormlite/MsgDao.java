package yzb.com.festival_msg.ormlite;

import android.content.Context;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import yzb.com.festival_msg.bean.Msg;

/**
 * Created by Administrator on 2016/7/20.
 */
public class MsgDao {
    private Context context;
    private Dao<Msg, Integer> msgDaoOpe;
    private DatabaseHelper helper;

    public MsgDao(Context context){
        this.context = context;
        try {
            helper = DatabaseHelper.getHelper(context);
            msgDaoOpe = helper.getDao(Msg.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 增加一条短信
     * @param msg
     */
    public void addMsg(Msg msg){
        try {
            msgDaoOpe.create(msg);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据节ID称获取短信内容
     * @param fesId
     * @return
     */
    public List<Msg> getmMsgsByfesId(int fesId){
        List<Msg> msgs = new ArrayList<>();
        try {
            msgs = msgDaoOpe.queryForEq("festId",fesId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return msgs;
    }

    /**
     * 根据短信ID获取短信内容
     * @param msgId
     * @return
     */
    public Msg getmMsgsById(int msgId){
        Msg msg = new Msg();
        try {
            msg = msgDaoOpe.queryForEq("msgId",msgId).get(0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return msg;
    }

}
