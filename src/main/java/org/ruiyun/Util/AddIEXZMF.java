package org.ruiyun.Util;

import org.crypto.sse.IEXZMF;
import org.crypto.sse.Printer;
import org.crypto.sse.TextExtractPar;
import org.crypto.sse.TextProc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class AddIEXZMF {
  private static final int falsePosRate = 25;
  private static final int maxLengthOfMask = 20;


  public static void Add(List<byte[]> listSK ,String pathName) throws Exception {
    TextProc.TextProc(false, pathName);

    int bigBlock = 2;
    int smallBlock = 1;
    int dataSize = 0;
    for (String keyword : TextExtractPar.lp1.keySet()) {
      if (dataSize < TextExtractPar.lp1.get(keyword).size()) {
        dataSize = TextExtractPar.lp1.get(keyword).size();
      }
    }
    IEXZMF.constructEMMParGMM(listSK.get(1), TextExtractPar.lp1, bigBlock, smallBlock, dataSize);//对文件进行2lev分块

    IEXZMF.constructMatryoshkaPar(new ArrayList(TextExtractPar.lp1.keySet()), listSK.get(0), listSK.get(1),
      maxLengthOfMask, falsePosRate);//IEXZMF初始化

    System.out.println("IEXZMF添加完成");
  }
}
