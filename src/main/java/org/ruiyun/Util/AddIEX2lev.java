package org.ruiyun.Util;

import org.crypto.sse.IEX2Lev;
import org.crypto.sse.Printer;
import org.crypto.sse.TextExtractPar;
import org.crypto.sse.TextProc;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AddIEX2lev {

  public static void Add(List<byte[]> listSK,String pathName) throws Exception {
    ArrayList<File> listOfFile = new ArrayList<File>();

    TextProc.listf(pathName, listOfFile);//文件读取
    TextProc.TextProc(false, pathName);//TextProc初始化时TextExtractPar初始化，lp1(关键词：文件名)，lp2(文件名：关键词)

    int bigBlock = 2;
    int smallBlock = 1;//2lev的分包处理参数

    IEX2Lev.setup(listSK, TextExtractPar.lp1, TextExtractPar.lp2, bigBlock, smallBlock, 0);//IEX初始化

    System.out.println("IEX2lev添加完成");
  }

}
