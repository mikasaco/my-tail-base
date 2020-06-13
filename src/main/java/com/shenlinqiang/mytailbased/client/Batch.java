package com.shenlinqiang.mytailbased.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Batch {

    private Integer batchNo = 0;

    /**
     * key traceId , value Trace
     */
    private Map<String, Trace> traceMap = new ConcurrentHashMap<>();

    private Boolean canDel = false;


    public Map<String, Trace> getTraceMap() {
        return traceMap;
    }

    public void setTraceMap(Map<String, Trace> traceMap) {
        this.traceMap = traceMap;
    }

    public Boolean getCanDel() {
        return canDel;
    }

    public void setCanDel(Boolean canDel) {
        this.canDel = canDel;
    }

    public void setBatchNo(Integer batchNo) {
        this.batchNo = batchNo;
    }
    public int getBatchNo() {
        return batchNo;
    }


}
