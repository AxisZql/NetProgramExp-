package database;
import Object.Chat;
import Object.User;
import Object.Group;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
/**
 * author：李志强
 * class：网络194
 * date：2021-11-24
 * 注：本项目github：https://github.com/AxisZql/NetProgramExp-
 */
/*
数据库操作函数
 */
public class CURD {

    //初始化数据库连接
    private Connection ConnectDB() {
        ConnectDatabase cdb = new ConnectDatabase();
        return cdb.ConnectDB();
    }

    /*
    插入数据部分：INSERT DATA
     */
    public int AddUser(String username, String password, String avatar) {
        Connection conn = ConnectDB();
        try {
            PreparedStatement stmt = null;
            String sql = "insert into t_user (username,password,avatar)" +
                    "values(?,?,?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, avatar);
            return stmt.executeUpdate();//返回值代表受到影响的行数

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return -1;//如果错误返回-1
    }

    public int AddGroup(String name, String avatar, Integer created_by) {
        Connection conn = ConnectDB();
        try {
            PreparedStatement stmt = null;
            String sql = "insert into t_group (name,created_by,avatar)" +
                    "values(?,?,?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, String.valueOf(created_by));
            stmt.setString(3, avatar);
            return stmt.executeUpdate();//返回值代表受到影响的行数

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return -1;//如果错误返回-1
    }

    public int AddChat(Integer status, Integer created_by, Integer receive, String content, Integer type,
                       String object_type, String fieldName) {
        Connection conn = ConnectDB();
        try {
            PreparedStatement stmt = null;
            String sql = "insert into t_chat (status,created_by,receive,content,type,object_type,fileName)" +
                    "values(?,?,?,?,?,?,?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, String.valueOf(status));
            stmt.setString(2, String.valueOf(created_by));
            stmt.setString(3, String.valueOf(receive));
            stmt.setString(4, content);
            stmt.setString(5, String.valueOf(type));
            stmt.setString(6, object_type);
            stmt.setString(7, fieldName);
            return stmt.executeUpdate();//返回值代表受到影响的行数

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return -1;//如果错误返回-1
    }

    public int AddRelation(Integer ua_id, Integer ub_id, Integer type) {
        Connection conn = ConnectDB();
        try {
            PreparedStatement stmt = null;
            String sql = "insert into t_relation (ua_id,ub_id,type)" +
                    "values(?,?,?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, String.valueOf(ua_id));
            stmt.setString(2, String.valueOf(ub_id));
            stmt.setString(3, String.valueOf(type));
            return stmt.executeUpdate();//返回值代表受到影响的行数

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return -1;//如果错误返回-1
    }

    /*
    获取数据部分
     */
    //利用用户名获取对应用户的所有信息
    public User GetUserByUsername(String username) {
        Connection conn = ConnectDB();
        try {
            PreparedStatement stmt = null;
            String sql = "select * from t_user where username = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            rs.next();//读取数据
            //封装查询数据到User对象中
            User user = new User(rs.getString("username"), rs.getString("password"),
                    rs.getString("avatar"), rs.getInt("id"));
            rs.close();
            stmt.close();
            return user;
        } catch (SQLException e) {
            System.out.println("不存在该用户");
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                System.out.println("数据库关闭失败");
            }
        }
        return null;
    }

    //利用用户id获取对应用户的所有信息
    public User GetUserByUserID(Integer userid) {
        Connection conn = ConnectDB();
        try {
            PreparedStatement stmt = null;
            String sql = "select * from t_user where id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, String.valueOf(userid));
            ResultSet rs = stmt.executeQuery();
            rs.next();//读取数据
            //封装查询数据到User对象中
            User user = new User(rs.getString("username"), rs.getString("password"),
                    rs.getString("avatar"), rs.getInt("id"));
            rs.close();
            stmt.close();
            return user;
        } catch (SQLException e) {
            System.out.println("不存在该用户");
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                System.out.println("数据库关闭失败");
            }
        }
        return null;
    }

    public List<Group> getGroupByCreated_by(Integer created_by) {
        Connection conn = ConnectDB();
        try {
            PreparedStatement stmt = null;
            String sql = "select * from t_group where created_by =?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, String.valueOf(created_by));
            ResultSet rs = stmt.executeQuery();
            List<Group> groupList = new ArrayList<>();
            while (rs.next()) {
                Group group = new Group(rs.getInt("id"), rs.getString("name"),
                        rs.getInt("created_by"), rs.getDate("created_at"), rs.getString("avatar"));
                groupList.add(group);
            }
            rs.close();
            stmt.close();
            return groupList;
        } catch (SQLException e) {
            System.out.println("找不到该用户创建群聊");
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                System.out.println("数据库关闭失败");
            }
        }
        return null;
    }

    //查看创建者和群聊的关系是否存在
    public boolean CheckRelationGroup2Created_by(int gid, int uid) {
        Connection conn = ConnectDB();
        try {
            PreparedStatement stmt = null;
            String sql = "select * from t_relation where ua_id =? and ub_id=? and type=0";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, String.valueOf(gid));
            stmt.setString(2, String.valueOf(uid));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return true;
            }
            rs.close();
            stmt.close();
            return false;
        } catch (SQLException e) {
            System.out.println("找不到该群聊");
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                System.out.println("数据库关闭失败");
            }
        }
        return false;
    }

    //通过群聊id获取群聊信息
    public Group getGroupById(Integer id) {
        Connection conn = ConnectDB();
        try {
            PreparedStatement stmt = null;
            String sql = "select * from t_group where id =?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, String.valueOf(id));
            ResultSet rs = stmt.executeQuery();
            rs.next();
            Group group = new Group(rs.getInt("id"), rs.getString("name"),
                    rs.getInt("created_by"), rs.getDate("created_at"), rs.getString("avatar"));
            rs.close();
            stmt.close();
            return group;
        } catch (SQLException e) {
            System.out.println("找不到该群聊");
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                System.out.println("数据库关闭失败");
            }
        }
        return null;
    }

    //通过信息id获取信息
    public Chat getChat(Integer cid, Integer rid) {
        Connection conn = ConnectDB();
        try {
            PreparedStatement stmt = null;
            String sql = "select * from t_chat where  created_by=? and receive=? order by created_at desc";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, String.valueOf(cid));
            stmt.setString(2, String.valueOf(rid));
            ResultSet rs = stmt.executeQuery();
            rs.next();
            Chat chat = new Chat(rs.getInt("id"), rs.getInt("status"), rs.getInt("created_by"),
                    rs.getInt("receive"), rs.getString("content"), rs.getInt("type"), rs.getString("object_type"), rs.getString("fileName"),
                    rs.getDate("created_at"));
            rs.close();
            stmt.close();
            return chat;
        } catch (SQLException e) {
            System.out.println("找不到该信息");
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                System.out.println("数据库关闭失败");
            }
        }
        return null;
    }

    //通过用户id获取该用户的好友列表
    public List<User> getUserList(Integer id) {
        Connection conn = ConnectDB();
        try {
            PreparedStatement stmt = null;
            List<User> userlist = new ArrayList<User>();
            String sql = "select * from t_relation where (ua_id =? or ub_id=?) and type=1";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, String.valueOf(id));
            stmt.setString(2, String.valueOf(id));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int a = rs.getInt("ua_id");
                int b = rs.getInt("ub_id");
                int c = a;
                if (a == id) {
                    c = b;
                }
                User pu = GetUserByUserID(c);
                String username = pu.getUsername();
                User user = new User(username);
                userlist.add(user);
            }
            rs.close();
            stmt.close();
            return userlist;
        } catch (SQLException e) {
            System.out.println("无法查询该用户的好友列表");
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                System.out.println("数据库关闭失败");
            }
        }
        return null;
    }

    //通过用户id获取该用户的群聊列表
    public List<Group> getGroupList(Integer id) {
        Connection conn = ConnectDB();
        try {
            PreparedStatement stmt = null;
            List<Group> groupList = new ArrayList<Group>();
            String sql = "select * from t_relation where ub_id=? and type=0";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, String.valueOf(id));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Group group = new Group(rs.getInt("ua_id"));
                groupList.add(group);
            }
            rs.close();
            stmt.close();
            return groupList;
        } catch (SQLException e) {
            System.out.println("无法查询该用户的群聊列表");
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                System.out.println("数据库关闭失败");
            }
        }
        return null;
    }

    //获取聊天记录
    public List<Chat> getChatList(Integer created_by, Integer receive, Integer chat_type) {
        Connection conn = ConnectDB();
        try {
            PreparedStatement stmt = null;
            List<Chat> chatList = new ArrayList<Chat>();
            String sql = "";
            if (chat_type == 1) {
                sql = "select * from t_chat where ((created_by=? and receive=?) or (created_by=? and receive=?) ) and type=1 order by created_at asc";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, String.valueOf(created_by));
                stmt.setString(2, String.valueOf(receive));
                stmt.setString(3, String.valueOf(receive));
                stmt.setString(4, String.valueOf(created_by));
            } else if (chat_type == 0) {
                sql = "select * from t_chat where  receive=? and type=0 order by created_at asc";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, String.valueOf(receive));
            }
            assert stmt != null;
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Chat chat = new Chat(rs.getInt("id"), rs.getInt("status"), rs.getInt("created_by"), rs.getInt("receive"),
                        rs.getString("content"), rs.getInt("type"), rs.getString("object_type"),
                        rs.getString("fileName"), rs.getDate("created_at"));
                chatList.add(chat);
            }
            rs.close();
            stmt.close();
            return chatList;
        } catch (SQLException e) {
            System.out.println("无法查询该用户的对应聊天记录");
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                System.out.println("数据库关闭失败");
            }
        }
        return null;
    }

    public int UpdateChatStatus(Integer id) {
        Connection conn = ConnectDB();
        try {
            PreparedStatement stmt = null;
            List<Chat> chatList = new ArrayList<Chat>();
            String sql;

            sql = "update t_chat set status=1 where id=?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, String.valueOf(id));
            return stmt.executeUpdate();//返回值代表受到影响的行数

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return -1;
    }

        //根据用户名获取所有在该用户离线状态下未接收的聊天记录
    public List<Chat> getUreadChatList(Integer receive) {
        Connection conn = ConnectDB();
        try {
            PreparedStatement stmt = null;
            List<Chat> chatList = new ArrayList<Chat>();
            String sql;

            sql = "select * from t_chat where receive=? and type=1 and status=0 order by created_at asc";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, String.valueOf(receive));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Chat chat = new Chat(rs.getInt("id"), rs.getInt("status"), rs.getInt("created_by"), rs.getInt("receive"),
                        rs.getString("content"), rs.getInt("type"), rs.getString("object_type"),
                        rs.getString("fileName"), rs.getDate("created_at"));
                UpdateChatStatus(chat.getId());
                chatList.add(chat);
            }
            rs.close();
            stmt.close();
            return chatList;
        } catch (SQLException e) {
            System.out.println("无法查询该用户的对应聊天记录");
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                System.out.println("数据库关闭失败");
            }
        }
        return null;
    }

    //通过群聊获取所有在本群聊上的用户信息
    public List<User> GetUserListByGroupID(Integer gid) {
        Connection conn = ConnectDB();
        try {
            PreparedStatement stmt = null;
            List<User> userlist = new ArrayList<User>();
            String sql = "select t_user.* from (select * from t_relation where ua_id=? and type=0) as Pa , t_user where Pa.ub_id=t_user.id;";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, String.valueOf(gid));
            List<User> userList = new ArrayList<User>();
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                User user = new User(rs.getString("username"), rs.getString("password"),
                        rs.getString("avatar"), rs.getInt("id"));
                userList.add(user);
            }
            rs.close();
            stmt.close();
            return userList;

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }
}
