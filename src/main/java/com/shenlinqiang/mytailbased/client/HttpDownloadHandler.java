package com.shenlinqiang.mytailbased.client;

import com.shenlinqiang.mytailbased.CommonController;
import com.shenlinqiang.mytailbased.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;

import java.util.concurrent.CountDownLatch;

public class HttpDownloadHandler extends ChannelInboundHandlerAdapter {
    CountDownLatch countDownLatch = new CountDownLatch(Constants.THREAD_NUMBER);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            System.out.println("thread:" + Thread.currentThread().getName() + "开始处理");
            HttpContent content = (HttpContent) msg;
            ByteBuf buf = content.content();
//            System.out.println(buf.toString(io.netty.util.CharsetUtil.UTF_8));
            buf.release();
            countDownLatch.countDown();
            countDownLatch.await();
            long e = System.currentTimeMillis();
            System.out.println("读取文件花费时间 " + (e - CommonController.s) + "ms");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
        System.out.println(e);
        ctx.close();

    }
}