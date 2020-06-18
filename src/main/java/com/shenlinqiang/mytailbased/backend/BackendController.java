package com.shenlinqiang.mytailbased.backend;

import com.shenlinqiang.mytailbased.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.shenlinqiang.mytailbased.Constants.PROCESS_COUNT;

@RestController
public class BackendController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BackendController.class.getName());

    private static AtomicInteger FINISH_PROCESS_COUNT = new AtomicInteger(0);

    /**
     * 每个线程一个Map，key批次号，value TraceIdBatch，当TraceIdBatch的processCount=2放入线程池处理
     */
    public static List<Map<Integer, TraceIdBatch>> ALL_THREAD_TRACEIDBATCH = new ArrayList<>();

    public static AtomicInteger counter = new AtomicInteger();
    public static AtomicInteger counterRequest = new AtomicInteger();


    public static void init() {
        for (int i = 0; i < Constants.DOWNLOAD_NUMBER; i++) {
            Map<Integer, TraceIdBatch> map = new ConcurrentHashMap<>();
            ALL_THREAD_TRACEIDBATCH.add(map);
        }
    }

    private static ExecutorService executorService = new ThreadPoolExecutor(Constants.DOWNLOAD_NUMBER, Constants.DOWNLOAD_NUMBER,
            60L, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>());
//            Executors.newFixedThreadPool(Constants.THREAD_NUMBER);


    /**
     * 这个方法就是把错误的traceId进行上报，批次号用来校验上报的是哪一批错误的数据； 错误的这些traceId最后用TraceIdBatch封装起来的，TraceIdBatch又放在TRACEID_BATCH_LIST中，保存90份
     *
     * @return
     */
    @RequestMapping("/setWrongTraceId")
    public String setWrongTraceId(@RequestBody TraceIdBatch traceIdBatch) {

        Map<Integer, TraceIdBatch> map = ALL_THREAD_TRACEIDBATCH.get(traceIdBatch.getThreadNo());
        TraceIdBatch uploadedBatch = map.get(traceIdBatch.getBatchNo());
        if (uploadedBatch == null) {
            map.put(traceIdBatch.getBatchNo(), traceIdBatch);
        } else {
            counter.incrementAndGet();
            counterRequest.incrementAndGet();
            uploadedBatch.getTraceIdList().addAll(traceIdBatch.getTraceIdList());
            executorService.execute(new HandleFinishBatchDataTask(uploadedBatch));
        }
        return "suc";
    }

    @RequestMapping("/finish")
    public String finish() {
        FINISH_PROCESS_COUNT.incrementAndGet();
        LOGGER.warn("receive call 'finish', count:" + FINISH_PROCESS_COUNT);
        if (FINISH_PROCESS_COUNT.get() >= Constants.PROCESS_COUNT) {
            new Thread(new HandleLastBatchDataTask(true)).start();
        }

        return "suc";
    }


    public static boolean isFinished() {
        if (FINISH_PROCESS_COUNT.get() < PROCESS_COUNT) {
            return false;
        }
        for (Map<Integer, TraceIdBatch> map : ALL_THREAD_TRACEIDBATCH) {
            if (map.size() > 0) {
//                Collection<TraceIdBatch> traceIdBatches = map.values();
//                for (TraceIdBatch traceIdBatch : traceIdBatches) {
//                    executorService.execute(new HandleFinishBatchDataTask(traceIdBatch));
//                }
                return false;
            }
        }
        return true;
    }


}
