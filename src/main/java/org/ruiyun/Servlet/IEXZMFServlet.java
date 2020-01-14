package org.ruiyun.Servlet;

import org.ruiyun.Util.SearchIEX2lev;
import org.ruiyun.Util.SearchIEXZMF;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Set;

/**
 * @author ltc
 * @className IEXZMFServlet
 * @since 2020/1/14 19:26
 */
public class IEXZMFServlet extends HttpServlet {
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
    } else if (method.equalsIgnoreCase("register")) {
      //    register(request, response);
    }
  }

  private void SearchIEXZMF(HttpServletRequest request, HttpServletResponse response) throws Exception {
    String pathRoot = getServletContext().getRealPath("/");
    String s = request.getParameter("zmfsearch");
    List<String> ss = SearchIEXZMF.Search(s);
    request.setAttribute("ZmfResult", ss);
    request.getRequestDispatcher("main.jsp").forward(request,
      response);
  }
}
