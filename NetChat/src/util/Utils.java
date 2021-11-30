package util;

import java.io.*;
/**
 * author：李志强
 * class：网络194
 * date：2021-11-24
 * 注：本项目github：https://github.com/AxisZql/NetProgramExp-
 */

public class Utils {

    //判断头像文件是否符合要求
    public String CheckAvatarFileType(String avatar){
        if (avatar==null)return null;
        String[]strArry=avatar.split("\\.");
        int t_index=strArry.length-1;
        String extendName=strArry[t_index];
        if(extendName != null && (extendName.equals("png") || extendName.equals("jpg"))){
            return extendName;
        }
        return null;

    }

    public boolean SaveAvatarPic(String username, String extendName, InputStream in) {
        File dir = new File("avatar");//创建存储头像的文件夹
        if(!dir.exists()){
            dir.mkdirs();
        }
        File aFile=new File(dir,username+extendName);//创建对应用户的头像文件
        FileOutputStream fos= null;
        try {
            fos = new FileOutputStream(aFile);
            byte[] buf=new byte[1024];
            int len =0;
            while((len=in.read(buf))!=-1){
                fos.write(buf,0,len);
            }
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //将头像文件发送给客户端
    public boolean SendAvatarPic(String username, OutputStream out){
        File dir = new File("avatar");
        String[] allPic = dir.list();
        if(allPic==null)return false;
        String fileName = "";
        for(String v:allPic){
            if(v.contains(username+".")){
                fileName = v;
                break;
            }
        }
        try {
            FileInputStream fis =new FileInputStream("avatar"+"/"+fileName);
            byte[] buf = new byte[1024];
            int len=0;
            while((len=fis.read(buf))!=-1){
                out.write(buf,0,len);
            }
            fis.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
