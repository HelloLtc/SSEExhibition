package org.ruiyun.crypto;

import org.crypto.sse.IEX2Lev;
import org.ruiyun.JavaSwingServer.SM2Utils;
import org.ruiyun.JavaSwingServer.Util;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

public class TestMainClient extends JFrame implements ActionListener {
  private static final long serialVersionUID = 1L;
  private String url="127.0.0.1";
  private int port=1000;
  private Socket socket=null;
  private BufferedWriter bufOut=null;
  private BufferedReader bufIn=null;
  private static FileInputStream fis=null;
  private static DataOutputStream dos=null;
  public TestMainClient()
  {
    this.addListener();
    initialFrame();
  }
  @Override
  public void actionPerformed(ActionEvent e) {

    if(e.getSource()==this.jb1)
    {
      this.jl3.setText("正 在 查 询 ， 请 稍 候. . . . .");
      String number=this.jtf.getText().trim();
      String pwd=this.jpwf.getText().trim();
      if(pwd.equals("")){
        JOptionPane.showMessageDialog(this,"请输入密码","温馨提示",
          JOptionPane.INFORMATION_MESSAGE);
        return;
      }
      ArrayList<String> listSK = null;
      try {
        listSK = IEX2Lev.keyGenList(256, pwd, "salt/saltSetM", 100000);
      } catch (InvalidKeySpecException ex) {
        ex.printStackTrace();
      } catch (NoSuchAlgorithmException ex) {
        ex.printStackTrace();
      } catch (NoSuchProviderException ex) {
        ex.printStackTrace();
      } catch (UnsupportedEncodingException ex) {
        ex.printStackTrace();
      }
      try{
        int type = 0;
        while (!this.jrbArray[type].isSelected())
          type++;
        System.out.println("TYPE:"+type);
        socket=new Socket(url,port);
        bufOut=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        bufIn=new BufferedReader(new InputStreamReader(socket.getInputStream()));
        bufOut.write(type+"\n");
        bufOut.flush();
        //发送账号密码
        String pubk = "04F6E0C3345AE42B51E06BF50B98834988D54EBC7460FE135A48171BC0629EAE205EEDE253A530608178A98F1E19BB737302813BA39ED3FA3C51639D7A20C7391A";
        String textlist = number+"#"+listSK.get(0)+"#"+listSK.get(1);
        byte[] sourceData = textlist.getBytes();
        String cipherText = SM2Utils.encrypt(Util.hexToByte(pubk), sourceData);
        bufOut.write(cipherText+"\n");
        bufOut.flush();
        if(type==0||type==1){
          if(number.equals("")){
            JOptionPane.showMessageDialog(this,"请输入查询条件","温馨提示",
              JOptionPane.INFORMATION_MESSAGE);
            return;
          }
          //查询是否符合，符合返回Ok，否则说明输入信息不正确
          if(bufIn.readLine().equalsIgnoreCase("ok"))
          {
            String arr[] = null;
            //获取传送来的账号密码，一辈以后使用，减少查询数据库次数
            arr=bufIn.readLine().split("#");
            JOptionPane.showMessageDialog(this,arr,"查询结果",
              JOptionPane.INFORMATION_MESSAGE);
          }
          else{//弹出错误提示窗口
            JOptionPane.showMessageDialog(this,"用户名或密码错误","错误",
              JOptionPane.ERROR_MESSAGE);
            jl3.setText("");
          }
        }
       else if(type==2||type==3){
          JFileChooser chooser = new JFileChooser();
          chooser.setMultiSelectionEnabled(true);
          /** 过滤文件类型 * */
          FileNameExtensionFilter filter = new FileNameExtensionFilter("war",
            "pdf", "txt", "doc", "docx");
          chooser.setFileFilter(filter);
          int returnVal = chooser.showOpenDialog(jb1);
          if (returnVal == JFileChooser.APPROVE_OPTION) {
            /** 得到选择的文件* */
            File[] arrfiles = chooser.getSelectedFiles();
            if (arrfiles == null) {//|| arrfiles.length == 0
              return;
            }

            try {
              dos = new DataOutputStream(socket.getOutputStream());
              dos.write(arrfiles.length);
              dos.flush();
              for(int i=0;i<arrfiles.length;i++){
                dos.writeUTF(dealName(arrfiles[i].getPath()));
                dos.flush();
                dos.writeLong(arrfiles[i].length());
              }
              for(int j=0;j<arrfiles.length;j++) {
                fis = new FileInputStream(arrfiles[j].getPath());
                byte[] bytes = new byte[5];
                int length = 0;
                int remainder = (int) (arrfiles[j].length()%5);
                int IntValue = (int) (arrfiles[j].length()/5);
                //循环读取文件内容，输入流中将最多buf.length个字节的数据读入一个buf数组中,返回类型是读取到的字节数。
                // 当文件读取到结尾时返回 -1,循环结束。
                for(int i=0;i<IntValue;i++){
                  length = fis.read(bytes, 0, bytes.length);
                  dos.write(bytes, 0, length);
                  dos.flush();
                }
                fis.read(bytes, 0, remainder);
                dos.write(bytes, 0, remainder);
                dos.flush();
                fis.close();
              }

              System.out.println("文件上传成功！");
              //关闭相应的流
              dos.close();
            } catch (IOException ioe) {
              ioe.printStackTrace();
            }
          }
        }
      }
      catch(Exception e1) {
        e1.printStackTrace();
      }
    }
    else if(e.getSource()==this.jb2){//按下重置按钮,清空输入信息
      this.jtf.setText("");
      this.jpwf.setText("");
    }

  }
  public String dealName(String filename){

    return filename.split("\\\\")[filename.split("\\\\").length-1];

  }
  private JPanel jp=new JPanel();
  private JLabel jname=new JLabel("查  询  内  容");
  private JLabel jpassword=new JLabel("密            码");
  private JLabel jl3=new JLabel("");


  private JTextField jtf=new JTextField();
  private JPasswordField jpwf=new JPasswordField();
  private JRadioButton[] jrbArray=//创建单选按钮数组
    {
      new JRadioButton("IEX2Lev",true),
      new JRadioButton("IEXZMF"),
      new JRadioButton("AddIEX2Lev"),
      new JRadioButton("AddIEXZMF")
    };
  private ButtonGroup bg=new ButtonGroup();
  private JButton jb1=new JButton("查询//上传");
  private JButton jb2=new JButton("重      置");


  public void addListener(){
    this.jb1.addActionListener(this);
    this.jb2.addActionListener(this);
  }

  public void initialFrame()
  {
    jp.setLayout(null);
    this.jname.setBounds(30,20,110,25);
    this.add(jname);
    this.jtf.setBounds(120,20,130,25);
    this.add(jtf);
    this.jpassword.setBounds(30,60,110,25);
    this.add(jpassword);
    this.jpwf.setBounds(120,60,130,25);
    this.jpwf.setEchoChar('*');
    this.jp.add(jpwf);
    this.bg.add(jrbArray[0]);
    this.bg.add(jrbArray[1]);
    this.bg.add(jrbArray[2]);
    this.bg.add(jrbArray[3]);
    this.jrbArray[0].setBounds(40,100,100,25);
    this.jp.add(jrbArray[0]);
    this.jrbArray[1].setBounds(145,100,100,25);
    this.jp.add(jrbArray[1]);
    this.jrbArray[2].setBounds(250,100,100,25);
    this.jp.add(jrbArray[2]);
    this.jrbArray[3].setBounds(355,100,100,25);
    this.jp.add(jrbArray[3]);
    this.jb1.setBounds(35,140,100,30);
    this.jp.add(jb1);
    this.jb2.setBounds(150,140,100,30);
    this.jp.add(jb2);

    this.jl3.setBounds(50,500,150,25);
    this.jp.add(jl3);
    this.add(jp);

    this.setTitle("查询");

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int centerX=screenSize.width/2;
    int centerY=screenSize.height/2;
    int w=600;//本窗体宽度
    int h=300;//本窗体高度
    this.setBounds(centerX-w/2,centerY-h/2-100,w,h);//设置窗体出现在屏幕中央

    this.setVisible(true);

    //将填写账号的文本框设为默认焦点
    this.jtf.requestFocus(true);
  }

  public static void main(String args[])
  {
    TestMainClient login=new TestMainClient();
  }
}
