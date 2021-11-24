package util;

import Object.User;
import Object.Relation;
import Object.Group;
import net.sf.json.JSONObject;

//解析json格式数据
public class JsonToData {
    //注册请求json解析
    public User RegisterReq(String req){
        JSONObject obj = JSONObject.fromObject(req);
        return new User(obj.getString("username"),obj.getString("password"),
                obj.getString("fileName"));
    }

    //登录请求json解析
    public User LoginReq(String req){
        JSONObject obj = JSONObject.fromObject(req);
        return new User(obj.getString("username"),obj.getString("password"));
    }

    //搜索用户请求json解析
    public User SearchUserReq(String req){
        JSONObject obj = JSONObject.fromObject(req);
        return new User(obj.getString("type"),obj.getString("field"));
    }

    //搜索群聊请求json解析
    public Group SearchGroup(String req){
        JSONObject obj = JSONObject.fromObject(req);
        return new Group(obj.getInt("file"));
    }

    //添加好友、添加群聊请求json解析
    public Relation AddFriendReq(String req){
        JSONObject obj = JSONObject.fromObject(req);
        String Type = obj.getString("type");
        Relation rel=null;
        if(Type.equals("add_friend")){
             rel = new Relation(1,obj.getInt("ua_id"),obj.getInt("ub_id"));
        }else if(Type.equals("add_group")){
             rel = new Relation(0,obj.getInt("ua_id"),obj.getInt("ub_id"));
        }
        return rel;
    }

    // 创建群聊请求json解析
    public Group createGroup(String req){
        JSONObject obj = JSONObject.fromObject(req);
        return new Group(obj.getString("name"),obj.getInt("created_by"),obj.getString("fileName"));
    }


}


