package com.shenlinqiang.mytailbased;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;

public class Utils {
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class.getName());


    private final static OkHttpClient OK_HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(50L, TimeUnit.SECONDS)
            .readTimeout(60L, TimeUnit.SECONDS)
            .build();

    public static void callHttpAsync(Request request) throws IOException {
        Call call = OK_HTTP_CLIENT.newCall(request);
        long s = System.currentTimeMillis();
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LOGGER.error(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                response.body().string();
                long e = System.currentTimeMillis();
                if (e - s > 100) {
                    LOGGER.info("异步请求地址:" + request.url().encodedPath() + " ,时间:" + (e - s) + "ms");
                }
            }
        });
    }

    public static Response callHttp(Request request) throws IOException {
        long s = System.currentTimeMillis();
        Call call = OK_HTTP_CLIENT.newCall(request);
        Response response = call.execute();
        long e = System.currentTimeMillis();
        if ((e - s) > 100) {
//            LOGGER.info("请求地址:" + request.url().encodedPath() + " ,时间:" + (e - s) + "ms");
        }
        return response;

    }

    public static long toLong(String str, long defaultValue) {
        if (str == null) {
            return defaultValue;
        } else {
            try {
                return Long.parseLong(str);
            } catch (NumberFormatException var4) {
                return defaultValue;
            }
        }
    }

    public static String MD5(String key) {
        char hexDigits[] = {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
        };
        try {
            byte[] btInput = key.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isClientProcess() {
        String port = System.getProperty("server.port", "8080");
        if (Constants.CLIENT_PROCESS_PORT1.equals(port) ||
                Constants.CLIENT_PROCESS_PORT2.equals(port)) {
            return true;
        }
        return false;
    }

    public static boolean isBackendProcess() {
        String port = System.getProperty("server.port", "8080");
        if (Constants.BACKEND_PROCESS_PORT1.equals(port)) {
            return true;
        }
        return false;
    }
}
