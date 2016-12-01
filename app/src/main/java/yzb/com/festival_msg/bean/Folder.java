package yzb.com.festival_msg.bean;

/**
 * Created by Administrator on 2016/8/3.
 * 文件夹对象
 */
public class Folder {
    /**
     * 当前文件夹的路径
     */
    private String dir;
    private String firstImgPath;
    private String dirName;
    private int count;

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
        int lastIndexOf = this.dir.lastIndexOf("/");
        this.dirName = this.dir.substring(lastIndexOf+1);
    }

    public String getFirstImgPath() {
        return firstImgPath;
    }

    public void setFirstImgPath(String firstImgPath) {
        this.firstImgPath = firstImgPath;
    }

    public String getDirName() {
        return dirName;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
