package com.shenlinqiang.mytailbased.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpDownloadHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpDownloadHandler.class.getName());

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            HttpContent content = (HttpContent) msg;
            ByteBuf buf = content.content();
            while (buf.isReadable()) {
                String span = buildSpan(buf);
                if (span == null) {
                    break;
                }
//                LOGGER.info("读入的span:    " + span);
            }
            LOGGER.info("已经全部读入");
            if (lineEnd) {
                ReadDataTask.startOffset += new Long(buf.capacity());
            } else {
                ReadDataTask.startOffset += (long) buf.capacity() - p;
            }
            LOGGER.info("下一个开始的偏移量：" + ReadDataTask.startOffset);
            buf.release();

            ReadDataTask.semaphore.release();
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
    private static int p = 0;
    private static boolean lineEnd = false;

    private String buildSpan(ByteBuf buf) {
        byte readByte = buf.readByte();
        p = 0;
        while (buf.isReadable()) {
            if (LF == readByte) {
                lineEnd = true;
                break;
            }
            bytes[p++] = readByte;
            readByte = buf.readByte();
        }
        if (lineEnd) {
            return null;
//            return new String(bytes, 0, p);
        } else {
            lineEnd = false;
            return null;
        }

    }
}