package com.shenlinqiang.mytailbased;

import com.shenlinqiang.mytailbased.client.ReadData;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class CommonController {

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
            for (int i = 0; i < Constants.THREAD_NUMBER; i++) {
                new Thread(new ReadData(i)).start();
            }
        }

        return "suc";
    }

    @RequestMapping("/start")
    public String start() {
        return "suc";
    }


}
