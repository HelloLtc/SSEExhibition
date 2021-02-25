package org.ruiyun.Dao.IEXZMFDao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.ruiyun.DB.IEXZMFDB;

/**
 * 对数据库IEXZMF中BloomGlobalArrayIndex表和BloomGlobalArrayList表进行增删改查的功能，
 * BloomGlobalArrayIndex表存储（关键字，包含关键字的文件名/索引）
 * BloomGlobalArrayList表存储（索引（唯一标识），包含关键字的文件名/索引）
 * 其中索引是指向BloomGlobalArrayList
 * @author ltc
 * @className BloomGlobalArray
 * @since 2020/1/10 19:47
 */
public class BloomGlobalArray {

  /**
   *在BloomGlobalArrayIndex表中插入一条记录
   * @param keyword 关键字
   * @param listarrayindex 包含关键字的文件名/索引
   * @throws Exception
   */
  public static void InsertGlobalArrayIndex(String keyword,byte[] listarrayindex) throws Exception {
    Connection conn = null;
    PreparedStatement ps = null;
    try {
      conn =  IEXZMFDB.getConnection();
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

  public static void UpdateGlobalArrayIndex(String keyword,byte[] listarrayindex) throws Exception {
    Connection conn = null;
    PreparedStatement ps = null;
    try {
      conn =  IEXZMFDB.getConnection();
      conn.setAutoCommit(false);
      ps = conn
        .prepareStatement("update GlobalArrayIndex set GlobalListArrayIndex=? where GlobalKeyword=?");
      ps.setBytes(1, listarrayindex);
      ps.setString(2, keyword);
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
   * 根据keyword删除GlobalArrayIndex中的一条记录
   * @param keyword 关键字
   * @throws Exception
   */
  public static void deleteGlobalArrayIndex(String keyword) throws Exception {
    Connection conn = null;
    PreparedStatement ps = null;
    try {
      conn = IEXZMFDB.getConnection();
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
   * 根据keyword查找GlobalArrayIndex表中包含关键字的文件名/索引
   * @param keyword 关键字
   * @return 包含关键字的文件名/索引
   * @throws SQLException
   */
  public static byte[] FindGlobalListArrayIndex(String keyword) throws SQLException{
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    byte  temp[] = null;
    try {
      con = IEXZMFDB.getConnection();
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
   * 在GlobalArrayList中插入一条记录
   * 由于索引（唯一标识）自动递增，在SQL语句中不需添加索引
   * @param templist 包含关键字的文件名/索引
   * @throws Exception
   */
  public static void InsertGlobalArrayList(byte[] templist) throws Exception {
    Connection conn = null;
    PreparedStatement ps = null;

    try {
      conn = IEXZMFDB.getConnection();
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
   * 根据包含关键字的文件名/索引查找索引（唯一标识）
   * @param templist 包含关键字的文件名/索引
   * @return 索引（唯一标识）
   * @throws Exception
   */
  public static int FindGlobalArrayList(byte[] templist) throws Exception {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    int  GlobalCounter = -1;
    try {
      conn = IEXZMFDB.getConnection();
      ps = conn
        .prepareStatement("SELECT GlobalCounter FROM GlobalArrayList WHERE GlobalTempList=?");
      ps.setBytes(1, templist);
      rs = ps.executeQuery();
      while(rs.next())
        GlobalCounter = rs.getInt("GlobalCounter");//取得最新插入的记录
    } catch (Exception es) {
      conn.rollback();
      throw es;
    } finally {
      ps.close();
      conn.close();
    }
    return GlobalCounter;
  }

  /**
   * 根据索引（唯一标识）删除一条记录
   * @param GlobalCounter 索引（唯一标识）
   * @throws Exception
   */
  public static void deleteGlobalArrayList(int GlobalCounter) throws Exception {
    Connection conn = null;
    PreparedStatement ps = null;
    try {
      conn = IEXZMFDB.getConnection();
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
   * 根据索引（唯一标识）查找包含关键字的文件名/索引
   * @param GlobalCounter 索引（唯一标识）
   * @return 包含关键字的文件名/索引
   * @throws SQLException
   */
  public static byte[] findGlobalListArrayList(int GlobalCounter) throws SQLException{
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    byte  temp[] = null;
    try {
      con = IEXZMFDB.getConnection();
      ps = con.prepareStatement("SELECT GlobalTempList FROM GlobalArrayList WHERE GlobalCounter=?");
      ps.setInt(1, GlobalCounter);
      rs = ps.executeQuery();
      rs.next();
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
   * 清空GlobalArrayIndex表的数据
   * @throws Exception
   */
  public static void TruncateGlobalArrayIndex() throws Exception {
    Connection conn = null;
    PreparedStatement ps = null;

    try {
      conn = IEXZMFDB.getConnection();
      ps = conn
        .prepareStatement("truncate  table globalarrayindex");
      ps.executeUpdate();
    } catch (Exception es) {
      throw es;
    } finally {
      ps.close();
      conn.close();
    }
  }

  /**
   * 清空GlobalArrayList表的数据
   * @throws Exception
   */
  public static void TruncateGlobalArrayList() throws Exception {
    Connection conn = null;
    PreparedStatement ps = null;

    try {
      conn = IEXZMFDB.getConnection();
      ps = conn
        .prepareStatement("truncate  table GlobalArrayList");
      ps.executeUpdate();
    } catch (Exception es) {
      throw es;
    } finally {
      ps.close();
      conn.close();
    }
  }
}
