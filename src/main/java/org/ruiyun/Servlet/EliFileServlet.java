package org.ruiyun.Servlet;

import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

class EliFileStatus{
  private String msg;
  private Boolean status;
  public EliFileStatus(String msg,boolean status){
    this.msg = msg;
    this.status = status;
  }
}

public class EliFileServlet extends HttpServlet {

  private static String path ;

  public void init() throws ServletException {
    Object pathRoot = getServletContext().getRealPath("/");
    path = (String) pathRoot+"testfile";
    System.out.println("FileUpload servlet Init(), uploaderFolder: " + path);
  }


  public void doGet(HttpServletRequest request, HttpServletResponse response) {
    doPost(request, response);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) {
    try {
      DeleteDir(request, response);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void DeleteDir(HttpServletRequest request, HttpServletResponse response) throws IOException {
    PrintWriter out = response.getWriter();

    File file = new File(path);
    if(!file.exists()){//判断是否待删除目录是否存在
      System.err.println("The dir are not exists!");
      Object  EliFileObj= new EliFileStatus("未删除目录文件",false);
      out.println(new Gson().toJson(EliFileObj));
      out.flush();
    }
    String[] content = file.list();//取得当前目录下所有文件和文件夹
    for(String name : content){
      File temp = new File(path, name);
      if(temp.isDirectory()){//判断是否是目录
        DeleteDir(request,response);//递归调用，删除目录里的内容
        temp.delete();//删除空目录
      }else{
        if(!temp.delete()){//直接删除文件
          System.err.println("Failed to delete " + name);
        }
      }
    }
    Object  EliFileObj= new EliFileStatus("",true);
    out.println(new Gson().toJson(EliFileObj));
    out.flush();
  }
}
