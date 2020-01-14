package org.ruiyun.crypto;

import org.ruiyun.DB.IEXZMFDB;

import java.sql.Connection;

/**
 * 测试IEXZMF的数据库是否连接成功
 * @author ltc
 * @className TestIEXZMFDB
 * @since 2020/1/14 18:57
 */
public class TestIEXZMFDB {
  public static void main(String[] args) throws Exception {
    Connection conn = IEXZMFDB.getConnection();
    if(conn!=null)
      System.out.println("IEXZMF数据库连接成功");
    else
      System.out.println("IEXZMF数据库连接失败");
    }
  }
