package org.ruiyun.Dao.IEX2levDao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.ruiyun.Util.IEX2levDB;
/**对数据库IEX2lev中LocalArrayIndex表和LocalArrayList表进行增删改查的功能，
 * LocalArrayIndex表存储（关键字一，关键字二，包含关键字一，二的文件名/指向LocalArrayList的索引）
 * LocalArrayList表存储（索 引，包含关键字的文件名/指向LocalArrayList的索引）
 * @author ltc
 * @className LocalArray
 * @since 2020/1/10 10:23
 */
public class LocalArray {

  /**
   * 在LocalArrayIndex表中插入一条数据
   * @param keyword 关键字一
   * @param word 关键字二
   * @param listarrayindex 包含关键字一二的文件名/索引
   * @throws Exception
   */
  public static void InsertLocalArrayIndex(String keyword,String word,byte[] listarrayindex) throws Exception {
    Connection conn = null;
    PreparedStatement ps = null;
    try {
      conn = IEX2levDB.getConnection();
      conn.setAutoCommit(false);
      ps = conn
        .prepareStatement("INSERT INTO LocalArrayIndex (LocalKeyword,LocalWord,LocalListArrayIndex) values(?,?,?)");
      ps.setString(1, keyword);
      ps.setString(2, word);
      ps.setBytes(3, listarrayindex);
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
   * 根据keyword和word删除LocalArrayIndex表中的一条记录
   * @param keyword 关键字一
   * @param word 关键字二
   * @throws Exception
   */
  public static void deleteLocalArrayIndex(String keyword,String word) throws Exception {
    Connection conn = null;
    PreparedStatement ps = null;
    try {
      conn = IEX2levDB.getConnection();
      ps = conn
        .prepareStatement("DELETE FROM LocalArrayIndex WHERE LocalKeyword = ? and Localword = ?");
      ps.setString(1, keyword);
      ps.setString(2, word);
      ps.executeUpdate();
    } catch (Exception es) {
      throw es;
    } finally {
      ps.close();
      conn.close();
    }
  }

  /**
   * 根据keyword和word查找包含keyword和word的文件名/索引
   * @param keyword 关键字一
   * @param word 关键字二
   * @return 包含keyword和word的文件名/索引
   * @throws SQLException
   */
  public static byte[] FindLocalListArrayIndex(String keyword,String word) throws SQLException{
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    byte  temp[] = null;
    try {
      con = IEX2levDB.getConnection();
      ps = con.prepareStatement("SELECT LocalListArrayIndex FROM LocalArrayIndex WHERE LocalKeyword=? and Localword = ? ");
      ps.setString(1, keyword);
      ps.setString(2, word);
      rs = ps.executeQuery();
      if(!rs.next())
        return null;
      temp = rs.getBytes("LocalListArrayIndex");
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
   * 查找包含关键字keyword的文件名/索引
   * @param keyword 关键字
   * @return 包含关键字keyword的文件名/索引
   * @throws SQLException
   */
  public static List<byte[]> FindLocalListWord(String keyword) throws SQLException{
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    List<byte[]>  temp = new ArrayList<byte[]>();
    try {
      con = IEX2levDB.getConnection();
      ps = con.prepareStatement("SELECT LocalListArrayIndex FROM LocalArrayIndex WHERE LocalKeyword=? ");
      ps.setString(1, keyword);
      rs = ps.executeQuery();
      if(!rs.next())
        return null;
      while(rs.next()){
        byte[] t = rs.getBytes("LocalListArrayIndex");
        temp.add(t);	}
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
   * 在LocalArrayList表中插入一条记录
   * （由于LocalCounter在表中是自动递增，所以在SQL语句中不用添加）
   * @param templist 包含关键字一，二的文件名/索引
   * @throws Exception
   */
  public static void InsertLocalArrayList(byte[] templist) throws Exception {
    Connection conn = null;
    PreparedStatement ps = null;

    try {
      conn = IEX2levDB.getConnection();
      conn.setAutoCommit(false);
      ps = conn
        .prepareStatement("INSERT INTO LocalArrayList (LocalTempList) values(?)");
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
   * 查找LocalCounter的主键LocalCounter
   * @param LocalTempList 包含关键字的文件名/索引
   * @return 主键LocalCounter
   * @throws SQLException
   */
  public static int findLocalListArrayTempList(byte[] LocalTempList) throws SQLException{
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    int  temp = -1;
    try {
      con = IEX2levDB.getConnection();
      ps = con.prepareStatement("SELECT LocalCounter FROM LocalArrayList WHERE LocalTempList=?");
      ps.setBytes(1, LocalTempList);
      rs = ps.executeQuery();
      rs.next();
      temp = rs.getInt("LocalCounter");
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
   * 根据主键LocalCounter删除LocalArrayList的一条记录
   * @param LocalCounter 主键LocalCounter
   * @throws Exception
   */
  public static void deleteLocalArrayList(int LocalCounter) throws Exception {
    Connection conn = null;
    PreparedStatement ps = null;
    try {
      conn = IEX2levDB.getConnection();
      ps = conn
        .prepareStatement("DELETE FROM LocalArrayList WHERE LocalCounter = ?");
      ps.setInt(1, LocalCounter);
      ps.executeUpdate();
    } catch (Exception es) {
      throw es;
    } finally {
      ps.close();
      conn.close();
    }
  }

  /**
   * 根据主键LocalCounter查找包含关键字的文件名/索引的LocalTempList
   * @param LocalCounter 主键LocalCounter
   * @return 包含关键字的文件名/索引的LocalTempList
   * @throws SQLException
   */
  public static byte[] findLocalListArrayList(int LocalCounter) throws SQLException{
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    byte  temp[] = null;
    try {
      con = IEX2levDB.getConnection();
      ps = con.prepareStatement("SELECT LocalTempList FROM LocalArrayList WHERE LocalCounter=?");
      ps.setInt(1, LocalCounter);
      rs = ps.executeQuery();
      rs.next();
      if(!rs.next())
        return null;
      temp = rs.getBytes("LocalTempList");
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
   * 清空LocalArrayIndex表的信息
   * @throws Exception
   */
  public static void TruncateLocalArrayIndex() throws Exception {
    Connection conn = null;
    PreparedStatement ps = null;

    try {
      conn = IEX2levDB.getConnection();
      ps = conn
        .prepareStatement("truncate  table localarrayindex ");
      ps.executeUpdate();
    } catch (Exception es) {
      throw es;
    } finally {
      ps.close();
      conn.close();
    }
  }

  /**
   * 清空LocalArrayList表的信息
   * @throws Exception
   */
  public static void TruncateLocalArrayList() throws Exception {
    Connection conn = null;
    PreparedStatement ps = null;

    try {
      conn = IEX2levDB.getConnection();
      ps = conn
        .prepareStatement("truncate  table localarraylist");
      ps.executeUpdate();
    } catch (Exception es) {
      throw es;
    } finally {
      ps.close();
      conn.close();
    }
  }
}

