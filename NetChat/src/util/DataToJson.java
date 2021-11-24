package util;

import Object.Group;
import Object.User;
import Object.Chat;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.List;

public class DataToJson {

    // 默认Json格式提醒
    public String DefaultResp(String type,boolean success,String msg) {
        JSONObject result = new JSONObject();
        result.put("type",type);
        result.put("success", success);
        result.put("msg", msg);
        return result.toString();
    }

    // 好友列表接口返回的json数据
    public String getUserListResp(List<User> userlist) {
        JSONObject result = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        result.put("success", true);
        result.put("msg", "获取好友列表成功");
        int count = 0;
        for (User user : userlist) {
            JSONObject u = new JSONObject();
            u.put("id", user.getId());
            jsonArray.add(count, u);
            count += 1;
        }
        result.element("data", jsonArray);
        return result.toString();
    }

    // 通过用户名查询用户信息返回的json数据
    public String getUserInfoResp(User user) {
        JSONObject result = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        result.put("success", true);
        result.put("msg", "获取用户信息成功");

        JSONObject u = new JSONObject();
        u.put("id", user.getId());
        u.put("username", user.getUsername());
        u.put("nickname", user.getNickname());
        u.put("fileName", user.getAvatar());
        jsonArray.add(0, u);
        result.element("data", jsonArray);
        return result.toString();

    }

    // 群聊列表接口返回的json数据
    public String getGroupListResp(List<Group> grouplist) {
        JSONObject result = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        result.put("success", true);
        result.put("msg", "获取群聊列表成功");
        int count = 0;
        for (Group group : grouplist) {
            JSONObject u = new JSONObject();
            u.put("id", group.getId());
            jsonArray.add(count, u);
            count += 1;
        }
        result.element("data", jsonArray);
        return result.toString();
    }

    // 通过用户名查询群聊信息返回的json数据
    public String getGroupInfoResp(Group group) {
        JSONObject result = new JSONObject();
        JSONArray jsonArray = new JSONArray();
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
            u.put("next_id", chat.getNext_id());
            u.put("created_by", chat.getCreated_by());
            u.put("receive",chat.getReceive());
            u.put("content",chat.getContent());
            u.put("type",chat.getType());
            u.put("created_at",chat.getCreated_at());
            u.put("object_type",chat.getObject_type());
            u.put("fileName",chat.getFileName());
            jsonArray.add(count, u);
            count+=1;
        }
        result.element("data", jsonArray);
        return result.toString();
    }


}
