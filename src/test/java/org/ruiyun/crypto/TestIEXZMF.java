package org.ruiyun.crypto;
//这个是多关键字查找，请先看TestLocalZMF的单关键字查询
import com.google.common.collect.Multimap;
import org.crypto.sse.*;
import org.ruiyun.Dao.IEXZMFDao.BloomFilterID;
import org.ruiyun.Dao.IEXZMFDao.BloomFilterMap;
import org.ruiyun.Dao.IEXZMFDao.BloomFilterStart;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * @author ltc
 * @className TestIEXZMF
 * @since 2020/1/11 14:11
 */
public class TestIEXZMF{
  private static final int falsePosRate = 25;
  private static final int maxLengthOfMask = 20;

  public static void main(String[] args) throws Exception {

    Printer.addPrinter(new Printer(Printer.LEVEL.EXTRA));

    BufferedReader keyRead = new BufferedReader(new InputStreamReader(System.in));

    Printer.normalln("Enter your password :");

    String pass = keyRead.readLine();
    //String pass = "123";

    List<byte[]> listSK = IEXZMF.keyGen(128, pass, "salt/saltInvIX", 100);

    long startTime = System.nanoTime();
    System.out.println("Would you like to add some new file ? <Yes/No>");

    String addfile = keyRead.readLine();
    //String addfile = "yes";
    while(!addfile.equalsIgnoreCase("Yes")&&!addfile.equalsIgnoreCase("No")){
      System.out.println("Error input!");
      System.out.println("----------------------------------------------");
      System.out.println("Would you like to add some new file ? <Yes/No>");
      addfile = keyRead.readLine();
    }
    if(addfile.equalsIgnoreCase("Yes")){
      Printer.normalln("Enter the relative path name of the folder that contains the files to make searchable");

      String pathName = keyRead.readLine();

      TextProc.TextProc(false, pathName);

      long startTime2 = System.nanoTime();
      Printer.debugln("Number of keywords pairs (w. id): " + TextExtractPar.lp1.size());
      Printer.debugln("Number of keywords " + TextExtractPar.lp1.keySet().size());

      Printer.debugln("\n Beginning of global encrypted multi-map construction \n");

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

      Printer.debugln("\n Beginning of local encrypted multi-map construction \n");

      IEXZMF.constructMatryoshkaPar(new ArrayList(TextExtractPar.lp1.keySet()), listSK.get(0), listSK.get(1),
        maxLengthOfMask, falsePosRate);//IEXZMF初始化

      long endTime2 = System.nanoTime();

      long totalTime2 = endTime2 - startTime2;
      Printer.statsln("\n*****************************************************************");
      Printer.statsln("\n\t\tSTATS");
      Printer.statsln("\n*****************************************************************");

      Printer.statsln(
        "\nTotal Time elapsed for the local multi-map construction in seconds: " + totalTime2 / 1000000);
    }
    // Beginning of search phase

    while (true) {

      Printer.normalln("How many disjunctions? ");
      int numDisjunctions = Integer.parseInt(keyRead.readLine());

      // Storing the CNF form
      String[][] bool = new String[numDisjunctions][];
      for (int i = 0; i < numDisjunctions; i++) {
        Printer.normalln("Enter the keywords of the " + i + "th disjunctions ");
        bool[i] = keyRead.readLine().split(" ");
      }

      test("logZMF.txt", "Test", 1, listSK, bool);
    }
  }

  public static void test(String output, String word, int numberIterations, List<byte[]> listSK,
                          String[][] bool) throws Exception {

    long minimum = (long) Math.pow(10, 10);
    long maximum = 0;
    long average = 0;

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

          // Here we perform an intersection (contrary to its
          // argument)

          //List<String> resultTMP = RR2Lev.query(tokenTMP.get(0).getTokenMMGlobal(),false);

          Map<String, boolean[]> listOfbloomFilter = new HashMap<String, boolean[]>();

          //List<Integer> bFIDPaddeds = IEXZMF.bloomFilterStart.get(new String(tokenTMP.get(0).getTokenSI1()));
          String tempstartstring = BloomFilterStart.FindBloomFilterStart(new String(tokenTMP.get(0).getTokenSI1()));
          String[] tempstartlist = tempstartstring.split(",");
          if (!(tempstartlist == null)) {
            for (int j = 0; j < tempstartlist.length; j++) {
              int bFID = Integer.parseInt(tempstartlist[j]);
              //listOfbloomFilter.put(IEXZMF.bloomFilterID.get(bFID),
              //		IEXZMF.bloomFilterMap.get(Integer.toString(bFID)).getSecureSetM());
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

      Printer.normalln("Result " + tmpBol);

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


