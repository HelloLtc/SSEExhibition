package org.ruiyun.crypto;

import org.crypto.sse.IEXZMF;
import org.crypto.sse.Printer;
import org.crypto.sse.TextExtractPar;
import org.crypto.sse.TextProc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ltc
 * @className TestAddIEXZMF
 * @since 2020/1/11 16:14
 */
public class TestAddIEXZMF {

  private static final int falsePosRate = 25;
  private static final int maxLengthOfMask = 20;

  public static void main(String[] args) throws Exception {

  BufferedReader keyRead = new BufferedReader(new InputStreamReader(System.in));

  System.out.println("Enter your password :");

  String pass = keyRead.readLine();

  List<byte[]> listSK = IEXZMF.keyGen(128, pass, "salt/saltInvIX", 100);

  System.out.println("Enter the relative path name of the folder that contains the files to make searchable");

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
    System.out.println("IEXZMF添加完成");
  }
}
