package yzb.com.festival_msg.bean;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Administrator on 2016/7/20.
 */
@DatabaseTable(tableName = "tb_msg")
public class Msg {
    @DatabaseField(columnName = "msgId",generatedId = false)
    private int msgId;//信息id
    @DatabaseField(columnName = "festId",dataType = DataType.INTEGER)
    private int festId;//节日id
    @DatabaseField(columnName = "msgContent",dataType = DataType.STRING)
    private String msgContent;//节日内容
    public Msg(){

    }
    public Msg(int msgId,int festId,String msgContent){
        this.msgId = msgId;
        this.festId = festId;
        this.msgContent = msgContent;
    }
    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public int getFestId() {
        return festId;
    }

    public void setFestId(int festId) {
        this.festId = festId;
    }

    public String getMsgContent() {
        return msgContent;
    }

    public void setMsgContent(String msgContent) {
        this.msgContent = msgContent;
    }
}
