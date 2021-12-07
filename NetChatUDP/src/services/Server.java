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
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * author：李志强
 * class：网络194
 * date：2021-12-7
 * 注：本项目github：https://github.com/AxisZql/NetProgramExp-
 */

public class Server {
    // 设定同一个ip的端口使用范围未9300 -- 19900 ，用户退出后要将对应的ip和端口数据在数据库中删除
    private static final Integer BPort = 9020;  //用户未登陆之前使用的端口
    private static final Map<String, String> UserHost = new HashMap<>();//建立起用户名和IP的映射
    private static final Map<String, Integer> UserPort = new HashMap<>();//建立用户和端口的映射

    public static void main(String[] args) throws Exception {
        System.out.println("The chat server is running...");
        ExecutorService pool = Executors.newFixedThreadPool(500);
        CURD curd = new CURD();
        try (DatagramSocket ds = new DatagramSocket(BPort)) {
            while (true) {
                // 用户打开窗口要先告知服务器,随便发送一个字符串
                byte[] buf = new byte[1024];
                DatagramPacket dp = new DatagramPacket(buf, buf.length);
                ds.receive(dp);
                System.out.println("有人访问9020端口");
                int _port = -1;
                //在这里接收到后为对应的请求发送回应，并分配对应端口
                for (int i = 9300; i <= 19900; i++) {
                    Boolean status = curd.CheckHostPort(dp.getAddress().getHostAddress(), String.valueOf(i));
                    if (status) {//对应ip的对应端口已经被同一个ip的另一个用户使用
                        continue;
                    }
                    // 如果没有被使用
                    int _status = curd.HostPort(dp.getAddress().getHostAddress(), String.valueOf(i));
                    if (_status == -1) {
                        continue;
                    }
                    _port = i;
                    break;
                }
                if (_port != -1) {
                    String resp = "Port:" + _port;
                    byte[] out_buf = resp.getBytes();
                    dp = new DatagramPacket(out_buf, out_buf.length, InetAddress.getByName(dp.getAddress().getHostAddress()), BPort);
                    ds.send(dp);
                    System.out.println(1);
                } else {
                    continue;//如果分配端口失败则不给对应客户端任何回应
                }

                pool.execute(new Handler(dp.getAddress().getHostAddress(), _port));
            }
        }
    }


    /**
     * 处理线程类
     */
    private static class Handler implements Runnable {
        private User cur_user;
        private final DataToJson d2j = new DataToJson();
        private final JsonToData j2d = new JsonToData();
        private final CURD curd = new CURD();
        private final Utils ut = new Utils();

        //记录对应用户的端口和IP
        private final String host;
        private final Integer port;
        private DatagramSocket ds;

        private DatagramPacket in_dp;
        private DatagramPacket out_dp;

        public Handler(String host, Integer port) {
            this.host = host;
            this.port = port;
        }

        public synchronized void Controller(String type, String req) throws IOException {
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
                    byte[] out_buf = resp.getBytes(StandardCharsets.UTF_8);
                    this.out_dp = new DatagramPacket(out_buf, out_buf.length, InetAddress.getByName(this.host), this.port);
                    this.ds.send(this.out_dp);
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
                        UserHost.put(user.getUsername(), this.host);//建立起用户名和主机的映射关系
                        UserPort.put(user.getUsername(), this.port);//建立用户名和端口的映射关系
                        cur_user = user;
                    }
                    byte[] out_buf = resp.getBytes(StandardCharsets.UTF_8);
                    this.out_dp = new DatagramPacket(out_buf, out_buf.length, InetAddress.getByName(this.host), this.port);
                    this.ds.send(this.out_dp);
                }
                case "search_user" -> {
                    if (obj.getString("filed") == null) {
                        resp = d2j.DefaultResp("search_user", false, "搜索内容不能为空");
                        byte[] out_buf = resp.getBytes(StandardCharsets.UTF_8);
                        this.out_dp = new DatagramPacket(out_buf, out_buf.length, InetAddress.getByName(this.host), this.port);
                        this.ds.send(this.out_dp);
                        break;
                    }
                    u = curd.GetUserByUsername(obj.getString("filed"));
                    if (u == null) {
                        resp = d2j.DefaultResp("search_user", false, "不存在该用户");
                    } else {
                        resp = d2j.getUserInfoResp(u, "search_user");
                    }
                    byte[] out_buf = resp.getBytes(StandardCharsets.UTF_8);
                    this.out_dp = new DatagramPacket(out_buf, out_buf.length, InetAddress.getByName(this.host), this.port);
                    this.ds.send(this.out_dp);
                }
                case "search_group" -> {
                    if (obj.getString("filed") == null) {
                        resp = d2j.DefaultResp("search_group", false, "搜索内容不能为空");
                        byte[] out_buf = resp.getBytes(StandardCharsets.UTF_8);
                        this.out_dp = new DatagramPacket(out_buf, out_buf.length, InetAddress.getByName(this.host), this.port);
                        this.ds.send(this.out_dp);
                        break;
                    }
                    g = curd.getGroupById(obj.getInt("filed"));
                    if (g == null) {
                        resp = d2j.DefaultResp("search_group", false, "不存在该群聊");
                    } else {
                        resp = d2j.getGroupInfoResp(g, "search_group");
                    }
                    byte[] out_buf = resp.getBytes(StandardCharsets.UTF_8);
                    this.out_dp = new DatagramPacket(out_buf, out_buf.length, InetAddress.getByName(this.host), this.port);
                    this.ds.send(this.out_dp);
                }
                case "add_friend" -> {
                    if (obj.getString("ua_id") == null || obj.getString("ub_id") == null) {
                        resp = d2j.DefaultResp("", false, "参数错误");
                        byte[] out_buf = resp.getBytes(StandardCharsets.UTF_8);
                        this.out_dp = new DatagramPacket(out_buf, out_buf.length, InetAddress.getByName(this.host), this.port);
                        this.ds.send(this.out_dp);
                        break;
                    }
                    status = curd.AddRelation(obj.getInt("ua_id"), obj.getInt("ub_id"), 1);
                    if (status == -1) {
                        resp = d2j.DefaultResp("add_friend", false, "添加好友失败");
                    } else {
                        resp = d2j.DefaultResp("add_friend", true, "添加好友成功");
                    }
                    byte[] out_buf = resp.getBytes(StandardCharsets.UTF_8);
                    this.out_dp = new DatagramPacket(out_buf, out_buf.length, InetAddress.getByName(this.host), this.port);
                    this.ds.send(this.out_dp);
                }
                case "add_group" -> {
                    if (obj.getString("ua_id") == null || obj.getString("ub_id") == null) {
                        resp = d2j.DefaultResp("add_group", false, "参数错误");
                        byte[] out_buf = resp.getBytes(StandardCharsets.UTF_8);
                        this.out_dp = new DatagramPacket(out_buf, out_buf.length, InetAddress.getByName(this.host), this.port);
                        this.ds.send(this.out_dp);
                        break;
                    }
                    status = curd.AddRelation(obj.getInt("ua_id"), obj.getInt("ub_id"), 0);
                    if (status == -1) {
                        resp = d2j.DefaultResp("add_group", false, "添加群聊失败");
                        byte[] out_buf = resp.getBytes(StandardCharsets.UTF_8);
                        this.out_dp = new DatagramPacket(out_buf, out_buf.length, InetAddress.getByName(this.host), this.port);
                        this.ds.send(this.out_dp);
                    } else {
                        resp = d2j.DefaultResp("add_group", true, "添加群聊成功");
                    }

                    byte[] out_buf = resp.getBytes(StandardCharsets.UTF_8);
                    this.out_dp = new DatagramPacket(out_buf, out_buf.length, InetAddress.getByName(this.host), this.port);
                    this.ds.send(this.out_dp);
                }
                case "create_group" -> {
                    g = j2d.createGroup(req);
                    status = curd.AddGroup(g.getName(), g.getAvatar(), g.getCreated_by());
                    if (status == -1) {
                        resp = d2j.DefaultResp("create_group", false, "已经存在同名群聊，创建群聊失败");
                    } else {
                        resp = d2j.DefaultResp("create_group", true, "创建群聊成功");
                        cur_user = curd.GetUserByUsername(cur_user.getUsername());
                        List<Group> groupList = curd.getGroupByCreated_by(cur_user.getId());
                        for (Group _g : groupList) {
                            if (!curd.CheckRelationGroup2Created_by(_g.getId(), cur_user.getId())) {
                                curd.AddRelation(_g.getId(), cur_user.getId(), 0);
                            }
                        }
                    }
                    byte[] out_buf = resp.getBytes(StandardCharsets.UTF_8);
                    this.out_dp = new DatagramPacket(out_buf, out_buf.length, InetAddress.getByName(this.host), this.port);
                    this.ds.send(this.out_dp);

                }
                case "get_friend_list" -> {
                    Integer cur_userId = Integer.valueOf((JSONObject.fromObject(req).getString("id")));
                    List<User> ul = curd.getUserList(cur_userId);//所有好友数据
                    if (ul == null) {
                        resp = d2j.DefaultResp("get_friend_list", true, "获取好友列表数据失败");
                    } else {
                        resp = d2j.getUserListResp(ul);
                    }
                    byte[] out_buf = resp.getBytes(StandardCharsets.UTF_8);
                    this.out_dp = new DatagramPacket(out_buf, out_buf.length, InetAddress.getByName(this.host), this.port);
                    this.ds.send(this.out_dp);
                }
                case "get_group_list" -> {
                    Integer cur_userId = Integer.valueOf((JSONObject.fromObject(req).getString("id")));
                    List<Group> ul = curd.getGroupList(cur_userId);//所有好友数据
                    if (ul == null) {
                        resp = d2j.DefaultResp("get_group_list", true, "获取群聊列表数据失败");
                    } else {
                        resp = d2j.getGroupListResp(ul);
                    }
                    byte[] out_buf = resp.getBytes(StandardCharsets.UTF_8);
                    this.out_dp = new DatagramPacket(out_buf, out_buf.length, InetAddress.getByName(this.host), this.port);
                    this.ds.send(this.out_dp);
                }
                case "friend_chat" -> {
                    Chat c = j2d.SendMsgReq(req);
                    int receiveId = c.getReceive();
                    if (c.getType() == 1) {
                        User pu = curd.GetUserByUserID(receiveId);
                        //当前对应用户不在线的情况
                        if ((UserHost.get(pu.getUsername())) == null) {
                            c.setStatus(0);
                        } else {
                            c.setStatus(1);
                        }
                    }
                    //用户在线的情况
                    if (c.getType() == 0) {
                        c.setStatus(1);
                    }
                    status = curd.AddChat(c.getStatus(), c.getCreated_by(), c.getReceive(), c.getContent(), c.getType(), c.getObject_type(), c.getFileName());
                    if (status == -1) {
                        resp = d2j.DefaultResp("friend_chat", false, "发送消息失败");
                    } else {
                        resp = d2j.DefaultResp("friend_chat", true, "发送消息成功");
                    }
                    byte[] out_buf = resp.getBytes(StandardCharsets.UTF_8);
                    this.out_dp = new DatagramPacket(out_buf, out_buf.length, InetAddress.getByName(this.host), this.port);
                    this.ds.send(this.out_dp);
                    //如果是一对一聊天
                    if (c.getStatus() == 1 && c.getType() == 1) {
                        cur_user = curd.GetUserByUsername(cur_user.getUsername());
                        User pu = curd.GetUserByUserID(c.getReceive());
                        c = curd.getChat(cur_user.getId(), pu.getId());
                        resp = d2j.NewMsqResp(c);
                        out_buf = resp.getBytes(StandardCharsets.UTF_8);
                        this.out_dp = new DatagramPacket(out_buf, out_buf.length, InetAddress.getByName(UserHost.get(pu.getUsername())), UserPort.get(pu.getUsername()));
                        this.ds.send(this.out_dp);
                    }
                    //如果是群聊
                    if (c.getStatus() == 1 && c.getType() == 0) {
                        cur_user = curd.GetUserByUsername(cur_user.getUsername());
                        c = curd.getChat(cur_user.getId(), c.getReceive());
                        List<User> gul = curd.GetUserListByGroupID(c.getReceive());
                        resp = d2j.NewMsqResp(c);
                        out_buf = resp.getBytes(StandardCharsets.UTF_8);
                        for (User pu : gul) {
                            if ((UserHost.get(pu.getUsername())) != null) {
                                this.out_dp = new DatagramPacket(out_buf, out_buf.length, InetAddress.getByName(UserHost.get(pu.getUsername())), UserPort.get(pu.getUsername()));
                                this.ds.send(this.out_dp);
                            }
                        }

                    }

                }
                case "friend_file_chat" -> {
                    byte[] in_buf = new byte[1024];
                    this.in_dp = new DatagramPacket(in_buf,in_buf.length);
                    this.ds.receive(this.in_dp);

                    byte[] data = this.in_dp.getData();
                    String val = new String(data,0,this.in_dp.getLength());
                    long fileLenght = Integer.parseInt(val);//获取文件长度
                    Chat c = j2d.SendFileMsgReq(req);
                    int receiveId = c.getReceive();
                    if (c.getType() == 1) {
                        User pu = curd.GetUserByUserID(receiveId);
                        //当前对应用户不在线的情况
                        if ((UserHost.get(pu.getUsername())) == null) {
                            resp = d2j.DefaultResp("friend_file_chat", false, "对方不在线无法发送文件消息");
                            byte[] out_buf = resp.getBytes(StandardCharsets.UTF_8);
                            this.out_dp = new DatagramPacket(out_buf, out_buf.length, InetAddress.getByName(this.host), this.port);
                            this.ds.send(this.out_dp);
                            break;
                        }
                    }
                    //用户在线的情况或者是群聊
                    c.setStatus(1);
                    status = curd.AddChat(c.getStatus(), c.getCreated_by(), c.getReceive(), c.getContent(), c.getType(), c.getObject_type(), c.getFileName());
                    if (status == -1) {
                        resp = d2j.DefaultResp("friend_file_chat", false, "发送消息失败");
                    } else {
                        resp = d2j.DefaultResp("friend_file_chat", true, "发送消息成功");
                    }
                    byte[] out_buf = resp.getBytes(StandardCharsets.UTF_8);
                    this.out_dp = new DatagramPacket(out_buf, out_buf.length, InetAddress.getByName(this.host), this.port);
                    this.ds.send(this.out_dp);

                    // 私发文件
                    if (status != -1 && c.getType() == 1) {
                        cur_user = curd.GetUserByUsername(cur_user.getUsername());
                        c = curd.getChat(cur_user.getId(), c.getReceive());
                        User pu = curd.GetUserByUserID(c.getReceive());
                        resp = d2j.NewMsqResp(c);
                        if ((UserHost.get(pu.getUsername())) != null) {
                            out_buf = resp.getBytes(StandardCharsets.UTF_8);
                            this.out_dp = new DatagramPacket(out_buf, out_buf.length, InetAddress.getByName(UserHost.get(pu.getUsername())), UserPort.get(pu.getUsername()));
                            this.ds.send(this.out_dp);
//                            writers.get(pu.getUsername()).println(resp);
                            out_buf = String.valueOf(fileLenght).getBytes(StandardCharsets.UTF_8);//发送文件长度
                            this.out_dp = new DatagramPacket(out_buf, out_buf.length, InetAddress.getByName(UserHost.get(pu.getUsername())), UserPort.get(pu.getUsername()));
                            this.ds.send(this.out_dp);
//                            writers.get(pu.getUsername()).println(fileLenght);//发送文件长度
                            int len;
                            long l = fileLenght;
                            byte[] fileBytes = new byte[10240];
                            this.in_dp = new DatagramPacket(fileBytes,fileBytes.length);
                            System.out.println("开始发送文件");
                            try {
                                while (l != 0) {
//                                    len = _in.read(fileBytes);
                                    this.ds.receive(this.in_dp);
                                    len = this.in_dp.getLength();
                                    wait(500);
                                    l -= len;
//                                    fileSender.get(pu.getUsername()).write(fileBytes, 0, len);
//                                    wait();
                                    out_buf = this.in_dp.getData();
                                    this.out_dp = new DatagramPacket(out_buf,out_buf.length,InetAddress.getByName(UserHost.get(pu.getUsername())), UserPort.get(pu.getUsername()));
                                    this.ds.send(out_dp);

                                }
//                                fileSender.get(pu.getUsername()).flush();
                            } catch (IOException | InterruptedException e) {
                                e.printStackTrace();
                            }
                            System.out.println("文件发送完毕");
                        }
                    }
                    // 群发文件
                    if (status != -1 && c.getType() == 0) {
                        cur_user = curd.GetUserByUsername(cur_user.getUsername());
                        c = curd.getChat(cur_user.getId(), c.getReceive());
                        List<User> gul = curd.GetUserListByGroupID(c.getReceive());
                        resp = d2j.NewMsqResp(c);
                        //通知用户接收文件
                        for (User pu : gul) {
                            if (!Objects.equals(pu.getUsername(), cur_user.getUsername()) && (UserHost.get(pu.getUsername())) != null) {
                                out_buf = resp.getBytes(StandardCharsets.UTF_8);
                                this.out_dp = new DatagramPacket(out_buf, out_buf.length, InetAddress.getByName(UserHost.get(pu.getUsername())),UserPort.get(pu.getUsername()));
                                this.ds.send(this.out_dp);
//                                writers.get(pu.getUsername()).println(resp);
//                                writers.get(pu.getUsername()).println(fileLenght);//发送文件长度
                                out_buf = String.valueOf(fileLenght).getBytes(StandardCharsets.UTF_8);//发送文件长度
                                this.out_dp = new DatagramPacket(out_buf, out_buf.length, InetAddress.getByName(UserHost.get(pu.getUsername())), UserPort.get(pu.getUsername()));
                                this.ds.send(this.out_dp);
                            }
                        }
                        int len;
                        long l = fileLenght;
                        byte[] fileBytes = new byte[10240];
                        this.in_dp = new DatagramPacket(fileBytes,fileBytes.length);
                        System.out.println("开始发送文件");
                        try {
                            while (l != 0) {
//                                len = _in.read(fileBytes);

                                this.ds.receive(this.in_dp);
                                len = this.in_dp.getLength();
                                wait(500);
                                l -= len;
                                out_buf = this.in_dp.getData();
                                for (User pu : gul) {
                                    if (!Objects.equals(pu.getUsername(), cur_user.getUsername()) && (UserHost.get(pu.getUsername())) != null) {
//                                        fileSender.get(pu.getUsername()).write(fileBytes, 0, len);
                                        this.out_dp = new DatagramPacket(out_buf,out_buf.length,InetAddress.getByName(UserHost.get(pu.getUsername())),UserPort.get(pu.getUsername()));
                                        this.ds.send(out_dp);
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
                        byte[] out_buf = resp.getBytes(StandardCharsets.UTF_8);
                        this.out_dp = new DatagramPacket(out_buf, out_buf.length, InetAddress.getByName(this.host), this.port);
                        this.ds.send(this.out_dp);
                        break;
                    }
                    resp = d2j.ChatHistory2Json(chatList);
                    byte[] out_buf = resp.getBytes(StandardCharsets.UTF_8);
                    this.out_dp = new DatagramPacket(out_buf, out_buf.length, InetAddress.getByName(this.host), this.port);
                    this.ds.send(this.out_dp);


                }


            }

        }

        public void run() {
            try {
                byte[] buf = new byte[2048];//用来接收json数据的缓存
                this.in_dp = new DatagramPacket(buf, buf.length);
                System.out.println("客户端连接");
                this.ds = new DatagramSocket(this.port);

                // 保持请求直到获取唯一的用户名
                while (true) {
                    this.ds.receive(this.in_dp);
                    byte[] data = this.in_dp.getData();//获取客户端请求
                    int len = this.in_dp.getLength();
                    String req = new String(data, 0, len);
                    System.out.println(req);

                    JSONObject obj = JSONObject.fromObject(req);
                    String type = obj.getString("type");//获取请求类型
                    if (type == null) {
                        String resp = d2j.DefaultResp(null, false, "请求不合法");
                        byte[] out_buf = resp.getBytes(StandardCharsets.UTF_8);
                        this.out_dp = new DatagramPacket(out_buf, out_buf.length, InetAddress.getByName(this.host), this.port);
                    } else if (type.equals("outLogin")) {
                        break;//用户退出登陆
                    }
                    Controller(type, req);
                }

            } catch (Exception e) {
                System.out.println(cur_user.getUsername() + "用户退出");
                e.printStackTrace();
            } finally {
                UserHost.remove(cur_user.getUsername());
                UserPort.remove(cur_user.getUsername());
                ds.close();
                ds.close();
                System.out.println(cur_user.getUsername() + "用户退出");
                curd.DelHostPort(this.host, String.valueOf(this.port));//解除对应ip和端口的占用情况
            }
        }
    }
}