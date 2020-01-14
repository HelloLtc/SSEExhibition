package org.ruiyun.DB;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;
/**建立IEX2lev方案的数据库连接
 * @author ltc
 * @className IEX2levDB
 * @since 2020/1/9 20:27
 */
public class IEX2levDB {

  public static Connection getConnection(){
    Connection conn=null;
    try {
      Class.forName("com.mysql.jdbc.Driver");
      conn = DriverManager
        .getConnection("jdbc:mysql://localhost:3306/iex2lev",
          "root", "123456");
    } catch (Exception e) {
      e.printStackTrace();
    }
    return conn;
  }
}
