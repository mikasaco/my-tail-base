package com.shenlinqiang.mytailbased.client;

import com.shenlinqiang.mytailbased.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HttpDownloadHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpDownloadHandler.class.getName());

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {

            ByteBufAllocator alloc = ctx.channel().alloc();
            ByteBuf byteBuf = alloc.directBuffer(Constants.DOWNLOAD_SIZE);

            HttpContent content = (HttpContent) msg;
            ByteBuf buf = content.content();
            ByteBuffer directBuffer = ByteBuffer.allocateDirect(Constants.DOWNLOAD_SIZE + 1);

//            for (int i = 0; i < buf.capacity(); i++) {
//                byte aByte = buf.getByte(i);
//                directBuffer.put(aByte);
//            }
            long s = System.currentTimeMillis();
            buildSpan(byteBuf);
            buf.release();
            long e = System.currentTimeMillis();
            System.out.println("read time: " + (e - s) + "ms");
//            ReadDataTask.semaphore.release();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
        LOGGER.warn("netty处理异常，" + e);
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.warn("channel关闭");
    }

    private static final byte LF = '\n';
    private static byte[] bytes = new byte[1024];
    private static byte[] transForBuffer = new byte[32 * 1024];
    private static byte[] tempBuffer = new byte[1024];
    private static int p = 0;
    private static boolean lineEnd = false;
    private static int i = 0;

    private void buildSpan(ByteBuf buf) {
        p = 0;

        while (i < buf.capacity()) {
            byte readByte = buf.getByte(i++);
//            buf.readBytes(transForBuffer);
            if (LF == readByte) {
            }
        }

    }

    public static Map<Integer, Batch> ALLDATA = new ConcurrentHashMap<>();
    public static List<String> temp = new ArrayList<>();


    private String readBuffer() {
        int i = 0;
        int traceNumber = 0;
        int batchNo = 0;
        while (i < transForBuffer.length && LF != transForBuffer[i]) {

        }
        traceNumber++;
        if (traceNumber % Constants.BATCH_SIZE == 0) {
//            ALLDATA.put(batchNo, )
        }
        return null;
    }
}











