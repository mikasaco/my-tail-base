package com.shenlinqiang.mytailbased.client;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Batch {

    private Integer batchNo = 0;

    /**
     * key traceId , value Trace
     */
    private Map<String, Trace> traceMap = new HashMap<>();

    private Integer canDel = 0;

    public void addCanDel() {
        canDel++;
    }


    public Map<String, Trace> getTraceMap() {
        return traceMap;
    }

    public void setTraceMap(Map<String, Trace> traceMap) {
        this.traceMap = traceMap;
    }

    public Integer getCanDel() {
        return canDel;
    }

    public void setCanDel(Integer canDel) {
        this.canDel = canDel;
    }

    public void setBatchNo(Integer batchNo) {
        this.batchNo = batchNo;
    }

    public int getBatchNo() {
        return batchNo;
    }


}
