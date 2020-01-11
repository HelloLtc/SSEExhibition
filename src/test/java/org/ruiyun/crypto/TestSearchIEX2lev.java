package org.ruiyun.crypto;


import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.crypto.sse.*;
import org.ruiyun.Dao.IEX2levDao.LocalArray;


/**多关键字查询
 * 一次输入的多个关键字之间是或查询
 * 每次输入的多个关键字之间是且查询
 * @author ltc
 * @className TestIEX2lev
 * @since 2020/1/10 14:42
 */
public class TestSearchIEX2lev {
  public static void main(String[] args) throws Exception {

    BufferedReader keyRead = new BufferedReader(new InputStreamReader(System.in));

    System.out.println("Enter your password :");

    String pass = keyRead.readLine();//输入密码

    List<byte[]> listSK = IEX2Lev.keyGen(256, pass, "salt/saltSetM", 100000);//根据输入值生成密钥

		while (true) {
    System.out.println("How many disjunctions? ");
    int numDisjunctions = Integer.parseInt(keyRead.readLine());//输入或查询次数

    // Storing the CNF form
    String[][] bool = new String[numDisjunctions][];
    for (int i = 0; i < numDisjunctions; i++) {
      System.out.println("Enter the keywords of the disjunctions ");
      bool[i] = keyRead.readLine().split(" ");//输入或查询的关键词组
    }

    test("log-1.txt", "Test", 1, listSK, bool);
  }

}

  public static void test(String output, String word, int numberIterations, List<byte[]> listSK,
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

      // Generation of stream file to measure size of the token

      long startTime3 = System.nanoTime();

      // Printer.debugln(searchBol);

      Set<String> tmpBol = IEX2Lev.query(IEX2Lev.token(listSK, searchBol), null);//token是根据密钥和第一个输入关键词形成查询密文

      // Printer.debugln(tmpBol);

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

          //	Set<String> result = new HashSet<String>(RR2Lev.query(tokenTMP.get(0).getTokenMMGlobal(),
          //			disj.getGlobalMM().getDictionary(), disj.getGlobalMM().getArray(),false));//result没有用到

          if (!(tmpBol.size() == 0)) {//如果第一次输入的关键词没有结果，直接返回
            //	List<Integer> temp = new ArrayList<Integer>(
            //			disj.getDictionaryForMM().get(new String(tokenTMP.get(0).getTokenDIC())));//temp获得tokenTMP第一个关键字的字典编号
            List<byte[]> newtemp = LocalArray.FindLocalListWord(new String(tokenTMP.get(0).getTokenDIC()));//temp获得tokenTMP第一个关键字的字典
            String keyword = new String(tokenTMP.get(0).getTokenDIC());
            if ((newtemp!= null)) {
              //int pos = temp.get(0);//根据temp的值，确定字典内容位置

              for (int j = 0; j < tokenTMP.get(0).getTokenMMLocal().size(); j++) {

                Set<String> temporary = new HashSet<String>();
                List<String> tempoList = RR2Lev.LocalQuery(keyword,tokenTMP.get(0).getTokenMMLocal().get(j),
                  null,
                  null,false);//根据tokenTMP首个关键字形成的token串和首个关键字的字典进行查找文档

                if (!(tempoList == null)) {
                  temporary = new HashSet<String>(
                    RR2Lev.LocalQuery(keyword,tokenTMP.get(0).getTokenMMLocal().get(j),
                      null,
                      null,false));
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
      long endTime3 = System.nanoTime();
      long totalTime3 = endTime3 - startTime3;

      if (totalTime3 < minimum) {

        minimum = totalTime3;

      }

      if (totalTime3 > maximum) {

        maximum = totalTime3;

      }

      average = average + totalTime3;

    }

    BufferedWriter writer2 = new BufferedWriter(new FileWriter(output, true));
    writer2.write("\n Word " + word + " minimum " + minimum);
    writer2.write("\n Word " + word + " maximum " + maximum);
    writer2.write("\n Word " + word + " average " + average / numberIterations + "\n\n");
    writer2.close();

  }
}
