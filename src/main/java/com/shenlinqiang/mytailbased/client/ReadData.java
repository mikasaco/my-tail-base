package com.shenlinqiang.mytailbased.client;

import com.alibaba.fastjson.JSON;
import com.shenlinqiang.mytailbased.CommonController;
import com.shenlinqiang.mytailbased.Constants;
import com.shenlinqiang.mytailbased.Utils;
import com.shenlinqiang.mytailbased.backend.TraceIdBatch;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
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
import java.util.concurrent.atomic.AtomicInteger;

public class ReadData implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReadData.class.getName());

    private static CountDownLatch countDownLatch = new CountDownLatch(Constants.THREAD_NUMBER);

    private Integer threadNo;

    public ReadData(Integer threadNo) {
        this.threadNo = threadNo;
        init();
    }

    public ReadData() {

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

    @Override
    public void run() {
        String path = getPath();
        long s = System.currentTimeMillis();
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
            Set<String> badTraceIdList = new HashSet<>(50);
            long count = 0;
            String line;
            int batchNo = 0;
            Batch batch = null;
            while ((line = bf.readLine()) != null) {
                batch = ALLDATA.get(threadNo).get(batchNo);
                if (batch == null) {
                    batch = new Batch();
                    batch.setBatchNo(batchNo);

                    while (ALLDATA.get(threadNo).size() > 50) {
                        Thread.sleep(10);
                    }
//                    LOGGER.info("添加批次：" + batchNo);
                    ALLDATA.get(threadNo).put(batchNo, batch);
                }
                count++;
                String[] cols = line.split("\\|");
                if (cols.length > 1) {
                    String traceId = cols[0];
                    Trace trace = null;
                    trace = batch.getTraceMap().get(traceId);
                    if (trace == null) {
                        trace = new Trace(traceId);
                        List<String> spanList = new ArrayList<>();
                        trace.setSpans(spanList);
                        batch.getTraceMap().put(traceId, trace);
                    }
                    trace.getSpans().add(line);
                    if (cols.length > 8) {
                        String tags = cols[8];
                        if (tags != null) {
                            if (tags.contains("error=1")) {
                                badTraceIdList.add(traceId);
                            } else if (tags.contains("http.status_code=") && tags.indexOf("http.status_code=200") < 0) {
                                badTraceIdList.add(traceId);
                            }
                        }
                    }
                }
                if (count % Constants.BATCH_SIZE == 0) {
                    if (batchNo == 0) {
                        FIRSTBATCH[threadNo] = batch;
                    } else if (batchNo == 1) {
                        SECONDBATCH[threadNo] = batch;
                    }

                    TraceIdBatch traceIdBatch = new TraceIdBatch();
                    traceIdBatch.setThreadNo(threadNo);
                    traceIdBatch.setBatchNo(batch.getBatchNo());
                    traceIdBatch.setTraceIdList(badTraceIdList);
                    updateWrongTraceId(traceIdBatch);
                    badTraceIdList.clear();
                    batchNo++;
                }
            }
            // 应该不会有正好2W读完这个线程所有数据，概率很低，不管了
            LASTBATCH[threadNo] = batch;
            TraceIdBatch traceIdBatch = new TraceIdBatch();
            traceIdBatch.setTraceIdList(badTraceIdList);
            traceIdBatch.setThreadNo(threadNo);
            traceIdBatch.setBatchNo(batchNo);
            traceIdBatch.setLastBatch(true);
            updateWrongTraceId(traceIdBatch);
            long e = System.currentTimeMillis();
            LOGGER.info("线程:{}，读完文件花费时间:{} ms", threadNo, (e - s));
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
            Response response = Utils.callHttp(request);
            response.close();
//            Utils.callHttpAsync(request);
        } catch (Exception e) {
            LOGGER.warn("fail to updateBadTraceId, json:" + jsonStr);
        }
    }

    public void callFinish() {
        try {
            countDownLatch.await();
            if (threadNo == 0) {
                LOGGER.info("所有线程都处理完成");
                Request request = new Request.Builder().url("http://localhost:8002/finish").build();
                Utils.callHttpAsync(request);
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
                hasBatch(threadNo, batchNo - 1);
                hasBatch(threadNo, batchNo);
                hasBatch(threadNo, batchNo + 1);
                getWrongTraceWithBatch(ALLDATA.get(threadNo).get(batchNo - 1), traceIdList, wrongTraceMap, batchNo - 1);
                getWrongTraceWithBatch(batch, traceIdList, wrongTraceMap, batchNo);
                getWrongTraceWithBatch(ALLDATA.get(threadNo).get(batchNo + 1), traceIdList, wrongTraceMap, batchNo + 1);
            }

            if (batchNo == 0 || lastBatch) {

            } else {
                ALLDATA.get(threadNo).get(batchNo - 1).addCanDel();
                ALLDATA.get(threadNo).get(batchNo).addCanDel();
                ALLDATA.get(threadNo).get(batchNo + 1).addCanDel();
                RemoveBatchTask.holder.get(threadNo).add(batchNo - 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return JSON.toJSONString(wrongTraceMap);
    }

    private static boolean hasBatch(int threadNo, int batchNo) {
        while (true) {
            if (ALLDATA.get(threadNo).get(batchNo) != null) {
                return true;
            } else {
                LOGGER.warn("thread:{},batch:{}为空", threadNo, batchNo);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
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















