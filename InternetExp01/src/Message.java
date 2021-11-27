/**
 * 普通聊天消息对象
 */
public class Message {
    private String create_time;
    private String content;

    public Message(String create_time, String content) {
        this.create_time = create_time;
        this.content = content;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
