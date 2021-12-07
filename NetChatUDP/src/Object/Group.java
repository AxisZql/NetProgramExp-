package Object;

import java.util.Date;
/**
 * author：李志强
 * class：网络194
 * date：2021-11-24
 * 注：本项目github：https://github.com/AxisZql/NetProgramExp-
 */

public class Group {
    private Integer id;
    private String name;
    private Integer created_by;
    private Date created_at;
    private String avatar;

    public Group(Integer id, String name, Integer created_by, Date created_at, String avatar) {
        this.id = id;
        this.name = name;
        this.created_by = created_by;
        this.created_at = created_at;
        this.avatar = avatar;
    }


    //创建群聊时的构造函数
    public Group(String name, Integer created_by, String avatar) {
        this.name = name;
        this.created_by = created_by;
        this.avatar = avatar;
    }

    // 搜索群聊的构造函数
    public Group(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getCreated_by() {
        return created_by;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public String getAvatar() {
        return avatar;
    }
}
