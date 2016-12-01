package yzb.com.festival_msg.bean;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/7/23.
 */
public class Contact implements Serializable {
    private String contactId;
    private String contactName;
    private String contactNumber;
    private String sortLetters;  //显示数据拼音的首字母
    private boolean selectTag = false;
    public Contact(String name,String number){
        this.contactName = name;
        this.contactNumber = number;
    }
    public boolean isSelectTag() {
        return selectTag;
    }

    public void setSelectTag(boolean selectTag) {
        this.selectTag = selectTag;
    }
    public String getSortLetters() {
        return sortLetters;
    }

    public void setSortLetters(String sortLetters) {
        this.sortLetters = sortLetters;
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }
}
