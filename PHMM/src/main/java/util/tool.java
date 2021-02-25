package main.java.util;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;

public class tool {

    private static ByteBuffer buffer = ByteBuffer.allocate(16);

    public static byte[] longToBytes(int x) {
        buffer.clear();
        buffer.putLong(0, x);
        return buffer.array();
    }

    public static long bytesToLong(byte[] bytes) {
        if(bytes==null) {
            return 0;
        }
        buffer.clear();
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();//need flip
        return buffer.getLong();
    }

    public static boolean Xor_Empty(byte[] xor) {
        for(int i=0;i<xor.length;i++) {
            if (xor[i] == 0) {
                continue;
            } else
                return false;
        }
        return true;
    }


    public static byte[] Xor(byte[] x,byte[] y){
        byte[] temp = new byte[32];
        for(int i=0;i<32;i++){
            temp[i]= (byte)(x[i]^y[i]);
        }
        return temp;
    }

    public static byte[] Gen_Proof(byte[] j, int[] proof_key){
        byte[] arr = j;
        for(int i=0;i<arr.length;i++){
            int pos = proof_key[i];
            byte temp = arr[i];
            arr[i] = arr[pos];
            arr[pos] = temp;
        }
        return arr;
    }

    public static int[] Gen_Proof_Key(){
        int[] proof_key = new int[32];
        Random random = new Random();
        for(int i=0;i<32;i++)
            proof_key[i] = random.nextInt(32);
        return proof_key;
    }


}
