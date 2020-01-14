package org.ruiyun.Dao.IEXZMFDao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.ruiyun.DB.IEXZMFDB;

/**
 * 对数据库IEXZMF中BloomFilterMap表进行增删改查的功能，
 * BloomFilterMap表存储（布隆过滤文件唯一标识（布隆过滤器的编号），文件布隆过滤的信息）
 * @author ltc
 * @className BloomFilterMap
 * @since 2020/1/10 18:28
 */
public class BloomFilterMap {

  /**
   * 在BloomFilterMap表中添加一条记录
   * @param position 文件布隆过滤的信息
   * @throws Exception
   */
  public static void InsertBloomFilterMap(String position) throws Exception {
    Connection conn = null;
    PreparedStatement ps = null;
    try {
      conn =  IEXZMFDB.getConnection();
      conn.setAutoCommit(false);
      ps = conn
        .prepareStatement("INSERT INTO BloomFilterMap (position) values(?)");
      ps.setString(1, position);
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

  public static int FindBloomFilterMap(String position) throws Exception {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    int numberofbf = -1;
    try {
      conn =  IEXZMFDB.getConnection();
      conn.setAutoCommit(false);
      ps = conn
        .prepareStatement("SELECT numberofbf FROM BloomFilterMap WHERE position=?");
      ps.setString(1, position);
      rs = ps.executeQuery();
      while(rs.next())
        numberofbf = rs.getInt("numberofbf");
    } catch (Exception es) {
      conn.rollback();
      throw es;
    } finally {
      conn.setAutoCommit(true);
      ps.close();
      conn.close();
    }
    return numberofbf;
  }

  /**
   * 根据布隆过滤文件唯一标识（布隆过滤器编号）查找布隆过滤器的信息
   * @param numberofbf 布隆过滤文件唯一标识（布隆过滤器编号）
   * @return 布隆过滤器的信息
   * @throws SQLException
   */
  public static String FindBloomFilterMap(int numberofbf) throws SQLException{
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    String  temp = "";
    try {
      con = IEXZMFDB.getConnection();
      ps = con.prepareStatement("SELECT position FROM BloomFilterMap WHERE numberofbf=?");
      ps.setInt(1, numberofbf);
      rs = ps.executeQuery();
      if(!rs.next())
        return null;
      temp = rs.getString("position");
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
   * 清空BloomFilterMap表的数据
   * @throws Exception
   */
  public static void TruncateBloomFilterMap() throws Exception {
    Connection conn = null;
    PreparedStatement ps = null;

    try {
      conn = IEXZMFDB.getConnection();
      ps = conn
        .prepareStatement("truncate  table BloomFilterMap");
      ps.executeUpdate();
    } catch (Exception es) {
      throw es;
    } finally {
      ps.close();
      conn.close();
    }
  }

}

