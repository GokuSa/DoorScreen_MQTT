package shine.com.doorscreen.entity;

import java.io.Serializable;
/*
* 要下载的多媒体视频元素*/
public class Element implements Serializable {
    private String name;
    private int type;
    //服務器路徑
    private String src;
    //本地路徑
    private String path="";
//    private int life;
    private int id;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    /*public int getLife() {
        return life;
    }

    public void setLife(int life) {
        this.life = life;
    }*/

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "Element{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", src='" + src + '\'' +
                ", path='" + path + '\'' +
                ", id=" + id +
                '}';
    }
}