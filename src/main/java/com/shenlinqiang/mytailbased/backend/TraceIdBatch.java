package com.shenlinqiang.mytailbased.backend;


import com.shenlinqiang.mytailbased.Constants;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class TraceIdBatch {

    private int batchNo;

    private int threadNo;

    private boolean lastBatch = false;

    private long sendTime;

    private Set<String> traceIdList = new HashSet<>(Constants.BATCH_SIZE / 10);


    public boolean isLastBatch() {
        return lastBatch;
    }

    public void setLastBatch(boolean lastBatch) {
        this.lastBatch = lastBatch;
    }


    public void setTraceIdList(Set<String> traceIdList) {
        this.traceIdList = traceIdList;
    }

    public Set<String> getTraceIdList() {
        return traceIdList;
    }


    public int getThreadNo() {
        return threadNo;
    }

    public void setThreadNo(int threadNo) {
        this.threadNo = threadNo;
    }

    public int getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(int batchNo) {
        this.batchNo = batchNo;
    }

    public long getSendTime() {
        return sendTime;
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }
}
