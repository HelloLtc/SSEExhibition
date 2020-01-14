package org.ruiyun.Dao.IEX2levDao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.ruiyun.DB.IEX2levDB;

/**
 * 对数据库IEX2lev中GlobalArrayIndex表和GlobalArrayList表进行增删改查的功能，
 * GlobalArrayIndex表存储（关键字，包含关键字的文件名/指向GlobalArrayList的索引）
 * GlobalArrayList表存储（索 引，包含关键字的文件名/指向GlobalArrayList的索引）
 * @author ltc
 * @className GlobalArray
 * @since 2020/1/9 21:06
 */
public class GlobalArray {

  /**
   * 在GlobalArrayIndex表中插入一条数据
   * @param keyword 关键字
   * @param listarrayindex 包含关键字的文件/索引（索引指向GlobalArrayList）
   * @throws Exception
   */
  public static void InsertGlobalArrayIndex(String keyword,byte[] listarrayindex) throws Exception {
    Connection conn = null;
    PreparedStatement ps = null;
    try {
      conn =  IEX2levDB.getConnection();
      conn.setAutoCommit(false);
      ps = conn
        .prepareStatement("INSERT INTO GlobalArrayIndex (GlobalKeyword,GlobalListArrayIndex) values(?,?)");
      ps.setString(1, keyword);
      ps.setBytes(2, listarrayindex);
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
   * 根据关键字删除GlobalArrayIndex表中的一条记录
   * @param keyword 关键字
   * @throws Exception
   */
  public static void deleteGlobalArrayIndex(String keyword) throws Exception {
    Connection conn = null;
    PreparedStatement ps = null;

    try {
      conn = IEX2levDB.getConnection();
      ps = conn
        .prepareStatement("DELETE FROM GlobalArrayIndex WHERE GlobalKeyword = ?");
      ps.setString(1, keyword);
      ps.executeUpdate();
    } catch (Exception es) {
      throw es;
    } finally {
      ps.close();
      conn.close();
    }
  }

  /**
   * 根据关键字找到包含关键字的文件名称/指向GlobalArrayIndex的索引
   * @param keyword 关键字
   * @return 包含关键字的文件名称/指向GlobalArrayIndex的索引
   * @throws SQLException
   */
  public static byte[] FindGlobalListArrayIndex(String keyword) throws SQLException{
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    byte  temp[] = null;
    try {
      con = IEX2levDB.getConnection();
      ps = con.prepareStatement("SELECT GlobalListArrayIndex FROM GlobalArrayIndex WHERE GlobalKeyword=?");
      ps.setString(1, keyword);
      rs = ps.executeQuery();
      if(!rs.next())
        return null;
      temp = rs.getBytes("GlobalListArrayIndex");
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
   * 在GlobalArrayList表中插入一条数据
   * (由于主键GlobalCounter在数据库中自动递增，所以SQL不用添加GlobalCounter)
   * @param templist 包含关键字的文件/索引串（GlobalCounter为单个索引）
   * @throws Exception
   */
  public static void InsertGlobalArrayList(byte[] templist) throws Exception {
    Connection conn = null;
    PreparedStatement ps = null;
    try {
      conn = IEX2levDB.getConnection();
      conn.setAutoCommit(false);
      ps = conn
        .prepareStatement("INSERT INTO GlobalArrayList (GlobalTempList) values(?)");
      ps.setBytes(1, templist);
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
   * 根据GlobalTempList查找其标识GlobalCounter
   * @param GlobalTempList 包含关键字文件名/索引
   * @return 包含GlobalTempList的标识GlobalCounter
   * @throws Exception
   */
  public static int FindGlobalArrayList(byte[] GlobalTempList) throws Exception {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    int temp = -1;
    try {
      conn = IEX2levDB.getConnection();
      conn.setAutoCommit(false);
      ps = conn
        .prepareStatement("SELECT GlobalCounter FROM GlobalArrayList WHERE GlobalTempList=?");
      ps.setBytes(1, GlobalTempList);
      rs = ps.executeQuery();
      while(rs.next())
        temp = rs.getInt("GlobalCounter");
    } catch (Exception es) {
      conn.rollback();
      throw es;
    } finally {
      conn.setAutoCommit(true);
      ps.close();
      conn.close();
    }
    return temp;
  }

  /**
   * 根据主键GlobalCounter删除GlobalArrayList表中的一条记录
   * @param GlobalCounter GlobalArrayList表的主键GlobalCounter
   * @throws Exception
   */
  public static void deleteGlobalArrayList(int GlobalCounter) throws Exception {
    Connection conn = null;
    PreparedStatement ps = null;
    try {
      conn = IEX2levDB.getConnection();
      ps = conn
        .prepareStatement("DELETE FROM GlobalArrayList WHERE GlobalCounter = ?");
      ps.setInt(1, GlobalCounter);
      ps.executeUpdate();
    } catch (Exception es) {
      throw es;
    } finally {
      ps.close();
      conn.close();
    }
  }

  /**
   * 根据主键GlobalCounter查找GlobalArrayList表中的GlobalTempList（包含关键字的文件名/索引）
   * @param GlobalCounter GlobalArrayList表中的主键GlobalCounter
   * @return GlobalTempList（包含关键字的文件名/索引）
   * @throws SQLException
   */
  public static byte[] findGlobalListArrayList(int GlobalCounter) throws SQLException{
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    byte  temp[] = null;
    try {
      con = IEX2levDB.getConnection();
      ps = con.prepareStatement("SELECT GlobalTempList FROM GlobalArrayList WHERE GlobalCounter=?");
      ps.setInt(1, GlobalCounter);
      rs = ps.executeQuery();
      //rs.next();
      if(!rs.next())
        return null;
      temp = rs.getBytes("GlobalTempList");
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
   * 清空GlobalArrayIndex表的信息
   * @throws Exception
   */
  public static void TruncateGlobalArrayIndex() throws Exception {
    Connection conn = null;
    PreparedStatement ps = null;

    try {
      conn = IEX2levDB.getConnection();
      ps = conn
        .prepareStatement(" truncate  table globalarrayindex");
      ps.executeUpdate();
    } catch (Exception es) {
      throw es;
    } finally {
      ps.close();
      conn.close();
    }
  }

  /**
   * 清空GlobalArrayList表的信息
   * @throws Exception
   */
  public static void TruncateGlobalArrayList() throws Exception {
    Connection conn = null;
    PreparedStatement ps = null;

    try {
      conn = IEX2levDB.getConnection();
      ps = conn
        .prepareStatement("truncate  table globalarraylist");
      ps.executeUpdate();
    } catch (Exception es) {
      throw es;
    } finally {
      ps.close();
      conn.close();
    }
  }

}
