package com.shenlinqiang.mytailbased;

import com.shenlinqiang.mytailbased.client.ReadData;
import com.shenlinqiang.mytailbased.client.ReadDataHttpClient;
import com.shenlinqiang.mytailbased.client.ReadDataTask;
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
import java.util.concurrent.Semaphore;


@RestController
public class CommonController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonController.class.getName());


    private static Integer DATA_SOURCE_PORT = 0;

    public static Integer getDataSourcePort() {
        return DATA_SOURCE_PORT;
    }

    private static ReadDataHttpClient httpClient = new ReadDataHttpClient();

    public static long DATA_SIZE;


    @RequestMapping("/ready")
    public String ready() {
        return "suc";
    }

    @RequestMapping("/setParameter")
    public String setParamter(@RequestParam Integer port) {
        if (Utils.isBackendProcess()) {
            return "suc";
        }

        if ("test".equals(System.getProperty("env", "8080"))) {
            ReadDataTask.DATA_SOURCE_PORT = 8080;
        } else {
            ReadDataTask.DATA_SOURCE_PORT = port;
        }

        try {
            URL url = new URL("http://localhost:" + ReadDataTask.DATA_SOURCE_PORT + ReadDataTask.getPath());
            final Request request = new Request.Builder().url(url).head().build();
            Response response = Utils.callHttp(request);
            DATA_SIZE = Long.parseLong(response.header("content-length"));
            LOGGER.info("文件总大小:{}", DATA_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(new ReadDataTask()).start();
        return "suc";
    }

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


    @RequestMapping("/start")
    public String start() {
        return "suc";
    }


}
