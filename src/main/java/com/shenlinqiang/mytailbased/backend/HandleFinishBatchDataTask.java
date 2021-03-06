package com.shenlinqiang.mytailbased.backend;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.shenlinqiang.mytailbased.Constants;
import com.shenlinqiang.mytailbased.Utils;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.shenlinqiang.mytailbased.Constants.CLIENT_PROCESS_PORT1;
import static com.shenlinqiang.mytailbased.Constants.CLIENT_PROCESS_PORT2;

public class HandleFinishBatchDataTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(HandleFinishBatchDataTask.class.getName());


    private TraceIdBatch traceIdBatch;

    public HandleFinishBatchDataTask(TraceIdBatch traceIdBatch) {
        this.traceIdBatch = traceIdBatch;
    }

    public static Map<String, String> TRACE_CHUCKSUM_MAP = new ConcurrentHashMap<>();


    private static final String[] PORTS = new String[]{CLIENT_PROCESS_PORT1, CLIENT_PROCESS_PORT2};

    public static AtomicInteger counterDe = new AtomicInteger(0);

    @Override
    public void run() {
        try {
            if (traceIdBatch.getBatchNo() == 0 || traceIdBatch.isLastBatch()) {
                LOGGER.info("往阻塞队列中添加," + traceIdBatch.getBatchNo());
                HandleLastBatchDataTask.queue.add(traceIdBatch);
                return;
            }
            aggregate(traceIdBatch);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static void aggregate(TraceIdBatch traceIdBatch) {
        try {
            Map<String, Set<String>> wrongTraces = new HashMap<>();
            for (String port : PORTS) {
                Map<String, List<String>> processMap = null;
                try {
                    processMap = getWrongTrace(traceIdBatch, port);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (processMap != null) {
                    for (Map.Entry<String, List<String>> entry : processMap.entrySet()) {
                        String traceId = entry.getKey();
                        Set<String> spanSet = wrongTraces.get(traceId);
                        if (spanSet == null) {
                            spanSet = new HashSet<>();
                            wrongTraces.put(traceId, spanSet);
                        }
                        spanSet.addAll(entry.getValue());
                    }
                }
            }
//        LOGGER.info("getWrong:" + batchPos + ", traceIdsize:" + traceIdBatch.getTraceIdList().size() + ",result:" + wrongTraces.size());
            for (Map.Entry<String, Set<String>> entry : wrongTraces.entrySet()) {
                String traceId = entry.getKey();
                Set<String> spanSet = entry.getValue();
                String spans = spanSet.stream().sorted(
                        Comparator.comparing(HandleFinishBatchDataTask::getStartTime)).collect(Collectors.joining("\n"));
                spans = spans + "\n";
//            LOGGER.info("traceId:" + traceId + ",value:\n" + spans);
                TRACE_CHUCKSUM_MAP.put(traceId, Utils.MD5(spans));
            }
            BackendController.ALL_THREAD_TRACEIDBATCH.get(traceIdBatch.getThreadNo()).remove(traceIdBatch.getBatchNo());
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            BackendController.counter.decrementAndGet();
            counterDe.incrementAndGet();
        }

    }


    private static Map<String, List<String>> getWrongTrace(TraceIdBatch traceIdBatch, String port) {
        traceIdBatch.setSendTime(System.currentTimeMillis());
        String json = JSONObject.toJSONString(traceIdBatch);
        try {
            RequestBody body = RequestBody.create(Constants.MEDIATYPE, json);
            String url = String.format("http://localhost:%s/getWrongTrace", port);
            Request request = new Request.Builder().url(url).post(body).build();
            Response response = Utils.callHttp(request);
            Map<String, List<String>> resultMap = JSON.parseObject(response.body().string(),
                    new TypeReference<Map<String, List<String>>>() {
                    });
            response.close();
            return resultMap;
        } catch (Exception e) {
            LOGGER.warn("fail to getWrongTrace, json:" + json, e);
        }
        return null;
    }

    public static long getStartTime(String span) {
        if (span != null) {
            String[] cols = span.split("\\|");
            if (cols.length > 8) {
                return Utils.toLong(cols[1], -1);
            }
        }
        return -1;
    }
}
