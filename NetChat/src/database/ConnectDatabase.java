package database;

import java.sql.*;
/**
 * author：李志强
 * class：网络194
 * date：2021-11-24
 * 注：本项目github：https://github.com/AxisZql/NetProgramExp-
 */
public class ConnectDatabase {
    /*
    @测试程序
     */
    public static void main(String args[]){
        Connection conn = null;
        try{
            Class.forName("org.sqlite.JDBC");
            conn=DriverManager.getConnection("jdbc:sqlite:src/database/chat.db");
            Statement stmt=conn.createStatement();
            String sql="insert into t_user(username,password,avatar) values(" +
                    "'test001','1qaz','myavatar.jpg','DSB')";
            stmt.executeUpdate(sql);
            stmt.close();
            conn.close();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            System.exit(0);
        }
        System.out.println("Successfully");
    }
    public Connection ConnectDB(){
        Connection conn = null;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:src/database/chat.db");
            return conn;//连接成功则返回conn
        }catch (SQLException | ClassNotFoundException e){
            e.printStackTrace();
        }
        return null;//连接失败则返回null
    }
}
