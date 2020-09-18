package com.young.music.draft.zk;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

/**
 * @author yzx
 * create_time 2020/7/16
 */
@Slf4j
public class MyWatcher implements Watcher {

    @Override
    public void process(WatchedEvent event) {
        log.info("watcher监听的事件[{}]", event.getState());
        log.info("watcher监听的路径[{}]", event.getPath());
        log.info("watcher监听的类型[{}]", event.getType());
    }
}
