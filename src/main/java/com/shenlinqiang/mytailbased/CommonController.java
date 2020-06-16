package com.shenlinqiang.mytailbased;

import com.shenlinqiang.mytailbased.client.ReadData;
import com.shenlinqiang.mytailbased.client.ReadDataHttpClient;
import com.shenlinqiang.mytailbased.client.RemoveBatchTask;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.*;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;


@RestController
public class CommonController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonController.class.getName());


    private static Integer DATA_SOURCE_PORT = 0;

    public static Integer getDataSourcePort() {
        return DATA_SOURCE_PORT;
    }

    private static ReadDataHttpClient httpClient = new ReadDataHttpClient();


    @RequestMapping("/ready")
    public String ready() {
        return "suc";
    }

    @RequestMapping("/setParameter")
    public String setParamter(@RequestParam Integer port) {
        if ("test".equals(System.getProperty("env"))) {
            DATA_SOURCE_PORT = 8080;
        } else {
            DATA_SOURCE_PORT = port;
        }
        String path = ReadData.getPath();
        if (Utils.isClientProcess()) {
            try {
                URL url = new URL(path);
                final Request request = new Request.Builder()
                        .url(url)
                        .head()//这里注意请求方式为head
                        .build();
                Response response = Utils.callHttp(request);
                long length = Long.parseLong(response.header("content-length"));
                Constants.ONEG = length / Constants.THREAD_NUMBER;
                LOGGER.info("文件总大小:{}，下载线程数：{},每个线程下载大小：{}", length, Constants.THREAD_NUMBER, Constants.ONEG);
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (int i = 0; i < Constants.THREAD_NUMBER; i++) {
                try {
                    DefaultFullHttpRequest request = new DefaultFullHttpRequest(
                            HttpVersion.HTTP_1_1, HttpMethod.GET, "/trace1.data");
                    request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderNames.CONNECTION);
                    request.headers().set(HttpHeaderNames.HOST, "localhost");
                    request.headers().set(HttpHeaderNames.TRANSFER_ENCODING, "chunked");
                    if (i == Constants.THREAD_NUMBER - 1) {
                        request.headers().set(HttpHeaderNames.RANGE, "bytes=" + i * Constants.ONEG + "-");
                    } else {
                        request.headers().set(HttpHeaderNames.RANGE, "bytes=" + i * Constants.ONEG + "-" + (i + 1) * Constants.ONEG);
                    }
                    request.headers().set(HttpHeaderNames.USER_AGENT, "netty");
                    Channel channel = httpClient.getChannel("localhost", DATA_SOURCE_PORT);
                    ChannelFuture future = channel.writeAndFlush(request).sync();
                    future.get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
        return "suc";
    }

    @RequestMapping("/start")
    public String start() {
        return "suc";
    }


}
