import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

//获得聊天信息时,首先判断该用户当前正与哪一个好友或者群聊在聊天,如果该消息恰好对应上,则直接发送到聊天面板,否则存放在客户端缓存
//并提示用户有未读消息

public class clientLoginRegister {



    JFrame jFrame=new JFrame("Chatter");
    JFrame jf = new JFrame("登录/注册");
    String serverAddress;

    Scanner in;
    PrintWriter out;
    Map<String, List<Message>> messageMap=new HashMap<>();       //客户端缓存未读消息
    Map<String,String>nameAndId=new HashMap<>();               //好友(群聊)名称与好友(群聊)id
    String nowChatUser=null;            //当前正与哪一个好友/群聊聊天

    JSplitPane jSplitPane=new JSplitPane();
    JSplitPane jLeft=new JSplitPane();          //左边面板用作个人中心
    JSplitPane jRight=new JSplitPane();         //右边面板用作聊天面板

    JPanel myPanel1=new JPanel();       //面板1
    JPanel myPanel2=new JPanel();       //面板2

    JButton button1=new JButton("个人中心");
    JPanel jtabP1,jtabP2,jtabP3,jtabP4;
    JTabbedPane jtabP;
    JButton button4=new JButton("Q群");
    JButton button5=new JButton("最近");

    JPanel rightPanel1=new JPanel();        //右边上部面板一
    JPanel rightPanel2=new JPanel();        //右边下部面板二
    JTextArea jTextArea=new JTextArea();        //聊天面板
    JTextField jTextField=new JTextField(70);     //输入面板

    JButton buttonSendMeg=new JButton("发送");        //消息发送按钮
    JButton buttonSendFile=new JButton("发送文件");     //文件发送按钮

    DefaultListModel dflFriend=new DefaultListModel();        //实例化默认数据模型对象
    DefaultListModel dflChatGroup=new DefaultListModel();
    DefaultListModel dflMessage=new DefaultListModel();

    JList jlFriend;             //好友列表
    JList jlChatGroup;          //群聊列表
    JList jlMessage;            //未读消息列表
    JScrollPane jspFriend;
    JScrollPane jspChatGroup;
    JScrollPane jspMessage;

    JTextField jtSearchFriend=new JTextField(10);              //搜索用户输入框
    JTextField jtSearchChatGroup=new JTextField(10);           //搜索群聊输入框
    JButton jbSearchFriend=new JButton("搜索用户");                 //搜索用户按钮
    JButton jbSearchChatGroup=new JButton("搜索群聊");              //搜索群聊按钮
    JTextField jtBuildChatGroup=new JTextField(10);             //创建群聊输入框
    JButton jbBuildChatGroup=new JButton("创建群聊");                //创建群聊输入按钮

    String userName;
    String id;
    int flag=2;                 //设置标记位判断是私聊还是群聊
    String searchUser;          //搜索的用户名
    String searchChatGroup;     //搜索的群聊名
    String buildChatGroup;      //创建的群聊名
    File sendFile;              //要发送的文件
    String sendFileName;        //要发送的文件的名称
    String sendMessage;         //发送的消息
    String sendMessageTime;     //发送消息的时间

    /**
     * 聊天窗口
     *
     */
    public clientLoginRegister(){

        try {
            Socket socket=new Socket("",8081);
            in=new Scanner(socket.getInputStream());
            out=new PrintWriter(socket.getOutputStream());
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        jFrame.setBounds(100,100,1300,700);

        //设置左边面板
        jLeft.setOneTouchExpandable(true);     //让分割线显示出箭头
        jLeft.setContinuousLayout(true);       //操作箭头,重新绘图
        jLeft.setOrientation(JSplitPane.VERTICAL_SPLIT);     //设置分割线方向
        jLeft.setTopComponent(myPanel1);
        jLeft.setBottomComponent(myPanel2);
        jLeft.setDividerLocation(100);     //设定分割线距离左边的位置
        jLeft.setEnabled(false);

        //设置右边面板
        jRight.setOneTouchExpandable(true);     //让分割线显示出箭头
        jRight.setContinuousLayout(true);       //操作箭头,重新绘图
        jRight.setOrientation(JSplitPane.VERTICAL_SPLIT);     //设置分割线方向
        jRight.setTopComponent(jTextArea);
        jRight.setBottomComponent(rightPanel2);
        jRight.setDividerLocation(600);
        jTextArea.setText("这里是聊天室");

        rightPanel2.add(jTextField);
        rightPanel2.add(buttonSendMeg);
        rightPanel2.add(buttonSendFile);


        jSplitPane.setOneTouchExpandable(true);     //让分割线显示出箭头
        jSplitPane.setContinuousLayout(true);       //操作箭头,重新绘图
        jSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);     //设置分割线方向

        jSplitPane.setLeftComponent(jLeft);
        jSplitPane.setRightComponent(jRight);
        jSplitPane.setDividerSize(1);           //设置分割线的宽度
        jSplitPane.setDividerLocation(300);     //设定分割线距离左边的位置

        jLeft.setBorder(BorderFactory.createLineBorder(Color.green));
        jRight.setBorder(BorderFactory.createLineBorder(Color.red));

        jFrame.add(jSplitPane);

        myPanel1.add(button1);
        myPanel2.setLayout(new BorderLayout());

        myPanel1.setBorder(BorderFactory.createLineBorder(Color.green));
        myPanel2.setBorder(BorderFactory.createLineBorder(Color.red));

        jtabP = new JTabbedPane();
        jtabP1 = new JPanel(new BorderLayout());
        jtabP2 = new JPanel(new BorderLayout());
        jtabP3 = new JPanel();
        jtabP4 = new JPanel(new BorderLayout());

        jtabP1.setName("好友列表");
        jtabP2.setName("群聊列表");
        jtabP3.setName("用户/群聊");        //可以搜索用户或者是群聊,达到添加用户或群聊的目的
        jtabP4.setName("未读消息");         //提示当前用户尚未阅读的消息

        jtabP.add(jtabP1);
        jtabP.add(jtabP2);
        jtabP.add(jtabP3);
        jtabP.add(jtabP4);
        myPanel2.add(jtabP);

        //dfl.addElement("###############当前的好友列表为################");
//        dfl.addElement("辉1");        //给模型加入字符串元素a
//        dfl.addElement("辉2");        //给模型加入字符串元素b
//        dfl.addElement("辉3");
//        dfl.addElement("辉3");
//        dfl.addElement("辉3");
//        dfl.addElement("辉3");
//        dfl.addElement("辉3");
//        dflFriend.addElement("辉");
//        dflChatGroup.addElement("群聊一");
        jlFriend=new JList(dflFriend);        //实例化JList对象，构造方法写入默认数据模型对象
        jlChatGroup=new JList(dflChatGroup);            //群聊的JList
        jlMessage=new JList(dflMessage);                //未读消息JList
        jspFriend=new JScrollPane();        //实例化滚动条对象
        jspChatGroup=new JScrollPane();
        jspFriend.setViewportView(jlFriend);        //把JList加到滚动条里面
        jspChatGroup.setViewportView(jlChatGroup);
        dflFriend.addElement("辉1");
        dflChatGroup.addElement("群聊1");
        jspMessage=new JScrollPane();
        jspMessage.setViewportView(jlMessage);

        jtabP1.add(jspFriend,BorderLayout.CENTER);
        jlFriend.setCellRenderer(new exp01.CRTest());        //设置渲染器
        jtabP2.add(jspChatGroup,BorderLayout.CENTER);
        jlChatGroup.setCellRenderer(new exp01.CRTest());      //设置渲染器
        jtabP4.add(jspMessage,BorderLayout.CENTER);
        jtabP3.add(jtSearchFriend);
        jtabP3.add(jbSearchFriend);
        jtabP3.add(jtSearchChatGroup);
        jtabP3.add(jbSearchChatGroup);
        jtabP3.add(jtBuildChatGroup);
        jtabP3.add(jbBuildChatGroup);

        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setResizable(false);

        /**
         * 搜索用户并响应事件(已写完)
         */
        jbSearchFriend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchUser=jtSearchFriend.getText();
                JSONObject jsonObject=new JSONObject();
                jsonObject.put("type","search_user");
                jsonObject.put("field",searchUser);
                String message=jsonObject.toString();
                out.println(message);
            }
        });

        /**
         * 搜索群聊响应事件(已写完)
         */
        jbSearchFriend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchChatGroup=jtSearchChatGroup.getText();
                JSONObject jsonObject=new JSONObject();
                jsonObject.put("type","search_group");
                jsonObject.put("file",searchChatGroup);
                String message= jsonObject.toString();
                out.println(message);
            }
        });

        /**
         * 创建群聊响应事件(已写完)
         */
        jbBuildChatGroup.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buildChatGroup=jtSearchChatGroup.getText();
                JSONObject jsonObject=new JSONObject();
                jsonObject.put("type","create_group");
                jsonObject.put("name",searchChatGroup);
                jsonObject.put("fileName","avator.jpg");
                jsonObject.put("create_by",userName);
                String message= jsonObject.toString();
                out.println(message);
            }
        });

        /**
         * 点击好友列表发起聊天(已写完)
         */
        jlFriend.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if(!jlFriend.getValueIsAdjusting()){        //设置只有释放鼠标时才触发
                    int result=JOptionPane.showConfirmDialog(jFrame, "是否发起聊天？", "提示",
                            JOptionPane.YES_NO_OPTION);     //是为0

                    System.out.println(result);

                    if(result==0){      //证明是发起聊天,此时应该向服务器发出获取聊天记录请求，然后再获得一直存放在客户端的该好友发来的消息
                        flag=1;         //设置为私聊标记
                        String name=String.valueOf(jlFriend.getSelectedValue());        //获得好友名称
                        nowChatUser=name;
                        jTextArea.setText("");
                        jTextArea.append("你正在和"+name+"聊天");
                        //查找客户端缓存,是否有这个好友发来的信息
//                        if(messageMap.containsKey(nowChatUser)){        //如果消息缓存存有该用户的信息,则将其打印到面板上
//                            List<Message>messageList=messageMap.get(nowChatUser);
//                            for (Message message:messageList){
//                                String create_time=message.getCreate_time();
//                                String content=message.getContent();
//                                jTextArea.append(nowChatUser+":"+content+"\t"+create_time);
//                            }
//                            messageList.clear();
////                            List<Message>messageList1=new ArrayList<>();
//                            messageMap.put(nowChatUser,messageList);       //更新消息缓存
//                        }
                        //首先请求服务器获得之前的聊天记录
                        JSONObject jsonObject=new JSONObject();
                        jsonObject.put("type","get_friend_chat");
                        jsonObject.put("create_by",userName);
                        jsonObject.put("receive",nowChatUser);
                        jsonObject.put("chat_type",1);
                        String message=jsonObject.toString();
                        out.println(message);
                    }
                }
            }
        });

        /**
         * 点击群聊列表加入群聊(已写完)
         */
        jlChatGroup.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if(!jlFriend.getValueIsAdjusting()){        //设置只有释放鼠标时才触发
                    int result=JOptionPane.showConfirmDialog(jFrame, "是否发起聊天？", "提示",
                            JOptionPane.YES_NO_OPTION);     //是为0

                    System.out.println(result);

                    if(result==0){
                        flag=2;         //设置为群聊标记
                        String name=String.valueOf(jlFriend.getSelectedValue());        //获得好友名称
                        nowChatUser=name;
                        jTextArea.setText("");
                        //向服务器请求之前的聊天记录
                        JSONObject jsonObject=new JSONObject();
                        jsonObject.put("type","get_friend_chat");
                        jsonObject.put("create_by",userName);
                        jsonObject.put("receive",nowChatUser);
                        jsonObject.put("chat_type",2);
                        String message=jsonObject.toString();
                        out.println(message);
                    }
                }
            }
        });

        /**
         * 点击未读信息发起和对应好友/群聊聊天
         */
        jlMessage.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {


            }
        });


        /**
         * 发送消息(已写完)
         */
        buttonSendMeg.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(flag==2){        //证明该用户还没有加入任何群聊
                    JOptionPane.showMessageDialog(jf,"不处于好友聊天或群聊状态,无法发送消息!");
                }else{
                    String content=jTextField.getText();        //获取输入的消息
                    sendMessage=content;                        //发送消息的内容
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
//                    System.out.println(df.format(new Date()));// new Date()为获取当前系统时间
                    sendMessageTime=String.valueOf(df.format(new Date()));          //发送消息的时间
                    JSONObject jsonObject=new JSONObject();
                    jsonObject.put("type","friend_chat");
                    jsonObject.put("create_by",userName);           //发送方
                    jsonObject.put("receive",nowChatUser);          //接收方
                    jsonObject.put("chat_type",flag);               //聊天类型
                    jsonObject.put("object_type","chat");
                    jsonObject.put("content",content);              //发送的消息内容
                    String message=jsonObject.toString();
                    out.println(message);
                }
            }
        });

        /**
         * 发送文件(已写完)
         */
        buttonSendFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                if(flag==2){
//                    JOptionPane.showMessageDialog(jf,"不处于好友聊天或群聊状态,无法发送消息!");
//                }else{
//                    JFileChooser fileChooser=new JFileChooser();
//                    int result=fileChooser.showOpenDialog(jf);      //对话框将会尽量显示在靠近parent的中心
//                    //点击确定
//                    if(result==JFileChooser.APPROVE_OPTION){
//                        //获取路径
//                        File file=fileChooser.getSelectedFile();        //获得选择的文件
//                        String fileName=file.getName();                 //获取选择的文件的名字
//                        try {
//                            BufferedReader br=new BufferedReader(new FileReader(file));
//                            String record=null;
//                            StringBuffer sb=new StringBuffer();
//                            while ((record= br.readLine())!=null){
//                                if(record.equals("")){
//                                    sb.append("\n");
//                                }else{
//                                    sb.append(record+"\n");
//                                }
//                            }
//                            String content=sb.toString();
//                            JSONObject jsonObject=new JSONObject();
//                            jsonObject.put("type","friend_chat");
//                            jsonObject.put("created_by",userName);
//                            jsonObject.put("receive",nowChatUser);
//                            jsonObject.put("content",content);
//                            jsonObject.put("pre_chat","312");
//                            jsonObject.put("chat_type",flag);
//                            jsonObject.put("object_type","file");
//                            jsonObject.put("fileName",fileName);
//                            String message= jsonObject.toString();
//                            out.println(message);
//
//                        } catch (FileNotFoundException fileNotFoundException) {
//                            fileNotFoundException.printStackTrace();
//                        } catch (IOException exception) {
//                            exception.printStackTrace();
//                        }
//                    }
//
//                }
                JFileChooser fileChooser=new JFileChooser();
                int result=fileChooser.showOpenDialog(jf);
                //点击确定
                if(result==JFileChooser.APPROVE_OPTION){
                    //获取路径
                    sendFile=fileChooser.getSelectedFile();     //获取选择的文件
                    sendFileName=fileChooser.getName();

                    JSONObject jsonObject=new JSONObject();
                    jsonObject.put("type","friend_chat");
                    jsonObject.put("created_by",userName);
                    jsonObject.put("receive",nowChatUser);
                    jsonObject.put("content","");
                    jsonObject.put("chat_type",flag);           //聊天类型
                    jsonObject.put("object_type","file");       //发送的是文件
                    jsonObject.put("fileName",sendFileName);        //发送的文件名
                    String message= jsonObject.toString();
                    out.println(message);
                }
            }
        });

    }

    /**
     * 登录窗口
     */
    public void init(){
        jf.setBounds(500, 250, 310, 210);
        jf.setResizable(false);  // 设置是否缩放

        JPanel jp1 = new JPanel();
        JLabel headJLabel = new JLabel("欢迎来到聊天室");
        headJLabel.setFont(new Font(null, 0, 25));  // 设置文本的字体类型、样式 和 大小
        jp1.add(headJLabel);


        JPanel jp2 = new JPanel();
        JLabel nameJLabel = new JLabel("用户名：");
        JTextField textField = new JTextField(20);
        JLabel pwdJLabel = new JLabel("密码：    ");
        JPasswordField pwdField = new JPasswordField(20);
        JButton loginButton = new JButton("登录");
        JButton registerButton = new JButton("注册");  // 没设置功能
        jp2.add(nameJLabel);
        jp2.add(textField);
        jp2.add(pwdJLabel);
        jp2.add(pwdField);
        jp2.add(loginButton);
        jp2.add(registerButton);

        JPanel jp = new JPanel(new BorderLayout());  // BorderLayout布局
        jp.add(jp1, BorderLayout.NORTH);
        jp.add(jp2, BorderLayout.CENTER);

        /**
         * 登录监听事件
         */
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String type="登录";
                String username= textField.getText();
                String password=String.valueOf(pwdField.getPassword());
                System.out.println("type:"+type);
                System.out.println("username:"+username);
                System.out.println("password:"+password);

                JSONObject jsonObject=new JSONObject();
                jsonObject.put("type",type);
                jsonObject.put("username",username);
                jsonObject.put("password",password);

                String message=jsonObject.toString();
                out.println(message);

                //jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // 设置关闭图标作用
            }
        });

        /**
         * 注册监听事件
         */
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String type="注册";
                String username= textField.getText();
                String password=String.valueOf(pwdField.getPassword());

                JSONObject jsonObject=new JSONObject();
                jsonObject.put("type",type);
                jsonObject.put("username",username);
                jsonObject.put("password",password);
                jsonObject.put("nickname","lgh");
                jsonObject.put("fileName","lgh.png");
                String message=jsonObject.toString();
                out.println(message);
            }
        });

        jf.add(jp);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // 设置关闭图标作用
        jf.setVisible(true);  // 设置可见

    }

    /**
     * 客户端的主要操作
     * @throws IOException
     */
    private void run() throws IOException {
        try{
            Socket socket=new Socket(serverAddress,8081);
            in=new Scanner(socket.getInputStream());        //读取服务器传入的数据
            out=new PrintWriter(socket.getOutputStream(),true);         //传输数据给服务器

            init();         //初始登录窗体

            while (in.hasNextLine()) {
                String line = in.nextLine();            //有一行读一行,按理来说文件那里会出错
                JSONObject jsonObject = JSON.parseObject(line);
                String type = jsonObject.getString("type");          //获得服务器传给客户端的消息类型
//                if(line.startsWith("SUBMITNAME")){          //客户端成功与服务器连接上
//                    init();
//                }else if (line.startsWith("NAMEACCEPTED")){         //登录成功
//                    jf.setVisible(false);       //将窗体设置为不可见
//                    jf.dispose();               //释放窗体占用的资源
//                    this.jFrame.setTitle("Chatter - "+line.substring(13));
//                    this.jFrame.setVisible(true);
//
//                    //开启文件传输线程
//
//
//                }else if(line.startsWith("MESSAGE")){
//
//
//                }
                if (type.equals("login")) {             //登录
                    String success = jsonObject.getString("success");     //登录是否成功
                    if (success.equals("true")) {
                        jf.setVisible(false);
                        jf.dispose();
                        this.jFrame.setVisible(true);

//                        clientFile file = new clientFile("", jFrame, out);   //开启文件传输线程

                        //这里有大问题？？？？？？？？？？？？？？？？？？？？？？？？？？

                        JSONArray jsonArray = JSONArray.parseArray("data");
                        for (int i=0;i<jsonArray.size();i++){
                            JSONObject jsonObject1=JSONObject.parseObject(jsonArray.get(i).toString());
                            if(jsonObject1!=null){
                                userName=jsonObject1.getString("username");
                                id=jsonObject1.getString("id");
                            }
                        }

                        //请求好友列表以及群聊列表
                        String get_friend_list = "get_friend_list";
                        JSONObject json1 = new JSONObject();
                        json1.put("type", "get_friend_list");
                        json1.put("id", id);
                        String get_friend = json1.toString();
                        out.println(get_friend);

                        JSONObject json2 = new JSONObject();
                        json2.put("type", "get_group_list");
                        json2.put("id", id);
                        String get_group = json2.toString();
                        out.println(get_group);

                    } else {
                        JOptionPane.showMessageDialog(jf, "登录失败,请检查用户名和密码是否正确");
                    }
                }                   //登录(1)

                else if (type.equals("register")) {          //注册
                    String success = jsonObject.getString("success");     //注册是否成功
                    if (success.equals("true")) {
                        JOptionPane.showMessageDialog(jf, "注册成功,请登录");
                    } else {
                        JOptionPane.showMessageDialog(jf, "注册失败,请检查用户名和密码是否正确");
                    }
                }           //注册(2)
//                else if(type.equals("friend_chat")){          //获取聊天信息
//
//                    Integer chat_type=jsonObject.getInteger("chat_type");         //获得聊天类型,是群聊还是私聊
//                    String content=jsonObject.getString("content");           //获得聊天内容
//                    String receive=jsonObject.getString("receive");           //获得接收者id
//                    String created_by=jsonObject.getString("create_by");      //获得发送者id
//                    if(chat_type.equals("1")){                          //1代表私聊
//                        String friendName=nameAndId.get(receive);       //获得好友名称
//                        if(nowChatUser.equals(friendName)){             //表示用户正在和该好友聊天,直接将信息发送到聊天面板即可
//                            jTextArea.append(friendName+content);
//                        }else{                                          //不和该好友聊天则将消息存放在消息缓存中
//                            if(messageMap.containsKey(created_by)){         //如果这个好友发送的消息已经存放在缓存当中,则直接添加即可
//                                List<String>list=messageMap.get(created_by);
//                                list.add(content);
//                                messageMap.put(created_by,list);
//                            }else{
//                                List<String>list=new ArrayList<>();
//                                list.add(content);
//                                messageMap.put(created_by,list);
//                            }
//                        }
//                    }else{                                               //0代表群聊        同私聊操作
//                        String newContent=created_by+":"+content;
//                        if(messageMap.containsKey(receive)){
//                            List<String>list=messageMap.get(receive);
//                            list.add(newContent);
//                            messageMap.put(receive,list);
//                        }else{
//                            List<String>list=new ArrayList<>();
//                            list.add(newContent);
//                            messageMap.put(receive,list);
//                        }
//                    }
//            }
                else if(type.equals("get_friend_list")){           //获取好友列表(3)
                    //把获取到的好友列表加入到面板中
                    JSONArray jsonArrayFriend=jsonObject.getJSONArray("data");
                    for (int i=0;i<jsonArrayFriend.size();i++){
                        JSONObject jsonObject1=JSONObject.parseObject( jsonArrayFriend.get(i).toString());
                        String friendName=jsonObject1.toString();
                        dflFriend.addElement(friendName);           //添加到好友列表当中
                    }

                }else if(type.equals("get_group_list")){            //获取群聊列表(4)
                    //把获取到的群聊列表加入到面板中
                    JSONArray jsonArrayGroup=jsonObject.getJSONArray("data");
                    for (int i=0;i<jsonArrayGroup.size();i++){
                        JSONObject jsonObject1=JSONObject.parseObject( jsonArrayGroup.get(i).toString());
                        String friendName=jsonObject1.toString();
                        dflFriend.addElement(friendName);           //添加到好友列表当中
                    }

                }else if(type.equals("searcher_user")){                //搜索好友(5)
                    String success=jsonObject.getString("success");
                    if(success.equals("true")){                         //如果搜索成功,则添加好友
                        JSONObject jsonObject1=new JSONObject();
                        jsonObject1.put("type","add_friend");
                        jsonObject1.put("ua_id",userName);
                        jsonObject1.put("ub_id",searchUser);
                        String message= jsonObject1.toString();
                        out.println(message);
                    }

                }else if(type.equals("add_friend")){                  //添加好友(6)
                    String success=jsonObject.getString("success");
                    if(success.equals("true")){
                        dflFriend.addElement(searchUser);           //添加到好友列表
                        JOptionPane.showMessageDialog(jf,"添加好友成功");
                    }

                } else if(type.equals("creat_group")){               //创建群聊(7)
                    String success=jsonObject.getString("success");
                    if(success.equals("true")){
                        dflChatGroup.addElement(buildChatGroup);
                        JOptionPane.showMessageDialog(jf,"创建群聊成功!");
                    }else{
                        JOptionPane.showMessageDialog(jf,"创建群聊失败!");
                    }

                }else if(type.equals("search_group")){              //搜索群聊(8)
                    String success=jsonObject.getString("success");
                    if(success.equals("true")){                     //搜索群聊成功,并请求加入群聊
                        JSONObject jsonObject1=new JSONObject();
                        jsonObject1.put("type","add_group");
                        jsonObject1.put("ua_id",searchChatGroup);
                        jsonObject1.put("ub_id",userName);
                        String message= jsonObject1.toString();
                        out.println(message);               //向服务器请求加入群聊
                    }

                }else if(type.equals("add_group")){                 //添加群聊(9)
                    String success=jsonObject.getString("success");
                    if(success.equals("true")){
                        dflChatGroup.addElement(searchChatGroup);       //将该群聊加入到群聊列表中
                        JOptionPane.showMessageDialog(jf,"加入群聊成功!");
                    }

                }else if(type.equals("get_friend_chat")){           //获取用户聊天记录,只有在打开聊天面板的时候才会请求聊天记录(10)
                    JSONArray jsonArray=jsonObject.getJSONArray("data");        //获取未读消息内容
                    for (int i=0;i<jsonArray.size();i++){
                        JSONObject jsonObject1=JSONObject.parseObject(jsonArray.get(i).toString());
                        if(jsonObject1!=null){
                            String created_by=jsonObject1.getString("created_by_user");      //发送方
                            String create_time=jsonObject1.getString("created_at");     //发送时间
                            String object_type=jsonObject1.getString("object_type");
                            String content=jsonObject1.getString("content");

                            if(object_type.equals("file")){         //发送的是文件
                                String fileName=jsonObject1.getString("fileName");      //获取文件名
                                jTextArea.append(created_by+"给你发送了一份名为"+fileName+"的文件"+'\t'+create_time);

                            }else{   //发送的是普通消息
//                                if(nowChatUser.equals(created_by)){     //当聊天信息恰好来自当前正在聊天的好友/群聊时,直
//                                    // 接将消息打印到面板
//                                    jTextArea.append(created_by+":"+content+"\t"+create_time);
//                                }else{                              //不是则将消息存放到客户端缓存中并且在更新未读消息面板
//                                    Message message=new Message(create_time,content);
//                                    if(messageMap.containsKey(created_by)){     //如果这个好友已经有消息存放在缓存中,则直接打开
//                                        List<Message>list=messageMap.get(created_by);
//                                        list.add(message);
//                                        messageMap.put(created_by,list);
//                                    }else{
//                                        List<Message>list=new ArrayList<>();
//                                        list.add(message);
//                                        messageMap.put(created_by,list);
//                                    }
//                                }
                                jTextArea.append(created_by+":"+content+"\t"+create_time);
                            }

                        }
                    }

                    //读取消息缓存,检查是否有未读的消息
                    if(messageMap.containsKey(nowChatUser)){
                        List<Message>list=new ArrayList<>();
                        for (int i=0;i<list.size();i++){
                            jTextArea.append(nowChatUser+":"+list.get(i).getContent()+"\t"+list.get(i).getCreate_time());
                        }
                    }

                }else if(type.equals("friend_file_chat")){                          //发送文件请求的回应(11)
                    String success=jsonObject.getString("success");
                    if(success.equals("true")){                         //可以发送文件()
                        DataInputStream fileIn=new DataInputStream(socket.getInputStream());
                        DataOutputStream fileOut=new DataOutputStream(socket.getOutputStream());
                        DataInputStream fileReader=new DataInputStream(new FileInputStream(sendFile));
                        DataOutputStream fileWriter=new DataOutputStream(new FileOutputStream(sendFile));

                        fileOut.writeLong(sendFile.length());       //发送文件的长度
                        fileOut.flush();
                        int length=-1;
                        byte[] buff=new byte[1024];
                        while ((length=fileReader.read(buff))>0){       //发送内容
                            fileOut.write(buff,0,length);
                            fileOut.flush();
                        }

                    }else{                                              //不可以发送文件
                        JOptionPane.showMessageDialog(jf,"好友不在线,无法发送文件");
                    }
                }else if(type.equals("friend_chat")){                               //发送消息(12)
                    String success=jsonObject.getString("success");
                    if(success.equals("true")){
                        jTextArea.append(userName+":"+sendMessage+'\t'+sendMessageTime);
                    }else{
                        JOptionPane.showMessageDialog(jf,"消息发送失败");
                    }
                }else if(type.equals("new_msg")){                                   //获取新消息(13)(怀疑这里有问题,会出bug)
                    JSONArray jsonArray=jsonObject.getJSONArray("data");
                    for (int i=0;i<jsonArray.size();i++){
                        JSONObject jsonObject1=JSONObject.parseObject(jsonArray.get(i).toString());
                        if(jsonObject1!=null) {
                            String created_by = jsonObject1.getString("created_by_user");      //发送方
                            String create_time = jsonObject1.getString("created_at");     //发送时间
                            String object_type = jsonObject1.getString("object_type");
                            String content = jsonObject1.getString("content");
                            String fileName=jsonObject1.getString("fileName");

                            if(object_type.equals("file")){                     //发送的是文件
                                DataInputStream fileIn=new DataInputStream(socket.getInputStream());
                                DataOutputStream fileWriter;
                                int length = -1;
                                byte[] buff = new byte[1024];
                                long curLength = 0;
                                long totalLength = fileIn.readLong();
                                int result=JOptionPane.showConfirmDialog(jf,"是否接收"+created_by+"发送的文件?","提示",
                                        JOptionPane.YES_NO_CANCEL_OPTION);
                                if(result==0){
                                    File fileDir=new File("D:\\接收文件\\"+userName);
                                    if(!fileDir.exists()){
                                        fileDir.mkdir();
                                    }
                                    File file=new File("D:\\接收文件\\"+userName+"\\"+fileName);
                                    fileWriter = new DataOutputStream(new FileOutputStream(file));
                                    while((length = fileIn.read(buff)) > 0) {  // 把文件写进本地
                                        fileWriter.write(buff, 0, length);
                                        fileWriter.flush();
                                        curLength += length;
                                        if (curLength == totalLength) {  // 强制结束
                                            break;
                                        }
                                    }
                                    // 提示文件存放地址
                                    JOptionPane.showMessageDialog(jf, "文件存放地址：\n" +
                                            "D:\\接收文件\\" +
                                            userName + "\\" + fileName, "提示", JOptionPane.INFORMATION_MESSAGE);

                                    fileWriter.close();
                                }
                                else {  // 不接受文件
                                    while((length = fileIn.read(buff)) > 0) {
                                        curLength += length;
                                        if(curLength == totalLength) {  // 强制结束
                                            break;
                                        }
                                    }
                                }
                                fileIn.close();
                            }else{
                                if(nowChatUser.equals(created_by)){         //正好在与这个用户聊天
                                    jTextArea.append(created_by+":"+content+"\t"+create_time);
                                }else{                              //加入到未读消息列表中,并存放到消息缓存中(已发送,但未读取)
                                    if(messageMap.containsKey(created_by)){     //如果消息缓存里面已经存放该用户的未读信息
                                        List<Message>list=messageMap.get(created_by);
                                        Message message=new Message(create_time,content);
                                        list.add(message);
                                        messageMap.put(created_by,list);
                                    }else{
                                        List<Message>list=new ArrayList<>();
                                        Message message=new Message(create_time,content);
                                        list.add(message);
                                        messageMap.put(created_by,list);
                                    }
                                    dflMessage.addElement(created_by+"给你发来一条消息"+create_time);       //加入到消息列表中
                                }
                            }

                        }
                    }
                }


            }
        }finally {
            jFrame.setVisible(false);
            jFrame.dispose();

        }
    }

    public static void main(String[] args) throws IOException {
        clientLoginRegister client=new clientLoginRegister();
//        Socket socket=new Socket("127.0.0.1",8081);
//        client.in=new Scanner(socket.getInputStream());
        client.init();
        client.run();

    }

}
