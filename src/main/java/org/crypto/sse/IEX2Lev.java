/** * Copyright (C) 2016 Tarik Moataz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

//***********************************************************************************************//

// This file contains IEX-2Lev implementation. KeyGen, Setup, Token and Query algorithms.
// We also propose an implementation of a possible filtering mechanism that reduces the storage overhead.

//***********************************************************************************************//

package org.crypto.sse;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class IEX2Lev implements Serializable {

  // Parameter of Disjunctive search
  public static int maxDocumentIDs = 0;

  // Change it based on data distribution and storage restrictions
  static double filterParameter = 0;

  public static long numberPairs = 0;
  private static boolean flag = true;
  RR2Lev globalMM = null;
  RR2Lev[] localMultiMap = null;
  Multimap<String, Integer> dictionaryForMM = null;

  public IEX2Lev(RR2Lev globalMM, RR2Lev[] localMultiMap, Multimap<String, Integer> dictionaryForMM) {
    this.globalMM = globalMM;
    this.localMultiMap = localMultiMap;
    this.dictionaryForMM = dictionaryForMM;
  }



  public RR2Lev getGlobalMM() {
    return globalMM;
  }

  public void setGlobalMM(RR2Lev globalMM) {
    this.globalMM = globalMM;
  }

  public RR2Lev[] getLocalMultiMap() {
    return localMultiMap;
  }

  public void setLocalMultiMap(RR2Lev[] localMultiMap) {
    this.localMultiMap = localMultiMap;
  }

  public Multimap<String, Integer> getDictionaryForMM() {
    return dictionaryForMM;
  }

  public void setDictionaryForMM(Multimap<String, Integer> dictionaryForMM) {
    this.dictionaryForMM = dictionaryForMM;
  }

  // ***********************************************************************************************//

  ///////////////////// Key Generation /////////////////////////////

  // ***********************************************************************************************//

  public static List<byte[]> keyGen(int keySize, String password, String filePathString, int icount)
    throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException {
    System.out.println("password:"+password);
    List<byte[]> listOfkeys = new ArrayList<byte[]>();

    // Generation of two keys for Secure inverted index
    listOfkeys.add(TSet.keyGen(keySize, password + "secureIndex", filePathString, icount));
    listOfkeys.add(TSet.keyGen(keySize, password + "dictionary", filePathString, icount));

    // Generation of one key for encryption
    listOfkeys.add(ZMF.keyGenSM(keySize, password + "encryption", filePathString, icount));

    return listOfkeys;

  }

  public static ArrayList<String> keyGenList(int keySize, String password, String filePathString, int icount)
    throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException, UnsupportedEncodingException {

    ArrayList<String> listOfkeys = new ArrayList<String>();
    List<byte[]> bytekey = keyGen(keySize, password, filePathString, icount);
    System.out.println("bytekey:"+bytekey.get(0).length);
    try {
      listOfkeys.add(new String( bytekey.get(0), "iso-8859-1" ));
      listOfkeys.add(new String( bytekey.get(1), "iso-8859-1" ));
      listOfkeys.add(new String( bytekey.get(2), "iso-8859-1" ));
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    System.out.println("listOfkeys:"+listOfkeys.get(0).getBytes("iso-8859-1" ).length);
    return listOfkeys;

  }

  public static String keyGenString(int keySize, String password, String filePathString, int icount)
    throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException, UnsupportedEncodingException {

    List<String> listOfkeys = new ArrayList<String>();
    List<byte[]> bytekey = keyGen(keySize, password, filePathString, icount);
    listOfkeys.add(new String( bytekey.get(0), "gbk" ));
    listOfkeys.add(new String( bytekey.get(1), "gbk" ));
    listOfkeys.add(new String( bytekey.get(2), "gbk" ));
    List<byte[]> byteskey = new ArrayList<>();
    byteskey.add(listOfkeys.get(0).getBytes("gbk"));
    byteskey.add(listOfkeys.get(1).getBytes("gbk"));
    byteskey.add(listOfkeys.get(2).getBytes("gbk"));
    return listOfkeys.get(0);

  }
  // ***********************************************************************************************//

  ///////////////////// Setup /////////////////////////////

  // ***********************************************************************************************//

  public static void setup(List<byte[]> keys, Multimap<String, String> lookup, Multimap<String, String> lookup2,
                           int bigBlock, int smallBlock, int dataSize) throws Exception {

    // Instantiation of the object that contains Global MM, Local MMs and
    // the dictionary
    Multimap<String, Integer> dictionaryForMM = ArrayListMultimap.create();

    Printer.debugln("Number of (w, id) pairs " + lookup.size());

    Printer.debugln("Number of keywords " + lookup.keySet().size());

    BufferedWriter writer = new BufferedWriter(new FileWriter("logs.txt", true));//日志的填写，方便查找

    writer.write("\n *********************Stats******* \n");
    writer.write("\n Number of (w, id) pairs " + lookup2.size());
    writer.write("\n Number of keywords " + lookup.keySet().size());

    int counter = 0;

    ///////////////////// Computing Filtering Factor and exact needed data
    ///////////////////// size/////////////////////////////

    HashMap<Integer, Integer> histogram = new HashMap<Integer, Integer>();
    Printer.debugln("Number of documents " + lookup2.keySet().size());
    for (String keyword : lookup.keySet()) {
      if (histogram.get(lookup.get(keyword).size()) != null) {//histogram关键字的文件数量，出现的次数
        int tmp = histogram.get(lookup.get(keyword).size());
        histogram.put(lookup.get(keyword).size(), tmp + 1);
      } else {
        histogram.put(lookup.get(keyword).size(), 1);
      }

      if (dataSize < lookup.get(keyword).size()) {
        dataSize = lookup.get(keyword).size();
      }

    }

    // Construction of the global multi-map
    Printer.debugln("\nBeginning of Global MM creation \n");

    long startTime1 = System.nanoTime();

    RR2Lev.constructEMMParGMM(keys.get(0), lookup, bigBlock, smallBlock, dataSize);//pad,相当于构造MMg

    long endTime1 = System.nanoTime();

    //	writer.write("\n Time of MM global setup time #(w, id)/#DB " + (endTime1 - startTime1) / lookup2.size());
    writer.close();

    numberPairs = numberPairs + lookup.size();

    // Construction of the local multi-map

    Printer.debugln("Start of Local Multi-Map construction");

    long startTime = System.nanoTime();

    for (String keyword : lookup.keySet()) {


      // Filter setting optional. For a setup without any filtering set
      // filterParameter to 0
      if (((double) lookup.get(keyword).size() / TextExtractPar.maxTupleSize > filterParameter)) {



        // First computing V_w. Determine Doc identifiers

        Set<String> VW = new TreeSet<String>();
        for (String idDoc : lookup.get(keyword)) {//包含关键字keyword的文件idDoc
          VW.addAll(lookup2.get(idDoc));//VW是包含关键字keyword文件的所有词组
        }

        Multimap<String, String> secondaryLookup = ArrayListMultimap.create();

        // here we are only interested in documents in the intersection
        // between "keyword" and "word"
        for (String word : VW) {//word是VM中所有的词组
          // Filter setting optional. For a setup without any
          // filtering set filterParameter to 0
          if (((double) lookup.get(word).size() / TextExtractPar.maxTupleSize > filterParameter)) {
            Collection<String> l1 = new ArrayList<String>(lookup.get(word));//包含word的所有文件
            Collection<String> l2 = new ArrayList<String>(lookup.get(keyword));//包含keyword的所有文件
            l1.retainAll(l2);//取交集
            secondaryLookup.putAll(word, l1);
          }
        }

        // End of VW construction
        RR2Lev.counter = 0;
        // dataSize = (int) filterParameter;

        byte[] key3 = CryptoPrimitives.generateCmac(keys.get(1), 3 + keyword);
        RR2Lev.constructLocalEMMParGMM(new String(key3),
          CryptoPrimitives.generateCmac(keys.get(0), keyword), secondaryLookup, bigBlock, smallBlock,
          dataSize);//2Lev的分块压缩
        numberPairs = numberPairs + secondaryLookup.size();
        dictionaryForMM.put(new String(key3), counter);

      }
      counter++;

    }

    long endTime = System.nanoTime();

    Printer.statsln("Time to construct LMM " + (endTime - startTime) / 1000000000);



  }

  // ***********************************************************************************************//

  ///////////////////// Search Token Generation /////////////////////////////

  // ***********************************************************************************************//

  public static List<TokenDIS> token(List<byte[]> listOfkeys, List<String> search)
    throws UnsupportedEncodingException {
    List<TokenDIS> token = new ArrayList<TokenDIS>();

    for (int i = 0; i < search.size(); i++) {

      List<String> subSearch = new ArrayList<String>();
      // Create a temporary list that carry keywords in *order*
      for (int j = i; j < search.size(); j++) {
        subSearch.add(search.get(j));//把i开始到结尾的所有关键字加入到subSearch
      }

      token.add(new TokenDIS(subSearch, listOfkeys));//subSearch生成token
    }
    return token;

  }

  // ***********************************************************************************************//

  ///////////////////// Query /////////////////////////////

  // ***********************************************************************************************//

  public static Set<String> query(List<TokenDIS> token, IEX2Lev disj)
    throws Exception {

    Set<String> finalResult = new TreeSet<String>();
    for (int i = 0; i < token.size(); i++) {
      Set<String> result = null;
      if(RR2Lev.query(token.get(i).getTokenMMGlobal(),false)!=null)
        result = new HashSet<String>(RR2Lev.query(token.get(i).getTokenMMGlobal(),false));//RR2Lev.query根据MMg查找包含token.get(i)的文件
      else
        result = new HashSet<String>();
      if ((result.size()!= 0)) {//如果token中第i个关键词没结果，直接返回
        //	List<Integer> temp = new ArrayList<Integer>(//如果token中第i个关键词有结果，则根据关键词打开对应的字典Dictionary
        //			disj.getDictionaryForMM().get(new String(token.get(i).getTokenDIC())));

        //	if (!(temp.size() == 0)) {//如果没有关键词对应的字典，直接返回
        //		int pos = temp.get(0);//取出索引

        for (int j = 0; j < token.get(i).getTokenMMLocal().size(); j++) {

          Set<String> temporary = new HashSet<String>();
          //根据token和keyword的字典进行查找文档
          List<String> tempoList = RR2Lev.query(token.get(i).getTokenMMLocal().get(j),false);//问题--------------------------

          if (!(tempoList == null)) {
            temporary = new HashSet<String>(RR2Lev.query(token.get(i).getTokenMMLocal().get(j),false));
          }

          result = Sets.difference(result, temporary);//求差集，result有中temporary没有
          if (result.isEmpty()) {
            break;
          }

        }
      }
      finalResult.addAll(result);//求并集
    }
    //}
    return finalResult;
  }




}
