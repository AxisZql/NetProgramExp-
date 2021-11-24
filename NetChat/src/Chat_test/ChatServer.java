package Chat_test;

import services.Server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ChatServer {

    // 记录所有连接服务器的用户名，利用哈希集合存储保证用户名唯一
    private static Set<String> names = new HashSet<>();

    private static Set<PrintWriter> writers = new HashSet<>();
    private static Set<OutputStream> file_outs = new HashSet<>();

    public static void main(String[] args) throws Exception {
        System.out.println("The chat server is running...");
        ExecutorService pool = Executors.newFixedThreadPool(500);
        ServerSocket fileServer = new ServerSocket(8080);//负责文件传输的端口
        try (ServerSocket listener = new ServerSocket(8090)) {
            while (true) {
                pool.execute(new Handler(listener.accept(),fileServer.accept()));
            }
        }
    }

    /**
     * 处理线程类
     */
    private static class Handler implements Runnable {
        private String name;
        private Socket socket;
        private Socket file_socket;
        private Scanner in;
        private PrintWriter out;


        public Handler(Socket socket,Socket file_s) throws IOException {
            this.socket = socket;
            this.file_socket = file_s;
        }

        public void run() {
            try {
                in = new Scanner(socket.getInputStream());
                out = new PrintWriter(socket.getOutputStream(), true);
                InputStream f_in = file_socket.getInputStream();
                OutputStream f_out = file_socket.getOutputStream();

                // 保持请求直到获取唯一的用户名
                while (true) {
                    out.println("SUBMITNAME");//发送信息给客户端，要求输入用户名
                    name = in.nextLine();
                    if (name == null) {//如果没有输入则直接结束
                        return;
                    }
                    synchronized (names) {//同步操作
                        if (!(name.length() == 0) && !names.contains(name)) {
                            names.add(name);
                            break;
                        }
                    }
                }


                //该用户名的用户加入成功
                out.println("NAMEACCEPTED " + name);
                for (PrintWriter writer : writers) {
                    writer.println("MESSAGE " + name + " has joined");
                }
                writers.add(out);//将对某个客户端的输出流加入writers哈希集合中
                file_outs.add(f_out);//将文件输入流加入集合


                while (true) {
                    String input = in.nextLine();
                    if (input.toLowerCase().startsWith("/quit")) {//如果客户端发送退出指令
                        return;
                    }
                    if (input.toLowerCase().startsWith("/uploading")) {//如果是上传文件
                        String fileType = in.nextLine();//获取文件类型名
                        for(PrintWriter writer:writers){
                            writer.println("FILE");//通知客户端接收文件
                            writer.println(fileType);//紧接着发送文件类型
                        }
                        byte[] buf = new byte[1024];
                        int len =0;
                        while((len=f_in.read(buf))!=-1){
                            for(OutputStream fo : file_outs){
                                fo.write(buf,0,len);
                                System.out.println(len);

                            }
                            System.out.println(len);
                        }
                        System.out.println(name+"的文件群发成功");

                    }
                    for (PrintWriter writer : writers) {//向所有用户发送该用户发送的信息
                        writer.println("MESSAGE " + name + ": " + input);
                    }
                }
            } catch (Exception e) {
                System.out.println(e);
            } finally {
                if (out != null) {
                    writers.remove(out);
                }
                if (name != null) {
                    System.out.println(name + " is leaving");
                    names.remove(name);
                    for (PrintWriter writer : writers) {
                        writer.println("MESSAGE " + name + " has left");//向所有用户发送退出用户的消息
                    }
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}