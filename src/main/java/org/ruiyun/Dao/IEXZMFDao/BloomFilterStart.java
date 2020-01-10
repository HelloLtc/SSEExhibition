package org.ruiyun.Dao.IEXZMFDao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.ruiyun.Util.IEXZMFDB;

/**
 * 对数据库IEXZMF中BloomFilterStart表进行增删改查的功能，
 * BloomFilterMap表存储（关键字，包含关键字的文件标识（布隆过滤器的编号））
 * @author ltc
 * @className BloomFilterStart
 * @since 2020/1/10 18:47
 */
public class BloomFilterStart {

  /**
   * 在BloomFilterStart插入一条记录
   * @param keyword 关键字
   * @param numberofbflist 包含关键字的文件标识（布隆过滤器的编号）
   * @throws Exception
   */
  public static void InsertBloomFilterStart(String keyword,String numberofbflist) throws Exception {
    Connection conn = null;
    PreparedStatement ps = null;
    try {
      conn =  IEXZMFDB.getConnection();
      conn.setAutoCommit(false);
      ps = conn
        .prepareStatement("INSERT INTO BloomFilterStart (keyword,numberofbflist) values(?,?)");
      ps.setString(1, keyword);
      ps.setString(2, numberofbflist);
      ps.executeUpdate();
      conn.commit();
    } catch (Exception es) {
      conn.rollback();
      throw es;
    } finally {
      conn.setAutoCommit(true);
      ps.close();
      conn.close();
    }
  }

  /**
   * 根据关键字查找包含关键字的文件标识（布隆过滤器的编号）
   * @param keyword 关键字
   * @return 包含关键字的文件标识（布隆过滤器的编号）
   * @throws SQLException
   */
  public static String FindBloomFilterStart(String keyword) throws SQLException{
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    String  temp = "";
    try {
      con = IEXZMFDB.getConnection();
      ps = con.prepareStatement("SELECT numberofbflist FROM BloomFilterStart WHERE keyword=?");
      ps.setString(1, keyword);
      rs = ps.executeQuery();
      if(!rs.next())
        return null;
      temp = rs.getString("numberofbflist");
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      con.close();
      ps.close();
      rs.close();
    }
    return temp;
  }

  /**
   * 清空BloomFilterStart表的数据
   * @throws Exception
   */
  public static void TruncateBloomFilterStart() throws Exception {
    Connection conn = null;
    PreparedStatement ps = null;

    try {
      conn = IEXZMFDB.getConnection();
      ps = conn
        .prepareStatement("truncate  table BloomFilterStart");
      ps.executeUpdate();
    } catch (Exception es) {
      throw es;
    } finally {
      ps.close();
      conn.close();
    }
  }

}
