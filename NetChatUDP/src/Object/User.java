package Object;
/**
 * author：李志强
 * class：网络194
 * date：2021-11-24
 * 注：本项目github：https://github.com/AxisZql/NetProgramExp-
 */

//定义User结构体
public class User {
    private Integer id;
    private final String username;
    private String password;

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public  void setId(Integer id){
        this.id=id;
    }

    private String avatar;

    public User(String u, String p, String a, Integer id) {
        username = u;
        password = p;
        avatar = a;
        this.id = id;
    }

    // 注册请求体
    public User(String u, String p,  String a) {
        username = u;
        password = p;
        avatar = a;

    }

    //登录请求体
    public User( String u, String p) {
        username = u;
        password = p;
    }

    //查找用户请求体
    public User(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getAvatar() {
        return avatar;
    }

    public Integer getId() {
        return id;
    }




}
