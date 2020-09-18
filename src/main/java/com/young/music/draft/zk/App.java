package com.young.music.draft.zk;

import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @author yzx
 * create_time 2020/7/16
 */
public class App {
    public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(10);
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                try {
                    countDownLatch.await();
                    DistributedLock distributedLock = new DistributedLock();
                    distributedLock.lock();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, "Th" + i).start();
            countDownLatch.countDown();
        }
        int read = System.in.read();
        System.out.println(read);
    }
}
