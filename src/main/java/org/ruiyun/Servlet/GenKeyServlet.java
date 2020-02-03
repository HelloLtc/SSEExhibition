package org.ruiyun.Servlet;

import com.google.gson.Gson;
import org.crypto.sse.IEX2Lev;
import org.crypto.sse.IEXZMF;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.List;

/**
 * @author ltc
 * @className GenKeyServlet
 * @since 2020/1/16 15:36
 */
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
    String pass = request.getParameter("genkey");
    String listSK = IEX2Lev.keyGenString(256, pass, "salt/saltSetM", 100000);//根据输入值生成密钥
    PrintWriter out = response.getWriter();
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    out.println(new Gson().toJson(listSK));
    out.flush();
  }

  }
