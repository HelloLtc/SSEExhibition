package org.ruiyun.Dao.IEXZMFDao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.ruiyun.Util.IEXZMFDB;

/**
 * 对数据库IEXZMF中BloomFilterID表进行增删改查的功能，
 * BloomFilterID表存储（布隆过滤文件唯一标识（布隆过滤器的编号），文件名）
 * @author ltc
 * @className BloomFilterID
 * @since 2020/1/10 18:12
 */
public class BloomFilterID {

  /**
   *在BloomFilterID表中插入一条记录
   * @param numberofbf 文件唯一标识（布隆过滤器的编号）
   * @param id 文件名
   * @throws Exception
   */
  public static void InsertBloomFilterID(int numberofbf,String id) throws Exception {
    Connection conn = null;
    PreparedStatement ps = null;
    try {
      conn =  IEXZMFDB.getConnection();
      conn.setAutoCommit(false);
      ps = conn
        .prepareStatement("INSERT INTO BloomFilterID (numberofbf,docid) values(?,?)");
      ps.setInt(1, numberofbf);
      ps.setString(2, id);
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
   * 根据文件唯一标识（布隆过滤器的编号）查找文件名
   * @param numberofbf 文件唯一标识（布隆过滤器的编号）
   * @return 文件名
   * @throws SQLException
   */
  public static String FindBloomFilterID(int numberofbf) throws SQLException{
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    String  temp = "";
    try {
      con = IEXZMFDB.getConnection();
      ps = con.prepareStatement("SELECT docid FROM BloomFilterID WHERE numberofbf=?");
      ps.setInt(1, numberofbf);
      rs = ps.executeQuery();
      if(!rs.next())
        return null;
      temp = rs.getString("docid");
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
   *查找BloomFilterID表中记录的数目
   * @return 文件数目
   * @throws SQLException
   */
  public static int FindBloomFilterIDSum() throws SQLException{
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    int  temp = 0;
    try {
      con = IEXZMFDB.getConnection();
      ps = con.prepareStatement("SELECT COUNT(docid) FROM BloomFilterID");
      rs = ps.executeQuery();
      if(!rs.next())
        return 0;
      temp = rs.getInt("COUNT(docid)");
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
   * 清空BloomFilterID表的数据
   * @throws Exception
   */
  public static void TruncateBloomFilterID() throws Exception {
    Connection conn = null;
    PreparedStatement ps = null;
    try {
      conn = IEXZMFDB.getConnection();
      ps = conn
        .prepareStatement("truncate  table BloomFilterID");
      ps.executeUpdate();
    } catch (Exception es) {
      throw es;
    } finally {
      ps.close();
      conn.close();
    }
  }

}

