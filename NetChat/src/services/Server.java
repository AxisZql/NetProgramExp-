package services;


import Object.Group;
import Object.User;
import Object.Chat;
import database.CURD;
import net.sf.json.JSONObject;
import util.DataToJson;
import util.JsonToData;
import util.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * author：李志强
 * class：网络194
 * date：2021-11-24
 * 注：本项目github：https://github.com/AxisZql/NetProgramExp-
*/

public class Server {

    // 记录所有连接服务器的用户名，利用哈希集合存储保证用户名唯一
    private static final Map<String, OutputStream> fileSender = new HashMap<>(); //记录用户名和输出流的对应关系
    private static final Map<String, PrintWriter> writers = new HashMap<>();//建立用户名和字符发送流的关系


    public static void main(String[] args) throws Exception {
        System.out.println("The chat server is running...");
        ExecutorService pool = Executors.newFixedThreadPool(500);
        try (ServerSocket listener = new ServerSocket(9010)) {
            while (true) {
                pool.execute(new Handler(listener.accept()));
            }
        }
    }


    /**
     * 处理线程类
     */
    private static class Handler implements Runnable {
        private final Socket socket;
        private PrintWriter out;
        private User cur_user;
        private final DataToJson d2j = new DataToJson();
        private final JsonToData j2d = new JsonToData();
        private final CURD curd = new CURD();
        private final Utils ut = new Utils();

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public synchronized void Controller(String type, PrintWriter out, String req, InputStream _in, OutputStream _out, Scanner in) {
            JSONObject obj = JSONObject.fromObject(req);
            String resp;
            User user;
            User u;
            Group g;
            int status;
            String extendName;
            switch (Objects.requireNonNull(type)) {
                //处理注册
                case "register" -> {
                    user = j2d.RegisterReq(req);
                    //修改头像文件字段为用户名加文件扩展名
                    status = curd.AddUser(user.getUsername(), user.getPassword(), user.getAvatar());
                    if (status == -1) {
                        resp = d2j.DefaultResp("register", false, "注册失败，该用户名已经存在");
                    } else {
                        resp = d2j.DefaultResp("register", true, "注册成功");
                    }
                    out.println(resp);
                }
                //处理登录
                case "login" -> {
                    user = j2d.LoginReq(req);
                    //到数据库中查询该用户
                    u = curd.GetUserByUsername(user.getUsername());
                    if (u == null || (!u.getPassword().equals(user.getPassword()))) {
                        resp = d2j.DefaultResp("login", false, "账号或者密码错误");
                    } else {
                        resp = d2j.getUserInfoResp(u, "login");//登录成功
                        writers.put(user.getUsername(), out);//将对某个客户端的输出流加入writers字典中
                        fileSender.put(user.getUsername(), _out);
                        cur_user = user;
                    }
                    out.println(resp);
                }
                case "search_user" -> {
                    if (obj.getString("filed") == null) {
                        resp = d2j.DefaultResp("search_user", false, "搜索内容不能为空");
                        out.println(resp);
                        break;
                    }
                    u = curd.GetUserByUsername(obj.getString("filed"));
                    if (u == null) {
                        resp = d2j.DefaultResp("search_user", false, "不存在该用户");
                    } else {
                        resp = d2j.getUserInfoResp(u, "search_user");
                    }
                    out.println(resp);
                }
                case "search_group" -> {
                    if (obj.getString("filed") == null) {
                        resp = d2j.DefaultResp("search_group", false, "搜索内容不能为空");
                        out.println(resp);
                        break;
                    }
                    g = curd.getGroupById(obj.getInt("filed"));
                    if (g == null) {
                        resp = d2j.DefaultResp("search_group", false, "不存在该群聊");
                    } else {
                        resp = d2j.getGroupInfoResp(g, "search_group");
                    }
                    out.println(resp);
                }
                case "add_friend" -> {
                    if (obj.getString("ua_id") == null || obj.getString("ub_id") == null) {
                        resp = d2j.DefaultResp("", false, "参数错误");
                        out.println(resp);
                        break;
                    }
                    status = curd.AddRelation(obj.getInt("ua_id"), obj.getInt("ub_id"), 1);
                    if (status == -1) {
                        resp = d2j.DefaultResp("add_friend", false, "添加好友失败");
                        out.println(resp);
                    }
                    resp = d2j.DefaultResp("add_friend", true, "添加好友成功");
                    out.println(resp);
                }
                case "add_group" -> {
                    if (obj.getString("ua_id") == null || obj.getString("ub_id") == null) {
                        resp = d2j.DefaultResp("add_group", false, "参数错误");
                        out.println(resp);
                        break;
                    }
                    status = curd.AddRelation(obj.getInt("ua_id"), obj.getInt("ub_id"), 0);
                    if (status == -1) {
                        resp = d2j.DefaultResp("add_group", false, "添加群聊失败");
                        out.println(resp);
                    }
                    resp = d2j.DefaultResp("add_group", true, "添加群聊成功");
                    out.println(resp);
                }
                case "create_group" -> {
                    g = j2d.createGroup(req);
                    status = curd.AddGroup(g.getName(), g.getAvatar(), g.getCreated_by());
                    if (status == -1) {
                        resp = d2j.DefaultResp("create_group", false, "已经存在同名群聊，创建群聊失败");
                    } else {
                        resp = d2j.DefaultResp("create_group", true, "创建群聊成功");
                        cur_user=curd.GetUserByUsername(cur_user.getUsername());
                        List<Group> groupList=curd.getGroupByCreated_by(cur_user.getId());
                        for(Group _g:groupList){
                            if(!curd.CheckRelationGroup2Created_by(_g.getId(),cur_user.getId())){
                                curd.AddRelation(_g.getId(),cur_user.getId(),0);
                            }
                        }
                    }
                    out.println(resp);

                }
                case "get_friend_list" -> {
                    Integer cur_userId = Integer.valueOf((JSONObject.fromObject(req).getString("id")));
                    List<User> ul = curd.getUserList(cur_userId);//所有好友数据
                    if (ul == null) {
                        resp = d2j.DefaultResp("get_friend_list", true, "获取好友列表数据失败");
                    } else {
                        resp = d2j.getUserListResp(ul);
                    }
                    out.println(resp);
                }
                case "get_group_list" -> {
                    Integer cur_userId = Integer.valueOf((JSONObject.fromObject(req).getString("id")));
                    List<Group> ul = curd.getGroupList(cur_userId);//所有好友数据
                    if (ul == null) {
                        resp = d2j.DefaultResp("get_group_list", true, "获取群聊列表数据失败");
                    } else {
                        resp = d2j.getGroupListResp(ul);
                    }
                    out.println(resp);
                }
                case "friend_chat" -> {
                    Chat c = j2d.SendMsgReq(req);
                    int receiveId = c.getReceive();
                    if (c.getType()==1) {
                        User pu = curd.GetUserByUserID(receiveId);
                        //当前对应用户不在线的情况
                        if ((writers.get(pu.getUsername())) == null) {
                            c.setStatus(0);
                        }else{
                            c.setStatus(1);
                        }
                    }
                    //用户在线的情况
                    if(c.getType()==0){
                        c.setStatus(1);
                    }
                    status = curd.AddChat(c.getStatus(), c.getCreated_by(), c.getReceive(), c.getContent(),
                            c.getType(), c.getObject_type(), c.getFileName());
                    if (status == -1) {
                        resp = d2j.DefaultResp("friend_chat", false, "发送消息失败");
                    } else {
                        resp = d2j.DefaultResp("friend_chat", true, "发送消息成功");
                    }
                    out.println(resp);
                    //如果是一对一聊天
                    if (c.getStatus() == 1 && c.getType()==1) {
                        cur_user=curd.GetUserByUsername(cur_user.getUsername());
                        User pu=curd.GetUserByUserID(c.getReceive());
                        c=curd.getChat(cur_user.getId(),pu.getId());
                        resp = d2j.NewMsqResp(c);
                        writers.get(pu.getUsername()).println(resp);//将消息发送给对应用户
                    }
                    //如果是群聊
                    if (c.getStatus()==1 && c.getType()==0){
                        cur_user=curd.GetUserByUsername(cur_user.getUsername());
                        c=curd.getChat(cur_user.getId(),c.getReceive());
                        List<User> gul=curd.GetUserListByGroupID(c.getReceive());
                        resp = d2j.NewMsqResp(c);
                        for(User pu:gul){
                            if((writers.get(pu.getUsername()))!=null)
                            writers.get(pu.getUsername()).println(resp);//将消息发送给对应用户
                        }

                    }

                }
                case "friend_file_chat" -> {
                    long fileLenght = Integer.parseInt(in.nextLine());//获取文件长度
                    Chat c = j2d.SendFileMsgReq(req);
                    int receiveId = c.getReceive();
                    if (c.getType() == 1) {
                        User pu = curd.GetUserByUserID(receiveId);
                        //当前对应用户不在线的情况
                        if ((writers.get(pu.getUsername())) == null) {
                            resp = d2j.DefaultResp("friend_file_chat", false, "对方不在线无法发送文件消息");
                            out.println(resp);
                            break;
                        }
                    }

                    //用户在线的情况或者是群聊
                    c.setStatus(1);
                    status = curd.AddChat(c.getStatus(), c.getCreated_by(), c.getReceive(), c.getContent(),
                            c.getType(), c.getObject_type(), c.getFileName());
                    if (status == -1) {
                        resp = d2j.DefaultResp("friend_file_chat", false, "发送消息失败");
                    } else {
                        resp = d2j.DefaultResp("friend_file_chat", true, "发送消息成功");
                    }
                    out.println(resp);
                    if (status != -1 && c.getType() == 1) {
                        cur_user = curd.GetUserByUsername(cur_user.getUsername());
                        c = curd.getChat(cur_user.getId(), c.getReceive());
                        User pu = curd.GetUserByUserID(c.getReceive());
                        resp = d2j.NewMsqResp(c);
                        if ((writers.get(pu.getUsername())) != null) {
                            writers.get(pu.getUsername()).println(resp);
                            writers.get(pu.getUsername()).println(fileLenght);//发送文件长度
                            int len;
                            long l = fileLenght;
                            byte[] fileBytes = new byte[10240];
                            System.out.println("开始发送文件");
                            try {
                                while (l != 0) {
                                    len = _in.read(fileBytes);
                                    wait(500);
                                    l -= len;
                                    fileSender.get(pu.getUsername()).write(fileBytes, 0, len);
//                                    wait();

                                }
//                                fileSender.get(pu.getUsername()).flush();
                            } catch (IOException | InterruptedException e) {
                                e.printStackTrace();
                            }
                            System.out.println("文件发送完毕");
                        }
                    }
                    if (status != -1 && c.getType() == 0) {
                        cur_user = curd.GetUserByUsername(cur_user.getUsername());
                        c = curd.getChat(cur_user.getId(), c.getReceive());
                        List<User> gul = curd.GetUserListByGroupID(c.getReceive());
                        resp = d2j.NewMsqResp(c);
                        //通知用户接收文件
                        for (User pu : gul) {
                            if (!Objects.equals(pu.getUsername(), cur_user.getUsername()) && (writers.get(pu.getUsername())) != null) {
                                writers.get(pu.getUsername()).println(resp);
                                writers.get(pu.getUsername()).println(fileLenght);//发送文件长度
                            }
                        }
                        int len;
                        long l = fileLenght;
                        byte[] fileBytes = new byte[10240];
                        System.out.println("开始发送文件");
                        try {
                            while (l != 0) {
                                len = _in.read(fileBytes);
                                wait(500);
                                l -= len;
                                for (User pu : gul) {
                                    if (!Objects.equals(pu.getUsername(), cur_user.getUsername()) && (writers.get(pu.getUsername())) != null) {
                                        fileSender.get(pu.getUsername()).write(fileBytes, 0, len);
                                    }
                                }

                            }
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                case "get_friend_chat" -> {
                    Chat c = j2d.GetChatHistoryReq(req);
                    List<Chat> chatList = curd.getChatList(c.getCreated_by(), c.getReceive(), c.getType());
                    if (chatList == null) {
                        resp = d2j.DefaultResp("friend_file_chat", true, "获取聊天记录失败");
                        out.println(resp);
                        break;
                    }
                    resp = d2j.ChatHistory2Json(chatList);
                    out.println(resp);


                }


            }

        }

        public void run() {
            try {

                Scanner in = new Scanner(socket.getInputStream());
                out = new PrintWriter(socket.getOutputStream(), true);
                InputStream _in = socket.getInputStream();
                OutputStream _out = socket.getOutputStream();
                System.out.println("客户端连接");

                // 保持请求直到获取唯一的用户名
                while (true) {
                    String req = in.nextLine();//获取客户端请求
                    System.out.println(req);
                    if (req == null) {//如果没有输入则直接结束
                        return;
                    }
                    JSONObject obj = JSONObject.fromObject(req);
                    String type = obj.getString("type");//获取请求类型
                    if (type == null) {
                        String resp = d2j.DefaultResp(null, false, "请求不合法");
                        out.println(resp);
                    }
                    Controller(type, out, req, _in, _out, in);
                }

            } catch (Exception e) {
//                System.out.println(cur_user.getUsername()+"用户退出");]
                e.printStackTrace();
            } finally {
                if (out != null) {
                    writers.remove(cur_user.getUsername());
                    fileSender.remove(cur_user.getUsername());
                }

                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}