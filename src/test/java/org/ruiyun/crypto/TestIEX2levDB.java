package org.ruiyun.crypto;

import org.ruiyun.DB.IEX2levDB;

import java.sql.Connection;

/**
 * 测试IEX2lev数据库是否连接成功
 * @author ltc
 * @className Test
 * @since 2020/1/13 15:38
 */
public class TestIEX2levDB {
  public static void main(String[] args) throws Exception {
    Connection conn = IEX2levDB.getConnection();
    if(conn!=null)
      System.out.println("IEX2lev数据库连接成功");
    else
      System.out.println("IEX2lev数据库连接失败");
  }
}
