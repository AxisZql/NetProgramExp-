package Chat_test;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;


public class ChatClient{

    String serverAddress;
    Scanner in;
    PrintWriter out;
    InputStream f_in;
    OutputStream f_out;
    JFrame frame = new JFrame("Chatter");
    JTextField textField = new JTextField(50);
    JButton upfileButton = new JButton("群发文件");
    JButton selectFile = new JButton("选择文件");
    JTextArea messageArea = new JTextArea(16, 50);
    JFileChooser jfc = new JFileChooser(".");//文件选择框
    File file = null;


    public ChatClient(String serverAddress) {
        this.serverAddress = serverAddress;

        textField.setEditable(false);
        messageArea.setEditable(false);
        upfileButton.setEnabled(false);//设置按钮不能点击
        selectFile.setEnabled(false);
        frame.getContentPane().add(upfileButton,BorderLayout.NORTH);
        frame.pack();
        frame.getContentPane().add(selectFile,BorderLayout.WEST);
        frame.getContentPane().add(textField, BorderLayout.SOUTH);
        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);
        frame.pack();


        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                out.println(textField.getText());
                textField.setText("");
            }
        });
        selectFile.addActionListener(new ActionListener(){//选择文件事件
            @Override
            public void actionPerformed(ActionEvent e) {
                jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);//设置可以选中文件或者目录
                //打开文件选择器对话框

                int status = jfc.showOpenDialog(frame);
                //如果没有打开
                if (status != JFileChooser.APPROVE_OPTION) {
                    JPanel jPanel = new JPanel();
                    JOptionPane.showMessageDialog(jPanel, "没有选中文件", "提示", JOptionPane.WARNING_MESSAGE);
                } else {
                    //将被选中的文件保存为文件对象
                    file = jfc.getSelectedFile();
                    if(file.isDirectory()){
                        JPanel jPanel = new JPanel();
                        file = null;
                        JOptionPane.showMessageDialog(jPanel, "不能发送文件夹", "提示", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        });
        upfileButton.addActionListener(new ActionListener() {//发送文件事件
            @Override
            public void actionPerformed(ActionEvent e) {
                if (file ==null){
                    JPanel jPanel = new JPanel();
                    JOptionPane.showMessageDialog(jPanel, "没有选中文件", "提示", JOptionPane.WARNING_MESSAGE);
                }else{
                    try {
                        out.println("/uploading");
                        FileInputStream fis = new FileInputStream(file.getAbsolutePath());
                        String[] strArry = file.getName().split("\\.");
                        int t_index = strArry.length -1;
                        String fileType=strArry[t_index];
                        out.println(fileType);//发送文件类型名
                        byte[] buf =new byte[1024];
                        int len =0;
                        while((len=fis.read(buf))!=-1){
                            f_out.write(buf,0,len);
                        }
                    } catch (IOException fileNotFoundException) {
                        fileNotFoundException.printStackTrace();
                    }

                }
            }
        });
    }

    private String getName() {
        return JOptionPane.showInputDialog(frame, "Choose a screen name:", "Screen name selection",
                JOptionPane.PLAIN_MESSAGE);
    }

    private void run() throws IOException {
        try {
        	Socket socket = new Socket(serverAddress, 8090);
        	Socket file_socket = new Socket(serverAddress,8080);
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);
            f_in = file_socket.getInputStream();
            f_out = file_socket.getOutputStream();


            while (in.hasNextLine()) {
                String line = in.nextLine();
                if (line.startsWith("SUBMITNAME")) {
                    out.println(getName());
                } else if (line.startsWith("NAMEACCEPTED")) {
                    this.frame.setTitle("Chatter - " + line.substring(13));
                    textField.setEditable(true);
                    upfileButton.setEnabled(true);//设置按钮可以点击
                    selectFile.setEnabled(true);
                } else if (line.startsWith("MESSAGE")) {
                    messageArea.append(line.substring(8) + "\n");//服务器端代码：writer.println("MESSAGE " + name + ": " + input);
                }else if(line.startsWith("FILE")){
                    String fileType= in.nextLine();//获取文件类型
                    File dir = new File("receiveFile");
                    if(!dir.exists()){
                        dir.mkdirs();
                    }
                    String _time =String.valueOf(System.currentTimeMillis());//获取系统当前时间戳
                    File _f = new File(dir,_time+"."+fileType);
                    System.out.println(fileType);
                    FileOutputStream fos = new FileOutputStream(_f);
                    byte[] buf = new byte[1024];
                    int len =0;
                    while((len=f_in.read(buf))!=-1){
                        fos.write(buf, 0,len);
                        System.out.println(len);
                    }
                    fos.flush();
                    fos.close();
                }
            }
        } finally {
            frame.setVisible(false);
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {

        ChatClient client = new ChatClient("127.0.0.1");
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run();
    }
}