package com.shenlinqiang.mytailbased;

import com.shenlinqiang.mytailbased.backend.BackendController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@EnableAutoConfiguration
@ComponentScan(basePackages = "com.shenlinqiang.mytailbased")
public class MyTailBasedApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyTailBasedApplication.class.getName());


    public static void main(String[] args) {
        if (Utils.isBackendProcess()) {
            BackendController.init();
        }
        String port = System.getProperty("server.port", "8080");
        String env = System.getProperty("env", "online");
        LOGGER.info("运行环境：" + env);
        SpringApplication.run(MyTailBasedApplication.class,
                "--server.port=" + port
        );

    }

}
