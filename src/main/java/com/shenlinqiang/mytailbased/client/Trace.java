package com.shenlinqiang.mytailbased.client;

import java.util.ArrayList;
import java.util.List;

public class Trace {
    private String traceId ;

    private List<String> spans = new ArrayList<>(15);

    public Trace(String traceId) {
        this.traceId = traceId;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public List<String> getSpans() {
        return spans;
    }

    public void setSpans(List<String> spans) {
        this.spans = spans;
    }
}
