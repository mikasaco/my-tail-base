package com.shenlinqiang.mytailbased;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class TestDirectMemory {
    private static final byte LF = '\n';

    public static void main(String[] args) throws Exception {
        FileInputStream fin = new FileInputStream(new File("/Users/jerryq/Desktop/bisai/trace1.data"));
        FileChannel channel = fin.getChannel();


        ByteBuffer direct = ByteBuffer.allocateDirect(Constants.DOWNLOAD_SIZE);
        channel.read(direct);
        long s = System.currentTimeMillis();
        readDirectBuffer(direct);
//            direct.get(bytes);
//            readHeapBuffer(bytes);
        long e = System.currentTimeMillis();
        System.out.println("花费时间： " + (e - s) + "ms");
    }

    public static void readDirectBuffer(ByteBuffer byteBuffer) {
        int i = 0;
        byte[] bytes = new byte[1024];
        int p = 0;
        while (i < byteBuffer.capacity()) {
            byte b = byteBuffer.get(i);
            bytes[p++] = b;
            if (LF == b) {
//                System.out.println(new String(bytes, 0, p));
                p = 0;
//                System.out.println("find LF,i=" + i);
            }
            i++;
        }
    }

    public static void readHeapBuffer(byte[] bytes) {
        int i = 0;
        while (i < bytes.length) {
            byte b = bytes[i];
            i++;
        }
    }
}
