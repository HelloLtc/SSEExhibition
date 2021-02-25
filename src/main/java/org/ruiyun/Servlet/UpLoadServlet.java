package org.ruiyun.Servlet;

import com.google.gson.Gson;
import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.apache.tomcat.util.http.fileupload.servlet.ServletRequestContext;
import org.ruiyun.Util.AddIEX2lev;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

class FileUploadStatus {
  private String fList;
  private String msg;
  private boolean status;

  /**
   * Error return
   */
  public FileUploadStatus(String msg) {
    this.msg = msg;
    this.status = false;
  }

  /**
   * @output: fList "[1, 3, 22]"
   */
  public FileUploadStatus(boolean status, ArrayList<String> fList) {
    this.fList = fList.toString();
    this.msg = "Upload successfully.";
    this.status = status;
  }

  public FileUploadStatus getObj() {
    return this;
  }
}



public class UpLoadServlet extends HttpServlet {

  private static final int MEMORY_THRESHOLD = 1024 * 1024 * 3;  // 3MB
  private static final int MAX_FILE_SIZE = 1024 * 1024 * 1024; // 1024MB
  private static final int MAX_REQUEST_SIZE = 1024 * 1024 * 1034; // 1034MB

  private static String uploadFolder ;

  public void init() throws ServletException {
    Object pathRoot = getServletContext().getRealPath("/");
    uploadFolder = (String) pathRoot+"testfile\\";
    System.out.println("FileUpload servlet Init(), uploaderFolder: " + uploadFolder);
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) {
    doPost(request, response);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) {
    try {
      upload(request, response);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void upload(HttpServletRequest req, HttpServletResponse resp) throws Exception {
    resp.setContentType("application/json");
    resp.setCharacterEncoding("UTF-8");

    PrintWriter out = resp.getWriter();

    if (!ServletFileUpload.isMultipartContent(req)) {
      //  check
      Object statusObj = new FileUploadStatus("Error: enctype=multipart/form-data").getObj();
      out.println(new Gson().toJson(statusObj));
      out.flush();
      return;
    }
    // 配置上传参数
    DiskFileItemFactory factory = new DiskFileItemFactory();
    // 设置内存临界值 - 超过后将产生临时文件并存储于临时目录中
    factory.setSizeThreshold(MEMORY_THRESHOLD);
    // 设置临时存储目录
    factory.setRepository(new File(System.getProperty("java.io.tmpdir")));

    ServletFileUpload upload = new ServletFileUpload(factory);

    // 设置最大文件上传值
    upload.setFileSizeMax(MAX_FILE_SIZE);

    // 设置最大请求值 (包含文件和表单数据)
    upload.setSizeMax(MAX_REQUEST_SIZE);

    // 中文处理
    upload.setHeaderEncoding("UTF-8");

    //  ATTENTION: 默认上传文件无文件夹分类
    try {
      // 解析请求的内容提取文件数据
      @SuppressWarnings("unchecked")
      ArrayList fList = new ArrayList<String>();
      //  TIPS: request context
      List<FileItem> formItems = upload.parseRequest(new ServletRequestContext(req));
      if (formItems != null && formItems.size() > 0) {
        //  iteration
        for (FileItem item : formItems) {
          //   处理不在表单中的字段
          String fileName = new File(item.getName()).getName();
          fList.add(fileName);
          String filePath = uploadFolder + fileName;
          File storeFile = new File(filePath);
//                    System.out.println("上传路径: " + filePath);
          item.write(storeFile);
        }
      }
      Object statusObj = new FileUploadStatus(true, fList);
      out.println(new Gson().toJson(statusObj));
      out.flush();
    } catch (Exception err) {
      System.out.println("upload failed: " + err.getMessage());
      Object statusObj = new FileUploadStatus("Upload failed");
      out.println(new Gson().toJson(statusObj));
      out.flush();
    }
  }
}
