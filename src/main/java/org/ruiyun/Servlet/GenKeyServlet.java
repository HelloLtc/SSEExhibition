package org.ruiyun.Servlet;

import com.google.gson.Gson;
import org.crypto.sse.IEX2Lev;
import org.crypto.sse.IEXZMF;
import org.hsqldb.Session;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.util.List;

/**
 * @author ltc
 * @className GenKeyServlet
 * @since 2020/1/16 15:36
 */
class GenKeyStatus{
  private String key0;
  private String key1;
  private String key2;
  private boolean status;
  public GenKeyStatus(String key0,String key1,String key2,boolean status){
    this.key0 = key0;
    this.key1 = key1;
    this.key2 = key2;
    this.status = status;
  }
}
public class GenKeyServlet  extends HttpServlet {
  public void doGet(HttpServletRequest request, HttpServletResponse response) {
    doPost(request, response);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) {
    try {
      GenKey(request, response);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void GenKey(HttpServletRequest request, HttpServletResponse response) throws Exception {
    PrintWriter out = response.getWriter();
    String pass = request.getParameter("genkey"); System.out.println("listSK "+pass);
    //  List<String> listSK = IEX2Lev.keyGenList(256, pass, "salt/saltSetM", 100000);//根据输入值生成密钥
    String listSK = IEX2Lev.keyGenString(128, pass, "salt/saltSetM", 100000);//根据输入值生成密钥
    System.out.println("listSK "+listSK);
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    Object listKey = new GenKeyStatus(listSK,listSK,listSK,true);
    out.println(new Gson().toJson(listKey));
    out.flush();
  }

}
