package exp01;

import javax.swing.*;
import java.awt.*;

public class CRTest extends DefaultListCellRenderer {        //继承渲染器类
    public Component getListCellRendererComponent(
            JList<? extends Object> list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {        //重写渲染器的方法

        setText(value.toString());		//设置文字（获取每个元素文字信息将其显示）
        ImageIcon ico=new ImageIcon("D:\\ChatRoom\\images\\1.png");		//实例化一个ImageIcon对象
        Image img=ico.getImage();		//实例化Image对象获取ico对象的内容
        img=img.getScaledInstance(25,25,Image.SCALE_DEFAULT);		//把图片全部缩放为25x25
        ico.setImage(img);		//ImageIcon对象重新获取缩放后图标
        setIcon(ico);		//设置图标
        if(isSelected) {		//当某个元素被选中时
            setForeground(Color.WHITE);		//设置前景色（文字颜色）为白色
            setBackground(Color.GREEN);		//设置背景色为蓝色
            //System.out.println(index+"被选中");
        } else {		//某个元素未被选中时（取消选中）
            setForeground(Color.BLACK);		//设置前景色（文字颜色）为黑色
            setBackground(Color.WHITE);		//设置背景色为白色
        }
        return this;
    }
}