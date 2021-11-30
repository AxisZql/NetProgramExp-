package Object;

import java.util.Date;
/**
 * author：李志强
 * class：网络194
 * date：2021-11-24
 * 注：本项目github：https://github.com/AxisZql/NetProgramExp-
 */

//聊天记录结构体
public class Chat {
    private Integer id;
    private Integer status;
    private final Integer created_by;
    private final Integer receive;
    private String content;
    private final Integer type;
    private String object_type;
    private String fileName;
    private Date created_at;

    public Chat(Integer id, Integer status, Integer created_by, Integer receive, String content,
                Integer type, String object_type, String fileName, Date created_at) {
        this.id = id;
        this.status = status;
        this.created_by = created_by;
        this.receive = receive;
        this.content = content;
        this.type = type;
        this.object_type = object_type;
        this.fileName = fileName;
        this.created_at = created_at;
    }

    //常规聊天请求
    public Chat(Integer created_by, Integer receive, String content,
                Integer chat_type, String object_type) {
        this.created_by = created_by;
        this.receive = receive;
        this.content = content;
        this.type = chat_type;
        this.object_type = object_type;

    }

    // 发送文件聊天请求
    public Chat(Integer created_by, Integer receive, String content,
                Integer chat_type, String object_type, String fileName) {
        this.created_by = created_by;
        this.receive = receive;
        this.content = content;
        this.type = chat_type;
        this.object_type = object_type;
        this.fileName = fileName;

    }

    //聊天记录请求
    public Chat(Integer created_by, Integer receive, Integer chat_type) {
        this.created_by = created_by;
        this.receive = receive;
        this.type = chat_type;

    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public Integer getStatus() {
        return status;
    }

    public Integer getCreated_by() {
        return created_by;
    }

    public Integer getReceive() {
        return receive;
    }

    public String getContent() {
        return content;
    }

    public Integer getType() {
        return type;
    }

    public String getObject_type() {
        return object_type;
    }

    public String getFileName() {
        return fileName;
    }

    public Date getCreated_at() {
        return created_at;
    }
}
