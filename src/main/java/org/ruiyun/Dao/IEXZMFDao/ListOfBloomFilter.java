package org.ruiyun.Dao.IEXZMFDao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.ruiyun.DB.IEXZMFDB;

/**
 * 对数据库中ListOfBloomFilter表进行增删改差的功能，
 * ListOfBloomFilter表存储（编号（唯一标识），文件名，布隆过滤文件唯一标识（布隆过滤器的编号））
 * @author ltc
 * @className ListOfBloomFilter
 * @since 2020/1/10 20:05
 */
public class ListOfBloomFilter {

  /**
   * 在ListOfBloomFilter表中插入一条记录
   * 编号（唯一标识）是自动递增的，不用在SQL写出
   * @param docid 文件名
   * @param position 布隆过滤文件唯一标识（布隆过滤器的编号）
   * @throws Exception
   */
  public static void InsertListOfBloomFilter(String docid,String position) throws Exception {
    Connection conn = null;
    PreparedStatement ps = null;
    try {
      conn =  IEXZMFDB.getConnection();
      conn.setAutoCommit(false);
      ps = conn
        .prepareStatement("INSERT INTO ListOfBloomFilter (docid,position) values(?,?)");
      ps.setString(1, docid);
      ps.setString(2, position);
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
   * 根据文件名查找布隆过滤文件唯一标识（布隆过滤器的编号）
   * @param docid 文件名
   * @return 布隆过滤文件唯一标识（布隆过滤器的编号）
   * @throws SQLException
   */
  public static String FindListOfBloomFilter(String docid) throws SQLException{
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    String  temp = "";
    try {
      con = IEXZMFDB.getConnection();
      ps = con.prepareStatement("SELECT position FROM ListOfBloomFilter WHERE docid=?");
      ps.setString(1, docid);
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
   * 查找ListOfBloomFilter中数据的数量
   * @return 数据的数量
   * @throws SQLException
   */
  public static int FindListOfBloomFilterSum() throws SQLException{
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    int  temp = 0;
    try {
      con = IEXZMFDB.getConnection();
      ps = con.prepareStatement("SELECT COUNT(docid) FROM listofbloomfilter");
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
   * 查找ListOfBloomFilter中文件名为docid的数目
   * @param docid 文件名
   * @return 文件名为docid的数目
   * @throws SQLException
   */
  public static int FindListOfBloomFilterIdSum(String docid) throws SQLException{
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    int  temp = 0;
    try {
      con = IEXZMFDB.getConnection();
      ps = con.prepareStatement("SELECT COUNT(docid) FROM listofbloomfilter where docid=?");
      ps.setString(1, docid);
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
   * 查找ListOfBloomFilter表中所有的文件名
   * @return 所有的文件名
   * @throws SQLException
   */
  public static List<String>  FindListOfBloomFilterId() throws SQLException{
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    List<String>  temp = new ArrayList<String>() ;
    try {
      con = IEXZMFDB.getConnection();
      ps = con.prepareStatement("SELECT docid FROM listofbloomfilter");
      rs = ps.executeQuery();
      while(rs.next()){
        temp.add(rs.getString("docid"));
      }
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
   * 清空ListOfBloomFilter表的数据
   * @throws Exception
   */
  public static void TruncateListOfBloomFilter() throws Exception {
    Connection conn = null;
    PreparedStatement ps = null;
    try {
      conn = IEXZMFDB.getConnection();
      ps = conn
        .prepareStatement("truncate  table listofbloomfilter");
      ps.executeUpdate();
    } catch (Exception es) {
      throw es;
    } finally {
      ps.close();
      conn.close();
    }
  }
}

