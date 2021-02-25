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
  private IEX2levDB(){}
  private static Connection con;
  public static Connection getConnection(){
    String driver;
    String url;
    String name;
    String password;
    try {
      InputStream is = IEX2levDB.class.getClassLoader().getResourceAsStream("config.properties");
      Properties properties = new Properties();
      properties.load(is);
      driver = properties.getProperty("driver");
      url = properties.getProperty("2levurl");
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
