package org.ruiyun.crypto;
import org.ruiyun.Dao.IEX2levDao.GlobalArray;
import org.ruiyun.Dao.IEX2levDao.LocalArray;
import org.ruiyun.Dao.IEXZMFDao.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
/**
 * @author ltc
 * @className TestTruncateDB
 * @since 2020/1/10 11:23
 */
public class TestTruncateDB {
  public static void main(String[] args) throws Exception {
    BufferedReader keyRead = new BufferedReader(new InputStreamReader(System.in));

    System.out.println("Enter the name of the DB that you want to truncate<IEX2Lev/IEXZMF>:");

    String DBName = keyRead.readLine();

    while(!DBName.equalsIgnoreCase("IEX2Lev")&&!DBName.equalsIgnoreCase("IEXZMF")){
      System.out.println("Error input!");
      System.out.println("----------------------------------------------");
      System.out.println("Would you like to add some new file ? <IEX2Lev/IEXZMF>");
      DBName = keyRead.readLine();
    }
    if(DBName.equalsIgnoreCase("IEX2Lev")){
      GlobalArray.TruncateGlobalArrayIndex();
      GlobalArray.TruncateGlobalArrayList();
      LocalArray.TruncateLocalArrayIndex();
      LocalArray.TruncateLocalArrayList();
      System.out.println("IEX2Lev数据删除成功");
    }else if(DBName.equalsIgnoreCase("IEXZMF")){
      BloomFilterID.TruncateBloomFilterID();;
      BloomFilterMap.TruncateBloomFilterMap();
      BloomFilterStart.TruncateBloomFilterStart();
      BloomGlobalArray.TruncateGlobalArrayIndex();
      BloomGlobalArray.TruncateGlobalArrayList();
      ListOfBloomFilter.TruncateListOfBloomFilter();
      System.out.println("IEXZMF数据删除成功");
    }
  }
}
