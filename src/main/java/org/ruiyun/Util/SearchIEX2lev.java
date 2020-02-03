package org.ruiyun.Util;


import org.crypto.sse.*;
import org.ruiyun.Dao.IEX2levDao.LocalArray;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**多关键字查询
 * 一次输入的多个关键字之间是或查询
 * 每次输入的多个关键字之间是且查询
 * @author ltc
 * @className TestIEX2lev
 * @since 2020/1/10 14:42
 */
public class SearchIEX2lev {
  public static Set<String> Search(String s,List<byte[]> listSK) throws Exception {
    System.out.println("Enter your password :");
   System.out.println("How many disjunctions? ");
    int numDisjunctions = Integer.parseInt("1");//输入或查询次数

    // Storing the CNF form
    String[][] bool = new String[numDisjunctions][];
    for (int i = 0; i < numDisjunctions; i++) {
      System.out.println("Enter the keywords of the disjunctions ");
      bool[i] = s.split(" ");//输入或查询的关键词组
    }
    Set<String> ss = test("log-1.txt", "Test", 1, listSK, bool);
    return  ss;
}

  public static Set<String> test(String output, String word, int numberIterations, List<byte[]> listSK,
                          String[][] bool) throws Exception {

    long minimum = 1000000000;
    long maximum = 0;
    long average = 0;

    // Generate the IEX token
    List<String> searchBol = new ArrayList<String>();
    for (int i = 0; i < bool[0].length; i++) {
      searchBol.add(bool[0][i]);//第一个输入的关键词组
    }

    for (int g = 0; g < numberIterations; g++) {
      Set<String> tmpBol = IEX2Lev.query(IEX2Lev.token(listSK, searchBol), null);//token是根据密钥和第一个输入关键词形成查询密文
      for (int i = 1; i < bool.length; i++) {
        Set<String> finalResult = new HashSet<String>();
        for (int k = 0; k < bool[0].length; k++) {
          List<String> searchTMP = new ArrayList<String>();
          searchTMP.add(bool[0][k]);
          for (int r = 0; r < bool[i].length; r++) {
            searchTMP.add(bool[i][r]);
          }
          //searchTMP包含每次查询中的一个关键词
          List<TokenDIS> tokenTMP = IEX2Lev.token(listSK, searchTMP);
          if (!(tmpBol.size() == 0)) {//如果第一次输入的关键词没有结果，直接返回
            List<byte[]> newtemp = LocalArray.FindLocalListWord(new String(tokenTMP.get(0).getTokenDIC()));//temp获得tokenTMP第一个关键字的字典
            String keyword = new String(tokenTMP.get(0).getTokenDIC());
            if ((newtemp!= null)) {
              for (int j = 0; j < tokenTMP.get(0).getTokenMMLocal().size(); j++) {
                Set<String> temporary = new HashSet<String>();
                List<String> tempoList = RR2Lev.LocalQuery(keyword,tokenTMP.get(0).getTokenMMLocal().get(j),
                  false);//根据tokenTMP首个关键字形成的token串和首个关键字的字典进行查找文档
                if (!(tempoList == null)) {
                  temporary = new HashSet<String>(
                    RR2Lev.LocalQuery(keyword,tokenTMP.get(0).getTokenMMLocal().get(j),
                      false));
                }
                finalResult.addAll(temporary);//求并集
                if (tmpBol.isEmpty()) {
                  break;
                }
              }
            }
          }
        }
        tmpBol.retainAll(finalResult);//求交集
      }
      System.out.println("Final result " + tmpBol);
      return tmpBol;
    }
  return null;
  }
}
