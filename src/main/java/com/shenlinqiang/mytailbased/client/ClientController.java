package com.shenlinqiang.mytailbased.client;

import com.shenlinqiang.mytailbased.backend.TraceIdBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class ClientController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientController.class.getName());

    @RequestMapping("/getWrongTrace")
    public String getWrongTrace(@RequestBody TraceIdBatch traceIdBatch) {
//        LOGGER.info("收到了线程编号为:" + traceIdBatch.getThreadNo() +
//                "要求删除批次号为" + (traceIdBatch.getBatchNo() - 1) + " 的请求");
        long e = System.currentTimeMillis();
//        LOGGER.info("收到请求花费时间：" + (e - traceIdBatch.getSendTime()) + "ms");
        String res = ReadData.getWrongTracing(traceIdBatch);
        return res;
    }
}
