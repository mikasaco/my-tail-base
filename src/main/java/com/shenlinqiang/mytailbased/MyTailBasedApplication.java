package com.shenlinqiang.mytailbased;

import com.shenlinqiang.mytailbased.backend.BackendController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@EnableAutoConfiguration
@ComponentScan(basePackages = "com.shenlinqiang.mytailbased")
public class MyTailBasedApplication {


    public static void main(String[] args) {
        if (Utils.isBackendProcess()) {
            BackendController.init();
        }
        String port = System.getProperty("server.port", "8080");
        SpringApplication.run(MyTailBasedApplication.class,
                "--server.port=" + port
        );

    }

}
