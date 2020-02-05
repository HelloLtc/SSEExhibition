package org.ruiyun.Servlet;

import com.google.gson.Gson;
import org.ruiyun.Util.AddIEX2lev;
import org.ruiyun.Util.AddIEXZMF;
import org.ruiyun.Util.SearchIEX2lev;
import org.ruiyun.Util.SearchIEXZMF;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author ltc
 * @className IEXZMFServlet
 * @since 2020/1/14 19:26
 */
class IEX2levZMF{
  private String msg;
  private boolean status;
  public IEX2levZMF(String msg,boolean status){
    this.msg = msg;
    this.status = status;
  }
}


public class IEXZMFServlet extends HttpServlet {
  private static String uploadFolder ;

  public void init() throws ServletException {
    Object pathRoot = getServletContext().getRealPath("/");
    uploadFolder = (String) pathRoot+"testfile";
    System.out.println("FileUpload servlet Init(), uploaderFolder: " + uploadFolder);
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) {
    doPost(request, response);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) {
    String method = request.getParameter("method");
    if (method == null) {
      return;
    } else if (method.equalsIgnoreCase("search")) {
      try {
        SearchIEXZMF(request, response);
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else if (method.equalsIgnoreCase("add")) {
      try {
        AddIEXZMF(request, response);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }



  private void SearchIEXZMF(HttpServletRequest request, HttpServletResponse response) throws Exception {
    PrintWriter out = response.getWriter();
    String s = request.getParameter("zmfsearch");
    byte[]  list0 = request.getParameter("listSK0").getBytes("gbk");
    System.out.println("list0.length:"+list0.length);
    byte[]  list1 = request.getParameter("listSK1").getBytes("gbk");
    List<byte[]> listSK = new ArrayList<byte[]>();
    listSK.add(list0);
    listSK.add(list1);
    List<String> ss = SearchIEXZMF.Search(listSK,s);
    String reslist = "";
    for(int i=0;i<ss.size();i++)
      reslist = reslist + ss.get(i) +" ";
    Object iexstatus = new IEX2levZMF(reslist,true);
    out.println(new Gson().toJson(iexstatus));
    out.flush();
  }

  private void AddIEXZMF(HttpServletRequest request, HttpServletResponse response) throws Exception {
    PrintWriter out = response.getWriter();
    byte[]  list0 = request.getParameter("listSK0").getBytes("gbk");
    System.out.println("list0.length:"+list0.length);
    byte[]  list1 = request.getParameter("listSK1").getBytes("gbk");
    List<byte[]> listSK = new ArrayList<byte[]>();
    listSK.add(list0);
    listSK.add(list1);
    AddIEXZMF.Add(listSK,uploadFolder);
    Object iexstatus = new IEX2levZMF("",true);
    out.println(new Gson().toJson(iexstatus));
    out.flush();
  }
}
