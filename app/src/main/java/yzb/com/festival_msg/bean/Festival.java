package yzb.com.festival_msg.bean;

/**
 * Created by Administrator on 2016/7/19.
 */
public class Festival {
    private int id;//节日id
    private String name;//节日名称

    public Festival(int id,String name){
        this.id = id;
        this.name = name;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;

    }

    public void setName(String name) {
        this.name = name;
    }
}
