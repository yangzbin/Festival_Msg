package yzb.com.festival_msg.bean;


import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2016/7/21.
 */
@DatabaseTable(tableName = "tb_sendmsg")
public class SendMsg {
    @DatabaseField(columnName = "msgId",generatedId = false)
    private int msgId;
    @DatabaseField(columnName = "msg",dataType = DataType.STRING)
    private String msg;
    @DatabaseField(columnName = "number",dataType = DataType.STRING)
    private String number;
    @DatabaseField(columnName = "name",dataType = DataType.STRING)
    private String name;
    @DatabaseField(columnName = "fesname",dataType = DataType.STRING)
    private String fesName;
    @DatabaseField(columnName = "dateStr",dataType = DataType.STRING)
    private String dateStr;
    public SendMsg(){

    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFesName() {
        return fesName;
    }

    public void setFesName(String fesName) {
        this.fesName = fesName;
    }
    public String getDateStr() {
        return dateStr;
    }
    public void setDateStr(String dateStr) {
        this.dateStr = dateStr;
    }
}
