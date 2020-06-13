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
        String res = ReadData.getWrongTracing(traceIdBatch);
//        LOGGER.info("获取WrongTrace，线程:{},批次:{},结果:{}", traceIdBatch.getThreadNo(), traceIdBatch.getBatchNo(), res);
        return res;
    }
}
