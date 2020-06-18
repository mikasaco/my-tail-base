package com.shenlinqiang.mytailbased.client;

import com.shenlinqiang.mytailbased.CommonController;
import com.shenlinqiang.mytailbased.Constants;
import com.shenlinqiang.mytailbased.Utils;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.internal.PlatformDependent;
import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;

public class ReadDataTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReadDataTask.class.getName());


    public static String getPath() {
        String port = System.getProperty("server.port", "8080");
        if ("8000".equals(port)) {
            return "/trace1.data";
        } else if ("8001".equals(port)) {
            return "/trace2.data";
        } else {
            return null;
        }
    }

    private static ReadDataHttpClient httpClient = new ReadDataHttpClient();

    public static Integer DATA_SOURCE_PORT;

    public static Long startOffset = 0L;

    public static Semaphore semaphore = new Semaphore(1);


    @Override
    public void run() {
        try {
            boolean isFinish = false;
            long s = System.currentTimeMillis();
            while (!isFinish) {
                semaphore.acquire();

                DefaultFullHttpRequest request = new DefaultFullHttpRequest(
                        HttpVersion.HTTP_1_1, HttpMethod.GET, getPath());
                request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderNames.CONNECTION);
                request.headers().set(HttpHeaderNames.HOST, "localhost");
                request.headers().set(HttpHeaderNames.TRANSFER_ENCODING, "chunked");
                if (startOffset + Constants.DOWNLOAD_SIZE > CommonController.DATA_SIZE) {
                    request.headers().set(HttpHeaderNames.RANGE, "bytes=" + startOffset + "-");
                    isFinish = true;
                } else {
                    request.headers().set(HttpHeaderNames.RANGE, "bytes=" + startOffset + "-" + (startOffset + Constants.DOWNLOAD_SIZE));
                }

                Channel channel = httpClient.getChannel("localhost", DATA_SOURCE_PORT);
                channel.writeAndFlush(request).sync();
                LOGGER.info("建立request请求，读取 {} 的部分文件", request.headers().get("range"));
            }
            long e = System.currentTimeMillis();
            LOGGER.info("整个文件全部读取完成,总共花费时间：" + (e - s) + "ms");
            callFinish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void callFinish() {
        try {
            Request request = new Request.Builder().url("http://localhost:8002/finish").build();
            Utils.callHttpAsync(request);
        } catch (Exception e) {
            LOGGER.warn("fail to callFinish");
        }
    }
}
