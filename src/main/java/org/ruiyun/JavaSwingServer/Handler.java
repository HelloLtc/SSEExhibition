package org.ruiyun.JavaSwingServer;

import org.ruiyun.Util.AddIEX2lev;
import org.ruiyun.Util.AddIEXZMF;
import org.ruiyun.Util.SearchIEX2lev;
import org.ruiyun.Util.SearchIEXZMF;

import javax.servlet.ServletException;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


class socketUtil {
  public static void close(Socket socket) {
    // TODO Auto-generated method stub
    try
    {
      if(socket!=null)
        socket.close();
    }catch(IOException e)
    {
      e.printStackTrace();
    }
  }
}


public class Handler implements Runnable{
  public static final int IEX2LEVTYPE=0;//售货员登录
  public static final int IEXZMFTYPE=1;//管理员登录
  public static final int AddIEX2LEVTYPE=2;//管理员登录
  public static final int AddIEXZMFTYPE=3;//管理员登录
  private String prik = "3690655E33D5EA3D9A4AE1A1ADD766FDEA045CDEAA43A9206FB8C430CEFE0D94";
  private Socket socket;
  private BufferedReader bufIn=null;
  private BufferedWriter bufOut=null;
  private String uploadFolder = "";
  private static DataInputStream dis;
  private String arr[] = null;
  List<byte[]> listSK = null;
  public void init() throws ServletException {
    uploadFolder = this.getClass().getResource(".").getPath();;
  }

  public Handler(Socket socket) throws IOException
  {
    this.socket=socket;
    bufIn=new BufferedReader(new InputStreamReader(socket.getInputStream()));
    bufOut=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
  }
  @Override
  public void run() {
    try
    {
      String line=bufIn.readLine();
      //读取第一个选着功能
      int i=Integer.valueOf(line);
      String cipherText = bufIn.readLine();
      String plainText = new String(SM2Utils.decrypt(Util.hexToByte(prik), Util.hexToByte(cipherText)));
      arr = plainText.split("#");
      byte[]  list0 = arr[1].getBytes("iso-8859-1");
      System.out.println("list0.length:"+list0.length);
      byte[]  list1 = arr[2].getBytes("iso-8859-1");
      listSK = new ArrayList<byte[]>();
      listSK.add(list0);
      listSK.add(list1);
      switch(i)
      {
        case IEX2LEVTYPE:
          IEX2LevSearch();//IEX2lev查询0
          break;
        case  IEXZMFTYPE:
          IEXZMFSearch();//IEXZMF查询1
          break;
        case  AddIEX2LEVTYPE:
          AddIEX2LEV();//AddIEXZMF查询2
          break;
        case  AddIEXZMFTYPE:
          AddIEXZMF();//AddIEXZMF查询3
          break;
      }
    }catch(Exception e)
    {
      socketUtil.close(socket);
    }
  }

  private void IEX2LevSearch() throws Exception {
    System.out.println("arr[0]:"+arr[0]);
    Set<String> ss = SearchIEX2lev.Search(arr[0],listSK);
    String resultset ="";
    for(String str:ss)
      resultset = resultset + str +"#";
    bufOut.write("ok\n");//回馈查询信心
    bufOut.flush();
    bufOut.write(resultset+"\n");
    bufOut.flush();
  }

  private void IEXZMFSearch() throws Exception {
    System.out.println("arr[0]:"+arr[0]);
    List<String> ss = SearchIEXZMF.Search(listSK, arr[0]);
    String resultset ="";
    for(String str:ss)
      resultset = resultset + str +"#";
    bufOut.write("ok\n");//回馈查询信心
    bufOut.flush();
    bufOut.write(resultset+"\n");
    bufOut.flush();
  }


  private void AddIEX2LEV() throws Exception {
      //得到input流用于数据的获取
    dis = new DataInputStream(socket.getInputStream());
    int FileNum = dis.read();
    List<File> FileList = new ArrayList<File>();
    List<Long> FileLength = new ArrayList<Long>();
    for(int i=0;i<FileNum;i++) {
      String name = dis.readUTF();
      FileList.add(new File(".\\testfile\\" + name));
      FileLength.add(dis.readLong());
    }
      //创建一个output流用于保存文件
    for(int j=0;j<FileNum;j++){
      FileOutputStream fos = new FileOutputStream(FileList.get(j));
      //将文件的字节数组写入output流中
      int remainder = (int) (FileLength.get(j)%5);
      int IntValue = (int) (FileLength.get(j)/5);
      byte[] bytes = new byte[5];
      int length = 0;
      for(int i=0;i<IntValue;i++){
        length = dis.read(bytes, 0, bytes.length);
        fos.write(bytes, 0, length);
        fos.flush();
      }
      dis.read(bytes, 0, remainder);
      fos.write(bytes, 0, remainder);
      fos.flush();
      fos.close();
    }
    dis.close();
    //关闭相应的流
    System.out.println("文件:"+"上传成功！");
    AddIEX2lev.Add(listSK,".\\testfile");
    deleteDir(".\\testfile");
  }

  private void AddIEXZMF() throws Exception {
    //得到input流用于数据的获取
    dis = new DataInputStream(socket.getInputStream());
    int FileNum = dis.read();
    List<File> FileList = new ArrayList<File>();
    List<Long> FileLength = new ArrayList<Long>();
    for(int i=0;i<FileNum;i++) {
      String name = dis.readUTF();
      FileList.add(new File(".\\testfile\\" + name));
      FileLength.add(dis.readLong());
    }
    //创建一个output流用于保存文件
    for(int j=0;j<FileNum;j++){
      FileOutputStream fos = new FileOutputStream(FileList.get(j));
      //将文件的字节数组写入output流中
      int remainder = (int) (FileLength.get(j)%5);
      int IntValue = (int) (FileLength.get(j)/5);
      byte[] bytes = new byte[5];
      int length = 0;
      for(int i=0;i<IntValue;i++){
        length = dis.read(bytes, 0, bytes.length);
        fos.write(bytes, 0, length);
        fos.flush();
      }
      dis.read(bytes, 0, remainder);
      fos.write(bytes, 0, remainder);
      fos.flush();
      fos.close();
    }
    dis.close();
    //关闭相应的流
    System.out.println("文件:"+"上传成功！");
    AddIEXZMF.Add(listSK,".\\testfile");
    deleteDir(".\\testfile");
  }


  public static boolean deleteDir(String path){
    File file = new File(path);
    if(!file.exists()){//判断是否待删除目录是否存在
      System.err.println("The dir are not exists!");
      return false;
    }
    String[] content = file.list();//取得当前目录下所有文件和文件夹
    for(String name : content){
      File temp = new File(path, name);
      if(temp.isDirectory()){//判断是否是目录
        deleteDir(temp.getAbsolutePath());//递归调用，删除目录里的内容
        temp.delete();//删除空目录
      }else{
        if(!temp.delete()){//直接删除文件
          System.err.println("Failed to delete " + name);
        }
      }
    }
    return true;
  }

}
