package com.shenlinqiang.mytailbased.backend;

import com.alibaba.fastjson.JSON;
import com.shenlinqiang.mytailbased.CommonController;
import com.shenlinqiang.mytailbased.Constants;
import com.shenlinqiang.mytailbased.Utils;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class HandleLastBatchDataTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(HandleLastBatchDataTask.class.getName());


    public static BlockingQueue<TraceIdBatch> queue = new ArrayBlockingQueue<>(2 * Constants.THREAD_NUMBER);

    private boolean needSend;

    public HandleLastBatchDataTask(boolean needSend) {
        this.needSend = needSend;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < 2 * Constants.THREAD_NUMBER; i++) {
                try {
                    TraceIdBatch traceIdBatch = queue.poll(10, TimeUnit.MILLISECONDS);
                    if (traceIdBatch == null) {
                        LOGGER.warn("阻塞队列中没有任务");
                    }
                    new HandleFinishBatchDataTask().aggregate(traceIdBatch);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (needSend) {
                LOGGER.warn("======应该是所有数据都处理好了，要准备上报了======");
                sendCheckSum();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static Object lock = new Object();

    private void sendCheckSum() {
        try {
            while (true) {
                if (BackendController.isFinished() && BackendController.counter.get() == 0) {

                    String result = JSON.toJSONString(HandleFinishBatchDataTask.TRACE_CHUCKSUM_MAP);
                    RequestBody body = new FormBody.Builder()
                            .add("result", result).build();
                    String url = String.format("http://localhost:%s/api/finished", CommonController.getDataSourcePort());
                    Request request = new Request.Builder().url(url).post(body).build();
                    Response response = Utils.callHttp(request);
                    response.close();
                    return;
                } else {
                    Thread.sleep(10);
                }
            }
        } catch (Exception e) {
            LOGGER.warn("fail to call finish", e);
        }
    }
}
