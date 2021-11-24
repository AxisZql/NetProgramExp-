package Object;

import java.util.Date;

//聊天记录结构体
public class Chat {
    private final Integer id;
    private final Integer status;
    private final Integer created_by;
    private final Integer receive;
    private final String content;
    private final Integer type;
    private final String object_type;
    private final String fileName;
    private final Date created_at;

    public Chat(Integer id,Integer status,Integer created_by,Integer receive,String content,
                Integer type,String object_type,String fileName,Date created_at){
        this.id=id;
        this.status=status;
        this.created_by=created_by;
        this.receive=receive;
        this.content=content;
        this.type=type;
        this.object_type=object_type;
        this.fileName=fileName;
        this.created_at=created_at;
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
