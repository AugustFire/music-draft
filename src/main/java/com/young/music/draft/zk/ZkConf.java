package com.young.music.draft.zk;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @author yzx
 * create_time 2020/7/16
 */
@Configuration
@Slf4j
public class ZkConf {

    @Bean
    public ZooKeeper zkClient() {
        String connectStr = "localhost:2181";
        int timeout = 4000;
        ZooKeeper zooKeeper = null;
        try {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            zooKeeper = new ZooKeeper(connectStr, timeout, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    if (Event.KeeperState.SyncConnected == watchedEvent.getState()) {
                        countDownLatch.countDown();
                    }
                }
            });
            countDownLatch.await();
            log.info("[初始化ZK连接转态...]={}", zooKeeper.getState());

        }catch (IOException | InterruptedException e) {
            log.info("[初始化ZK连接异常...]={}", e.getMessage());
        }
        return zooKeeper;
    }
}
