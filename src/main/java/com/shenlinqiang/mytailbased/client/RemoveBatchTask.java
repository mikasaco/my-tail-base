package com.shenlinqiang.mytailbased.client;

import com.shenlinqiang.mytailbased.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class RemoveBatchTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveBatchTask.class.getName());

    private Integer threadNo;

    public RemoveBatchTask(Integer threadNo) {
        this.threadNo = threadNo;
        holder.add(threadNo, needRemoveBatch);
    }

    private BlockingQueue<Integer> needRemoveBatch = new PriorityBlockingQueue<>();

    public static List<Queue<Integer>> holder = new ArrayList<>(Constants.THREAD_NUMBER);

    @Override
    public void run() {
        while (true) {
            Integer poll = null;
            try {
                poll = needRemoveBatch.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (poll != null) {
                Batch batch = ReadData.ALLDATA.get(threadNo).get(poll);
                if (batch.getCanDel() == 3) {
                    ReadData.ALLDATA.get(threadNo).remove(poll);
                }
            }
        }
    }
}
