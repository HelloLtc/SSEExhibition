package org.crypto.sse;

import javax.crypto.NoSuchPaddingException;

import org.ruiyun.Dao.IEXZMFDao.BloomGlobalArray;
import org.ruiyun.Dao.IEXZMFDao.BloomFilterID;
import org.ruiyun.Dao.IEXZMFDao.BloomFilterMap;
import org.ruiyun.Dao.IEXZMFDao.BloomFilterStart;
import org.ruiyun.Dao.IEXZMFDao.ListOfBloomFilter;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.*;

public class IEXZMF implements Serializable {

  public static int numberOfBF = 0;
  public static int numberOfkeywordsProcessed = 0;
  public static double filterParameter = 0.2;

  public static List<ZMFFormat> bloomFilterList = new ArrayList<ZMFFormat>();
  public static HashMap<String, ZMFFormat> bloomFilterMap = new HashMap<String, ZMFFormat>();
  public static HashMap<String, List<Integer>> bloomFilterStart = new HashMap<String, List<Integer>>();
  public static HashMap<Integer, String> bloomFilterID = new HashMap<Integer, String>();
  public static SecureRandom random = new SecureRandom();
  public static List<Integer> free = new ArrayList<Integer>();
  public static int counter = 0;
  public static int sizeOfFileIdentifer = 40;
  // ***********************************************************************************************//

  ///////////////////// KeyGen /////////////////////////////

  // ***********************************************************************************************//

  public static List<byte[]> keyGen(int keySize, String password, String filePathString, int icount)
    throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException {

    List<byte[]> listOfkeys = new ArrayList<byte[]>();

    // Generation of the key for Secure Set membership
    listOfkeys.add(ZMF.keyGenSM(keySize * 3, password + "setM", filePathString, icount));

    // Generation of two keys for Secure inverted index
    listOfkeys.add(TSet.keyGen(keySize, password + "secureIndex1", filePathString, icount));
    listOfkeys.add(TSet.keyGen(keySize, password + "secureIndex2", filePathString, icount));

    // Generation of one key for encryption
    listOfkeys.add(ZMF.keyGenSM(keySize, password + "encryption", filePathString, icount));

    return listOfkeys;

  }

  // ***********************************************************************************************//

  ///////////////////// Setup /////////////////////////////

  // ***********************************************************************************************//

  public static void setup(ObjectOutputStream output, List<byte[]> listOfkeys, String pwd, int maxLengthOfMask,
                           int falsePosRate) throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException,
    NoSuchProviderException, NoSuchPaddingException, InvalidKeySpecException, IOException, InterruptedException,
    ExecutionException {

    TextProc.TextProc(false, pwd);

    Printer.debugln("\n Beginning of ZMF construction \n");

    Printer.debugln("Number of extracted keywords " + TextExtractPar.lp1.keySet().size());
    Printer.debugln("Size of the inverted index (leakage N) " + TextExtractPar.lp1.size());

    constructMatryoshkaPar(new ArrayList(TextExtractPar.lp1.keySet()), listOfkeys.get(0), listOfkeys.get(1),
      maxLengthOfMask, falsePosRate);

  }

  // ***********************************************************************************************//

  ///////////////////// GenToken /////////////////////////////

  // ***********************************************************************************************/

  public static List<Token> token(List<byte[]> listOfkeys, List<String> search, int falsePosRate, int maxLengthOfMask)
    throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException,
    IOException {
    List<Token> token = new ArrayList<Token>();

    for (int i = 0; i < search.size(); i++) {

      List<String> subSearch = new ArrayList<String>();
      // Create a temporary list that carry keywords in *order*
      for (int j = i; j < search.size(); j++) {
        subSearch.add(search.get(j));
      }

      token.add(new Token(subSearch, listOfkeys, maxLengthOfMask, falsePosRate));
    }
    return token;

  }

  // ***********************************************************************************************//

  ///////////////////// Query Algorithm /////////////////////////////

  // ***********************************************************************************************/

  public static List<String> query(List<Token> token, int bucketSize, int falsePosRate)
    throws Exception {
    List<String> result = new ArrayList<String>();

    for (int i = 0; i < token.size(); i++) {
      List<String> resultTMP = IEXZMF.query2lev(token.get(i).getTokenMMGlobal(), false);
      if(resultTMP==null)
        resultTMP = new ArrayList<String>();
      // Printer.debugln("Result of MM Global "+resultTMP);

      Map<String, boolean[]> listOfbloomFilter = new HashMap<String, boolean[]>();

      //	List<Integer> bFIDPaddeds = new ArrayList<Integer>();

      //	bFIDPaddeds = bloomFilterStart.get(new String(token.get(i).getTokenSI1()));
      String tempstartstring = BloomFilterStart.FindBloomFilterStart(new String(token.get(i).getTokenSI1()));
      String[] tempstartlist = null;
      if(tempstartstring!=null)
        tempstartlist = tempstartstring.split(",");

      if ((i < token.size() - 1) && !(tempstartlist == null)) {
        // Printer.debugln("bFIDPaddeds "+bFIDPaddeds);

        for (int j = 0; j < tempstartlist.length; j++) {

          // Decode first the BF identifier

          int bFID = Integer.parseInt(tempstartlist[j]);
          // endOf decoding the BF id

          // Checking all corresponding tokenSM against the bloom
          // filter

          //listOfbloomFilter.put(bloomFilterID.get(bFID),
          //		bloomFilterMap.get(Integer.toString(bFID)).getSecureSetM());
          listOfbloomFilter.put(BloomFilterID.FindBloomFilterID(bFID),CryptoPrimitives.StringToBoolean(BloomFilterMap.FindBloomFilterMap(bFID)));
        }

      }

      Map<Integer, boolean[]> tempBF = new HashMap<Integer, boolean[]>();

      if (i < token.size() - 1) {
        for (int v = 0; v < token.get(i).getTokenSM().size(); v++) {
          tempBF.put(v, ZMF.testSMV22(listOfbloomFilter, token.get(i).getTokenSM().get(v), falsePosRate));
        }
      }
      if (i < token.size() - 1) {

        if (!(tempstartlist == null)) {
          for (int j = 0; j < tempstartlist.length; j++) {

            boolean flag = true;

            int counter = 0;
            while (flag) {

              if (tempBF.get(counter)[j] == true) {
                flag = false;
              } else if (counter == token.get(i).getTokenSM().size() - 1) {
                break;
              }
              counter++;
            }

            // due to filtering replace resultTMP by the following:

            if (flag == true) {
              result.add(BloomFilterID.FindBloomFilterID(Integer.parseInt(tempstartlist[j])));

            }
          }

        }

      } else {
        result.addAll(resultTMP);
      }

    }

    return result;

  }
  // ***********************************************************************************************//

  ///////////////////// Decryption of identifiers Client side
  ///////////////////// ///////////////////// /////////////////////////////

  // ***********************************************************************************************/

  public static List<String> decryptMatch(List<byte[]> encryptedID, byte[] keyENC)
    throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException,
    NoSuchProviderException, NoSuchPaddingException, IOException {
    List<String> result = new ArrayList<String>();

    for (int i = 0; i < encryptedID.size(); i++) {
      String tmp = new String(CryptoPrimitives.decryptAES_CTR_String(encryptedID.get(i), keyENC))
        .split("\t\t\t")[0];
      result.add(tmp);
    }

    return result;
  }

  public static void constructMatryoshkaPar(List<String> listOfKeyword, final byte[] keySM, final byte[] keyInvInd,
                                            final int maxLengthOfMask, final int falsePosRate)
    throws InterruptedException, ExecutionException, IOException {

    long startTime = System.nanoTime();
    //分线程
    int threads = 0;
    if (Runtime.getRuntime().availableProcessors() > listOfKeyword.size()) {
      threads = listOfKeyword.size();
    } else {
      threads = Runtime.getRuntime().availableProcessors();
    }

    Printer.extraln("Number of threads " + threads);

    ExecutorService service = Executors.newFixedThreadPool(threads);

    final Map<Integer, String> concurrentMap = new ConcurrentHashMap<Integer, String>();
    for (int i = 0; i < listOfKeyword.size(); i++) {
      concurrentMap.put(i, listOfKeyword.get(i));//索引+关键字
    }

    for (int j = 0; j < threads; j++) {
      service.execute(new Runnable() {
        @SuppressWarnings("unused")
        @Override
        public void run() {//根据线程，随机选取进行初始化

          while (concurrentMap.keySet().size() > 0) {
            // write code
            Set<Integer> possibleValues = concurrentMap.keySet();

            Random rand = new Random();

            int temp = rand.nextInt(possibleValues.size());//返回0到size的随机数

            List<Integer> listOfPossibleKeywords = new ArrayList<Integer>(possibleValues);

            // set the input as randomly selected from the remaining
            // possible keys
            String[] input = { concurrentMap.get(listOfPossibleKeywords.get(temp)) };

            // remove the key
            concurrentMap.remove(listOfPossibleKeywords.get(temp));

            try {
              bloomFilterList
                .addAll(secureSetMPar(input, keySM, keyInvInd, maxLengthOfMask, falsePosRate));
            } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException
              | NoSuchPaddingException | IOException e) {
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

    long endTime = System.nanoTime();
    long totalTime = endTime - startTime;
    Printer.debugln(
      "\nTime in (ns) for one Matryoshka filter in average: " + totalTime / TextExtractPar.lp1.size());
    Printer.debugln("\nTime to construct local multi-maps in ms " + totalTime / 1000000);

  }

  public static List<ZMFFormat> secureSetMPar(String[] input, byte[] keySM, byte[] keyInvInd, int maxLengthOfMask,
                                              int falsePosRate) throws Exception {
    List<ZMFFormat> result = new ArrayList<ZMFFormat>();

    for (String keyword : input) {

      // First step of filtering where we reduce all BFs that do not
      // verify the threshold

      Printer.debugln("\n \n Number of keywords processed % "
        + (numberOfkeywordsProcessed * 100) / TextExtractPar.lp1.keySet().size() + "\n");

      Printer.debugln("keyword being processed to issue matryoshka filters: " + keyword);

      Map<String, boolean[]> secureSetM2 = ZMF.setupSetMV2(keySM, keyword, TextExtractPar.lp2, TextExtractPar.lp1,
        falsePosRate);//ZMF初始化
      String tempstring = "";
      String tempstart = "";
      for (String id : secureSetM2.keySet()) {
        tempstring = CryptoPrimitives.booleanToString(secureSetM2.get(id));
        ListOfBloomFilter.InsertListOfBloomFilter(id, tempstring);
        BloomFilterMap.InsertBloomFilterMap(CryptoPrimitives.booleanToString(secureSetM2.get(id)));
        int newnumberOfBF = BloomFilterMap.FindBloomFilterMap(CryptoPrimitives.booleanToString(secureSetM2.get(id)));
        tempstart = tempstart + newnumberOfBF +",";
        BloomFilterID.InsertBloomFilterID(newnumberOfBF, id);


        Printer.debugln("Matryoshka filter number: " + newnumberOfBF);


      }
      BloomFilterStart.InsertBloomFilterStart(new String(TSet.token(keyInvInd, keyword)), tempstart);
    }
    numberOfkeywordsProcessed++;

    return result;
  }

  //-----------------------------------------------------------------------------------------------------------------
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

    ExecutorService service = Executors.newFixedThreadPool(2);
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

    service.shutdown();

    for (Future<Multimap<String, byte[]>> future : futures) {
      Set<String> keys = future.get().keySet();

      for (String k : keys) {
        dictionary.putAll(k, future.get().get(k));
        List<byte[]> tempList = new ArrayList<byte[]>(dictionary.get(k));
        //	System.out.print("(1)tempList "+tempList.size());
        BloomGlobalArray.InsertGlobalArrayIndex(k, tempList.get(0));

      }

    }

  }
  // ***********************************************************************************************//

  ///////////////////// Setup /////////////////////////////

  // ***********************************************************************************************//

  public static Multimap<String, byte[]> setup(byte[] key, String[] listOfKeyword, Multimap<String, String> lookup,
                                               int bigBlock, int smallBlock, int dataSize) throws Exception {

    // determine the size f the data set and therefore the size of the array

    byte[][] newarray = new byte[dataSize][];
    Multimap<String, byte[]> gamma = ArrayListMultimap.create();
    long startTime = System.nanoTime();

    byte[] iv = new byte[16];
    List<String> temp =null;
    for (String word : listOfKeyword) {
      byte[] oldarray = null;
      byte[][] oldtoken = RR2Lev.token(key, word);
      byte[] oldl = CryptoPrimitives.generateCmac(oldtoken[0], Integer.toString(0));
      oldarray=BloomGlobalArray.FindGlobalListArrayIndex(new String(oldl));
      //判断word是否存
      if(oldarray!=null){
        temp =RR2Lev.query(oldtoken,true);
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

          // generate the integer which is associated to free[b]

          byte[] randomBytes = CryptoPrimitives
            .randomBytes((int) Math.ceil(((float) Math.log(free.size()) / (Math.log(2) * 8))));

          int position = CryptoPrimitives.getIntFromByte(randomBytes,
            (int) Math.ceil(Math.log(free.size()) / Math.log(2)));

          while (position >= free.size() - 1) {
            position = position / 2;
          }

          int tmpPos = free.get(position);
          random.nextBytes(iv);

          int newPos = 0;
          newarray[newPos]=CryptoPrimitives.encryptAES_CTR_String(key2, iv,
            tmpList.toString(), bigBlock * sizeOfFileIdentifer);

          BloomGlobalArray.InsertGlobalArrayList(newarray[newPos]);
          newPos = BloomGlobalArray.FindGlobalArrayList(newarray[newPos]);
          newlistArrayIndex.add(newPos + "");

          free.remove(position);

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

            // generate the integer which is associated to free[b]

            byte[] randomBytes = CryptoPrimitives
              .randomBytes((int) Math.ceil((Math.log(free.size()) / (Math.log(2) * 8))));

            int position = CryptoPrimitives.getIntFromByte(randomBytes,
              (int) Math.ceil(Math.log(free.size()) / Math.log(2)));

            while (position >= free.size()) {
              position = position / 2;
            }

            int tmpPos = free.get(position);
            random.nextBytes(iv);


            int newPos = 0;
            newarray[newPos]=CryptoPrimitives.encryptAES_CTR_String(key2, iv,
              tmpListTwo.toString(), bigBlock * sizeOfFileIdentifer);

            BloomGlobalArray.InsertGlobalArrayList(newarray[newPos]);
            newPos = BloomGlobalArray.FindGlobalArrayList(newarray[newPos]);
            newlistArrayIndexTwo.add(newPos + "");


            free.remove(position);

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

  ///////////////////// Query Alg /////////////////////////////

  // ***********************************************************************************************//

  public static List<String> query2lev(byte[][] keys,boolean Dle)
    throws Exception {

    byte[] l = CryptoPrimitives.generateCmac(keys[0], Integer.toString(0));

    //List<byte[]> tempList = new ArrayList<byte[]>(dictionary.get(new String(l)));
    if(BloomGlobalArray.FindGlobalListArrayIndex(new String(l))==null)
      return null;
    //if (!(tempList.size() == 0)) {
    String temp = (new String(CryptoPrimitives.decryptAES_CTR_String(BloomGlobalArray.FindGlobalListArrayIndex(new String(l)), keys[1])))
      .split("\t\t\t")[0];
    if(Dle==true)
      BloomGlobalArray.deleteGlobalArrayIndex(new String(l));

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
        if (!(BloomGlobalArray.findGlobalListArrayList(Integer.parseInt((String) key.subSequence(0, counter))) == null)) {
          temp2 = (new String(CryptoPrimitives.decryptAES_CTR_String(
            BloomGlobalArray.findGlobalListArrayList(Integer.parseInt((String) key.subSequence(0, counter))), keys[1])))
            .split("\t\t\t")[0];//array[Integer.parseInt((String) key.subSequence(0, counter))]
          if(Dle==true)
            BloomGlobalArray.deleteGlobalArrayList(Integer.parseInt((String) key.subSequence(0, counter)));
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
          BloomGlobalArray.findGlobalListArrayList(Integer.parseInt((String) key.subSequence(0, counter))), keys[1])))
          .split("\t\t\t")[0];
        if(Dle==true)
          BloomGlobalArray.deleteGlobalArrayList(Integer.parseInt((String) key.subSequence(0, counter)));
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
          BloomGlobalArray.findGlobalListArrayList(Integer.parseInt((String) key.subSequence(0, counter))), keys[1])))
          .split("\t\t\t")[0];
        if(Dle==true)
          BloomGlobalArray.deleteGlobalArrayList(Integer.parseInt((String) key.subSequence(0, counter)));
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
    //	}
    return new ArrayList<String>();
  }
}
