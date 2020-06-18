package com.shenlinqiang.mytailbased;

import com.shenlinqiang.mytailbased.client.ReadDataHttpClient;
import com.shenlinqiang.mytailbased.client.ReadDataTask;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URL;


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
        if ("test".equals(System.getProperty("env", "8080"))) {
            if (Utils.isBackendProcess()) {
                ReadDataTask.DATA_SOURCE_PORT = port;
                return "suc";
            }
            ReadDataTask.DATA_SOURCE_PORT = 8080;
        } else {
            ReadDataTask.DATA_SOURCE_PORT = port;
            if (Utils.isBackendProcess()) {
                return "suc";
            }
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
