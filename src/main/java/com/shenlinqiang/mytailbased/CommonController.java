package com.shenlinqiang.mytailbased;

import com.shenlinqiang.mytailbased.client.ReadData;
import com.shenlinqiang.mytailbased.client.RemoveBatchTask;
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


    @RequestMapping("/ready")
    public String ready() {
        return "suc";
    }

    @RequestMapping("/setParameter")
    public String setParamter(@RequestParam Integer port) {
        DATA_SOURCE_PORT = port;
        if (Utils.isClientProcess()) {
            String path = ReadData.getPath();
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
                long s = System.currentTimeMillis();
                new Thread(new ReadData(i,s)).start();
//                new Thread(new RemoveBatchTask(i)).start();
            }
        }

        return "suc";
    }

    @RequestMapping("/start")
    public String start() {
        return "suc";
    }


}
