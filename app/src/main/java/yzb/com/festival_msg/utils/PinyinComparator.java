package yzb.com.festival_msg.utils;

import java.util.Comparator;

import yzb.com.festival_msg.bean.Contact;

/**
 * Created by Administrator on 2016/7/26.
 */
public class PinyinComparator implements Comparator<Contact> {
    @Override
    public int compare(Contact o1, Contact o2) {
        //这里主要是用来对ListView里面的数据根据ABCDEFG...来排序 //升序
        if (o2.getSortLetters().equals("#")) {
            return -1;
        } else if (o1.getSortLetters().equals("#")) {
            return 1;
        } else {
            return o1.getSortLetters().compareTo(o2.getSortLetters());
        }
    }
}
