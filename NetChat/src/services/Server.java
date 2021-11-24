package services;


import Object.Group;
import Object.User;
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
import java.util.HashSet;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    // 记录所有连接服务器的用户名，利用哈希集合存储保证用户名唯一
    private static final Set<String> names = new HashSet<>();

    private static final Set<PrintWriter> writers = new HashSet<>();
    private static final Set<OutputStream> fileSender = new HashSet<>();

    public static void main(String[] args) throws Exception {
        System.out.println("The chat server is running...");
        ExecutorService pool = Executors.newFixedThreadPool(500);
        try (ServerSocket listener = new ServerSocket(9000)) {
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
        private final DataToJson d2j=new DataToJson();
        private final JsonToData j2d=new JsonToData();
        private final CURD curd = new CURD();
        private final Utils ut=new Utils();

        public Handler(Socket socket){
            this.socket = socket;
        }

        public synchronized void Controller(String type,PrintWriter out,String req,InputStream _in,OutputStream _out){
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
                    extendName = ut.CheckAvatarFileType(user.getAvatar());
                    if (extendName == null) {
                        resp = d2j.DefaultResp("register",false, "不支持该头像文件格式");
                        out.println(resp);
                        break;
                    }
                    //修改头像文件字段为用户名加文件扩展名
                    user.setAvatar(user.getUsername() + "." + extendName);
                    status = curd.AddUser(user.getUsername(), user.getPassword(), user.getAvatar());
                    if (status == -1) {
                        resp = d2j.DefaultResp("register",false, "注册失败，该用户名已经存在");
                    } else {
                        resp = d2j.DefaultResp("register",true, "注册成功");
                        if (!ut.SaveAvatarPic(user.getUsername(), extendName, _in)) {
                            resp = d2j.DefaultResp("register",false, "服务器错误");
                        }

                    }
                    out.println(resp);
                }
                //处理登录
                case "login" -> {
                    user = j2d.LoginReq(req);
                    //到数据库中查询该用户
                    u = curd.GetUserByUsername(user.getUsername());
                    if (u == null || (!u.getPassword().equals(user.getPassword()))) {
                        resp = d2j.DefaultResp("login",false, "账号或者密码错误");
                    } else {
                        resp = d2j.getUserInfoResp(u);//登录成功
                        writers.add(out);//将对某个客户端的输出流加入writers哈希集合中
                        names.add(user.getUsername());//记录当前登录的用户的用户名
                        fileSender.add(_out);
                    }
                    out.println(resp);
                    if (!ut.SendAvatarPic(user.getUsername(), _out)) {
                        resp = d2j.DefaultResp("login",false, "获取头像失败");
                        out.println(resp);
                    }
                }
                case "search_user" -> {
                    if (obj.getString("filed") == null) {
                        resp = d2j.DefaultResp("",false, "搜索内容不能为空");
                        out.println(resp);
                        break;
                    }
                    u = curd.GetUserByUsername(obj.getString("filed"));
                    resp = d2j.getUserInfoResp(u);
                    out.println(resp);
                    if (!ut.SendAvatarPic(u.getUsername(), _out)) {
                        resp = d2j.DefaultResp("search_user",false, "获取头像失败");
                        out.println(resp);
                    }
                }
                case "search_group" -> {
                    if (obj.getString("filed") == null) {
                        resp = d2j.DefaultResp("search_group",false, "搜索内容不能为空");
                        out.println(resp);
                        break;
                    }
                    g = curd.getGroupById(obj.getInt("filed"));
                    resp = d2j.getGroupInfoResp(g);
                    out.println(resp);
                }
                case "add_friend" -> {
                    if (obj.getString("ua_id") == null || obj.getString("ub_id") == null) {
                        resp = d2j.DefaultResp("",false, "参数错误");
                        out.println(resp);
                        break;
                    }
                    status = curd.AddRelation(obj.getInt("ua_id"), obj.getInt("ub_id"), 1);
                    if (status == -1) {
                        resp = d2j.DefaultResp("add_friend",false, "添加好友失败");
                        out.println(resp);
                    }
                    resp = d2j.DefaultResp("add_friend",true, "添加好友成功");
                    out.println(resp);
                }
                case "add_group" -> {
                    if (obj.getString("ua_id") == null || obj.getString("ub_id") == null) {
                        resp = d2j.DefaultResp("add_group",false, "参数错误");
                        out.println(resp);
                        break;
                    }
                    status = curd.AddRelation(obj.getInt("ua_id"), obj.getInt("ub_id"), 0);
                    if (status == -1) {
                        resp = d2j.DefaultResp("add_group",false, "添加群聊失败");
                        out.println(resp);
                    }
                    resp = d2j.DefaultResp("add_group",true, "添加群聊成功");
                    out.println(resp);
                }
//                case ""


            }

        }

        public void run() {
            try {
                Scanner in = new Scanner(socket.getInputStream());
                out = new PrintWriter(socket.getOutputStream(), true);
                InputStream _in=socket.getInputStream();
                OutputStream _out=socket.getOutputStream();
                System.out.println("客户端连接");

                // 保持请求直到获取唯一的用户名
                while (true) {
                    String req = in.nextLine();//获取客户端请求
                    System.out.println(req);
                    if (req == null) {//如果没有输入则直接结束
                        return;
                    }
                    JSONObject obj = JSONObject.fromObject(req);
                    String type=obj.getString("type");//获取请求类型
                    if(type==null){
                        String resp=d2j.DefaultResp(false,"请求不合法");
                        out.println(resp);
                    }
                    Controller(type,out,req,_in,_out);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (out != null) {
                    writers.remove(out);
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