package com.shenlinqiang.mytailbased;

import okhttp3.MediaType;

public class Constants {

    public static final String CLIENT_PROCESS_PORT1 = "8000";
    public static final String CLIENT_PROCESS_PORT2 = "8001";
    public static final String BACKEND_PROCESS_PORT1 = "8002";
    public static final String BACKEND_PROCESS_PORT2 = "8003";
    public static final String BACKEND_PROCESS_PORT3 = "8004";
    public static int BATCH_SIZE = 20000;
    public static int PROCESS_COUNT =  2;
    public static final Integer ONEG = 1024 * 1024 * 20;
    public static final int THREAD_NUMBER = 20;

    public static final MediaType MEDIATYPE = MediaType.parse("application/json; charset=utf-8");

}
