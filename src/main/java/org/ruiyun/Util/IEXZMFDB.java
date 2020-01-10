package org.ruiyun.Util;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;
/**建立IEXZMF方案的数据库连接
 * @author ltc
 * @className IEXZMFDB
 * @since 2020/1/9 20:40
 */
public class IEXZMFDB {
  private IEXZMFDB(){}
  private static Connection con;
  public static Connection getConnection(){
    String driver;
    String url;
    String name;
    String password;
    try {
      InputStream is = IEXZMFDB.class.getClassLoader().getResourceAsStream("resources/config.properties");
      Properties properties = new Properties();
      properties.load(is);
      driver = properties.getProperty("driver");
      url = properties.getProperty("zmfurl");
      name = properties.getProperty("name");
      password = properties.getProperty("password");
      Class.forName(driver);
      con = DriverManager.getConnection(url, name, password);
    }catch (Exception ep){
      throw new RuntimeException(ep+"IEXZMF数据库连接失败");
    }
    return con;
  }
}
