package org.ruiyun.crypto;

import org.crypto.sse.IEX2Lev;
import org.crypto.sse.Printer;
import org.crypto.sse.TextExtractPar;
import org.crypto.sse.TextProc;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ltc
 * @className TestAddIEX2lev
 * @since 2020/1/11 16:12
 */
public class TestAddIEX2lev {

  public static void main(String[] args) throws Exception {

    BufferedReader keyRead = new BufferedReader(new InputStreamReader(System.in));

    System.out.println("Enter your password :");

    String pass = keyRead.readLine();//输入密码

    List<byte[]> listSK = IEX2Lev.keyGen(128, pass, "salt/saltSetM", 100000);//根据输入值生成密钥

    long startTime = System.nanoTime();

    BufferedWriter writer = new BufferedWriter(new FileWriter("logs.txt", true));

    System.out.println("Enter the relative path name of the folder that contains the files to make searchable: ");

    String pathName = keyRead.readLine();//输入文件路径

    // Creation of different files based on selectivity
    // Selectivity was computed in an inclusive way. All files that include
    // x(i+1) include necessarily xi
    // This is used for benchmarking and can be taken out of the code

    ArrayList<File> listOfFile = new ArrayList<File>();
    TextProc.listf(pathName, listOfFile);//文件读取

    TextProc.TextProc(false, pathName);//TextProc初始化时TextExtractPar初始化，lp1(关键词：文件名)，lp2(文件名：关键词)

    int bigBlock = 2;
    int smallBlock = 1;//2lev的分包处理参数

    long startTime2 = System.nanoTime();

    IEX2Lev.setup(listSK, TextExtractPar.lp1, TextExtractPar.lp2, bigBlock, smallBlock, 0);//IEX初始化

    long endTime2 = System.nanoTime();
    long totalTime2 = endTime2 - startTime2;

    // Writing logs

    Printer.debugln("\n*****************************************************************");
    Printer.debugln("\n\t\tSTATS");
    Printer.debugln("\n*****************************************************************");

    // Printer.debugln("\nNumber of keywords
    // "+TextExtractPar.totalNumberKeywords);
    Printer.debugln("\nNumber of (w, id) pairs " + TextExtractPar.lp2.size());
    writer.write("\n Number of (w, id) pairs " + TextExtractPar.lp2.size());

    Printer.debugln("\nTotal number of stored (w, Id) including in local MM : " + IEX2Lev.numberPairs);
    writer.write("\n Total number of stored (w, Id) including in local MM : " + IEX2Lev.numberPairs);

    //	Printer.debugln("\nTime elapsed per (w, Id) in ns: " + totalTime2 / IEX2Lev.numberPairs);
    //	writer.write("\n Time elapsed per (w, Id) in ns: " + totalTime2 / IEX2Lev.numberPairs);

    Printer.debugln("\nTotal Time elapsed for the entire construction in seconds: " + totalTime2 / 1000000000);
    writer.write("\n Total Time elapsed for the entire construction in seconds: " + totalTime2 / 1000000000);

//	  Printer.debugln("\nRelative Time elapsed per (w, Id) in ns: " + totalTime2 / TextExtractPar.lp1.size());
//		writer.write("\n Relative Time elapsed per (w, Id) in ns: " + totalTime2 / TextExtractPar.lp1.size());

    // The two commented commands are used to compute the size of the
    // encrypted Local multi-maps and global multi-maps

    // Printer.debugln("\nSize of the Structure LMM: "+
    // SizeOf.humanReadable(SizeOf.deepSizeOf(disj.getLocalMultiMap())));
    // Printer.debugln("\nSize of the Structure MMg: "+
    // SizeOf.humanReadable(SizeOf.deepSizeOf(disj.getGlobalMM())));
    writer.close();
    System.out.println("IEX2lev添加完成");
  }
}
