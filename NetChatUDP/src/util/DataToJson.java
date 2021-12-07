package util;

import Object.Group;
import Object.User;
import Object.Chat;
import database.CURD;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.List;
/**
 * author：李志强
 * class：网络194
 * date：2021-11-24
 * 注：本项目github：https://github.com/AxisZql/NetProgramExp-
 */

public class DataToJson {

    // 默认Json格式提醒
    public String DefaultResp(String type,boolean success,String msg) {
        JSONObject result = new JSONObject();
        result.put("type",type);
        result.put("success", success);
        result.put("msg", msg);
        JSONArray jsonArray = new JSONArray();
        result.element("data",jsonArray);
        return result.toString();
    }

    // 好友列表接口返回的json数据
    public String getUserListResp(List<User> userlist) {
        JSONObject result = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        result.put("type","get_friend_list");
        result.put("success", true);
        result.put("msg", "获取好友列表成功");
        int count = 0;
        for (User user : userlist) {
//            JSONObject u = new JSONObject();
//            u.put("id", user.getId());
            jsonArray.add(count, user.getUsername());
            count += 1;
        }
        result.element("data", jsonArray);
        return result.toString();
    }

    // 通过用户名查询用户信息返回的json数据
    public String getUserInfoResp(User user,String type) {
        JSONObject result = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        result.put("type",type);
        result.put("success", true);
        result.put("msg", "获取用户信息成功");

        JSONObject u = new JSONObject();
        u.put("id", user.getId());
        u.put("username", user.getUsername());
        u.put("fileName", user.getAvatar());
        jsonArray.add(0, u);
        result.element("data", jsonArray);
        if (type.equals("login")){
            JSONArray pre_msg = GetUnreadChatJsonArray(user);
            result.element("pre_msg", pre_msg);
        }
        return result.toString();

    }

    //将所有未读消息转为json数组对象
    public JSONArray GetUnreadChatJsonArray(User user){
        CURD curd = new CURD();
        JSONArray jsonArray = new JSONArray();
        List<Chat> chatList=curd.getUreadChatList(user.getId());
        int count =0;
        for (Chat chat:chatList){
            JSONObject obj=Chat2Json(chat);
            curd.UpdateChatStatus(chat.getId());
            jsonArray.add(count,obj);
            count+=1;
        }
        return jsonArray;
    }

    //将聊天记录转json格式对象
    public JSONObject Chat2Json(Chat chat){
        CURD curd= new CURD();
        User user=curd.GetUserByUserID(chat.getCreated_by());
        JSONObject result=new JSONObject();
        result.put("id",chat.getId());
        result.put("created_by_user",user.getUsername());
        result.put("created_by",chat.getCreated_by());
        result.put("receive",chat.getReceive());
        result.put("content",chat.getContent());
        result.put("type",chat.getType());
        result.put("created_at",chat.getCreated_at().toString());
        result.put("object_type",chat.getObject_type());
        result.put("fileName",chat.getFileName());
        return result;
    }

    //将聊天记录转换为json格式的
    public String ChatHistory2Json(List<Chat> chatList){
        JSONObject result = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        result.put("type","get_friend_chat");
        result.put("success", true);
        result.put("msg", "获取聊天记录成功");
        int count=0;
        for(Chat chat:chatList){
            JSONObject obj=Chat2Json(chat);
            jsonArray.add(count,obj);
            count+=1;
        }
        result.element("data",jsonArray);
        return  result.toString();
    }

    // 群聊列表接口返回的json数据
    public String getGroupListResp(List<Group> grouplist) {
        JSONObject result = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        result.put("type","get_group_list");
        result.put("success", true);
        result.put("msg", "获取群聊列表成功");
        int count = 0;
        for (Group group : grouplist) {
            jsonArray.add(count, group.getId());
            count += 1;
        }
        result.element("data", jsonArray);
        return result.toString();
    }

    // 通过用户名查询群聊信息返回的json数据
    public String getGroupInfoResp(Group group,String type) {
        JSONObject result = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        result.put("type",type);
        result.put("success", true);
        result.put("msg", "获取用户信息成功");

        JSONObject u = new JSONObject();
        u.put("id", group.getId());
        u.put("name", group.getName());
        u.put("fileName", group.getAvatar());
        jsonArray.add(0, u);
        result.element("data", jsonArray);
        return result.toString();
    }

    // 通过用户名查询群聊信息返回的json数据
    public String getChatListResp(List<Chat> chatList) {
        JSONObject result = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        result.put("success", true);
        result.put("msg", "获取聊天记录成功");
        int count = 0;
        for (Chat chat : chatList) {
            JSONObject u = new JSONObject();
            u.put("id", chat.getId());
            u.put("created_by", chat.getCreated_by());
            u.put("receive",chat.getReceive());
            u.put("content",chat.getContent());
            u.put("type",chat.getType());
            u.put("created_at",chat.getCreated_at().toString());
            u.put("object_type",chat.getObject_type());
            u.put("fileName",chat.getFileName());
            jsonArray.add(count, u);
            count+=1;
        }
        result.element("data", jsonArray);
        return result.toString();
    }

    //新消息通知
    public String NewMsqResp(Chat chat){
        CURD curd=new CURD();
        User user=curd.GetUserByUserID(chat.getCreated_by());
        JSONObject result = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        result.put("type","new_msg");
        result.put("success", true);
        result.put("msg", "收到新消息");
        JSONObject u = new JSONObject();
        u.put("id", chat.getId());
        u.put("created_by_user",user.getUsername());
        u.put("created_by", chat.getCreated_by());
        u.put("receive",chat.getReceive());
        u.put("content",chat.getContent());
        u.put("type",chat.getType());
        u.put("created_at",chat.getCreated_at().toString());
        u.put("object_type",chat.getObject_type());
        u.put("fileName",chat.getFileName());
        jsonArray.add(0, u);
        result.element("data", jsonArray);
        return result.toString();
    }
}
