package org.ruiyun.Servlet;


import com.google.gson.Gson;
import org.crypto.sse.CryptoPrimitives;
import org.ruiyun.Util.AddIEX2lev;
import org.ruiyun.Util.SearchIEX2lev;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author ltc
 * @className IEX2lev
 * @since 2020/1/13 13:23
 */
class IEX2levStatus{
  private String msg;
  private boolean status;
  public IEX2levStatus(String msg,boolean status){
    this.msg = msg;
    this.status = status;
  }
}
public class IEX2levServlet extends HttpServlet {

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
        SearchIEX2lev(request, response);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }else if (method.equalsIgnoreCase("add")) {
      try {
        AddIEX2lev(request, response);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }



  private void SearchIEX2lev(HttpServletRequest request, HttpServletResponse response) throws Exception {
    PrintWriter out = response.getWriter();

    String s = request.getParameter("search");
    byte[]  list0 = request.getParameter("listSK0").getBytes("gbk");
    System.out.println("list0.length:"+list0.length);
    byte[]  list1 = request.getParameter("listSK1").getBytes("gbk");
    List<byte[]> listSK = new ArrayList<byte[]>();
    listSK.add(list0);
    listSK.add(list1);
    Set<String> ss = SearchIEX2lev.Search(s,listSK);
    String reslist = "";
    for (String i : ss)
      reslist = reslist + i+" ";
    out.println(new Gson().toJson(new IEX2levStatus(reslist,true)));
    out.flush();
  }


  private void AddIEX2lev(HttpServletRequest request, HttpServletResponse response) throws Exception {
    PrintWriter out = response.getWriter();
    byte[]  list0 = request.getParameter("listSK0").getBytes("gbk");
    System.out.println("list0.length:"+list0.length);
    byte[]  list1 = request.getParameter("listSK1").getBytes("gbk");
    List<byte[]> listSK = new ArrayList<byte[]>();
    listSK.add(list0);
    listSK.add(list1);
    AddIEX2lev.Add(listSK,uploadFolder);
    Object iexstatus = new IEX2levStatus("",true);
    out.println(new Gson().toJson(iexstatus));
    out.flush();
  }


}
