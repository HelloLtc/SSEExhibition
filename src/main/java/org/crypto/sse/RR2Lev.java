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

package org.crypto.sse;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.ruiyun.Dao.IEX2levDao.GlobalArray;
import org.ruiyun.Dao.IEX2levDao.LocalArray;

import javax.crypto.NoSuchPaddingException;



import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.*;

//***********************************************************************************************//

/////////////////////    Implementation of 2Lev scheme of NDSS'14 paper by David Cash Joseph Jaeger Stanislaw Jarecki  Charanjit Jutla Hugo Krawczyk Marcel-Catalin Rosu and Michael Steiner. Finding
//		the right parameters--- of the array size as well as the threshold to differentiate between large and small database,  to meet the same reported benchmarks is empirically set in the code
//		as it was not reported in the paper. The experimental evaluation of the  scheme is one order of magnitude slower than the numbers reported by Cash as we use Java and not C
//		Plus, some enhancements on the code itself that can be done.

///		This class can be used independently of the IEX-2Lev or IEX-ZMF if needed /////////////////////////////

//***********************************************************************************************//

public class RR2Lev implements Serializable {

  // define the number of character that a file identifier can have
  public static int sizeOfFileIdentifer = 40;

  // instantiate the Secure Random Object
  public static SecureRandom random = new SecureRandom();

  public static int counter = 0;

  public Multimap<String, byte[]> dictionary = ArrayListMultimap.create();
  public static List<Integer> free = new ArrayList<Integer>();
  static byte[][] array = null;
  byte[][] arr = null;

  public RR2Lev(Multimap<String, byte[]> dictionary, byte[][] arr) {
    this.dictionary = dictionary;
    this.arr = arr;
  }

  public Multimap<String, byte[]> getDictionary() {
    return dictionary;
  }

  public void setDictionary(Multimap<String, byte[]> dictionary) {
    this.dictionary = dictionary;
  }

  public byte[][] getArray() {
    return arr;
  }

  public void setArray(byte[][] array) {
    this.arr = array;
  }

  // ***********************************************************************************************//

  ///////////////////// Key Generation /////////////////////////////

  // ***********************************************************************************************//

  public static byte[] keyGen(int keySize, String password, String filePathString, int icount)
    throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException {
    File f = new File(filePathString);
    byte[] salt = null;

    if (f.exists() && !f.isDirectory()) {
      salt = CryptoPrimitives.readAlternateImpl(filePathString);
    } else {
      salt = CryptoPrimitives.randomBytes(8);
      CryptoPrimitives.write(salt, "saltInvIX", "salt");

    }

    byte[] key = CryptoPrimitives.keyGenSetM(password, salt, icount, keySize);
    return key;

  }

  // ***********************************************************************************************//

  ///////////////////// Setup Parallel/////////////////////////////

  // ***********************************************************************************************//

  public static RR2Lev constructEMMPar(final byte[] key, final Multimap<String, String> lookup, final int bigBlock,
                                       final int smallBlock, final int dataSize) throws InterruptedException, ExecutionException, IOException {

    final Multimap<String, byte[]> dictionary = ArrayListMultimap.create();

    random.setSeed(CryptoPrimitives.randomSeed(16));

    for (int i = 0; i < dataSize; i++) {
      // initialize all buckets with random values
      free.add(i);
    }

    List<String> listOfKeyword = new ArrayList<String>(lookup.keySet());
    int threads = 0;
    if (Runtime.getRuntime().availableProcessors() > listOfKeyword.size()) {
      threads = listOfKeyword.size();
    } else {
      threads = Runtime.getRuntime().availableProcessors();
    }

    ExecutorService service = Executors.newFixedThreadPool(threads);
    ArrayList<String[]> inputs = new ArrayList<String[]>(threads);

    final Map<Integer, String> concurrentMap = new ConcurrentHashMap<Integer, String>();
    for (int i = 0; i < listOfKeyword.size(); i++) {
      concurrentMap.put(i, listOfKeyword.get(i));
    }

    for (int j = 0; j < threads; j++) {
      service.execute(new Runnable() {
        @SuppressWarnings("unused")
        @Override
        public void run() {

          while (concurrentMap.keySet().size() > 0) {
            // write code
            Set<Integer> possibleValues = concurrentMap.keySet();

            Random rand = new Random();

            int temp = rand.nextInt(possibleValues.size());

            List<Integer> listOfPossibleKeywords = new ArrayList<Integer>(possibleValues);

            // set the input as randomly selected from the remaining
            // possible keys
            String[] input = { concurrentMap.get(listOfPossibleKeywords.get(temp)) };

            // remove the key
            concurrentMap.remove(listOfPossibleKeywords.get(temp));

            try {

              Multimap<String, byte[]> output = setup(key, input, lookup, bigBlock, smallBlock, dataSize);
              Set<String> keys = output.keySet();

              for (String k : keys) {
                dictionary.putAll(k, output.get(k));
              }
            } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException
              | NoSuchPaddingException | IOException | InvalidAlgorithmParameterException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            } catch (Exception e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
          }
        }
      });
    }

    // Make sure executor stops
    service.shutdown();

    // Blocks until all tasks have completed execution after a shutdown
    // request
    service.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);

    return new RR2Lev(dictionary, array);
  }

  public static void constructEMMParGMM(final byte[] key, final Multimap<String, String> lookup, final int bigBlock,
                                        final int smallBlock, final int dataSize) throws Exception {

    final Multimap<String, byte[]> dictionary = ArrayListMultimap.create();

    random.setSeed(CryptoPrimitives.randomSeed(16));

    for (int i = 0; i < dataSize; i++) {
      // initialize all buckets with random values
      free.add(i);
    }

    List<String> listOfKeyword = new ArrayList<String>(lookup.keySet());//所有的关键词
    int threads = 0;//可用处理器的Java虚拟机的数量
    if (Runtime.getRuntime().availableProcessors() > listOfKeyword.size()) {
      threads = listOfKeyword.size();
    } else {
      threads = Runtime.getRuntime().availableProcessors();
    }

    ExecutorService service = Executors.newFixedThreadPool(threads);
    ArrayList<String[]> inputs = new ArrayList<String[]>(threads);
    //根据处理器的数量，分割关键词
    for (int i = 0; i < threads; i++) {
      String[] tmp;
      if (i == threads - 1) {
        tmp = new String[listOfKeyword.size() / threads + listOfKeyword.size() % threads];
        for (int j = 0; j < listOfKeyword.size() / threads + listOfKeyword.size() % threads; j++) {
          tmp[j] = listOfKeyword.get((listOfKeyword.size() / threads) * i + j);
        }
      } else {
        tmp = new String[listOfKeyword.size() / threads];
        for (int j = 0; j < listOfKeyword.size() / threads; j++) {

          tmp[j] = listOfKeyword.get((listOfKeyword.size() / threads) * i + j);
        }
      }
      inputs.add(i, tmp);
    }

    Printer.debugln("End of Partitionning  \n");
    //每个处理器进行处理操作
    List<Future<Multimap<String, byte[]>>> futures = new ArrayList<Future<Multimap<String, byte[]>>>();
    for (final String[] input : inputs) {
      Callable<Multimap<String, byte[]>> callable = new Callable<Multimap<String, byte[]>>() {
        public Multimap<String, byte[]> call() throws Exception {
          //关键的setup，使用2lev分小，中，大包进行处理
          Multimap<String, byte[]> output = setup(key, input, lookup, bigBlock, smallBlock, dataSize);//pad
          return output;
        }
      };
      futures.add(service.submit(callable));
    }


    try {
      // 告诉线程池，如果所有任务执行完毕则关闭线程池
      service.shutdown();

      // 判断线程池是否在限定时间内，或者线程池内线程全部结束
      if(!service.awaitTermination(3, TimeUnit.SECONDS)){
        // 超时的时候向线程池中所有的线程发出中断(interrupted)。
        service.shutdownNow();
      }
    } catch (InterruptedException e) {
      System.out.println("awaitTermination interrupted: " + e);
    }

    for (Future<Multimap<String, byte[]>> future : futures) {
      Set<String> keys = future.get().keySet();

      for (String k : keys) {
        dictionary.putAll(k, future.get().get(k));
        List<byte[]> tempList = new ArrayList<byte[]>(dictionary.get(k));
        //	System.out.print("(1)tempList "+tempList.size());
        GlobalArray.InsertGlobalArrayIndex(k, tempList.get(0));

      }

    }

  }

  public static RR2Lev constructLocalEMMParGMM(final String keyword,final byte[] key, final Multimap<String, String> lookup, final int bigBlock,
                                               final int smallBlock, final int dataSize) throws Exception {

    final Multimap<String, byte[]> dictionary = ArrayListMultimap.create();

    random.setSeed(CryptoPrimitives.randomSeed(16));

    for (int i = 0; i < dataSize; i++) {
      // initialize all buckets with random values
      free.add(i);
    }

    List<String> listOfKeyword = new ArrayList<String>(lookup.keySet());//所有的关键词
    int threads = 0;//可用处理器的Java虚拟机的数量
    if (Runtime.getRuntime().availableProcessors() > listOfKeyword.size()) {
      threads = listOfKeyword.size();
    } else {
      threads = Runtime.getRuntime().availableProcessors();
    }

    ExecutorService service = Executors.newFixedThreadPool(threads);
    ArrayList<String[]> inputs = new ArrayList<String[]>(threads);
    //根据处理器的数量，分割关键词
    for (int i = 0; i < threads; i++) {
      String[] tmp;
      if (i == threads - 1) {
        tmp = new String[listOfKeyword.size() / threads + listOfKeyword.size() % threads];
        for (int j = 0; j < listOfKeyword.size() / threads + listOfKeyword.size() % threads; j++) {
          tmp[j] = listOfKeyword.get((listOfKeyword.size() / threads) * i + j);
        }
      } else {
        tmp = new String[listOfKeyword.size() / threads];
        for (int j = 0; j < listOfKeyword.size() / threads; j++) {

          tmp[j] = listOfKeyword.get((listOfKeyword.size() / threads) * i + j);
        }
      }
      inputs.add(i, tmp);
    }

    //每个处理器进行处理操作
    List<Future<Multimap<String, byte[]>>> futures = new ArrayList<Future<Multimap<String, byte[]>>>();
    for (final String[] input : inputs) {
      Callable<Multimap<String, byte[]>> callable = new Callable<Multimap<String, byte[]>>() {
        public Multimap<String, byte[]> call() throws Exception {
          //关键的setup，使用2lev分小，中，大包进行处理
          Multimap<String, byte[]> output = Localsetup(keyword,key, input, lookup, bigBlock, smallBlock, dataSize);//pad
          return output;
        }
      };
      futures.add(service.submit(callable));
    }

    try {
      // 告诉线程池，如果所有任务执行完毕则关闭线程池
      service.shutdown();

      // 判断线程池是否在限定时间内，或者线程池内线程全部结束
      if(!service.awaitTermination(2, TimeUnit.SECONDS)){
        // 超时的时候向线程池中所有的线程发出中断(interrupted)。
        service.shutdownNow();
      }
    } catch (InterruptedException e) {
      System.out.println("awaitTermination interrupted: " + e);
    }

    for (Future<Multimap<String, byte[]>> future : futures) {
      Set<String> keys = future.get().keySet();

      for (String k : keys) {
        dictionary.putAll(k, future.get().get(k));
        List<byte[]> tempList = new ArrayList<byte[]>(dictionary.get(k));
        //	System.out.print("(1)tempList "+tempList.size());
        LocalArray.InsertLocalArrayIndex(keyword,k, tempList.get(0));

      }

    }

    return new RR2Lev(dictionary, array);
  }
  // ***********************************************************************************************//

  ///////////////////// Setup /////////////////////////////

  // ***********************************************************************************************//

  public static Multimap<String, byte[]> setup(byte[] key, String[] listOfKeyword, Multimap<String, String> lookup,
                                               int bigBlock, int smallBlock, int dataSize) throws Exception {

    // determine the size f the data set and therefore the size of the array
    array = new byte[dataSize][];
    byte[][] newarray = new byte[dataSize][];
    Multimap<String, byte[]> gamma = ArrayListMultimap.create();
    long startTime = System.nanoTime();

    byte[] iv = new byte[16];
    List<String> temp =null;
    for (String word : listOfKeyword) {
      byte[] oldarray = null;
      byte[][] oldtoken = RR2Lev.token(key, word);
      byte[] oldl = CryptoPrimitives.generateCmac(oldtoken[0], Integer.toString(0));
      oldarray=GlobalArray.FindGlobalListArrayIndex(new String(oldl));
      //判断word是否存
      if(oldarray!=null){
        temp =RR2Lev.query(oldtoken,true);
      }

      counter++;

      // generate the tag
      byte[] key1 = CryptoPrimitives.generateCmac(key, 1 + word);
      byte[] key2 = CryptoPrimitives.generateCmac(key, 2 + word);
      int sum = lookup.get(word).size();
      if(temp!=null)
        sum = sum +temp.size();
      int t = (int) Math.ceil((float) (sum) / bigBlock);//Math.ceil 取最大正整数

      if ((sum) <= smallBlock) {
        // pad DB(w) to "small block"
        byte[] l = CryptoPrimitives.generateCmac(key1, Integer.toString(0));
        random.nextBytes(iv);
        String crystring ="1 " + lookup.get(word).toString();
        if(temp!=null)
          crystring = crystring + temp;
        byte[] v =CryptoPrimitives.encryptAES_CTR_String(key2, iv,
          crystring, smallBlock * sizeOfFileIdentifer);
        gamma.put(new String(l), v);
      }

      else {

        List<String> listArrayIndex = new ArrayList<String>();
        List<String> newlistArrayIndex = new ArrayList<String>();


        for (int j = 0; j < t; j++) {

          List<String> tmpList = new ArrayList<String>(lookup.get(word));
          if(temp!=null)
            for (String i:temp) {
              tmpList.add(i);
            }


          if (j != t - 1) {
            tmpList = tmpList.subList(j * bigBlock, (j + 1) * bigBlock);
          } else {
            int sizeList = tmpList.size();

            tmpList = tmpList.subList(j * bigBlock, tmpList.size());

            for (int s = 0; s < ((j + 1) * bigBlock - sizeList); s++) {
              tmpList.add("XX");
            }

          }

          int newPos = 0;
          newarray[newPos]=CryptoPrimitives.encryptAES_CTR_String(key2, iv,
            tmpList.toString(), bigBlock * sizeOfFileIdentifer);

          GlobalArray.InsertGlobalArrayList(newarray[newPos]);
          newPos = GlobalArray.FindGlobalArrayList(newarray[newPos]);
          newlistArrayIndex.add(newPos + "");


        }

        // medium case
        if (t <= smallBlock) {
          byte[] l = CryptoPrimitives.generateCmac(key1, Integer.toString(0));
          random.nextBytes(iv);
          byte[] v = CryptoPrimitives.encryptAES_CTR_String(key2, iv,
            "2 " + newlistArrayIndex.toString(), smallBlock * sizeOfFileIdentifer);
          gamma.put(new String(l),v);
        }
        // big case
        else {
          int tPrime = (int) Math.ceil((float) t / bigBlock);

          List<String> listArrayIndexTwo = new ArrayList<String>();
          List<String> newlistArrayIndexTwo = new ArrayList<String>();
          for (int l = 0; l < tPrime; l++) {
            List<String> tmpListTwo = new ArrayList<String>(newlistArrayIndex);

            if (l != tPrime - 1) {
              tmpListTwo = tmpListTwo.subList(l * bigBlock, (l + 1) * bigBlock);
            } else {

              int sizeList = tmpListTwo.size();

              tmpListTwo = tmpListTwo.subList(l * bigBlock, tmpListTwo.size());
              for (int s = 0; s < ((l + 1) * bigBlock - sizeList); s++) {
                tmpListTwo.add("XX");
              }
            }




            int newPos = 0;
            newarray[newPos]=CryptoPrimitives.encryptAES_CTR_String(key2, iv,
              tmpListTwo.toString(), bigBlock * sizeOfFileIdentifer);

            GlobalArray.InsertGlobalArrayList(newarray[newPos]);
            newPos = GlobalArray.FindGlobalArrayList(newarray[newPos]);
            newlistArrayIndexTwo.add(newPos + "");


          }

          // Pad the second set of identifiers

          byte[] l = CryptoPrimitives.generateCmac(key1, Integer.toString(0));
          random.nextBytes(iv);
          byte[] v = CryptoPrimitives.encryptAES_CTR_String(key2, iv,
            "3 " + newlistArrayIndexTwo.toString(), smallBlock * sizeOfFileIdentifer);
          gamma.put(new String(l),v);
        }

      }

    }
    return gamma;
  }

  // ***********************************************************************************************//

  ///////////////////// LocalSetup /////////////////////////////

  // ***********************************************************************************************//

  public static Multimap<String, byte[]> Localsetup(String keyword,byte[] key, String[] listOfKeyword, Multimap<String, String> lookup,
                                                    int bigBlock, int smallBlock, int dataSize) throws Exception {

    // determine the size f the data set and therefore the size of the array
    array = new byte[dataSize][];
    byte[][] newarray = new byte[dataSize][];
    Multimap<String, byte[]> gamma = ArrayListMultimap.create();
    long startTime = System.nanoTime();

    byte[] iv = new byte[16];
    List<String> temp =null;
    for (String word : listOfKeyword) {
      byte[] oldarray = null;
      byte[][] oldtoken = RR2Lev.token(key, word);
      byte[] oldl = CryptoPrimitives.generateCmac(oldtoken[0], Integer.toString(0));
      oldarray=LocalArray.FindLocalListArrayIndex(keyword,new String(oldl));
      //判断word是否存
      if(oldarray!=null){
        temp =RR2Lev.LocalQuery(keyword,oldtoken,true);//转换为 Local
      }

      counter++;
      if (((float) counter / 10000) == (int) (counter / 10000)) {
        Printer.debugln("Number of processed keywords " + counter);
      }

      // generate the tag
      byte[] key1 = CryptoPrimitives.generateCmac(key, 1 + word);
      byte[] key2 = CryptoPrimitives.generateCmac(key, 2 + word);
      int sum = lookup.get(word).size();
      if(temp!=null)
        sum = sum +temp.size();
      int t = (int) Math.ceil((float) (sum) / bigBlock);//Math.ceil 取最大正整数

      if ((sum) <= smallBlock) {
        // pad DB(w) to "small block"
        byte[] l = CryptoPrimitives.generateCmac(key1, Integer.toString(0));
        random.nextBytes(iv);
        String crystring ="1 " + lookup.get(word).toString();
        if(temp!=null)
          crystring = crystring + temp;
        byte[] v =CryptoPrimitives.encryptAES_CTR_String(key2, iv,
          crystring, smallBlock * sizeOfFileIdentifer);
        gamma.put(new String(l), v);
      }

      else {

        List<String> listArrayIndex = new ArrayList<String>();
        List<String> newlistArrayIndex = new ArrayList<String>();


        for (int j = 0; j < t; j++) {

          List<String> tmpList = new ArrayList<String>(lookup.get(word));
          if(temp!=null)
            for (String i:temp) {
              tmpList.add(i);
            }


          if (j != t - 1) {
            tmpList = tmpList.subList(j * bigBlock, (j + 1) * bigBlock);
          } else {
            int sizeList = tmpList.size();

            tmpList = tmpList.subList(j * bigBlock, tmpList.size());

            for (int s = 0; s < ((j + 1) * bigBlock - sizeList); s++) {
              tmpList.add("XX");
            }

          }


          int newPos = 0;
          newarray[newPos]=CryptoPrimitives.encryptAES_CTR_String(key2, iv,
            tmpList.toString(), bigBlock * sizeOfFileIdentifer);

          LocalArray.InsertLocalArrayList(newarray[newPos]);
          newPos = LocalArray.findLocalListArrayTempList(newarray[newPos]);
          newlistArrayIndex.add(newPos + "");


        }

        // medium case
        if (t <= smallBlock) {
          byte[] l = CryptoPrimitives.generateCmac(key1, Integer.toString(0));
          random.nextBytes(iv);
          byte[] v = CryptoPrimitives.encryptAES_CTR_String(key2, iv,
            "2 " + newlistArrayIndex.toString(), smallBlock * sizeOfFileIdentifer);
          gamma.put(new String(l),v);
        }
        // big case
        else {
          int tPrime = (int) Math.ceil((float) t / bigBlock);

          List<String> listArrayIndexTwo = new ArrayList<String>();
          List<String> newlistArrayIndexTwo = new ArrayList<String>();
          for (int l = 0; l < tPrime; l++) {
            List<String> tmpListTwo = new ArrayList<String>(newlistArrayIndex);

            if (l != tPrime - 1) {
              tmpListTwo = tmpListTwo.subList(l * bigBlock, (l + 1) * bigBlock);
            } else {

              int sizeList = tmpListTwo.size();

              tmpListTwo = tmpListTwo.subList(l * bigBlock, tmpListTwo.size());
              for (int s = 0; s < ((l + 1) * bigBlock - sizeList); s++) {
                tmpListTwo.add("XX");
              }
            }



            int newPos = 0;
            newarray[newPos]=CryptoPrimitives.encryptAES_CTR_String(key2, iv,
              tmpListTwo.toString(), bigBlock * sizeOfFileIdentifer);

            LocalArray.InsertLocalArrayList( newarray[newPos]);
            newPos = LocalArray.findLocalListArrayTempList( newarray[newPos]);
            newlistArrayIndexTwo.add(newPos + "");

          }

          // Pad the second set of identifiers

          byte[] l = CryptoPrimitives.generateCmac(key1, Integer.toString(0));
          random.nextBytes(iv);
          byte[] v = CryptoPrimitives.encryptAES_CTR_String(key2, iv,
            "3 " + newlistArrayIndexTwo.toString(), smallBlock * sizeOfFileIdentifer);
          gamma.put(new String(l),v);
        }

      }

    }
    long endTime = System.nanoTime();
    long totalTime = endTime - startTime;
    // Printer.debugln("Time for one (w, id) "+totalTime/lookup.size());
    return gamma;
  }

  // ***********************************************************************************************//

  ///////////////////// Search Token generation /////////////////////
  ///////////////////// /////////////////////////////

  // ***********************************************************************************************//

  public static byte[][] token(byte[] key, String word) throws UnsupportedEncodingException {

    byte[][] keys = new byte[2][];
    keys[0] = CryptoPrimitives.generateCmac(key, 1 + word);
    keys[1] = CryptoPrimitives.generateCmac(key, 2 + word);

    return keys;
  }

  // ***********************************************************************************************//

  ///////////////////// Query Alg /////////////////////////////

  // ***********************************************************************************************//

  public static List<String> query(byte[][] keys,boolean Dle)
    throws Exception {

    byte[] l = CryptoPrimitives.generateCmac(keys[0], Integer.toString(0));

    //List<byte[]> tempList = new ArrayList<byte[]>(dictionary.get(new String(l)));
    if(GlobalArray.FindGlobalListArrayIndex(new String(l))==null)
      return null;
    //if (!(tempList.size() == 0)) {
    String temp = (new String(CryptoPrimitives.decryptAES_CTR_String(GlobalArray.FindGlobalListArrayIndex(new String(l)), keys[1])))
      .split("\t\t\t")[0];
    if(Dle==true)
      GlobalArray.deleteGlobalArrayIndex(new String(l));

    temp = temp.replaceAll("\\s", "");
    temp = temp.replace('[', ',');
    temp = temp.replace("]", "");

    String[] result = temp.split(",");

    List<String> resultFinal = new ArrayList<String>(Arrays.asList(result));
    // We remove the flag that identifies the size of the dataset

    if (result[0].equals("1")) {

      resultFinal.remove(0);
      return resultFinal;
    }

    else if (result[0].equals("2")) {
      resultFinal.remove(0);

      List<String> resultFinal2 = new ArrayList<String>();

      for (String key : resultFinal) {

        boolean flag = true;
        int counter = 0;
        while (flag) {

          if (counter < key.length() && Character.isDigit(key.charAt(counter))) {

            counter++;
          }

          else {
            flag = false;
          }
        }

        String temp2 = "";

        byte[] templist = null;
        templist = GlobalArray.findGlobalListArrayList(Integer.parseInt((String) key.subSequence(0, counter)));
        if (templist!= null) {

          temp2 = (new String(CryptoPrimitives.decryptAES_CTR_String(
            GlobalArray.findGlobalListArrayList(Integer.parseInt((String) key.subSequence(0, counter))), keys[1])))
            .split("\t\t\t")[0];//array[Integer.parseInt((String) key.subSequence(0, counter))]
          if(Dle==true)
            GlobalArray.deleteGlobalArrayList(Integer.parseInt((String) key.subSequence(0, counter)));
        }
        temp2 = temp2.replaceAll("\\s", "");

        temp2 = temp2.replaceAll(",XX", "");

        temp2 = temp2.replace("[", "");
        temp2 = temp2.replace("]", "");

        String[] result3 = temp2.split(",");

        List<String> tmp = new ArrayList<String>(Arrays.asList(result3));

        resultFinal2.addAll(tmp);
      }

      return resultFinal2;
    }

    else if (result[0].equals("3")) {
      resultFinal.remove(0);
      List<String> resultFinal2 = new ArrayList<String>();
      for (String key : resultFinal) {

        boolean flag = true;
        int counter = 0;
        while (flag) {

          if (counter < key.length() && Character.isDigit(key.charAt(counter))) {

            counter++;
          }

          else {
            flag = false;
          }
        }
        String temp2 = (new String(CryptoPrimitives.decryptAES_CTR_String(
          GlobalArray.findGlobalListArrayList(Integer.parseInt((String) key.subSequence(0, counter))), keys[1])))
          .split("\t\t\t")[0];
        if(Dle==true)
          GlobalArray.deleteGlobalArrayList(Integer.parseInt((String) key.subSequence(0, counter)));
        temp2 = temp2.replaceAll("\\s", "");

        temp2 = temp2.replaceAll(",XX", "");
        temp2 = temp2.replace("[", "");
        temp2 = temp2.replace("]", "");

        String[] result3 = temp2.split(",");
        List<String> tmp = new ArrayList<String>(Arrays.asList(result3));
        resultFinal2.addAll(tmp);
      }
      List<String> resultFinal3 = new ArrayList<String>();

      for (String key : resultFinal2) {

        boolean flag = true;
        int counter = 0;
        while (flag) {

          if (counter < key.length() && Character.isDigit(key.charAt(counter))) {

            counter++;
          }

          else {
            flag = false;
          }
        }
        String temp2 = (new String(CryptoPrimitives.decryptAES_CTR_String(
          GlobalArray.findGlobalListArrayList(Integer.parseInt((String) key.subSequence(0, counter))), keys[1])))
          .split("\t\t\t")[0];
        if(Dle==true)
          GlobalArray.deleteGlobalArrayList(Integer.parseInt((String) key.subSequence(0, counter)));
        temp2 = temp2.replaceAll("\\s", "");
        temp2 = temp2.replaceAll(",XX", "");

        temp2 = temp2.replace("[", "");
        temp2 = temp2.replace("]", "");
        String[] result3 = temp2.split(",");

        List<String> tmp = new ArrayList<String>(Arrays.asList(result3));

        resultFinal3.addAll(tmp);
      }

      return resultFinal3;
    }
    return new ArrayList<String>();
  }


  // ***********************************************************************************************//

  ///////////////////// LocalQuery Alg /////////////////////////////

  // ***********************************************************************************************//

  public static List<String> LocalQuery(String keyword,byte[][] keys,boolean Dle)
    throws Exception {

    byte[] l = CryptoPrimitives.generateCmac(keys[0], Integer.toString(0));

    //List<byte[]> tempList = new ArrayList<byte[]>(dictionary.get(new String(l)));
    if(LocalArray.FindLocalListArrayIndex(keyword,new String(l))==null)
      return null;
    //if (!(tempList.size() == 0)) {
    String temp = (new String(CryptoPrimitives.decryptAES_CTR_String(LocalArray.FindLocalListArrayIndex(keyword,new String(l)), keys[1])))
      .split("\t\t\t")[0];
    if(Dle==true)
      LocalArray.deleteLocalArrayIndex(keyword,new String(l));

    temp = temp.replaceAll("\\s", "");
    temp = temp.replace('[', ',');
    temp = temp.replace("]", "");

    String[] result = temp.split(",");

    List<String> resultFinal = new ArrayList<String>(Arrays.asList(result));
    // We remove the flag that identifies the size of the dataset

    if (result[0].equals("1")) {

      resultFinal.remove(0);
      return resultFinal;
    }

    else if (result[0].equals("2")) {
      resultFinal.remove(0);

      List<String> resultFinal2 = new ArrayList<String>();

      for (String key : resultFinal) {

        boolean flag = true;
        int counter = 0;
        while (flag) {

          if (counter < key.length() && Character.isDigit(key.charAt(counter))) {

            counter++;
          }

          else {
            flag = false;
          }
        }

        String temp2 = "";
        if (!(LocalArray.findLocalListArrayList(Integer.parseInt((String) key.subSequence(0, counter))) == null)) {
          temp2 = (new String(CryptoPrimitives.decryptAES_CTR_String(
            LocalArray.findLocalListArrayList(Integer.parseInt((String) key.subSequence(0, counter))), keys[1])))
            .split("\t\t\t")[0];//array[Integer.parseInt((String) key.subSequence(0, counter))]
          if(Dle==true)
            LocalArray.deleteLocalArrayList(Integer.parseInt((String) key.subSequence(0, counter)));
        }
        temp2 = temp2.replaceAll("\\s", "");

        temp2 = temp2.replaceAll(",XX", "");

        temp2 = temp2.replace("[", "");
        temp2 = temp2.replace("]", "");

        String[] result3 = temp2.split(",");

        List<String> tmp = new ArrayList<String>(Arrays.asList(result3));

        resultFinal2.addAll(tmp);
      }

      return resultFinal2;
    }

    else if (result[0].equals("3")) {
      resultFinal.remove(0);
      List<String> resultFinal2 = new ArrayList<String>();
      for (String key : resultFinal) {

        boolean flag = true;
        int counter = 0;
        while (flag) {

          if (counter < key.length() && Character.isDigit(key.charAt(counter))) {

            counter++;
          }

          else {
            flag = false;
          }
        }
        String temp2 = (new String(CryptoPrimitives.decryptAES_CTR_String(
          LocalArray.findLocalListArrayList(Integer.parseInt((String) key.subSequence(0, counter))), keys[1])))
          .split("\t\t\t")[0];
        if(Dle==true)
          LocalArray.deleteLocalArrayList(Integer.parseInt((String) key.subSequence(0, counter)));
        temp2 = temp2.replaceAll("\\s", "");

        temp2 = temp2.replaceAll(",XX", "");
        temp2 = temp2.replace("[", "");
        temp2 = temp2.replace("]", "");

        String[] result3 = temp2.split(",");
        List<String> tmp = new ArrayList<String>(Arrays.asList(result3));
        resultFinal2.addAll(tmp);
      }
      List<String> resultFinal3 = new ArrayList<String>();

      for (String key : resultFinal2) {

        boolean flag = true;
        int counter = 0;
        while (flag) {

          if (counter < key.length() && Character.isDigit(key.charAt(counter))) {

            counter++;
          }

          else {
            flag = false;
          }
        }
        String temp2 = (new String(CryptoPrimitives.decryptAES_CTR_String(
          GlobalArray.findGlobalListArrayList(Integer.parseInt((String) key.subSequence(0, counter))), keys[1])))
          .split("\t\t\t")[0];
        if(Dle==true)
          GlobalArray.deleteGlobalArrayList(Integer.parseInt((String) key.subSequence(0, counter)));
        temp2 = temp2.replaceAll("\\s", "");
        temp2 = temp2.replaceAll(",XX", "");

        temp2 = temp2.replace("[", "");
        temp2 = temp2.replace("]", "");
        String[] result3 = temp2.split(",");

        List<String> tmp = new ArrayList<String>(Arrays.asList(result3));

        resultFinal3.addAll(tmp);
      }

      return resultFinal3;
    }
    return new ArrayList<String>();
  }
}
