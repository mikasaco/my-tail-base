package com.shenlinqiang.mytailbased.client;

import com.alibaba.fastjson.JSON;
import com.shenlinqiang.mytailbased.CommonController;
import com.shenlinqiang.mytailbased.Constants;
import com.shenlinqiang.mytailbased.Utils;
import com.shenlinqiang.mytailbased.backend.TraceIdBatch;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class ReadData implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReadData.class.getName());

    private static CountDownLatch countDownLatch = new CountDownLatch(Constants.THREAD_NUMBER);

    private Integer threadNo;
    private long s;

    public ReadData(Integer threadNo, long s) {
        this.threadNo = threadNo;
        this.s = s;
        init();
    }

    public void init() {
        Map<Integer, Batch> map = new ConcurrentHashMap<>();
        ALLDATA.add(threadNo, map);
    }

    /**
     * 所有线程的数据,数组下标 线程编号,value 批次batch和批次号的映射map
     */
    public static List<Map<Integer, Batch>> ALLDATA = new ArrayList<>(Constants.THREAD_NUMBER);

    private static Batch[] FIRSTBATCH = new Batch[Constants.THREAD_NUMBER];
    private static Batch[] LASTBATCH = new Batch[Constants.THREAD_NUMBER];
    private static Batch[] SECONDBATCH = new Batch[Constants.THREAD_NUMBER];

    public static String getPath() {
        String port = System.getProperty("server.port", "8080");
        String env = System.getProperty("env", "online");
        if ("test".equals(env)) {
            if ("8000".equals(port)) {
                return "http://localhost:8080/trace1.data";
            } else if ("8001".equals(port)) {
                return "http://localhost:8080/trace2.data";
            } else {
                return null;
            }
        } else {
            if ("8000".equals(port)) {
                return "http://localhost:" + CommonController.getDataSourcePort() + "/trace1.data";
            } else if ("8001".equals(port)) {
                return "http://localhost:" + CommonController.getDataSourcePort() + "/trace2.data";
            } else {
                return null;
            }
        }

    }

    private int counter = 0;

    @Override
    public void run() {
        String path = getPath();
        if (StringUtils.isEmpty(path)) {
            LOGGER.warn("path is empty");
            return;
        }
        try {
            URL url = new URL(path);
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
            httpConnection.setRequestProperty("range", "bytes=" + threadNo * Constants.ONEG + "-"
                    + (threadNo + 1) * Constants.ONEG);
            InputStream input = httpConnection.getInputStream();
            BufferedReader bf = new BufferedReader(new InputStreamReader(input));
            while (bf.readLine() != null) {
                counter++;
            }
            LOGGER.info("thread:{},counter:{}", threadNo, counter);
            countDownLatch.countDown();
            callFinish();
            bf.close();
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateWrongTraceId(TraceIdBatch traceIdBatch) {
        String jsonStr = JSON.toJSONString(traceIdBatch);
        try {
            RequestBody body = RequestBody.create(Constants.MEDIATYPE, jsonStr);
//            LOGGER.info("上传错误traceBatch, :" + jsonStr);
            Request request = new Request.Builder().url("http://localhost:8002/setWrongTraceId").post(body).build();
            Utils.callHttpAsync(request);
        } catch (Exception e) {
            LOGGER.warn("fail to updateBadTraceId, json:" + jsonStr);
        }
    }

    public void callFinish() {
        try {
            countDownLatch.await();
            long e = System.currentTimeMillis();
            LOGGER.warn("文件读取处理时间 " + (e - s) + "ms");
            if (threadNo == 0) {
                LOGGER.info("所有线程都处理完成");

            }
        } catch (Exception e) {
            LOGGER.warn("fail to callFinish");
        }
    }

    public static String getWrongTracing(TraceIdBatch traceIdBatch) {
        Map<String, List<String>> wrongTraceMap = new HashMap<>();
        try {
            int batchNo = traceIdBatch.getBatchNo();
            int threadNo = traceIdBatch.getThreadNo();
            Set<String> traceIdList = traceIdBatch.getTraceIdList();
            boolean lastBatch = traceIdBatch.isLastBatch();

            Batch batch = ALLDATA.get(threadNo).get(batchNo);
            if (lastBatch) {
                if (threadNo == Constants.THREAD_NUMBER - 1) {
                    getWrongTraceWithBatch(ALLDATA.get(threadNo).get(batchNo - 1), traceIdList, wrongTraceMap, batchNo);
                    getWrongTraceWithBatch(batch, traceIdList, wrongTraceMap, batchNo);
                } else {
                    getWrongTraceWithBatch(ALLDATA.get(threadNo).get(batchNo - 1), traceIdList, wrongTraceMap, batchNo);
                    getWrongTraceWithBatch(batch, traceIdList, wrongTraceMap, batchNo);
                    getWrongTraceWithBatch(FIRSTBATCH[threadNo + 1], traceIdList, wrongTraceMap, batchNo);
                }
            } else if (batchNo == 0) {
                if (threadNo == 0) {
                    getWrongTraceWithBatch(FIRSTBATCH[threadNo], traceIdList, wrongTraceMap, batchNo);
                    getWrongTraceWithBatch(SECONDBATCH[threadNo], traceIdList, wrongTraceMap, batchNo);
                } else {
                    getWrongTraceWithBatch(LASTBATCH[threadNo - 1], traceIdList, wrongTraceMap, batchNo);
                    getWrongTraceWithBatch(FIRSTBATCH[threadNo], traceIdList, wrongTraceMap, batchNo);
                    getWrongTraceWithBatch(SECONDBATCH[threadNo], traceIdList, wrongTraceMap, batchNo);
                }
            } else {
                getWrongTraceWithBatch(ALLDATA.get(threadNo).get(batchNo - 1), traceIdList, wrongTraceMap, batchNo);
                getWrongTraceWithBatch(batch, traceIdList, wrongTraceMap, batchNo);
                getWrongTraceWithBatch(ALLDATA.get(threadNo).get(batchNo + 1), traceIdList, wrongTraceMap, batchNo);
            }

            if (batchNo == 0) {

            } else {
//                LOGGER.info("添加到移除队列，" + (batchNo - 1));
                RemoveBatchTask.holder.get(threadNo).add(batchNo - 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return JSON.toJSONString(wrongTraceMap);

        }

    }

    private static void getWrongTraceWithBatch(Batch batch, Set<String> traceIdList, Map<String, List<String>> wrongTraceMap, int batchNo) {
        if (batch == null) {
            LOGGER.warn("批次batch为空," + batchNo);
            return;
        }
        Map<String, Trace> traceMap = batch.getTraceMap();
        for (String traceId : traceIdList) {
            Trace trace = traceMap.get(traceId);
            if ("44be903a93b67406".equals(traceId) && trace != null) {
                StringBuilder sb = new StringBuilder();
                for (String span : trace.getSpans()) {
                    sb.append(span + "\n");
                }
                LOGGER.info("44be903a93b67406在批次:{}中的数据有:{}", batch.getBatchNo(), sb.toString());
            }
            if (trace != null && trace.getSpans() != null) {
                // one trace may cross to batch (e.g batch size 20000, span1 in line 19999, span2 in line 20001)
                List<String> existSpanList = wrongTraceMap.get(traceId);
                if (existSpanList != null) {
                    existSpanList.addAll(trace.getSpans());
                } else {
                    wrongTraceMap.put(traceId, trace.getSpans());
                }
            }
        }


    }
}















