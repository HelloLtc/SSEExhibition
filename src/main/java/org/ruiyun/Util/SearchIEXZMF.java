package org.ruiyun.Util;

import org.crypto.sse.*;
import org.ruiyun.Dao.IEXZMFDao.BloomFilterID;
import org.ruiyun.Dao.IEXZMFDao.BloomFilterMap;
import org.ruiyun.Dao.IEXZMFDao.BloomFilterStart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ltc
 * @className SearchIEXZMF
 * @since 2020/1/14 19:23
 */
public class SearchIEXZMF {
  private static final int falsePosRate = 25;
  private static final int maxLengthOfMask = 20;
  public static List<String> Search(String items) throws Exception {
    System.out.println("Enter your password :");
    String pass = "123";
    List<byte[]> listSK = IEXZMF.keyGen(128, pass, "salt/saltInvIX", 100);
/*
    System.out.println("Enter the relative path name of the folder that contains the files to make searchable");

    String pathName = "D:\\IdeaProjects\\1\\SSEExhibition\\testfile";
    TextProc.TextProc(false, pathName);
    int bigBlock = 2;
    int smallBlock = 1;
    int dataSize = 0;
    // Construction by Cash et al NDSS 2014
    for (String keyword : TextExtractPar.lp1.keySet()) {
      if (dataSize < TextExtractPar.lp1.get(keyword).size()) {
        dataSize = TextExtractPar.lp1.get(keyword).size();
      }

    }
    IEXZMF.constructEMMParGMM(listSK.get(1), TextExtractPar.lp1, bigBlock, smallBlock, dataSize);//对文件进行2lev分块

    IEXZMF.constructMatryoshkaPar(new ArrayList(TextExtractPar.lp1.keySet()), listSK.get(0), listSK.get(1),
      maxLengthOfMask, falsePosRate);//IEXZMF初始化
    System.out.println("IEXZMF添加完成");
*/
    System.out.println("How many disjunctions? ");
    int numDisjunctions = Integer.parseInt("1");
    // Storing the CNF form
    String[][] bool = new String[numDisjunctions][];
    for (int i = 0; i < numDisjunctions; i++) {
      System.out.println("Enter the keywords of the " + i + "th disjunctions ");
      bool[i] = items.split(" ");
    }
    List<String> zmfresult =  test("logZMF.txt", "Test", 1, listSK, bool);
    return zmfresult;
  }

  public static List<String> test(String output, String word, int numberIterations, List<byte[]> listSK,
    String[][] bool) throws Exception {
    // Generate the IEX token
    List<String> searchBol = new ArrayList<String>();
    for (int i = 0; i < bool[0].length; i++) {
      searchBol.add(bool[0][i]);
    }

    for (int g = 0; g < numberIterations; g++) {

      long startTime3 = System.nanoTime();

      List<Token> tokenBol = IEXZMF.token(listSK, searchBol, falsePosRate, maxLengthOfMask);//生成第一个字符的token
      List<String> tmpBol = IEXZMF.query(tokenBol, TSet.bucketSize, falsePosRate);//查询第一个token的值

      for (int i = 1; i < bool.length; i++) {
        List<String> tmpBol2 = new ArrayList<String>();

        for (int k = 0; k < bool[0].length; k++) {
          List<String> searchTMP = new ArrayList<String>();
          List<String> tmpList = new ArrayList<String>();
          searchTMP.add(bool[0][k]);
          for (int r = 0; r < bool[i].length; r++) {
            searchTMP.add(bool[i][r]);
          }
          List<Token> tokenTMP = IEXZMF.token(listSK, searchTMP, falsePosRate, maxLengthOfMask);
          Map<String, boolean[]> listOfbloomFilter = new HashMap<String, boolean[]>();
          String tempstartstring = BloomFilterStart.FindBloomFilterStart(new String(tokenTMP.get(0).getTokenSI1()));
          String[] tempstartlist = null;
          if(tempstartstring!=null)
            tempstartlist  = tempstartstring.split(",");
          if (!(tempstartlist == null)) {
            for (int j = 0; j < tempstartlist.length; j++) {
              int bFID = Integer.parseInt(tempstartlist[j]);
             listOfbloomFilter.put(BloomFilterID.FindBloomFilterID(bFID), CryptoPrimitives.StringToBoolean(BloomFilterMap.FindBloomFilterMap(bFID)));
            }
          }
          Map<Integer, boolean[]> tempBF = new HashMap<Integer, boolean[]>();
          for (int v = 0; v < tokenTMP.get(0).getTokenSM().size(); v++) {
            tempBF.put(v,
              ZMF.testSMV22(listOfbloomFilter, tokenTMP.get(0).getTokenSM().get(v), falsePosRate));
          }
          if (!(tempstartlist == null)) {
            for (int j = 0; j < tempstartlist.length; j++) {
              boolean flag = true;
              int counter = 0;
              while (flag) {
                if (tempBF.get(counter)[j] == true) {
                  flag = false;
                } else if (counter == tokenTMP.get(0).getTokenSM().size() - 1) {
                  break;
                }
                counter++;
              }
              if (flag == false) {
                tmpList.add(BloomFilterID.FindBloomFilterID(Integer.parseInt(tempstartlist[j])));
              }
            }
          }
          tmpBol2.addAll(tmpList);//求并集
        }
        tmpBol.retainAll(tmpBol2);//求交集
      }
      System.out.println("Result " + tmpBol);
      return tmpBol;
    }
    return null;
  }
}
