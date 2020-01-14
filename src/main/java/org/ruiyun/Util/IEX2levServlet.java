package org.ruiyun.Util;


import java.util.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author ltc
 * @className IEX2lev
 * @since 2020/1/13 13:23
 */
public class IEX2levServlet extends HttpServlet {

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
    } else if (method.equalsIgnoreCase("register")) {
  //    register(request, response);
    }
  }

  private void SearchIEX2lev(HttpServletRequest request, HttpServletResponse response) throws Exception {
    String pathRoot = getServletContext().getRealPath("/");
    String s = request.getParameter("search");
    Set<String> ss = SearchIEX2lev.func(s,pathRoot);
    request.setAttribute("result", ss);
    request.getRequestDispatcher("main.jsp").forward(request,
      response);
  }



}
